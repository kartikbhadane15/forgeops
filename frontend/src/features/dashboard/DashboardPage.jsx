import React, { useEffect, useState, useCallback } from 'react';
import { Activity, Server, AlertTriangle, CheckSquare, RefreshCw, TrendingUp, TrendingDown } from 'lucide-react';
import { dashboardApi } from '../../api/endpoints';
import StatusBadge from '../../components/common/StatusBadge';

function MetricCard({ label, value, sub, icon: Icon, iconColor = 'text-cyan-400', accent = '' }) {
  return (
    <div className={`p-5 rounded-xl border bg-slate-900/60 ${accent || 'border-slate-800'}`}>
      <div className="flex items-center justify-between text-xs font-medium text-slate-400 mb-3">
        <span>{label}</span>
        <Icon className={`w-4 h-4 ${iconColor}`} />
      </div>
      <div className="text-2xl font-bold text-slate-100">{value}</div>
      {sub && <div className="mt-1 text-xs text-slate-500">{sub}</div>}
    </div>
  );
}

function ServiceHealthRow({ svc }) {
  const isAlerting = svc.errorRatePct > 2 || svc.latencyP95Ms > 500;
  return (
    <tr className="border-t border-slate-800 hover:bg-slate-800/30 transition-colors">
      <td className="py-3 px-4">
        <div className="font-medium text-slate-200 text-sm">{svc.name}</div>
        <div className="text-xs font-mono text-slate-500">{svc.serviceKey}</div>
      </td>
      <td className="py-3 px-4">
        <StatusBadge status={svc.productionHealth} />
      </td>
      <td className="py-3 px-4">
        <span className={`text-sm font-mono font-semibold ${svc.latencyP95Ms > 500 ? 'text-amber-400' : 'text-slate-300'}`}>
          {svc.latencyP95Ms.toFixed(0)} ms
        </span>
      </td>
      <td className="py-3 px-4">
        <span className={`text-sm font-mono font-semibold ${svc.errorRatePct > 2 ? 'text-rose-400' : 'text-slate-300'}`}>
          {svc.errorRatePct.toFixed(2)}%
        </span>
      </td>
      <td className="py-3 px-4">
        <span className={`text-xs font-semibold px-2 py-0.5 rounded border ${
          svc.criticality === 'CRITICAL' ? 'bg-rose-950/60 text-rose-300 border-rose-700/50' :
          svc.criticality === 'HIGH' ? 'bg-orange-950/60 text-orange-300 border-orange-700/50' :
          'bg-slate-800 text-slate-400 border-slate-700'
        }`}>{svc.criticality}</span>
      </td>
      <td className="py-3 px-4 text-xs text-slate-400">{svc.owningTeam}</td>
    </tr>
  );
}

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [health, setHealth] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [sumRes, healthRes] = await Promise.all([
        dashboardApi.summary(),
        dashboardApi.serviceHealth(),
      ]);
      setSummary(sumRes.data);
      setHealth(healthRes.data);
    } catch (err) {
      setError(err?.message || 'Could not connect to backend. Start the Spring Boot server.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const degraded = health.filter(s => s.productionHealth !== 'HEALTHY').length;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-100">Operations Overview</h1>
          <p className="text-sm text-slate-400 mt-0.5">Infrastructure health, incidents, and pending approvals.</p>
        </div>
        <button
          onClick={fetchData}
          disabled={loading}
          className="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-slate-700 bg-slate-800 text-slate-200 text-xs font-medium hover:bg-slate-700 transition"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Simulation notice */}
      <div className="rounded-lg border border-cyan-800/40 bg-cyan-950/20 p-3 text-xs text-cyan-300 flex items-start gap-3">
        <Activity className="w-4 h-4 flex-shrink-0 text-cyan-400 mt-0.5" />
        <span>
          <span className="font-semibold text-cyan-200">Portfolio Simulation: </span>
          All metrics and telemetry are synthetic samples generated from local PostgreSQL seed data. No real infrastructure is connected.
        </span>
      </div>

      {error && (
        <div className="p-4 rounded-lg border border-amber-700/40 bg-amber-950/20 text-amber-300 text-sm">
          ⚠ {error}
        </div>
      )}

      {/* Summary metric cards */}
      {summary && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <MetricCard
            label="Total Services"
            value={summary.totalServices}
            sub="Registered in catalog"
            icon={Server}
            iconColor="text-indigo-400"
          />
          <MetricCard
            label="Healthy Services"
            value={summary.healthyServices}
            sub={`${summary.degradedServices} degraded · ${summary.unavailableServices} unavailable`}
            icon={TrendingUp}
            iconColor="text-emerald-400"
            accent={summary.degradedServices > 0 ? 'border-amber-700/40' : 'border-slate-800'}
          />
          <MetricCard
            label="Open Incidents"
            value={summary.openIncidents}
            sub="Phase 4 will populate live data"
            icon={AlertTriangle}
            iconColor="text-amber-400"
          />
          <MetricCard
            label="Pending Approvals"
            value={summary.pendingApprovalCount}
            sub="Dual-control workflow"
            icon={CheckSquare}
            iconColor="text-cyan-400"
          />
        </div>
      )}

      {/* Service health table */}
      <div className="rounded-xl border border-slate-800 bg-slate-900/50 overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-800 flex items-center justify-between">
          <h2 className="text-sm font-semibold text-slate-200">Service Health</h2>
          {degraded > 0 && (
            <span className="text-xs text-amber-400 flex items-center gap-1">
              <TrendingDown className="w-3.5 h-3.5" />
              {degraded} service{degraded > 1 ? 's' : ''} need attention
            </span>
          )}
        </div>
        {loading ? (
          <div className="p-8 text-center text-slate-500 text-sm animate-pulse">Loading service health data…</div>
        ) : health.length === 0 ? (
          <div className="p-8 text-center text-slate-500 text-sm">
            No services found. Start the backend and ensure PostgreSQL is running.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  <th className="py-3 px-4 text-left">Service</th>
                  <th className="py-3 px-4 text-left">Health</th>
                  <th className="py-3 px-4 text-left">P95 Latency</th>
                  <th className="py-3 px-4 text-left">Error Rate</th>
                  <th className="py-3 px-4 text-left">Criticality</th>
                  <th className="py-3 px-4 text-left">Team</th>
                </tr>
              </thead>
              <tbody>
                {health.map(svc => <ServiceHealthRow key={svc.serviceId} svc={svc} />)}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
