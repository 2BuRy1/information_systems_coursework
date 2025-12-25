import { useCallback, useEffect, useMemo, useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import { loadMembers, removeMember, updateMemberRole } from "../services/api";
import type { SessionMember } from "../types";
import { displayName } from "../utils/format";

interface Props {
  sessionId: number;
  currentUserId?: number;
  canManage: boolean;
}

const roleLabels: Record<string, string> = {
  owner: "Владелец",
  editor: "Редактор",
  viewer: "Наблюдатель",
};

const roleOptions: Array<{ value: string; label: string }> = [
  { value: "editor", label: "Редактор" },
  { value: "viewer", label: "Наблюдатель" },
];

const MembersList: React.FC<Props> = ({ sessionId, currentUserId, canManage }) => {
  const { tokens } = useAuth();
  const [members, setMembers] = useState<SessionMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingRole, setPendingRole] = useState<number | null>(null);
  const [pendingRemove, setPendingRemove] = useState<number | null>(null);

  const load = useCallback(() => {
    if (!tokens) return;
    setLoading(true);
    loadMembers(tokens, sessionId)
      .then((list) => {
        setMembers(list);
        setError(null);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Не удалось загрузить список"))
      .finally(() => setLoading(false));
  }, [sessionId, tokens]);

  useEffect(() => {
    load();
  }, [load]);

  const handleRoleChange = async (member: SessionMember, nextRole: string) => {
    if (!tokens || member.role === nextRole) return;
    setPendingRole(member.userId);
    try {
      const updated = await updateMemberRole(tokens, sessionId, member.userId, nextRole);
      setMembers((prev) => prev.map((item) => (item.userId === updated.userId ? updated : item)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Не удалось обновить роль");
    } finally {
      setPendingRole(null);
    }
  };

  const handleRemove = async (member: SessionMember) => {
    if (!tokens) return;
    setPendingRemove(member.userId);
    try {
      await removeMember(tokens, sessionId, member.userId);
      setMembers((prev) => prev.filter((item) => item.userId !== member.userId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Не удалось удалить участника");
    } finally {
      setPendingRemove(null);
    }
  };

  const canManageMembers = canManage;

  const renderAvatar = (member: SessionMember) => {
    if (member.avatarUrl) {
      return (
        <img
          src={member.avatarUrl}
          alt={member.name}
          style={{ width: 36, height: 36, borderRadius: "50%" }}
        />
      );
    }
    const nice = displayName({ name: member.name, email: "" });
    return (
      <div
        style={{
          width: 36,
          height: 36,
          borderRadius: "50%",
          background: "#1f2937",
          color: "#fff",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          fontWeight: 600,
        }}
      >
        {nice.charAt(0).toUpperCase()}
      </div>
    );
  };

  return (
    <div className="card" style={{ gap: 8, display: "flex", flexDirection: "column" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h3 style={{ margin: 0 }}>Участники</h3>
        <span style={{ fontSize: 12, color: "#94a3b8" }}>{members.length}</span>
      </div>
      {error && (
        <div style={{ color: "tomato", fontSize: 13 }}>
          {error}
          <button type="button" onClick={load} className="btn btn-ghost" style={{ marginLeft: 8 }}>
            Повторить
          </button>
        </div>
      )}
      {loading ? (
        <p style={{ padding: "12px 0" }}>Загрузка списка...</p>
      ) : (
        <ul
          style={{
            listStyle: "none",
            padding: 0,
            margin: 0,
            display: "flex",
            flexDirection: "column",
            gap: 8,
          }}
        >
          {members.map((member) => {
            const isSelf = member.userId === currentUserId;
            const disableRoleChange = member.role === "owner" || !canManageMembers || isSelf;
            const showRemove = canManageMembers && member.role !== "owner" && !isSelf;
            return (
              <li
                key={member.userId}
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  gap: 12,
                  padding: "4px 0",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: 12, minWidth: 0 }}>
                  {renderAvatar(member)}
                  <div style={{ display: "flex", flexDirection: "column", minWidth: 0 }}>
                    <strong
                      style={{ whiteSpace: "nowrap", textOverflow: "ellipsis", overflow: "hidden" }}
                    >
                      {member.name.includes("@")
                        ? displayName({ name: member.name, email: member.name })
                        : member.name}
                    </strong>
                    <small style={{ color: "#94a3b8" }}>
                      {new Date(member.joinedAt).toLocaleDateString()}
                    </small>
                  </div>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  {disableRoleChange ? (
                    <span style={{ fontSize: 13, color: "#64748b" }}>
                      {roleLabels[member.role] ?? member.role}
                    </span>
                  ) : (
                    <select
                      value={member.role}
                      onChange={(event) => handleRoleChange(member, event.target.value)}
                      disabled={pendingRole === member.userId}
                    >
                      {roleOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  )}
                  {showRemove && (
                    <button
                      type="button"
                      onClick={() => handleRemove(member)}
                      disabled={pendingRemove === member.userId}
                      className="btn btn-ghost danger"
                    >
                      Удалить
                    </button>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};

export default MembersList;
