import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, 
  Filter, 
  Clock, 
  User, 
  Activity, 
  FileText, 
  RefreshCw,
  Search,
  ChevronDown,
  ChevronRight
} from 'lucide-react';
import { auditApi } from '../../api/endpoints';
import { MOCK_AUDIT_EVENTS } from '../../data/mockData';

export default function AuditPage() {
  const [events, setEvents] = useState(MOCK_AUDIT_EVENTS);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [eventTypeFilter, setEventTypeFilter] = useState('');
  const [entityTypeFilter, setEntityTypeFilter] = useState('');
  const [expandedId, setExpandedId] = useState(null);

  const fetchEvents = async () => {
    try {
      const params = {};
      if (eventTypeFilter) params.eventType = eventTypeFilter;
      if (entityTypeFilter) params.entityType = entityTypeFilter;
      const res = await auditApi.list(params);
      const items = res.data?.data?.content || res.data?.content || res.data || [];
      if (items.length > 0) setEvents(items);
    } catch (err) {
      // Keeps mock data seamlessly
    }
  };

  useEffect(() => {
    fetchEvents();
  }, [eventTypeFilter, entityTypeFilter]);

  const toggleExpand = (id) => {
    setExpandedId(expandedId === id ? null : id);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
            <ShieldCheck className="w-6 h-6 text-emerald-400" />
            Audit Trail & Compliance Log
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Append-only, immutable record of authentication, lifecycle transitions, and operational actions
          </p>
        </div>
        <button
          onClick={fetchEvents}
          className="self-start sm:self-auto flex items-center gap-2 px-3 py-2 text-sm bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 rounded-lg transition-colors"
        >
          <RefreshCw className="w-4 h-4" />
          Refresh Audit Log
        </button>
      </div>

      {/* Filter Bar */}
      <div className="flex flex-wrap items-center gap-3 p-4 bg-slate-900/60 border border-slate-800 rounded-xl">
        <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-500">
          <Filter className="w-3.5 h-3.5" />
          Filters:
        </div>
        <select
          value={eventTypeFilter}
          onChange={(e) => setEventTypeFilter(e.target.value)}
          className="text-xs bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-3 py-1.5 focus:border-slate-700 focus:outline-none"
        >
          <option value="">All Event Types</option>
          <option value="AUTH_LOGIN_SUCCESS">AUTH_LOGIN_SUCCESS</option>
          <option value="INCIDENT_CREATED">INCIDENT_CREATED</option>
          <option value="INCIDENT_STATUS_CHANGED">INCIDENT_STATUS_CHANGED</option>
          <option value="ACTION_REQUEST_CREATED">ACTION_REQUEST_CREATED</option>
          <option value="ACTION_REQUEST_APPROVED">ACTION_REQUEST_APPROVED</option>
          <option value="ACTION_REQUEST_REJECTED">ACTION_REQUEST_REJECTED</option>
          <option value="ACTION_REQUEST_EXECUTED">ACTION_REQUEST_EXECUTED</option>
        </select>

        <select
          value={entityTypeFilter}
          onChange={(e) => setEntityTypeFilter(e.target.value)}
          className="text-xs bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-3 py-1.5 focus:border-slate-700 focus:outline-none"
        >
          <option value="">All Entity Types</option>
          <option value="USER">USER</option>
          <option value="INCIDENT">INCIDENT</option>
          <option value="ACTION_REQUEST">ACTION_REQUEST</option>
          <option value="SERVICE">SERVICE</option>
        </select>
      </div>

      {/* Event List */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center text-slate-500 text-sm">Loading audit events...</div>
        ) : error ? (
          <div className="p-8 text-center text-rose-400 text-sm">{error}</div>
        ) : events.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            No audit events found matching criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-300">
              <thead className="text-xs uppercase bg-slate-950/50 text-slate-400 border-b border-slate-800">
                <tr>
                  <th className="py-3 px-4">Timestamp (UTC)</th>
                  <th className="py-3 px-4">Actor</th>
                  <th className="py-3 px-4">Event Type</th>
                  <th className="py-3 px-4">Entity</th>
                  <th className="py-3 px-4">Summary</th>
                  <th className="py-3 px-4 text-right">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
                {events.map((evt) => {
                  const isExpanded = expandedId === evt.id;

                  return (
                    <React.Fragment key={evt.id}>
                      <tr className="hover:bg-slate-800/30 transition-colors">
                        <td className="py-3 px-4 text-slate-500 whitespace-nowrap">
                          {new Date(evt.createdAt).toISOString().replace('T', ' ').slice(0, 19)}
                        </td>
                        <td className="py-3 px-4 font-sans text-xs text-slate-300">
                          {evt.actor ? evt.actor.fullName : <span className="text-slate-600 italic">SYSTEM</span>}
                        </td>
                        <td className="py-3 px-4 font-semibold text-emerald-400">
                          {evt.eventType}
                        </td>
                        <td className="py-3 px-4 font-sans text-xs text-slate-400">
                          <span className="font-mono text-slate-300">{evt.entityType}</span>
                          <span className="text-slate-600 block text-[11px] truncate max-w-[120px]">{evt.entityId}</span>
                        </td>
                        <td className="py-3 px-4 font-sans text-xs text-slate-300 max-w-md">
                          {evt.safeSummary}
                        </td>
                        <td className="py-3 px-4 text-right font-sans">
                          {evt.detailsJson ? (
                            <button
                              onClick={() => toggleExpand(evt.id)}
                              className="inline-flex items-center gap-1 text-xs text-slate-400 hover:text-white"
                            >
                              {isExpanded ? <ChevronDown className="w-3.5 h-3.5" /> : <ChevronRight className="w-3.5 h-3.5" />}
                              Payload
                            </button>
                          ) : (
                            <span className="text-[11px] text-slate-600 italic">None</span>
                          )}
                        </td>
                      </tr>
                      {isExpanded && evt.detailsJson && (
                        <tr className="bg-slate-950/80">
                          <td colSpan={6} className="p-4 border-t border-slate-800/80">
                            <div className="space-y-1">
                              <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 block">
                                Context JSON Payload:
                              </span>
                              <pre className="p-3 bg-slate-900 border border-slate-800 rounded-lg text-emerald-300 text-xs overflow-x-auto whitespace-pre-wrap font-mono">
                                {JSON.stringify(JSON.parse(evt.detailsJson), null, 2)}
                              </pre>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
