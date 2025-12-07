import { useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const OAuthCallbackPage = () => {
  const { provider } = useParams<{ provider: 'github' | 'google' }>();
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const { finishOAuth } = useAuth();

  useEffect(() => {
    const code = params.get('code');
    const state = params.get('state');
    if (!provider || !code || !state) {
      navigate('/login');
      return;
    }
    finishOAuth(provider, code, state, window.location.origin + `/oauth/callback/${provider}`)
      .then(() => navigate('/sessions', { replace: true }))
      .catch(() => navigate('/login', { replace: true }));
  }, [provider, params, finishOAuth, navigate]);

  return <p style={{ padding: 32 }}>Завершаем авторизацию...</p>;
};

export default OAuthCallbackPage;
