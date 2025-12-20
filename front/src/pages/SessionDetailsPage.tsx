import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import DocumentEditor from '../components/DocumentEditor';
import MembersList from '../components/MembersList';
import PresencePanel from '../components/PresencePanel';
import TaskBoard from '../components/TaskBoard';
import { loadSession } from '../services/api';
import { SessionDetails } from '../types';
import { useAuth } from '../contexts/AuthContext';

const SessionDetailsPage = () => {
  const { sessionId } = useParams();
  const numericId = Number(sessionId);
  const { tokens, user, logout } = useAuth();
  const navigate = useNavigate();
  const [session, setSession] = useState<SessionDetails | null>(null);

  useEffect(() => {
    if (!tokens || Number.isNaN(numericId)) return;
    loadSession(tokens, numericId).then(setSession);
  }, [numericId, tokens]);

  if (!tokens || Number.isNaN(numericId)) {
    return null;
  }

  if (!session) {
    return <div className="container">Загрузка...</div>;
  }

  return (
    <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2>{session.name}</h2>
          <p>
            Ссылка для гостей:
            <code>{`${window.location.origin}/public/${session.link}`}</code>
          </p>
          <small>Срок действия: {new Date(session.linkExpiresAt).toLocaleString()}</small>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <button onClick={() => navigate('/sessions')}>К списку</button>
          <button onClick={logout}>Выйти</button>
        </div>
      </header>
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 16 }}>
        <DocumentEditor sessionId={numericId} />
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <MembersList
            sessionId={numericId}
            currentUserId={user?.id}
            canManage={session.role === 'owner'}
          />
          <PresencePanel sessionId={numericId} />
        </div>
      </div>
      <TaskBoard sessionId={numericId} />
    </div>
  );
};

export default SessionDetailsPage;
