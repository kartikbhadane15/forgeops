import React, { useEffect, useState } from 'react';
import { Activity, Server, AlertTriangle, CheckSquare, RefreshCw } from 'lucide-react';
import api from '../../api/client';
import StatusBadge from '../../components/common/StatusBadge';

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [healthStatus, setHealthStatus] = useState('CHECKING');
  const [loading, setLoading] = useState(true);

  const fetchHealth = async () => {
    setLoading(true);
    try {
      // Actuator health check
      const res = await fetch('/actuator/health');
      if (res.ok) {
        setHealthStatus('ONLINE');
      } else {
        setHealthStatus('DEGRADED');
      }
    } catch (err) {
      setHealthStatus('OFFLINE (WAITING FOR BACKEND)');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-100">Operations Overview</h1>
          <p className="text-sm text-slate-400 mt-1">
            Real-time infrastructure health, incidents, and pending approvals.
          </p>
        </div>
        <button
          onClick={fetchHealth}
          disabled={loading}
          className="flex items-center space-x-2 px-3 py-1.5 rounded-lg border border-slate-700 bg-slate-800 text-slate-200 text-xs font-medium hover:bg-slate-700 transition"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Synthetic Metric Notice Banner */}
      <div className="rounded-lg border border-cyan-800/40 bg-cyan-950/20 p-4 text-xs text-cyan-300 flex items-start space-x-3">
        <Activity className="w-5 h-5 flex-shrink-0 text-cyan-400 mt-0.5" />
        <div>
          <span className="font-semibold text-cyan-200">Portfolio Simulation Notice:</span>
          <p className="mt-0.5 text-cyan-300/80">
            Metrics, telemetry, and actions in ForgeOps are generated from simulated models and synthetic PostgreSQL samples. 
            No connection to real production infrastructure is made.
          </p>
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>Backend API Status</span>
            <Server className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="mt-3 text-lg font-bold">
            <StatusBadge status={healthStatus === 'ONLINE' ? 'HEALTHY' : 'DEGRADED'} />
          </div>
          <div className="mt-1 text-xs text-slate-500 font-mono">Actuator: /actuator/health</div>
        </div>

        <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>Services Monitored</span>
            <Server className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="mt-3 text-2xl font-bold text-slate-100">5</div>
          <div className="mt-1 text-xs text-slate-500">Seed catalog configured</div>
        </div>

        <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>Open Incidents</span>
            <AlertTriangle className="w-4 h-4 text-amber-400" />
          </div>
          <div className="mt-3 text-2xl font-bold text-amber-400">1</div>
          <div className="mt-1 text-xs text-slate-500">1 SEV2 Elevated Latency</div>
        </div>

        <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>Pending Approvals</span>
            <CheckSquare className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="mt-3 text-2xl font-bold text-cyan-400">0</div>
          <div className="mt-1 text-xs text-slate-500">Dual-control workflow</div>
        </div>
      </div>
    </div>
  );
}
