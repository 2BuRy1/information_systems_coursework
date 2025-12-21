import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import CopyField from "../components/CopyField";
import DocumentEditor from "../components/DocumentEditor";
import MembersList from "../components/MembersList";
import PresencePanel from "../components/PresencePanel";
import TaskBoard from "../components/TaskBoard";
import { useAuth } from "../contexts/AuthContext";
import { createInvite, loadSession } from "../services/api";
import type { SessionDetails } from "../types";

const SessionDetailsPage = () => {
  const { sessionId } = useParams();
  const numericId = Number(sessionId);
  const { tokens, user } = useAuth();
  const navigate = useNavigate();
  const [session, setSession] = useState<SessionDetails | null>(null);
  const [inviteTtlMinutes, setInviteTtlMinutes] = useState(60);
  const [renewing, setRenewing] = useState(false);

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
  const canRenewInvite = session.role === "owner";

  const handleRenewInvite = async () => {
    if (!tokens) return;
    setRenewing(true);
    try {
      await createInvite(tokens, numericId, inviteTtlMinutes);
      const updated = await loadSession(tokens, numericId);
      setSession(updated);
    } finally {
      setRenewing(false);
    }
  };

  return (
    <AppShell
      title={session.name}
      subtitle={session.language ? `Язык: ${session.language}` : undefined}
      actions={
        <button type="button" className="btn btn-ghost" onClick={() => navigate("/sessions")}>
          К списку
        </button>
      }
    >
      <section className="card" style={{ display: "flex", flexDirection: "column", gap: 10 }}>
        <CopyField
          label="Ссылка для гостей"
          value={publicUrl}
          hint={`Действует до: ${new Date(session.linkExpiresAt).toLocaleString()}`}
        />
        {canRenewInvite && (
          <div style={{ display: "flex", gap: 10, alignItems: "end", flexWrap: "wrap" }}>
            <label className="field" style={{ margin: 0, minWidth: 220 }}>
              <span className="field-label">Срок действия (мин)</span>
              <select
                className="input"
                value={inviteTtlMinutes}
                onChange={(e) => setInviteTtlMinutes(Number(e.target.value))}
              >
                <option value={5}>5</option>
                <option value={30}>30</option>
                <option value={60}>60</option>
                <option value={180}>180</option>
                <option value={1440}>1440 (сутки)</option>
              </select>
            </label>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleRenewInvite}
              disabled={renewing}
            >
              {renewing ? "Обновляем…" : "Сгенерировать новую ссылку"}
            </button>
          </div>
        )}
      </section>

      <div className="board-grid">
        <DocumentEditor sessionId={numericId} language={session.language} />
        <div className="board-side">
          <MembersList
            sessionId={numericId}
            currentUserId={user?.id}
            canManage={session.role === "owner"}
          />
          <PresencePanel sessionId={numericId} />
        </div>
      </div>

      <TaskBoard sessionId={numericId} />
    </AppShell>
  );
};

export default SessionDetailsPage;
