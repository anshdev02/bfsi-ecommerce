import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const u = localStorage.getItem('user');
    return u ? JSON.parse(u) : null;
  });

  const login = (userData, token, refreshToken) => {
    localStorage.clear();
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    window.location.href = '/dashboard';
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
    window.location.href = '/login';
  };

  const isAdmin = () => user?.roles?.includes('ROLE_ADMIN');
  const isBanker = () => user?.roles?.includes('ROLE_BANKER');

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin, isBanker }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
