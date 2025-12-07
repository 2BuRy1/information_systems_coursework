import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { loadPublicSession } from '../services/api';

const PublicSessionPage = () => {
  const { token } = useParams();
  const [data, setData] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;
    loadPublicSession(token)
      .then(setData)
      .catch(() => setError('Ссылка недействительна или истекла.'));
  }, [token]);

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
        <pre style={{ whiteSpace: 'pre-wrap', background: '#111', color: '#f8fafc', padding: 16, borderRadius: 8 }}>
          {data.document.content}
        </pre>
      </div>
    </div>
  );
};

export default PublicSessionPage;
