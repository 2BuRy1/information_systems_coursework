import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import DocumentEditor from '../components/DocumentEditor';
import MembersList from '../components/MembersList';
import PresencePanel from '../components/PresencePanel';
import TaskBoard from '../components/TaskBoard';
import { loadSession } from '../services/api';
import { SessionDetails } from '../types';
import { useAuth } from '../contexts/AuthContext';
import AppShell from '../components/AppShell';
import CopyField from '../components/CopyField';

const SessionDetailsPage = () => {
  const { sessionId } = useParams();
  const numericId = Number(sessionId);
  const { tokens, user } = useAuth();
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

  const publicUrl = `${window.location.origin}/public/${session.link}`;

  return (
    <AppShell
      title={session.name}
      subtitle={session.language ? `Язык: ${session.language}` : undefined}
      actions={
        <button type="button" className="btn btn-ghost" onClick={() => navigate('/sessions')}>
          К списку
        </button>
      }
    >
      <section className="card" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <CopyField
          label="Ссылка для гостей"
          value={publicUrl}
          hint={`Действует до: ${new Date(session.linkExpiresAt).toLocaleString()}`}
        />
      </section>

      <div className="board-grid">
        <DocumentEditor sessionId={numericId} />
        <div className="board-side">
          <MembersList sessionId={numericId} currentUserId={user?.id} canManage={session.role === 'owner'} />
          <PresencePanel sessionId={numericId} />
        </div>
      </div>

      <TaskBoard sessionId={numericId} />
    </AppShell>
  );
};

export default SessionDetailsPage;
