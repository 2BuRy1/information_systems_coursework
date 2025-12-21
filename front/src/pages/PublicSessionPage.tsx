import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { acceptInvite, loadPublicSession } from "../services/api";
import type { PublicSessionView, SessionDetails } from "../types";

const PublicSessionPage = () => {
  const { token } = useParams();
  const navigate = useNavigate();
  const { tokens } = useAuth();
  const [data, setData] = useState<PublicSessionView | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [joining, setJoining] = useState(false);
  const [joinError, setJoinError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;
    loadPublicSession(token)
      .then(setData)
      .catch(() => setError("Ссылка недействительна или истекла."));
  }, [token]);

  const handleJoin = async () => {
    if (!token) return;
    if (!tokens) {
      navigate("/login");
      return;
    }
    setJoining(true);
    setJoinError(null);
    try {
      const session: SessionDetails = await acceptInvite(tokens, token);
      navigate(`/sessions/${session.id}`);
    } catch (err) {
      setJoinError(err instanceof Error ? err.message : "Не удалось присоединиться.");
    } finally {
      setJoining(false);
    }
  };

  if (error) {
    return <div className="container">{error}</div>;
  }

  if (!data) {
    return <div className="container">Загрузка...</div>;
  }

  return (
    <div className="container">
      <div className="card">
        <h2>{data.name}</h2>
        <p>Владелец: {data.ownerName}</p>
        <p>Срок действия ссылки: {new Date(data.expiresAt).toLocaleString()}</p>
        <pre
          style={{
            whiteSpace: "pre-wrap",
            background: "#111",
            color: "#f8fafc",
            padding: 16,
            borderRadius: 8,
          }}
        >
          {data.document.content}
        </pre>
        {joinError && <p style={{ color: "tomato" }}>{joinError}</p>}
        <button type="button" disabled={joining} onClick={handleJoin} style={{ marginTop: 12 }}>
          {tokens
            ? joining
              ? "Подключаем..."
              : "Присоединиться к сессии"
            : "Войти, чтобы присоединиться"}
        </button>
      </div>
    </div>
  );
};

export default PublicSessionPage;
