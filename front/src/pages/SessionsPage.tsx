import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import AppShell from "../components/AppShell";
import LanguagePicker from "../components/LanguagePicker";
import { useAuth } from "../contexts/AuthContext";
import { createSession, listSessions } from "../services/api";
import type { SessionSummary } from "../types";
import { displayName } from "../utils/format";

const SessionsPage = () => {
  const { tokens, user } = useAuth();
  const [sessions, setSessions] = useState<SessionSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState("");
  const [language, setLanguage] = useState("typescript");
  const navigate = useNavigate();
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    if (!tokens) return;
    listSessions(tokens)
      .then((payload) => setSessions(payload.items))
      .finally(() => setLoading(false));
  }, [tokens]);

  const sorted = useMemo(
    () => [...sessions].sort((a, b) => (a.updatedAt > b.updatedAt ? -1 : 1)),
    [sessions],
  );

  const handleCreate = async () => {
    if (!tokens) return;
    const trimmed = name.trim() || "Новая доска";
    setCreating(true);
    try {
      const session = await createSession(tokens, { name: trimmed, language });
      navigate(`/sessions/${session.id}`);
    } finally {
      setCreating(false);
    }
  };

  if (!tokens) {
    return null;
  }

  return (
    <AppShell
      title="Мои доски"
      subtitle={user ? `Добро пожаловать, ${displayName(user)}.` : undefined}
    >
      <section className="card create-card">
        <div className="create-head">
          <div>
            <h3 style={{ margin: 0 }}>Создать доску</h3>
            <p className="muted" style={{ margin: "6px 0 0" }}>
              Выберите язык — и можно начинать совместную работу.
            </p>
          </div>
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleCreate}
            disabled={creating}
          >
            {creating ? "Создаём…" : "Создать"}
          </button>
        </div>
        <div className="create-body">
          <div className="create-form">
            <label className="field">
              <span className="field-label">Название</span>
              <input
                className="input"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Например: Подготовка к интервью"
              />
            </label>
            <div className="field">
              <div className="field-label">Язык</div>
              <LanguagePicker value={language} onChange={setLanguage} />
            </div>
          </div>
        </div>
      </section>

      <section className="sessions-grid">
        {loading && <p className="muted">Загрузка…</p>}
        {!loading &&
          sorted.map((session) => (
            <button
              key={session.id}
              type="button"
              className="card session-card"
              onClick={() => navigate(`/sessions/${session.id}`)}
            >
              <div className="session-card-top">
                <div className="session-title">{session.name}</div>
                <span className="pill pill-muted">{session.role}</span>
              </div>
              <div className="session-meta">
                <span className="pill">{session.language || "—"}</span>
                <span className="muted">{new Date(session.updatedAt).toLocaleString()}</span>
              </div>
            </button>
          ))}
      </section>
    </AppShell>
  );
};

export default SessionsPage;
