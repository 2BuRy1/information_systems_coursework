import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createSession, listSessions } from '../services/api';
import { SessionSummary } from '../types';
import { useAuth } from '../contexts/AuthContext';

const SessionsPage = () => {
  const { tokens, user, logout } = useAuth();
  const [sessions, setSessions] = useState<SessionSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState('Новая доска');
  const [language, setLanguage] = useState('typescript');
  const navigate = useNavigate();

  useEffect(() => {
    if (!tokens) return;
    listSessions(tokens)
      .then((payload) => setSessions(payload.items))
      .finally(() => setLoading(false));
  }, [tokens]);

  const sorted = useMemo(() => sessions.sort((a, b) => (a.updatedAt > b.updatedAt ? -1 : 1)), [sessions]);

  const handleCreate = async () => {
    if (!tokens) return;
    const session = await createSession(tokens, { name, language });
    navigate(`/sessions/${session.id}`);
  };

  if (!tokens) {
    return null;
  }

  return (
    <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2>Мои доски</h2>
          <p>{user?.email}</p>
        </div>
        <button onClick={logout}>Выйти</button>
      </header>
      <section className="card" style={{ display: 'flex', gap: 12 }}>
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Название" />
        <input value={language} onChange={(e) => setLanguage(e.target.value)} placeholder="Язык" />
        <button onClick={handleCreate}>Создать</button>
      </section>
      <section className="sessions-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 16 }}>
        {loading && <p>Загрузка...</p>}
        {!loading &&
          sorted.map((session) => (
            <div key={session.id} className="card" style={{ cursor: 'pointer' }} onClick={() => navigate(`/sessions/${session.id}`)}>
              <h3>{session.name}</h3>
              <p>Язык: {session.language}</p>
              <p>Роль: {session.role}</p>
            </div>
          ))}
      </section>
    </div>
  );
};

export default SessionsPage;
