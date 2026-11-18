import React from 'react';
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
              <PlaceholderPage title="Audit Trail (Phase 7)" />
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
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
