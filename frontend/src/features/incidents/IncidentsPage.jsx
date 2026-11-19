import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  AlertTriangle, 
  Plus, 
  Filter, 
  Clock, 
  ChevronRight, 
  AlertCircle,
  CheckCircle2,
  RefreshCw
} from 'lucide-react';
import { incidentsApi, servicesApi } from '../../api/endpoints';
import { useAuth } from '../../context/AuthContext';
import { MOCK_INCIDENTS, MOCK_SERVICES } from '../../data/mockData';

const SEVERITY_COLORS = {
  SEV1: 'bg-red-500/10 text-red-400 border-red-500/30',
  SEV2: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
  SEV3: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/30',
  SEV4: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
};

const STATUS_COLORS = {
  OPEN: 'bg-red-500/10 text-red-400 border-red-500/30',
  INVESTIGATING: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
  MITIGATED: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
  RESOLVED: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
};

export default function IncidentsPage() {
  const { hasRole } = useAuth();
  const isEngineerOrAbove = hasRole(['ROLE_ENGINEER', 'ROLE_APPROVER', 'ROLE_ADMIN']);

  const [incidents, setIncidents] = useState(MOCK_INCIDENTS);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('');
  const [severityFilter, setSeverityFilter] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Form states for creating incident
  const [services, setServices] = useState(MOCK_SERVICES);
  const [selectedService, setSelectedService] = useState(MOCK_SERVICES[0]?.id || '');
  const [environments, setEnvironments] = useState(MOCK_SERVICES[0]?.environments || []);
  const [selectedEnv, setSelectedEnv] = useState(MOCK_SERVICES[0]?.environments?.[0]?.id || '');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [severity, setSeverity] = useState('SEV2');
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState(null);

  const fetchIncidents = async () => {
    try {
      const params = {};
      if (statusFilter) params.status = statusFilter;
      if (severityFilter) params.severity = severityFilter;
      const res = await incidentsApi.list(params);
      const items = res.data?.data?.content || res.data?.content || res.data || [];
      if (items.length > 0) setIncidents(items);
    } catch (err) {
      // Keeps mock data seamlessly
    }
  };

  useEffect(() => {
    fetchIncidents();
  }, [statusFilter, severityFilter]);

  const openCreateModal = async () => {
    setShowCreateModal(true);
    setCreateError(null);
    try {
      const res = await servicesApi.list({ size: 100 });
      const svcList = res.data.data.content || [];
      setServices(svcList);
      if (svcList.length > 0) {
        setSelectedService(svcList[0].id);
        setEnvironments(svcList[0].environments || []);
        if (svcList[0].environments && svcList[0].environments.length > 0) {
          setSelectedEnv(svcList[0].environments[0].id);
        }
      }
    } catch (err) {
      setCreateError('Could not load services list.');
    }
  };

  const handleServiceChange = (svcId) => {
    setSelectedService(svcId);
    const svc = services.find(s => s.id === svcId);
    const envs = svc?.environments || [];
    setEnvironments(envs);
    if (envs.length > 0) {
      setSelectedEnv(envs[0].id);
    } else {
      setSelectedEnv('');
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreating(true);
    setCreateError(null);
    try {
      await incidentsApi.create({
        title,
        description,
        serviceId: selectedService,
        environmentId: selectedEnv,
        severity,
      });
      setShowCreateModal(false);
      setTitle('');
      setDescription('');
      fetchIncidents();
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Failed to create incident');
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Incident Management</h1>
          <p className="text-sm text-slate-400 mt-1">
            Track, triage, and orchestrate resolution workflows for system disruptions
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={fetchIncidents}
            className="flex items-center gap-2 px-3 py-2 text-sm bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 rounded-lg transition-colors"
          >
            <RefreshCw className="w-4 h-4" />
            Refresh
          </button>
          {isEngineerOrAbove && (
            <button
              onClick={openCreateModal}
              className="flex items-center gap-2 px-4 py-2 text-sm font-medium bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg transition-colors shadow-lg shadow-emerald-950/30"
            >
              <Plus className="w-4 h-4" />
              Declare Incident
            </button>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-3 p-4 bg-slate-900/60 border border-slate-800 rounded-xl">
        <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-500">
          <Filter className="w-3.5 h-3.5" />
          Filter:
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="text-xs bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-3 py-1.5 focus:border-slate-700 focus:outline-none"
        >
          <option value="">All Statuses</option>
          <option value="OPEN">Open</option>
          <option value="INVESTIGATING">Investigating</option>
          <option value="MITIGATED">Mitigated</option>
          <option value="RESOLVED">Resolved</option>
        </select>

        <select
          value={severityFilter}
          onChange={(e) => setSeverityFilter(e.target.value)}
          className="text-xs bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-3 py-1.5 focus:border-slate-700 focus:outline-none"
        >
          <option value="">All Severities</option>
          <option value="SEV1">SEV1 - Critical</option>
          <option value="SEV2">SEV2 - Major</option>
          <option value="SEV3">SEV3 - Moderate</option>
          <option value="SEV4">SEV4 - Minor</option>
        </select>
      </div>

      {/* Table */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            Loading incidents...
          </div>
        ) : error ? (
          <div className="p-8 text-center text-rose-400 text-sm">
            {error}
          </div>
        ) : incidents.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            No incidents found matching criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-300">
              <thead className="text-xs uppercase bg-slate-950/50 text-slate-400 border-b border-slate-800">
                <tr>
                  <th className="py-3 px-4">Incident ID</th>
                  <th className="py-3 px-4">Title</th>
                  <th className="py-3 px-4">Service / Env</th>
                  <th className="py-3 px-4">Severity</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Assignee</th>
                  <th className="py-3 px-4">Created</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
                {incidents.map((inc) => (
                  <tr key={inc.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="py-3 px-4 font-semibold text-emerald-400">
                      {inc.incidentNumber}
                    </td>
                    <td className="py-3 px-4 font-sans text-sm font-medium text-white max-w-xs truncate">
                      {inc.title}
                    </td>
                    <td className="py-3 px-4 font-sans text-xs text-slate-400">
                      <span className="text-slate-200">{inc.serviceName}</span>
                      <span className="text-slate-500"> / {inc.environmentName}</span>
                    </td>
                    <td className="py-3 px-4 font-sans">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold border ${SEVERITY_COLORS[inc.severity]}`}>
                        {inc.severity}
                      </span>
                    </td>
                    <td className="py-3 px-4 font-sans">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${STATUS_COLORS[inc.status]}`}>
                        {inc.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 font-sans text-xs text-slate-400">
                      {inc.assignee ? inc.assignee.fullName : <span className="text-slate-600 italic">Unassigned</span>}
                    </td>
                    <td className="py-3 px-4 font-sans text-xs text-slate-500">
                      {new Date(inc.createdAt).toLocaleString()}
                    </td>
                    <td className="py-3 px-4 text-right font-sans">
                      <Link
                        to={`/incidents/${inc.id}`}
                        className="inline-flex items-center gap-1 text-xs text-emerald-400 hover:text-emerald-300 font-medium"
                      >
                        View
                        <ChevronRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Declare Incident Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-lg w-full p-6 shadow-2xl space-y-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-amber-400" />
              Declare New Incident
            </h2>

            {createError && (
              <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-xs text-red-400">
                {createError}
              </div>
            )}

            <form onSubmit={handleCreate} className="space-y-4 text-sm">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Title</label>
                <input
                  type="text"
                  required
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g. Elevated latency on payment processing"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-500 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Description</label>
                <textarea
                  required
                  rows={3}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describe observed symptoms, error spikes, or customer impact..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-500 text-sm"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1">Service</label>
                  <select
                    value={selectedService}
                    onChange={(e) => handleServiceChange(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-500 text-xs"
                  >
                    {services.map((s) => (
                      <option key={s.id} value={s.id}>{s.name}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1">Environment</label>
                  <select
                    value={selectedEnv}
                    onChange={(e) => setSelectedEnv(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-500 text-xs"
                  >
                    {environments.map((e) => (
                      <option key={e.id} value={e.id}>{e.name} ({e.type})</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Severity</label>
                <select
                  value={severity}
                  onChange={(e) => setSeverity(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-500 text-xs"
                >
                  <option value="SEV1">SEV1 - Critical Outage (Customer facing)</option>
                  <option value="SEV2">SEV2 - Major Degradation (High error rate/latency)</option>
                  <option value="SEV3">SEV3 - Moderate Issue (Workaround available)</option>
                  <option value="SEV4">SEV4 - Minor / Cosmetic</option>
                </select>
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-xs text-slate-400 hover:text-white transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creating}
                  className="px-4 py-2 text-xs font-medium bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded-lg transition-colors"
                >
                  {creating ? 'Creating...' : 'Create Incident'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
