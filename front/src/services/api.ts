import {
  AuthTokens,
  DocumentState,
  OperationAck,
  OperationRequest,
  OperationsResponse,
  SessionDetails,
  SessionSummary,
  SessionPresence,
  SessionMember,
  Task,
  UserProfile
} from '../types';

const API_BASE = '/v1';

async function request<T>(path: string, options: RequestInit = {}, tokens?: AuthTokens | null): Promise<T> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };
  if (tokens?.accessToken) {
    headers['Authorization'] = `Bearer ${tokens.accessToken}`;
  }
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers
  });
  if (response.status === 204) {
    return undefined as T;
  }
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || response.statusText);
  }
  return (await response.json()) as T;
}

export async function getOAuthUrl(provider: 'github' | 'google'): Promise<{ url: string; state: string }> {
  return request(`/oauth/${provider}/url`);
}

export async function exchangeOAuth(
  provider: 'github' | 'google',
  payload: { code: string; state: string; redirectUri: string }
): Promise<{ tokens: AuthTokens; user: UserProfile }> {
  return request(`/oauth/${provider}/exchange`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function loadProfile(tokens: AuthTokens): Promise<UserProfile> {
  return request('/users/me', {}, tokens);
}

export async function listSessions(
  tokens: AuthTokens,
  params: { role?: string; cursor?: number; limit?: number } = {}
): Promise<{ items: SessionSummary[]; nextCursor?: number | null }> {
  const query = new URLSearchParams();
  if (params.role) query.set('role', params.role);
  if (params.limit) query.set('limit', String(params.limit));
  if (params.cursor) query.set('cursor', String(params.cursor));
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request(`/sessions${suffix}`, {}, tokens);
}

export async function createSession(
  tokens: AuthTokens,
  payload: { name: string; language: string }
): Promise<SessionDetails> {
  return request(
    '/sessions',
    {
      method: 'POST',
      body: JSON.stringify(payload)
    },
    tokens
  );
}

export async function loadSession(tokens: AuthTokens, sessionId: number): Promise<SessionDetails> {
  return request(`/sessions/${sessionId}`, {}, tokens);
}

export async function loadMembers(tokens: AuthTokens, sessionId: number): Promise<SessionMember[]> {
  return request(`/sessions/${sessionId}/members`, {}, tokens);
}

export async function updateMemberRole(
  tokens: AuthTokens,
  sessionId: number,
  userId: number,
  role: string
): Promise<SessionMember> {
  return request(
    `/sessions/${sessionId}/members/${userId}`,
    {
      method: 'PATCH',
      body: JSON.stringify({ role })
    },
    tokens
  );
}

export async function removeMember(tokens: AuthTokens, sessionId: number, userId: number): Promise<void> {
  await request(`/sessions/${sessionId}/members/${userId}`, { method: 'DELETE' }, tokens);
}

export async function loadTasks(tokens: AuthTokens, sessionId: number): Promise<Task[]> {
  return request(`/sessions/${sessionId}/tasks`, {}, tokens);
}

export async function createTask(
  tokens: AuthTokens,
  sessionId: number,
  payload: { text: string; metadata?: Record<string, string> }
): Promise<Task> {
  return request(
    `/sessions/${sessionId}/tasks`,
    {
      method: 'POST',
      body: JSON.stringify(payload)
    },
    tokens
  );
}

export async function updateTask(
  tokens: AuthTokens,
  sessionId: number,
  taskId: number,
  payload: Partial<{ text: string; status: string; metadata: Record<string, string> }>
): Promise<Task> {
  return request(
    `/sessions/${sessionId}/tasks/${taskId}`,
    {
      method: 'PATCH',
      body: JSON.stringify(payload)
    },
    tokens
  );
}

export async function deleteTask(tokens: AuthTokens, sessionId: number, taskId: number): Promise<void> {
  await request(`/sessions/${sessionId}/tasks/${taskId}`, { method: 'DELETE' }, tokens);
}

export async function loadDocument(tokens: AuthTokens, sessionId: number): Promise<DocumentState> {
  return request(`/sessions/${sessionId}/document`, {}, tokens);
}

export async function loadOperations(
  tokens: AuthTokens,
  sessionId: number,
  sinceVersion = 0
): Promise<OperationsResponse> {
  return request(`/sessions/${sessionId}/document/operations?sinceVersion=${Math.max(0, sinceVersion)}`, {}, tokens);
}

export async function fetchPresence(tokens: AuthTokens, sessionId: number): Promise<SessionPresence> {
  return request(`/sessions/${sessionId}/presence`, {}, tokens);
}

export async function appendOperations(
  tokens: AuthTokens,
  sessionId: number,
  body: OperationRequest
): Promise<OperationAck> {
  return request(
    `/sessions/${sessionId}/document/operations`,
    {
      method: 'POST',
      body: JSON.stringify(body)
    },
    tokens
  );
}

export async function updateCursor(
  tokens: AuthTokens,
  sessionId: number,
  payload: { line: number; col: number; color?: string }
): Promise<void> {
  await request(
    `/sessions/${sessionId}/presence/cursor`,
    {
      method: 'PUT',
      body: JSON.stringify(payload)
    },
    tokens
  );
}

export async function clearCursor(tokens: AuthTokens, sessionId: number): Promise<void> {
  await request(`/sessions/${sessionId}/presence/cursor`, { method: 'DELETE' }, tokens);
}

export async function updateHighlight(
  tokens: AuthTokens,
  sessionId: number,
  payload: { startLine: number; endLine: number; startCol: number; endCol: number; color?: string }
): Promise<void> {
  await request(
    `/sessions/${sessionId}/presence/highlight`,
    {
      method: 'PUT',
      body: JSON.stringify(payload)
    },
    tokens
  );
}

export async function clearHighlight(tokens: AuthTokens, sessionId: number): Promise<void> {
  await request(`/sessions/${sessionId}/presence/highlight`, { method: 'DELETE' }, tokens);
}

export async function loadPublicSession(token: string) {
  return request(`/public/sessions/${token}`);
}

export async function acceptInvite(token: AuthTokens, inviteToken: string): Promise<SessionDetails> {
  return request(`/invites/${inviteToken}/accept`, { method: 'POST' }, token);
}
