import { useEffect, useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import { fetchPresence, loadMembers } from "../services/api";
import type { SessionMember, SessionPresence } from "../types";

interface Props {
  sessionId: number;
}

const PresencePanel: React.FC<Props> = ({ sessionId }) => {
  const { tokens } = useAuth();
  const [presence, setPresence] = useState<SessionPresence | null>(null);
  const [members, setMembers] = useState<SessionMember[]>([]);

  useEffect(() => {
    if (!tokens) return;
    fetchPresence(tokens, sessionId).then(setPresence);
    const interval = setInterval(() => fetchPresence(tokens, sessionId).then(setPresence), 5000);
    return () => clearInterval(interval);
  }, [sessionId, tokens]);

  useEffect(() => {
    if (!tokens) return;
    loadMembers(tokens, sessionId).then(setMembers);
  }, [sessionId, tokens]);

  if (!presence) {
    return <div className="card">Загрузка присутствия...</div>;
  }

  return (
    <div className="card">
      <h3>Онлайн ({presence.cursors.length})</h3>
      <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
        {presence.cursors.map((cursor) => (
          <li key={cursor.userId} style={{ display: "flex", justifyContent: "space-between" }}>
            <span>
              {members.find((m) => m.userId === cursor.userId)?.name ?? `Участник ${cursor.userId}`}
            </span>
            <span style={{ color: cursor.color }}>{`${cursor.line + 1}:${cursor.col + 1}`}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default PresencePanel;
