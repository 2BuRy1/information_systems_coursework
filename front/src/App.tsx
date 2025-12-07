import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import LoginPage from './pages/LoginPage';
import OAuthCallbackPage from './pages/OAuthCallbackPage';
import SessionsPage from './pages/SessionsPage';
import SessionDetailsPage from './pages/SessionDetailsPage';
import PublicSessionPage from './pages/PublicSessionPage';

const RequireAuth: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, loading } = useAuth();
  if (loading) {
    return <p style={{ padding: 32 }}>Загрузка профиля...</p>;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const App = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/oauth/callback/:provider" element={<OAuthCallbackPage />} />
      <Route
        path="/sessions"
        element={
          <RequireAuth>
            <SessionsPage />
          </RequireAuth>
        }
      />
      <Route
        path="/sessions/:sessionId"
        element={
          <RequireAuth>
            <SessionDetailsPage />
          </RequireAuth>
        }
      />
      <Route path="/public/:token" element={<PublicSessionPage />} />
      <Route path="/" element={<Navigate to="/sessions" replace />} />
      <Route path="*" element={<Navigate to="/sessions" replace />} />
    </Routes>
  );
};

export default App;
