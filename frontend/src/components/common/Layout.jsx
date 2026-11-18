import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';

export default function Layout({ user, onLogout }) {
  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      <Header user={user} onLogout={onLogout} />
      <div className="flex-1 flex">
        <Sidebar userRoles={user?.roles || []} />
        <main className="flex-1 p-6 md:p-8 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
