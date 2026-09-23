import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { exchangeOAuth, getOAuthUrl, loadProfile, updateProfile } from "../services/api";
import type { AuthTokens, UserProfile } from "../types";

interface AuthContextValue {
  user: UserProfile | null;
  tokens: AuthTokens | null;
  loading: boolean;
  startOAuth: (provider: "github" | "google") => Promise<void>;
  finishOAuth: (
    provider: "github" | "google",
    code: string,
    state: string,
    redirectUri: string,
  ) => Promise<void>;
  patchProfile: (
    payload: Partial<{ name: string; avatarUrl: string }>,
  ) => Promise<UserProfile | null>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const storageKey = "codetogether-auth";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [tokens, setTokens] = useState<AuthTokens | null>(() => {
    const raw = localStorage.getItem(storageKey);
    return raw ? (JSON.parse(raw) as AuthTokens) : null;
  });
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState<boolean>(!!tokens);

  useEffect(() => {
    if (!tokens) {
      setUser(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    loadProfile(tokens)
      .then(setUser)
      .finally(() => setLoading(false));
  }, [tokens]);

  const startOAuth = useCallback(async (provider: "github" | "google") => {
    const { url } = await getOAuthUrl(provider);
    window.location.href = url;
  }, []);

  const finishOAuth = useCallback(
    async (provider: "github" | "google", code: string, state: string, redirectUri: string) => {
      const response = await exchangeOAuth(provider, { code, state, redirectUri });
      setTokens(response.tokens);
      localStorage.setItem(storageKey, JSON.stringify(response.tokens));
      setUser(response.user);
    },
    [],
  );

  const logout = useCallback(() => {
    setTokens(null);
    setUser(null);
    localStorage.removeItem(storageKey);
  }, []);

  const patchProfile = useCallback(
    async (payload: Partial<{ name: string; avatarUrl: string }>) => {
      if (!tokens) return null;
      const updated = await updateProfile(tokens, payload);
      setUser(updated);
      return updated;
    },
    [tokens],
  );

  const value = useMemo<AuthContextValue>(
    () => ({ user, tokens, loading, startOAuth, finishOAuth, patchProfile, logout }),
    [user, tokens, loading, startOAuth, finishOAuth, patchProfile, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("AuthContext is not available");
  }
  return ctx;
};
