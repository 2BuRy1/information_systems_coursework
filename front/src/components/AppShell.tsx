import React from 'react';
import { Link } from 'react-router-dom';
import ProfileMenu from './ProfileMenu';

interface Props {
  title?: string;
  subtitle?: string;
  children: React.ReactNode;
  actions?: React.ReactNode;
}

const AppShell: React.FC<Props> = ({ title, subtitle, actions, children }) => {
  return (
    <div className="app">
      <header className="topbar">
        <div className="topbar-inner">
          <Link to="/sessions" className="brand" aria-label="На главную">
            <div className="brand-mark">CT</div>
            <div className="brand-text">
              <div className="brand-title">CodeTogether</div>
              <div className="brand-subtitle">Совместные сессии</div>
            </div>
          </Link>
          <div className="topbar-right">
            {actions}
            <ProfileMenu />
          </div>
        </div>
      </header>
      <main className="container">
        {(title || subtitle) && (
          <div className="page-head">
            {title && <h2 className="page-title">{title}</h2>}
            {subtitle && <p className="page-subtitle">{subtitle}</p>}
          </div>
        )}
        {children}
      </main>
    </div>
  );
};

export default AppShell;
