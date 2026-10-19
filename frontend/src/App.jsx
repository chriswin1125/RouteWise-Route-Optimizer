import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { AppLayout } from './components/layout/AppLayout';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { RoutePlannerPage } from './pages/RoutePlannerPage';
import { RouteComparisonPage } from './pages/RouteComparisonPage';
import { DeliveryManagementPage } from './pages/DeliveryManagementPage';
import { RouteHistoryPage } from './pages/RouteHistoryPage';
import { AlgorithmVisualizerPage } from './pages/AlgorithmVisualizerPage';
import { SettingsPage } from './pages/SettingsPage';
import { LoadingSpinner } from './components/common/LoadingSpinner';

// Route guard requiring authentication
const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="h-screen w-screen flex items-center justify-center bg-slate-50">
        <LoadingSpinner text="Authenticating RouteWise..." />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
};

// Route guard redirecting authenticated users away from login/register
const PublicOnlyRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="h-screen w-screen flex items-center justify-center bg-slate-50">
        <LoadingSpinner text="Checking session..." />
      </div>
    );
  }

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Landing & Auth Pages */}
          <Route path="/" element={<LandingPage />} />
          <Route
            path="/login"
            element={
              <PublicOnlyRoute>
                <LoginPage />
              </PublicOnlyRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicOnlyRoute>
                <RegisterPage />
              </PublicOnlyRoute>
            }
          />

          {/* Protected Application Routes */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/planner" element={<RoutePlannerPage />} />
            <Route path="/compare" element={<RouteComparisonPage />} />
            <Route path="/deliveries" element={<DeliveryManagementPage />} />
            <Route path="/history" element={<RouteHistoryPage />} />
            <Route path="/visualizer" element={<AlgorithmVisualizerPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>

          {/* Catch-all redirect */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
