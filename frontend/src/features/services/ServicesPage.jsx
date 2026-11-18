import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Search, Server, RefreshCw } from 'lucide-react';
import { servicesApi } from '../../api/endpoints';
import StatusBadge from '../../components/common/StatusBadge';
import { useAuth } from '../../context/AuthContext';

const CRITICALITY_COLOR = {
  CRITICAL: 'bg-rose-950/60 text-rose-300 border-rose-700/50',
  HIGH: 'bg-orange-950/60 text-orange-300 border-orange-700/50',
  MEDIUM: 'bg-amber-950/60 text-amber-300 border-amber-700/50',
  LOW: 'bg-slate-800 text-slate-400 border-slate-700',
};

export default function ServicesPage() {
  const { hasRole } = useAuth();
  const canEdit = hasRole('ROLE_ENGINEER') || hasRole('ROLE_ADMIN');

  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');

  const fetchServices = useCallback(async (q = '') => {
    setLoading(true);
    setError('');
    try {
      const res = await servicesApi.list({ search: q || undefined, size: 50 });
      setServices(res.data || []);
    } catch (err) {
      setError(err?.message || 'Could not load services. Ensure the backend is running.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchServices(); }, [fetchServices]);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchServices(search);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-100">Services Inventory</h1>
          <p className="text-sm text-slate-400 mt-0.5">Registered services, environments, and health status.</p>
        </div>
        {canEdit && (
          <button className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold transition">
            <Plus className="w-3.5 h-3.5" />
            Register Service
          </button>
        )}
      </div>

      {/* Search bar */}
      <form onSubmit={handleSearch} className="flex gap-2">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="search"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search by name or service key…"
            className="w-full bg-slate-800 border border-slate-700 rounded-lg pl-9 pr-4 py-2 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/40"
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2 rounded-lg border border-slate-700 bg-slate-800 text-slate-200 text-xs font-medium hover:bg-slate-700 transition flex items-center gap-1.5"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Search
        </button>
      </form>

      {error && (
        <div className="p-4 rounded-lg border border-amber-700/40 bg-amber-950/20 text-amber-300 text-sm">
          ⚠ {error}
        </div>
      )}

      {/* Services table */}
      <div className="rounded-xl border border-slate-800 bg-slate-900/50 overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-800">
          <span className="text-sm font-semibold text-slate-200">
            {loading ? 'Loading…' : `${services.length} service${services.length !== 1 ? 's' : ''}`}
          </span>
        </div>

        {loading ? (
          <div className="p-10 text-center text-slate-500 animate-pulse text-sm">Fetching services from backend…</div>
        ) : services.length === 0 ? (
          <div className="p-10 text-center space-y-2">
            <Server className="w-10 h-10 mx-auto text-slate-700" />
            <p className="text-slate-400 text-sm">No services found. {search ? 'Try a different search.' : 'Start the backend to seed data.'}</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-xs font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-800">
                  <th className="py-3 px-4 text-left">Service</th>
                  <th className="py-3 px-4 text-left">Criticality</th>
                  <th className="py-3 px-4 text-left">Status</th>
                  <th className="py-3 px-4 text-left">Team</th>
                </tr>
              </thead>
              <tbody>
                {services.map(svc => (
                  <tr key={svc.id} className="border-t border-slate-800 hover:bg-slate-800/30 transition-colors cursor-pointer">
                    <td className="py-3 px-4">
                      <div className="font-medium text-slate-200">{svc.name}</div>
                      <div className="text-xs font-mono text-slate-500">{svc.serviceKey}</div>
                    </td>
                    <td className="py-3 px-4">
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded border ${CRITICALITY_COLOR[svc.criticality] || ''}`}>
                        {svc.criticality}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <StatusBadge status={svc.lifecycleStatus} />
                    </td>
                    <td className="py-3 px-4 text-slate-400 text-xs">{svc.owningTeam}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
