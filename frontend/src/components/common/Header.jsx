import React from 'react';
import { Shield, Bell, User, LogOut } from 'lucide-react';

export default function Header({ user, onLogout }) {
  return (
    <header className="h-16 border-b border-slate-800 bg-slate-900/60 backdrop-blur px-6 flex items-center justify-between sticky top-0 z-20">
      <div className="flex items-center space-x-3">
        <div className="flex items-center justify-center w-9 h-9 rounded-lg bg-cyan-600/20 text-cyan-400 border border-cyan-500/30 font-bold">
          <Shield className="w-5 h-5" />
        </div>
        <div>
          <span className="font-semibold text-slate-100 tracking-tight text-base">ForgeOps</span>
          <span className="ml-2 text-xs font-medium px-2 py-0.5 rounded bg-cyan-950 text-cyan-400 border border-cyan-800/50">
            Enterprise Ops
          </span>
        </div>
      </div>

      <div className="flex items-center space-x-4">
        {user ? (
          <div className="flex items-center space-x-3">
            <div className="text-right hidden sm:block">
              <div className="text-sm font-medium text-slate-200">{user.fullName || user.email}</div>
              <div className="text-xs text-cyan-400 font-mono">
                {user.roles?.map(r => r.replace('ROLE_', '')).join(', ') || 'VIEWER'}
              </div>
            </div>
            <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-300">
              <User className="w-4 h-4" />
            </div>
            <button
              onClick={onLogout}
              title="Logout"
              className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800 transition-colors"
              aria-label="Log out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <div className="text-xs text-slate-400">Demo Mode</div>
        )}
      </div>
    </header>
  );
}
