import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/common/Layout';
import DashboardPage from './features/dashboard/DashboardPage';

export default function App() {
  const [currentUser, setCurrentUser] = useState(() => {
    const saved = localStorage.getItem('forgeops_user');
    return saved ? JSON.parse(saved) : {
      email: 'engineer@forgeops.local',
      fullName: 'Devon Engineer',
      roles: ['ROLE_ENGINEER']
    };
  });

  const handleLogout = () => {
    localStorage.removeItem('forgeops_token');
    localStorage.removeItem('forgeops_user');
    setCurrentUser(null);
  };

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout user={currentUser} onLogout={handleLogout} />}>
          <Route index element={<DashboardPage />} />
          <Route path="services" element={<div className="text-slate-400">Services catalog coming in Phase 3</div>} />
          <Route path="incidents" element={<div className="text-slate-400">Incidents management coming in Phase 4</div>} />
          <Route path="approvals" element={<div className="text-slate-400">Approvals inbox coming in Phase 6</div>} />
          <Route path="audit" element={<div className="text-slate-400">Audit trail coming in Phase 7</div>} />
          <Route path="admin" element={<div className="text-slate-400">User admin coming in Phase 2</div>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
