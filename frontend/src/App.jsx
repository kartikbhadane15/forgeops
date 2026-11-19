import React, { Component } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Layout from './components/common/Layout';
import LoginPage from './features/auth/LoginPage';
import { ProtectedRoute } from './components/common/ProtectedRoute';
import DashboardPage from './features/dashboard/DashboardPage';
import ServicesPage from './features/services/ServicesPage';
import IncidentsPage from './features/incidents/IncidentsPage';
import IncidentDetailPage from './features/incidents/IncidentDetailPage';
import ApprovalsPage from './features/approvals/ApprovalsPage';
import AuditPage from './features/audit/AuditPage';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('App render error caught by ErrorBoundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-slate-950 flex items-center justify-center p-6 text-slate-100">
          <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-xl p-6 text-center space-y-4 shadow-xl">
            <h2 className="text-xl font-bold text-amber-400">Something went wrong</h2>
            <p className="text-sm text-slate-400">
              {this.state.error?.message || 'An error occurred while loading this view.'}
            </p>
            <button
              onClick={() => {
                this.setState({ hasError: false });
                window.location.reload();
              }}
              className="px-4 py-2 bg-cyan-600 hover:bg-cyan-500 text-white rounded-lg text-sm font-semibold transition"
            >
              Reload ForgeOps
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}

function PlaceholderPage({ title }) {
  return (
    <div className="p-6 rounded-xl border border-slate-800 bg-slate-900/40 text-slate-400">
      <p className="text-sm">{title} — coming in upcoming phase</p>
    </div>
  );
}

function AppRoutes() {
  const { user, logout } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout user={user} onLogout={logout} />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="services" element={<ServicesPage />} />
        <Route path="incidents" element={<IncidentsPage />} />
        <Route path="incidents/:id" element={<IncidentDetailPage />} />
        <Route path="approvals" element={<ApprovalsPage />} />
        <Route
          path="audit"
          element={
            <ProtectedRoute requiredRoles={['ROLE_APPROVER', 'ROLE_ADMIN']}>
              <AuditPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="admin"
          element={
            <ProtectedRoute requiredRoles={['ROLE_ADMIN']}>
              <PlaceholderPage title="User Administration (Phase 2 — backend done)" />
            </ProtectedRoute>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  );
}
