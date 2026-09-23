import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import CopyField from "../components/CopyField";
import DocumentEditor from "../components/DocumentEditor";
import LanguagePicker from "../components/LanguagePicker";
import MembersList from "../components/MembersList";
import PresencePanel from "../components/PresencePanel";
import TaskBoard from "../components/TaskBoard";
import { useAuth } from "../contexts/AuthContext";
import { createInvite, deleteSession, loadSession, updateSession } from "../services/api";
import type { SessionDetails } from "../types";

const SessionDetailsPage = () => {
  const { sessionId } = useParams();
  const numericId = Number(sessionId);
  const { tokens, user } = useAuth();
  const navigate = useNavigate();
  const [session, setSession] = useState<SessionDetails | null>(null);
  const [inviteTtlMinutes, setInviteTtlMinutes] = useState(60);
  const [renewing, setRenewing] = useState(false);
  const [editName, setEditName] = useState("");
  const [editLanguage, setEditLanguage] = useState("typescript");
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!tokens || Number.isNaN(numericId)) return;
    loadSession(tokens, numericId).then(setSession);
  }, [numericId, tokens]);

  useEffect(() => {
    if (!session) return;
    setEditName(session.name);
    setEditLanguage(session.language || "typescript");
  }, [session]);

  if (!tokens || Number.isNaN(numericId)) {
    return null;
  }

  if (!session) {
    return <div className="container">Загрузка...</div>;
  }

  const publicUrl = `${window.location.origin}/public/${session.link}`;
  const canManageSession = session.role === "owner";
  const canRenewInvite = canManageSession;
  const currentLanguage = session.language || "typescript";
  const trimmedName = editName.trim();
  const nameChanged = trimmedName !== session.name;
  const languageChanged = editLanguage !== currentLanguage;
  const hasChanges = nameChanged || languageChanged;
  const saveDisabled = saving || deleting || !hasChanges || !trimmedName;

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

  const handleSave = async () => {
    if (!tokens || saveDisabled) return;
    const payload: Partial<{ name: string; language: string }> = {};
    if (nameChanged) {
      payload.name = trimmedName;
    }
    if (languageChanged) {
      payload.language = editLanguage;
    }
    if (!Object.keys(payload).length) return;
    setSaving(true);
    try {
      const updated = await updateSession(tokens, numericId, payload);
      setSession(updated);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!tokens || deleting) return;
    const confirmed = window.confirm(
      `Удалить доску «${session.name}»? Это действие нельзя отменить.`,
    );
    if (!confirmed) return;
    setDeleting(true);
    try {
      await deleteSession(tokens, numericId);
      navigate("/sessions");
    } finally {
      setDeleting(false);
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

      {canManageSession && (
        <section className="card create-card">
          <div className="create-head">
            <div>
              <h3 style={{ margin: 0 }}>Настройки доски</h3>
              <p className="muted" style={{ margin: "6px 0 0" }}>
                Обновите название и язык подсветки в любой момент.
              </p>
            </div>
            <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleSave}
                disabled={saveDisabled}
              >
                {saving ? "Сохраняем…" : "Сохранить"}
              </button>
              <button
                type="button"
                className="btn danger"
                onClick={handleDelete}
                disabled={deleting || saving}
              >
                {deleting ? "Удаляем…" : "Удалить доску"}
              </button>
            </div>
          </div>
          <div className="create-form">
            <label className="field">
              <span className="field-label">Название</span>
              <input
                className="input"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                placeholder="Например: Командный стендап"
              />
            </label>
            <div className="field">
              <div className="field-label">Язык подсветки</div>
              <LanguagePicker value={editLanguage} onChange={setEditLanguage} />
            </div>
          </div>
        </section>
      )}

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
