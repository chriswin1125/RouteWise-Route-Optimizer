import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('routewise_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      if (token) {
        try {
          const userData = await api.getCurrentUser();
          setUser(userData);
        } catch (err) {
          console.error('Session expired or invalid:', err);
          logout();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, [token]);

  const login = async (email, password) => {
    const response = await api.login({ email, password });
    localStorage.setItem('routewise_token', response.token);
    setToken(response.token);
    setUser(response.user);
    return response.user;
  };

  const register = async (email, password, fullName, companyName) => {
    const response = await api.register({ email, password, fullName, companyName });
    localStorage.setItem('routewise_token', response.token);
    setToken(response.token);
    setUser(response.user);
    return response.user;
  };

  const logout = () => {
    localStorage.removeItem('routewise_token');
    setToken(null);
    setUser(null);
  };

  const refreshUser = async () => {
    try {
      const userData = await api.getCurrentUser();
      setUser(userData);
    } catch (err) {
      console.error('Failed to refresh user:', err);
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
