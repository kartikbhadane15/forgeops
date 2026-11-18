import React from 'react';

const statusStyles = {
  // Service health
  HEALTHY: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  DEGRADED: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
  UNAVAILABLE: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
  UNKNOWN: 'bg-slate-500/10 text-slate-400 border-slate-500/20',

  // Incident severity
  SEV1: 'bg-rose-950/80 text-rose-300 border-rose-600 font-bold',
  SEV2: 'bg-orange-950/80 text-orange-300 border-orange-500 font-semibold',
  SEV3: 'bg-amber-950/80 text-amber-300 border-amber-500',
  SEV4: 'bg-blue-950/80 text-blue-300 border-blue-500',

  // Incident status
  OPEN: 'bg-rose-500/15 text-rose-300 border-rose-500/30',
  INVESTIGATING: 'bg-amber-500/15 text-amber-300 border-amber-500/30',
  MITIGATED: 'bg-cyan-500/15 text-cyan-300 border-cyan-500/30',
  RESOLVED: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',

  // Approval status
  PENDING_APPROVAL: 'bg-amber-500/15 text-amber-300 border-amber-500/30 animate-pulse',
  APPROVED: 'bg-blue-500/15 text-blue-300 border-blue-500/30',
  REJECTED: 'bg-slate-500/20 text-slate-400 border-slate-600',
  EXECUTED: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',
  FAILED: 'bg-rose-500/20 text-rose-400 border-rose-500/40',
};

export default function StatusBadge({ status, className = '' }) {
  const style = statusStyles[status] || 'bg-slate-800 text-slate-300 border-slate-700';

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${style} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-75" />
      {status?.replace('_', ' ')}
    </span>
  );
}
