export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  avatarUrl?: string | null;
  role: string;
}

export interface SessionSummary {
  id: number;
  name: string;
  language?: string;
  ownerId: number;
  role: 'owner' | 'editor' | 'viewer';
  updatedAt: string;
}

export interface SessionDetails extends SessionSummary {
  link: string;
  linkExpiresAt: string;
  stats?: DocumentStats;
}

export interface DocumentState {
  id: number;
  sessionId: number;
  version: number;
  content: string;
  updatedAt?: string;
}

export interface DocumentStats {
  sessionId: number;
  documentId: number;
  activeParticipants: number;
  operationCount: number;
  lastSnapshotVersion: number;
  averageLatencyMs?: number | null;
  typingRate?: number | null;
}

export interface Task {
  id: number;
  sessionId: number;
  text: string;
  status: 'open' | 'in_progress' | 'done';
  userId: number;
  metadata?: Record<string, string>;
  createdAt: string;
  updatedAt: string;
}

export interface SessionMember {
  sessionId: number;
  userId: number;
  role: string;
  joinedAt: string;
  name: string;
  avatarUrl?: string;
}

export interface CursorState {
  sessionId: number;
  userId: number;
  line: number;
  col: number;
  color: string;
  updatedAt: string;
}

export interface Highlight {
  sessionId: number;
  userId: number;
  startLine: number;
  endLine: number;
  startCol: number;
  endCol: number;
  color: string;
  updatedAt: string;
}

export interface SessionPresence {
  sessionId: number;
  cursors: CursorState[];
  highlights: Highlight[];
}

export interface OperationRequest {
  baseVersion: number;
  operations: Array<{
    operationType: 'insert' | 'delete' | 'retain';
    nodeCounter: number;
    nodeSite: number;
    leftNode?: number | null;
    rightNode?: number | null;
    value: string;
    color?: string | null;
  }>;
}

export interface OperationAck {
  appliedVersion: number;
  operations: Array<{
    id: number;
    version: number;
    value: string;
    operationType: string;
  }>;
}
