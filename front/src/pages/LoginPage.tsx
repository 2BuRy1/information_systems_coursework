import { useAuth } from '../contexts/AuthContext';
import './LoginPage.css';

const LoginPage = () => {
  const { startOAuth } = useAuth();

  return (
    <div className="login-wrapper">
      <div className="login-card card">
        <h1>CodeTogether</h1>
        <p>Подключитесь через OAuth и получайте доступ к доскам совместного редактирования.</p>
        <div className="login-buttons">
          <button onClick={() => startOAuth('github')} className="primary">Войти через GitHub</button>
          <button onClick={() => startOAuth('google')} className="secondary">Войти через Google</button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
