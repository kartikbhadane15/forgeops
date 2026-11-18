import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export function ProtectedRoute({ children, requiredRoles = [] }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRoles.length > 0) {
    const hasRequired = requiredRoles.some(r => user.roles?.includes(r));
    if (!hasRequired) {
      return (
        <div className="p-8 text-center">
          <p className="text-rose-400 font-semibold">Access Denied</p>
          <p className="text-sm text-slate-400 mt-1">
            Your role ({user.roles?.map(r => r.replace('ROLE_', '')).join(', ')}) does not have permission to view this page.
          </p>
        </div>
      );
    }
  }

  return children;
}
