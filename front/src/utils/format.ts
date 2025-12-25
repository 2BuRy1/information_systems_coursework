import type { UserProfile } from "../types";

function titleCase(word: string) {
  if (!word) return word;
  return word.charAt(0).toUpperCase() + word.slice(1);
}

function humanizeHandle(handle: string) {
  const cleaned = handle
    .trim()
    .replace(/[_\-]+/g, " ")
    .replace(/\.+/g, " ")
    .replace(/\s+/g, " ");
  return cleaned
    .split(" ")
    .filter(Boolean)
    .map((part) => titleCase(part.toLowerCase()))
    .join(" ");
}

export function displayName(user: Pick<UserProfile, "name" | "email"> | null | undefined) {
  if (!user) return "Пользователь";
  const name = (user.name ?? "").trim();
  const email = (user.email ?? "").trim();
  const looksLikeEmail = name.includes("@") || name === email;
  if (name && !looksLikeEmail) {
    return name;
  }
  if (email) {
    const handle = email.split("@")[0] ?? email;
    return humanizeHandle(handle) || email;
  }
  return "Пользователь";
}

export function displayInitials(name: string) {
  const trimmed = name.trim();
  if (!trimmed) return "?";
  const parts = trimmed.split(/\s+/).filter(Boolean);
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
}
