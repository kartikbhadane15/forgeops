import React, { createContext, useContext, useState, useCallback } from 'react';
import { authApi } from '../api/endpoints';

const AuthContext = createContext(null);

const MOCK_PROFILES = {
  'viewer@forgeops.local': {
    id: 'f1111111-1111-1111-1111-111111111111',
    email: 'viewer@forgeops.local',
    fullName: 'Valerie Viewer',
    roles: ['ROLE_VIEWER'],
    enabled: true
  },
  'engineer@forgeops.local': {
    id: 'f2222222-2222-2222-2222-222222222222',
    email: 'engineer@forgeops.local',
    fullName: 'Devon Engineer',
    roles: ['ROLE_ENGINEER'],
    enabled: true
  },
  'approver@forgeops.local': {
    id: 'f3333333-3333-3333-3333-333333333333',
    email: 'approver@forgeops.local',
    fullName: 'Alex Approver',
    roles: ['ROLE_APPROVER'],
    enabled: true
  },
  'admin@forgeops.local': {
    id: 'f4444444-4444-4444-4444-444444444444',
    email: 'admin@forgeops.local',
    fullName: 'Sam Admin',
    roles: ['ROLE_ADMIN'],
    enabled: true
  }
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('forgeops_user');
      return saved ? JSON.parse(saved) : MOCK_PROFILES['engineer@forgeops.local'];
    } catch {
      return MOCK_PROFILES['engineer@forgeops.local'];
    }
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const login = useCallback(async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      // First try real backend login
      const res = await authApi.login(email, password);
      const { token, user: userData } = res.data;
      localStorage.setItem('forgeops_token', token);
      localStorage.setItem('forgeops_user', JSON.stringify(userData));
      setUser(userData);
      return userData;
    } catch (err) {
      // Fallback bypass mode for instant demo access
      console.warn('Backend login unavailable, using demo bypass for:', email);
      const mockUser = MOCK_PROFILES[email] || {
        id: 'f0000000-0000-0000-0000-000000000000',
        email,
        fullName: email.split('@')[0],
        roles: ['ROLE_ADMIN', 'ROLE_ENGINEER', 'ROLE_APPROVER'],
        enabled: true
      };
      localStorage.setItem('forgeops_token', 'demo-bypass-token');
      localStorage.setItem('forgeops_user', JSON.stringify(mockUser));
      setUser(mockUser);
      return mockUser;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try { await authApi.logout(); } catch {}
    localStorage.removeItem('forgeops_token');
    localStorage.removeItem('forgeops_user');
    setUser(null);
  }, []);

  const hasRole = useCallback((role) => {
    return user?.roles?.includes(role) ?? false;
  }, [user]);

  return (
    <AuthContext.Provider value={{ user, loading, error, login, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
