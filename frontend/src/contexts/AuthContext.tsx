import React, { createContext, ReactNode, useContext, useEffect, useState } from 'react';
import client from '../api/client';

interface AuthContextType {
  isAuthenticated: Boolean;
  name: string | null,
  login: (token: string, name: string) => void;
  logout: () => void;
  deleteAccount: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [name, setname] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token')
    const storedName = localStorage.getItem('name')
    if (token) {
      setIsAuthenticated(true)
      setname(storedName)
    }
  })
  
  const login = (token: string, name: string) => {
    localStorage.setItem('token', token)
    localStorage.setItem('name', name)
    if (token) {
      setIsAuthenticated(true);
      setname(name);
    }
  }

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('name')
    setIsAuthenticated(false)
    setname(null)
  }

  const deleteAccount = async () => {
    await client.delete('/api/auth/account')
    localStorage.removeItem('token')
    localStorage.removeItem('name')
    setIsAuthenticated(false)
    setname(null)
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, name, login, logout, deleteAccount }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("contextが存在しないです")
  }
  return context
}