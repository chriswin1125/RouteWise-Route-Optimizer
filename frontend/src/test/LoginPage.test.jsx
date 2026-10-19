import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { LoginPage } from '../pages/LoginPage';
import { AuthProvider } from '../context/AuthContext';

// Mock API login
vi.mock('../services/api', () => ({
  api: {
    login: vi.fn().mockResolvedValue({
      token: 'mock-jwt-token',
      user: { id: 1, email: 'demo@routewise.com', fullName: 'Sarah Jenkins' },
    }),
    getCurrentUser: vi.fn().mockResolvedValue({ id: 1, email: 'demo@routewise.com' }),
  },
}));

describe('LoginPage Component', () => {
  it('renders login form with work email and password inputs', () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      </AuthProvider>
    );

    expect(screen.getByPlaceholderText('name@company.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument();
    expect(screen.getByText('Sign In')).toBeInTheDocument();
  });

  it('populates demo credentials when Demo Fill button is clicked', () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      </AuthProvider>
    );

    const demoBtn = screen.getByText('Demo Fill');
    expect(demoBtn).toBeInTheDocument();

    fireEvent.click(demoBtn);

    const emailInput = screen.getByPlaceholderText('name@company.com');
    expect(emailInput.value).toBe('demo@routewise.com');
  });
});
