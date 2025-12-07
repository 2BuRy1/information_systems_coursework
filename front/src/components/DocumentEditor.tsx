import { useEffect, useMemo, useRef, useState } from 'react';
import Editor, { OnMount } from '@monaco-editor/react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { loadDocument } from '../services/api';
import { DocumentState } from '../types';
import { useAuth } from '../contexts/AuthContext';

interface Props {
  sessionId: number;
}

const DocumentEditor: React.FC<Props> = ({ sessionId }) => {
  const { tokens, user } = useAuth();
  const [documentState, setDocumentState] = useState<DocumentState | null>(null);
  const [content, setContent] = useState('');
  const clientRef = useRef<Client | null>(null);
  const editorRef = useRef<Parameters<OnMount>[0]>();
  const versionRef = useRef(0);
  const siteId = useMemo(() => user?.id ?? Math.floor(Math.random() * 100000), [user]);

  useEffect(() => {
    if (!tokens) return;
    loadDocument(tokens, sessionId).then((doc) => {
      setDocumentState(doc);
      setContent(doc.content);
      versionRef.current = doc.version;
    });
  }, [sessionId, tokens]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/v1/ws'),
      reconnectDelay: 5000
    });
    client.onConnect = () => {
      client.subscribe(`/topic/sessions/${sessionId}/document`, () => {
        if (!tokens) return;
        loadDocument(tokens, sessionId).then((doc) => {
          setDocumentState(doc);
          setContent(doc.content);
          versionRef.current = doc.version;
        });
      });
    };
    client.activate();
    clientRef.current = client;
    return () => client.deactivate();
  }, [sessionId, tokens]);

  const handleMount: OnMount = (editor) => {
    editorRef.current = editor;
  };

  const handleChange = (value?: string) => {
    const editor = editorRef.current;
    if (!editor || !value || !documentState || !tokens) {
      setContent(value ?? '');
      return;
    }
    setContent(value);
    const model = editor.getModel();
    const changes = editor.getModel()?.getAllDecorations();
    const diff = editor.getModel()?.getValue();
    const lineCount = model?.getLineCount() ?? 0;
    const text = value;

    const selection = editor.getSelection();
    const position = selection?.getStartPosition();
    const index = model?.getOffsetAt(position!) ?? 0;
    const oldText = documentState.content;
    if (text === oldText) return;
    const { start, endOld, endNew } = diffRange(oldText, text);
    const removed = oldText.slice(start, endOld);
    const inserted = text.slice(start, endNew);
    const operations = [] as Array<{
      operationType: 'insert' | 'delete';
      nodeCounter: number;
      nodeSite: number;
      value: string;
      leftNode: number | null;
      rightNode: number | null;
      color?: string;
    }>;
    if (removed.length) {
      operations.push({
        operationType: 'delete',
        nodeCounter: start,
        nodeSite: siteId,
        value: removed,
        leftNode: null,
        rightNode: null
      });
    }
    if (inserted.length) {
      operations.push({
        operationType: 'insert',
        nodeCounter: start,
        nodeSite: siteId,
        value: inserted,
        leftNode: null,
        rightNode: null,
        color: '#34d399'
      });
    }
    if (operations.length && tokens) {
      fetch(`/v1/sessions/${sessionId}/document/operations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${tokens.accessToken}`
        },
        body: JSON.stringify({ baseVersion: versionRef.current, operations })
      })
        .then((res) => res.json())
        .then((ack) => {
          versionRef.current = ack.appliedVersion;
        })
        .catch(() => {
          if (!tokens) return;
          loadDocument(tokens, sessionId).then((doc) => {
            setDocumentState(doc);
            setContent(doc.content);
            versionRef.current = doc.version;
          });
        });
    }
  };

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <h3>Документ</h3>
        <span>Версия: {documentState?.version ?? versionRef.current}</span>
      </div>
      <Editor
        height="400px"
        defaultLanguage="typescript"
        value={content}
        onMount={handleMount}
        onChange={handleChange}
        options={{ minimap: { enabled: false }, fontSize: 14 }}
      />
    </div>
  );
};

function diffRange(oldStr: string, newStr: string) {
  let start = 0;
  while (start < oldStr.length && start < newStr.length && oldStr[start] === newStr[start]) {
    start++;
  }
  let endOld = oldStr.length;
  let endNew = newStr.length;
  while (endOld > start && endNew > start && oldStr[endOld - 1] === newStr[endNew - 1]) {
    endOld--;
    endNew--;
  }
  return { start, endOld, endNew };
}

export default DocumentEditor;
