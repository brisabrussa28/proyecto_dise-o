"use client";
import React, { createContext, useContext, useEffect, useState } from 'react';

export type Role = 'Visitante' | 'Contribuyente' | 'Administrador';

interface SessionContextProps {
  role: Role;
  isAuthenticated: boolean;
  token: string | null;
  login: (role?: Exclude<Role, 'Visitante'>, token?: string) => void; // default: Contribuyente
  logout: () => void;
}

const SessionContext = createContext<SessionContextProps | undefined>(undefined);

export function SessionProvider({ children }: { children: React.ReactNode }) {
  const [role, setRoleState] = useState<Role>('Visitante');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    try {
      const savedRole = localStorage.getItem('metamapa-role');
      const savedAuth = localStorage.getItem('metamapa-auth');
      const savedToken = localStorage.getItem('metamapa-token');
      if (savedRole === 'Visitante' || savedRole === 'Contribuyente' || savedRole === 'Administrador') {
        setRoleState(savedRole);
      }
      if (savedAuth === 'true') setIsAuthenticated(true);
      if(savedToken) setToken(savedToken);
    } catch {}
  }, []);

  const login = (withRole: Exclude<Role, 'Visitante'> = 'Contribuyente', newToken?: string) => {
    setRoleState(withRole);
    setIsAuthenticated(true);
    if(newToken) setToken(newToken);
    try {
      localStorage.setItem('metamapa-role', withRole);
      localStorage.setItem('metamapa-auth', 'true');
      if(newToken) localStorage.setItem('metamapa-token', newToken);
    } catch {}
  };

  const logout = () => {
    setRoleState('Visitante');
    setIsAuthenticated(false);
    setToken(null);
    try {
      localStorage.setItem('metamapa-role', 'Visitante');
      localStorage.setItem('metamapa-auth', 'false');
      localStorage.removeItem('metamapa-token');
    } catch {}
  };

  return (
    <SessionContext.Provider value={{ role, isAuthenticated, token, login, logout }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error('useSession debe usarse dentro de SessionProvider');
  return ctx;
}
