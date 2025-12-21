import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { displayInitials, displayName } from '../utils/format';

const AVATARS = Array.from({ length: 12 }, (_, index) => `/avatars/av-${String(index + 1).padStart(2, '0')}.svg`);

interface AvatarModalProps {
  open: boolean;
  onClose: () => void;
  onPick: (avatarUrl: string) => void;
  busy: boolean;
}

const AvatarModal: React.FC<AvatarModalProps> = ({ open, onClose, onPick, busy }) => {
  if (!open) return null;
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" onMouseDown={onClose}>
      <div className="modal" onMouseDown={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <div className="modal-title">Выберите аватар</div>
            <div className="modal-subtitle">Можно поменять в любой момент</div>
          </div>
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Закрыть
          </button>
        </div>
        <div className="avatar-grid">
          {AVATARS.map((src) => (
            <button
              key={src}
              type="button"
              className="avatar-tile"
              onClick={() => onPick(src)}
              disabled={busy}
              title="Выбрать"
            >
              <img src={src} alt="avatar" width={48} height={48} />
            </button>
          ))}
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => onPick(AVATARS[Math.floor(Math.random() * AVATARS.length)])}
            disabled={busy}
          >
            Случайный
          </button>
        </div>
      </div>
    </div>
  );
};

const ProfileMenu: React.FC = () => {
  const { user, patchProfile, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const [avatarOpen, setAvatarOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const rootRef = useRef<HTMLDivElement | null>(null);

  const name = useMemo(() => displayName(user), [user]);
  const initials = useMemo(() => displayInitials(name), [name]);

  useEffect(() => {
    const handle = (event: MouseEvent) => {
      const target = event.target as Node | null;
      if (!target) return;
      if (!rootRef.current?.contains(target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handle);
    return () => document.removeEventListener('mousedown', handle);
  }, []);

  const handlePickAvatar = useCallback(
    async (avatarUrl: string) => {
      if (!user) return;
      setSaving(true);
      try {
        await patchProfile({ avatarUrl });
        setAvatarOpen(false);
        setOpen(false);
      } finally {
        setSaving(false);
      }
    },
    [patchProfile, user]
  );

  return (
    <div className="profile" ref={rootRef}>
      <button type="button" className="profile-button" onClick={() => setOpen((prev) => !prev)} aria-expanded={open}>
        {user?.avatarUrl ? (
          <img className="avatar" src={user.avatarUrl} alt={name} />
        ) : (
          <div className="avatar avatar-fallback">{initials}</div>
        )}
        <span className="profile-name">{name}</span>
      </button>
      {open && (
        <div className="profile-popover">
          <div className="profile-popover-head">
            <div className="profile-popover-title">{name}</div>
            {user?.email && <div className="profile-popover-subtitle">{user.email}</div>}
          </div>
          <div className="profile-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setAvatarOpen(true)}>
              Сменить аватар
            </button>
            <button type="button" className="btn btn-ghost danger" onClick={logout}>
              Выйти
            </button>
          </div>
        </div>
      )}
      <AvatarModal open={avatarOpen} onClose={() => setAvatarOpen(false)} onPick={handlePickAvatar} busy={saving} />
    </div>
  );
};

export default ProfileMenu;

