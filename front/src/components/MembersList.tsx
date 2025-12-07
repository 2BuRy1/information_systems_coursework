import { useEffect, useState } from 'react';
import { loadMembers } from '../services/api';
import { SessionMember } from '../types';
import { useAuth } from '../contexts/AuthContext';

interface Props {
  sessionId: number;
}

const MembersList: React.FC<Props> = ({ sessionId }) => {
  const { tokens } = useAuth();
  const [members, setMembers] = useState<SessionMember[]>([]);

  useEffect(() => {
    if (!tokens) return;
    loadMembers(tokens, sessionId).then(setMembers);
  }, [sessionId, tokens]);

  return (
    <div className="card">
      <h3>Участники</h3>
      <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
        {members.map((m) => (
          <li key={m.userId} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
            <span>{m.name}</span>
            <span>{m.role}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default MembersList;
