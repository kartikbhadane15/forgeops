import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Server, 
  AlertTriangle, 
  CheckSquare, 
  ScrollText, 
  Users, 
  Cpu 
} from 'lucide-react';

export default function Sidebar({ userRoles = [] }) {
  const isAdmin = userRoles.includes('ROLE_ADMIN');
  const isApproverOrAdmin = userRoles.includes('ROLE_APPROVER') || isAdmin;

  const navItems = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/services', label: 'Services Inventory', icon: Server },
    { to: '/incidents', label: 'Incidents', icon: AlertTriangle },
    { to: '/approvals', label: 'Approvals Inbox', icon: CheckSquare },
    ...(isApproverOrAdmin ? [{ to: '/audit', label: 'Audit Trail', icon: ScrollText }] : []),
    ...(isAdmin ? [{ to: '/admin', label: 'User Admin', icon: Users }] : []),
  ];

  return (
    <aside className="w-64 border-r border-slate-800 bg-slate-900/40 p-4 flex flex-col justify-between hidden md:flex min-h-[calc(100vh-4rem)]">
      <nav className="space-y-1">
        <div className="px-3 py-2 text-xs font-semibold uppercase tracking-wider text-slate-500">
          Operations
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`
              }
            >
              <Icon className="w-4 h-4" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="p-3 rounded-lg border border-slate-800 bg-slate-900/80 text-xs text-slate-400 space-y-1">
        <div className="flex items-center space-x-1.5 text-cyan-400 font-medium">
          <Cpu className="w-3.5 h-3.5" />
          <span>Local Rule Engine</span>
        </div>
        <p className="text-[11px] leading-relaxed text-slate-500">
          Deterministic local heuristics active. No external AI APIs or data transmission.
        </p>
      </div>
    </aside>
  );
}
