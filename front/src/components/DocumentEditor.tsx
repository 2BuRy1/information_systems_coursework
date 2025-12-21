import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import Editor, { OnMount } from '@monaco-editor/react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import type { Monaco } from '@monaco-editor/react';
import {
  appendOperations,
  loadDocument,
  loadMembers,
  loadOperations,
  fetchPresence,
  updateCursor,
  updateHighlight,
  clearCursor,
  clearHighlight
} from '../services/api';
import { DocumentState, OperationAck, OperationRequest, SessionMember, SessionPresence } from '../types';
import { CrdtOperation, CrdtSequence } from '../services/crdt';
import { useAuth } from '../contexts/AuthContext';

interface Props {
  sessionId: number;
}

interface PendingBatch {
  operations: OperationRequest['operations'];
  tempOperations: CrdtOperation[];
}

const DocumentEditor: React.FC<Props> = ({ sessionId }) => {
  const { tokens, user } = useAuth();
  const [documentState, setDocumentState] = useState<DocumentState | null>(null);
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(true);
  const editorRef = useRef<Parameters<OnMount>[0]>();
  const monacoRef = useRef<Monaco | null>(null);
  const clientRef = useRef<Client | null>(null);
  const sequenceRef = useRef<CrdtSequence>(new CrdtSequence());
  const versionRef = useRef(0);
  const contentRef = useRef('');
  const pendingTextRef = useRef('');
  const pendingBatchesRef = useRef<PendingBatch[]>([]);
  const sendingRef = useRef(false);
  const appliedOperationIdsRef = useRef<Set<number>>(new Set());
  const tempIdRef = useRef(-1);
  const counterRef = useRef(0);
  const mountedRef = useRef(true);
  const cursorTimeoutRef = useRef<number | null>(null);
  const highlightTimeoutRef = useRef<number | null>(null);
  const lastCursorRef = useRef<{ line: number; col: number }>({ line: 0, col: 0 });
  const memberMapRef = useRef<Map<number, string>>(new Map());
  const cursorDecorationsRef = useRef<string[]>([]);
  const highlightDecorationsRef = useRef<string[]>([]);
  const styleSheetRef = useRef<CSSStyleSheet | null>(null);
  const generatedClassesRef = useRef<Set<string>>(new Set());
  const siteId = useMemo(() => user?.id ?? Math.floor(Math.random() * 100000), [user]);
  const cursorColor = useMemo(() => {
    if (!user?.id) {
      return '#34d399';
    }
    const hash = Math.abs(user.id * 2654435761);
    return `#${(hash & 0xffffff).toString(16).padStart(6, '0')}`;
  }, [user?.id]);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      if (cursorTimeoutRef.current) {
        window.clearTimeout(cursorTimeoutRef.current);
      }
      if (highlightTimeoutRef.current) {
        window.clearTimeout(highlightTimeoutRef.current);
      }
      if (tokens) {
        clearCursor(tokens, sessionId).catch(() => {});
        clearHighlight(tokens, sessionId).catch(() => {});
      }
      mountedRef.current = false;
    };
  }, [sessionId, tokens]);

  const resetState = useCallback(async () => {
    if (!tokens) return;
    setLoading(true);
    pendingBatchesRef.current = [];
    sendingRef.current = false;
    appliedOperationIdsRef.current = new Set();
    tempIdRef.current = -1;
    counterRef.current = 0;
    try {
      const [doc, ops] = await Promise.all([
        loadDocument(tokens, sessionId),
        loadOperations(tokens, sessionId, 0)
      ]);
      if (!mountedRef.current) return;
      const sequence = new CrdtSequence();
      sequence.applyAll(ops.operations);
      sequenceRef.current = sequence;
      appliedOperationIdsRef.current = new Set(ops.operations.map((operation) => operation.id));
      versionRef.current = doc.version;
      const initialText = sequence.text();
      contentRef.current = initialText;
      pendingTextRef.current = initialText;
      setContent(initialText);
      setDocumentState({ ...doc, content: initialText });
    } catch (error) {
      console.error('Не удалось загрузить документ', error);
    } finally {
      if (mountedRef.current) {
        setLoading(false);
      }
    }
  }, [sessionId, tokens]);

  useEffect(() => {
    if (!tokens) return;
    resetState();
  }, [resetState, tokens]);

  useEffect(() => {
    if (!tokens) return;
    loadMembers(tokens, sessionId).then((members: SessionMember[]) => {
      const map = new Map<number, string>();
      members.forEach((member) => map.set(member.userId, member.name));
      memberMapRef.current = map;
    });
  }, [sessionId, tokens]);

  const sendNextBatch = useCallback(() => {
    if (!tokens) return;
    if (sendingRef.current) return;
    const batch = pendingBatchesRef.current[0];
    if (!batch) return;
    sendingRef.current = true;
    const payload: OperationRequest = {
      baseVersion: versionRef.current,
      operations: batch.operations
    };
    appendOperations(tokens, sessionId, payload)
      .then((ack) => {
        versionRef.current = ack.appliedVersion ?? versionRef.current;
        batch.tempOperations.forEach((tempOp, index) => {
          const actual = ack.operations[index];
          if (!actual) {
            return;
          }
          if (tempOp.operationType === 'insert') {
            sequenceRef.current.confirmLocalInsert(tempOp.id, actual);
          }
          appliedOperationIdsRef.current.add(actual.id);
        });
        setDocumentState((prev) =>
          prev ? { ...prev, version: versionRef.current, content: sequenceRef.current.text() } : prev
        );
        pendingBatchesRef.current.shift();
      })
      .catch((error) => {
        console.error('Не удалось применить операции', error);
        pendingBatchesRef.current = [];
        resetState();
      })
      .finally(() => {
        sendingRef.current = false;
        if (pendingBatchesRef.current.length) {
          sendNextBatch();
        }
      });
  }, [resetState, sessionId, tokens]);

  const applyRemoteAck = useCallback((ack: OperationAck) => {
    if (!ack.operations?.length) {
      return;
    }
    if (user?.id && ack.operations.every((operation) => operation.userId === user.id)) {
      return;
    }
    const sequence = sequenceRef.current;
    let changed = false;
    ack.operations.forEach((operation) => {
      if (appliedOperationIdsRef.current.has(operation.id)) {
        return;
      }
      sequence.apply(operation);
      appliedOperationIdsRef.current.add(operation.id);
      changed = true;
    });
    if (!changed) {
      return;
    }
    versionRef.current = Math.max(versionRef.current, ack.appliedVersion ?? versionRef.current);
    const nextText = sequence.text();
    contentRef.current = nextText;
    pendingTextRef.current = nextText;
    setContent(nextText);
    setDocumentState((prev) => (prev ? { ...prev, version: versionRef.current, content: nextText } : prev));
  }, [user?.id]);

  const socketUrl = useMemo(() => {
    if (import.meta.env.DEV) {
      return 'http://localhost:5173/v1/ws';
    }
    return '/v1/ws';
  }, []);

  useEffect(() => {
    if (!tokens) return;
    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000
    });
    client.onConnect = () => {
      client.subscribe(`/topic/sessions/${sessionId}/document`, (message) => {
        try {
          const ack: OperationAck = JSON.parse(message.body);
          applyRemoteAck(ack);
        } catch (error) {
          console.error('Не удалось обработать уведомление', error);
        }
      });
    };
    client.activate();
    clientRef.current = client;
    return () => {
      client.deactivate();
    };
  }, [applyRemoteAck, sessionId, socketUrl, tokens]);

  const handleMount: OnMount = (editor, monaco) => {
    editorRef.current = editor;
    monacoRef.current = monaco;
    editor.onDidChangeCursorPosition((event) => {
      const position = event.position;
      lastCursorRef.current = { line: position.lineNumber - 1, col: position.column - 1 };
      if (!tokens) return;
      if (cursorTimeoutRef.current) {
        window.clearTimeout(cursorTimeoutRef.current);
      }
      cursorTimeoutRef.current = window.setTimeout(() => {
        updateCursor(tokens, sessionId, {
          line: lastCursorRef.current.line,
          col: lastCursorRef.current.col,
          color: cursorColor
        }).catch(() => {});
      }, 200);
    });
    editor.onDidChangeCursorSelection((event) => {
      if (!tokens) return;
      const selection = event.selection;
      if (highlightTimeoutRef.current) {
        window.clearTimeout(highlightTimeoutRef.current);
      }
      highlightTimeoutRef.current = window.setTimeout(() => {
        if (!tokens) return;
        if (selection.isEmpty()) {
          clearHighlight(tokens, sessionId).catch(() => {});
          return;
        }
        updateHighlight(tokens, sessionId, {
          startLine: selection.startLineNumber - 1,
          endLine: selection.endLineNumber - 1,
          startCol: selection.startColumn - 1,
          endCol: selection.endColumn - 1,
          color: cursorColor
        }).catch(() => {});
      }, 200);
    });
  };

  const nextNodeCounter = () => {
    counterRef.current += 1;
    return counterRef.current;
  };

  const handleChange = (value?: string) => {
    const nextValue = value ?? '';
    pendingTextRef.current = nextValue;
    setContent(nextValue);
    if (!tokens || !documentState) {
      return;
    }
    const sequence = sequenceRef.current;
    const previous = contentRef.current;
    if (nextValue === previous) {
      return;
    }
    const { start, endOld, endNew } = diffRange(previous, nextValue);
    const removed = previous.slice(start, endOld);
    const inserted = nextValue.slice(start, endNew);
    const operations: OperationRequest['operations'] = [];
    const tempOperations: CrdtOperation[] = [];
    const baseVersion = versionRef.current;
    const timestamp = new Date().toISOString();

    if (removed.length) {
      const anchors = sequence.anchorsAt(start);
      const deleteInput: OperationRequest['operations'][number] = {
        operationType: 'delete',
        nodeCounter: nextNodeCounter(),
        nodeSite: siteId,
        leftNode: anchors.leftNode ?? undefined,
        rightNode: anchors.rightNode ?? undefined,
        value: removed
      };
      operations.push(deleteInput);
      const tempOperation: CrdtOperation = {
        id: tempIdRef.current--,
        documentId: documentState.id,
        operationType: 'delete',
        nodeCounter: deleteInput.nodeCounter,
        nodeSite: deleteInput.nodeSite,
        leftNode: deleteInput.leftNode ?? null,
        rightNode: deleteInput.rightNode ?? null,
        value: deleteInput.value,
        color: null,
        version: baseVersion,
        userId: user?.id ?? 0,
        createdAt: timestamp
      };
      sequence.apply(tempOperation);
      tempOperations.push(tempOperation);
    }

    if (inserted.length) {
      let offset = start;
      for (const char of inserted) {
        const anchors = sequence.anchorsAt(offset);
        const insertInput: OperationRequest['operations'][number] = {
          operationType: 'insert',
          nodeCounter: nextNodeCounter(),
          nodeSite: siteId,
          leftNode: anchors.leftNode ?? undefined,
          rightNode: anchors.rightNode ?? undefined,
          value: char,
          color: '#34d399'
        };
        operations.push(insertInput);
        const tempOperation: CrdtOperation = {
          id: tempIdRef.current--,
          documentId: documentState.id,
          operationType: 'insert',
          nodeCounter: insertInput.nodeCounter,
          nodeSite: insertInput.nodeSite,
          leftNode: insertInput.leftNode ?? null,
          rightNode: insertInput.rightNode ?? null,
          value: insertInput.value,
          color: insertInput.color,
          version: baseVersion,
          userId: user?.id ?? 0,
          createdAt: timestamp
        };
        sequence.apply(tempOperation);
        tempOperations.push(tempOperation);
        offset += char.length;
      }
    }

    if (!operations.length) {
      return;
    }

    contentRef.current = sequence.text();
    pendingBatchesRef.current.push({
      operations,
      tempOperations
    });
    if (!sendingRef.current) {
      sendNextBatch();
    }
  };

  const ensureStyleSheet = useCallback(() => {
    if (styleSheetRef.current) {
      return styleSheetRef.current;
    }
    const element = document.createElement('style');
    element.setAttribute('data-remote-presence', 'true');
    document.head.appendChild(element);
    styleSheetRef.current = element.sheet ?? null;
    return styleSheetRef.current;
  }, []);

  const colorWithAlpha = useCallback((color: string | undefined, alpha: number) => {
    if (!color) {
      return `rgba(249, 115, 22, ${alpha})`;
    }
    if (color.startsWith('#') && (color.length === 7 || color.length === 4)) {
      let hex = color.replace('#', '');
      if (hex.length === 3) {
        hex = hex
          .split('')
          .map((char) => char + char)
          .join('');
      }
      const parsed = Number.parseInt(hex, 16);
      const r = (parsed >> 16) & 255;
      const g = (parsed >> 8) & 255;
      const b = parsed & 255;
      return `rgba(${r}, ${g}, ${b}, ${alpha})`;
    }
    return color;
  }, []);

  const ensureColorClass = useCallback(
    (className: string, color: string | undefined, type: 'cursor' | 'highlight') => {
      const sheet = ensureStyleSheet();
      if (!sheet) {
        return;
      }
      const key = `${type}-${className}`;
      if (generatedClassesRef.current.has(key)) {
        return;
      }
      generatedClassesRef.current.add(key);
      const shade = color || '#f97316';
      if (type === 'cursor') {
        sheet.insertRule(
          `.${className} { border-left: 2px solid ${shade}; margin-left: -1px; }`,
          sheet.cssRules.length
        );
      } else {
        sheet.insertRule(
          `.${className} { background-color: ${colorWithAlpha(shade, 0.25)}; }`,
          sheet.cssRules.length
        );
      }
    },
    [colorWithAlpha, ensureStyleSheet]
  );

  const clearPresenceDecorations = useCallback(() => {
    const editor = editorRef.current;
    if (!editor) return;
    if (cursorDecorationsRef.current.length) {
      cursorDecorationsRef.current = editor.deltaDecorations(cursorDecorationsRef.current, []);
    }
    if (highlightDecorationsRef.current.length) {
      highlightDecorationsRef.current = editor.deltaDecorations(highlightDecorationsRef.current, []);
    }
  }, []);

  const applyPresenceDecorations = useCallback(
    (presence: SessionPresence) => {
      const editor = editorRef.current;
      const monaco = monacoRef.current;
      if (!editor || !monaco) {
        return;
      }
      const cursorDecorations = presence.cursors
        .filter((cursor) => cursor.userId !== user?.id)
        .map((cursor) => {
          const className = `remote-cursor-${cursor.userId}`;
          ensureColorClass(className, cursor.color, 'cursor');
          const label = memberMapRef.current.get(cursor.userId) ?? `Участник ${cursor.userId}`;
          return {
            range: new monaco.Range(
              cursor.line + 1,
              cursor.col + 1,
              cursor.line + 1,
              cursor.col + 1
            ),
            options: {
              className,
              hoverMessage: { value: `**${label}**` },
              stickiness: monaco.editor.TrackedRangeStickiness.NeverGrowsWhenTypingAtEdges
            }
          };
        });
      cursorDecorationsRef.current = editor.deltaDecorations(cursorDecorationsRef.current, cursorDecorations);

      const highlightDecorations = presence.highlights
        .filter((highlight) => highlight.userId !== user?.id)
        .map((highlight) => {
          const className = `remote-highlight-${highlight.userId}`;
          ensureColorClass(className, highlight.color, 'highlight');
          return {
            range: new monaco.Range(
              highlight.startLine + 1,
              highlight.startCol + 1,
              highlight.endLine + 1,
              highlight.endCol + 1
            ),
            options: {
              className
            }
          };
        });
      highlightDecorationsRef.current = editor.deltaDecorations(
        highlightDecorationsRef.current,
        highlightDecorations
      );
    },
    [ensureColorClass, user?.id]
  );

  useEffect(() => {
    if (!tokens) return;
    let cancelled = false;
    const tick = () => {
      fetchPresence(tokens, sessionId)
        .then((presence) => {
          if (!cancelled) {
            applyPresenceDecorations(presence);
          }
        })
        .catch(() => {});
    };
    tick();
    const interval = window.setInterval(tick, 2500);
    return () => {
      cancelled = true;
      window.clearInterval(interval);
      clearPresenceDecorations();
    };
  }, [applyPresenceDecorations, clearPresenceDecorations, sessionId, tokens]);

  if (!tokens) {
    return null;
  }

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <h3>Документ</h3>
        <span>Версия: {documentState?.version ?? versionRef.current}</span>
      </div>
      {loading ? (
        <p>Загрузка документа...</p>
      ) : (
        <Editor
          height="400px"
          defaultLanguage="typescript"
          value={content}
          onMount={handleMount}
          onChange={handleChange}
          options={{ minimap: { enabled: false }, fontSize: 14, readOnly: loading }}
        />
      )}
    </div>
  );
};

function diffRange(oldStr: string, newStr: string) {
  let start = 0;
  while (start < oldStr.length && start < newStr.length && oldStr[start] === newStr[start]) {
    start += 1;
  }
  let endOld = oldStr.length;
  let endNew = newStr.length;
  while (endOld > start && endNew > start && oldStr[endOld - 1] === newStr[endNew - 1]) {
    endOld -= 1;
    endNew -= 1;
  }
  return { start, endOld, endNew };
}

export default DocumentEditor;
