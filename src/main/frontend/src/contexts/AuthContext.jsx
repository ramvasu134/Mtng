import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { auth as authApi, students as studentsApi } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);       // { username, role, displayName }
  const [loading, setLoading] = useState(true);

  // Check session on mount
  useEffect(() => {
    authApi.me()
      .then(data => setUser(data))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  // Heartbeat: keep online status fresh every 8 seconds
  useEffect(() => {
    if (!user) return;
    // Send heartbeat immediately on login
    studentsApi.heartbeat().catch(() => {});
    // Send another heartbeat after 2 seconds to ensure it's registered
    const initial = setTimeout(() => studentsApi.heartbeat().catch(() => {}), 2000);
    const interval = setInterval(() => {
      studentsApi.heartbeat().catch(() => {});
    }, 8000);
    return () => { clearTimeout(initial); clearInterval(interval); };
  }, [user]);

  const login = useCallback(async (username, password) => {
    const data = await authApi.login(username, password);
    setUser(data);
    return data;
  }, []);

  const logout = useCallback(async () => {
    try { await authApi.logout(); } catch (e) { /* ignore */ }
    setUser(null);
  }, []);

  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
