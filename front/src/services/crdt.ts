import { OperationAck } from '../types';

export type CrdtOperation = OperationAck['operations'][number];

export interface AnchorPair {
  leftNode: number | null;
  rightNode: number | null;
}

class InsertNode {
  operation: CrdtOperation;
  value: string;
  tombstone: boolean;

  constructor(operation: CrdtOperation) {
    this.operation = operation;
    this.value = operation.value ?? '';
    this.tombstone = false;
  }
}

export class CrdtSequence {
  private nodes: InsertNode[] = [];

  constructor(initial?: CrdtOperation[]) {
    if (initial) {
      initial.forEach((operation) => this.apply(operation));
    }
  }

  apply(operation: CrdtOperation) {
    switch (operation.operationType) {
      case 'insert':
        this.insert(operation);
        break;
      case 'delete':
        this.delete(operation);
        break;
      default:
        break;
    }
  }

  applyAll(operations: CrdtOperation[]) {
    operations.forEach((operation) => this.apply(operation));
  }

  confirmLocalInsert(tempId: number, actual: CrdtOperation) {
    const node = this.nodes.find((candidate) => candidate.operation.id === tempId);
    if (node) {
      node.operation = actual;
      node.value = actual.value ?? node.value;
      node.tombstone = false;
      return;
    }
    this.apply(actual);
  }

  hasOperation(id: number) {
    return this.nodes.some((node) => node.operation.id === id);
  }

  text() {
    return this.nodes
      .filter((node) => !node.tombstone)
      .map((node) => node.value)
      .join('');
  }

  anchorsAt(offset: number): AnchorPair {
    const clamped = Math.max(0, Math.min(offset, this.length()));
    let cursor = 0;
    let previousConfirmed: number | null = null;
    for (let i = 0; i < this.nodes.length; i++) {
      const node = this.nodes[i];
      if (node.tombstone || node.value.length === 0) {
        continue;
      }
      const nextCursor = cursor + node.value.length;
      const confirmedId = this.confirmedId(node);
      if (confirmedId && nextCursor <= clamped) {
        previousConfirmed = confirmedId;
      }
      if (clamped === cursor) {
        const rightConfirmed = confirmedId ?? this.nextConfirmedId(i + 1);
        return {
          leftNode: previousConfirmed,
          rightNode: rightConfirmed
        };
      }
      if (clamped < nextCursor) {
        const rightConfirmed = this.nextConfirmedId(i + 1);
        return {
          leftNode: confirmedId ?? previousConfirmed,
          rightNode: rightConfirmed
        };
      }
      cursor = nextCursor;
    }
    return {
      leftNode: previousConfirmed,
      rightNode: null
    };
  }

  length() {
    return this.nodes.reduce((total, node) => (node.tombstone ? total : total + node.value.length), 0);
  }

  reset(operations: CrdtOperation[]) {
    this.nodes = [];
    this.applyAll(operations);
  }

  private insert(operation: CrdtOperation) {
    const index = this.insertionIndex(operation);
    const node = new InsertNode(operation);
    this.nodes.splice(index, 0, node);
  }

  private delete(operation: CrdtOperation) {
    const targetIndex = this.insertionIndex(operation);
    let offset = this.characterOffsetBefore(targetIndex);
    const toRemove = Math.max(1, operation.value ? operation.value.length : 1);
    let remaining = toRemove;
    let position = 0;
    for (const node of this.nodes) {
      if (node.tombstone) {
        continue;
      }
      const nodeLen = node.value.length;
      const nextPosition = position + nodeLen;
      if (nextPosition <= offset) {
        position = nextPosition;
        continue;
      }
      const start = Math.max(0, offset - position);
      const end = Math.min(nodeLen, start + remaining);
      node.value = node.value.slice(0, start) + node.value.slice(end);
      if (node.value.length === 0) {
        node.tombstone = true;
      }
      const removed = end - start;
      remaining -= removed;
      position = nextPosition - removed;
      offset = position;
      if (remaining <= 0) {
        break;
      }
    }
  }

  private insertionIndex(operation: CrdtOperation) {
    const leftId = operation.leftNode ?? null;
    const rightId = operation.rightNode ?? null;
    const insertPos = leftId !== null ? this.indexOf(leftId) + 1 : 0;
    let rightPos = rightId !== null ? this.indexOf(rightId) : this.nodes.length;
    let position = insertPos;
    if (rightPos >= 0) {
      position = Math.min(position, rightPos);
    } else {
      rightPos = this.nodes.length;
    }
    while (position < rightPos) {
      const current = this.nodes[position];
      if (
        current &&
        this.sameAnchors(current.operation, leftId, rightId) &&
        this.compare(operation, current.operation) > 0
      ) {
        position += 1;
      } else {
        break;
      }
    }
    return position;
  }

  private indexOf(operationId: number | null) {
    if (operationId === null) {
      return -1;
    }
    return this.nodes.findIndex((node) => node.operation.id === operationId);
  }

  private sameAnchors(candidate: CrdtOperation, leftId: number | null, rightId: number | null) {
    const candidateLeft = candidate.leftNode ?? null;
    const candidateRight = candidate.rightNode ?? null;
    return candidateLeft === leftId && candidateRight === rightId;
  }

  private compare(first: CrdtOperation, second: CrdtOperation) {
    const counterCmp = first.nodeCounter - second.nodeCounter;
    if (counterCmp !== 0) {
      return counterCmp;
    }
    return first.nodeSite - second.nodeSite;
  }

  private characterOffsetBefore(nodeIndex: number) {
    let offset = 0;
    for (let i = 0; i < nodeIndex && i < this.nodes.length; i++) {
      const node = this.nodes[i];
      if (!node.tombstone) {
        offset += node.value.length;
      }
    }
    return offset;
  }

  private confirmedId(node: InsertNode) {
    const id = node.operation.id;
    return typeof id === 'number' && id > 0 ? id : null;
  }

  private nextConfirmedId(fromIndex: number) {
    for (let i = fromIndex; i < this.nodes.length; i++) {
      const node = this.nodes[i];
      if (!node.tombstone && node.value.length > 0) {
        const id = this.confirmedId(node);
        if (id) {
          return id;
        }
      }
    }
    return null;
  }
}

export function cloneOperations(operations: CrdtOperation[]) {
  return operations.map((operation) => ({ ...operation }));
}
