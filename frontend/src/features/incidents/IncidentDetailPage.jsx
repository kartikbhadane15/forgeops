import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { 
  ArrowLeft, 
  AlertTriangle, 
  Clock, 
  User, 
  Activity, 
  Send, 
  CheckCircle, 
  ShieldAlert,
  Server
} from 'lucide-react';
import { incidentsApi } from '../../api/endpoints';
import { useAuth } from '../../context/AuthContext';

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

export default function IncidentDetailPage() {
  const { id } = useParams();
  const { user, hasRole } = useAuth();
  const isEngineerOrAbove = hasRole(['ROLE_ENGINEER', 'ROLE_APPROVER', 'ROLE_ADMIN']);

  const [incident, setIncident] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Transition & Note state
  const [noteBody, setNoteBody] = useState('');
  const [submittingNote, setSubmittingNote] = useState(false);
  const [targetStatus, setTargetStatus] = useState('');
  const [resolutionSummary, setResolutionSummary] = useState('');
  const [transitioning, setTransitioning] = useState(false);
  const [actionError, setActionError] = useState(null);

  const fetchIncident = async () => {
    try {
      const res = await incidentsApi.get(id);
      setIncident(res.data.data);
    } catch (err) {
      setError('Failed to load incident detail.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIncident();
  }, [id]);

  const handleAddNote = async (e) => {
    e.preventDefault();
    if (!noteBody.trim()) return;
    setSubmittingNote(true);
    setActionError(null);
    try {
      await incidentsApi.addNote(id, noteBody);
      setNoteBody('');
      fetchIncident();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to add note');
    } finally {
      setSubmittingNote(false);
    }
  };

  const handleTransition = async (status) => {
    setTransitioning(true);
    setActionError(null);
    try {
      await incidentsApi.transition(id, {
        targetStatus: status,
        resolutionSummary: status === 'RESOLVED' ? resolutionSummary : null,
      });
      setResolutionSummary('');
      setTargetStatus('');
      fetchIncident();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to update status');
    } finally {
      setTransitioning(false);
    }
  };

  const handleAssignToMe = async () => {
    setActionError(null);
    try {
      await incidentsApi.assign(id, user.id);
      fetchIncident();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to assign incident');
    }
  };

  if (loading) {
    return <div className="p-8 text-center text-slate-500 text-sm">Loading incident details...</div>;
  }

  if (error || !incident) {
    return (
      <div className="p-8 text-center text-rose-400 text-sm space-y-4">
        <p>{error || 'Incident not found'}</p>
        <Link to="/incidents" className="inline-flex items-center gap-1 text-xs text-emerald-400 hover:underline">
          <ArrowLeft className="w-3.5 h-3.5" /> Back to Incidents
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="space-y-3">
        <Link
          to="/incidents"
          className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Incidents
        </Link>

        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="flex items-center gap-3">
              <span className="font-mono text-sm font-bold text-emerald-400">
                {incident.incidentNumber}
              </span>
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded text-xs font-semibold border ${SEVERITY_COLORS[incident.severity]}`}>
                {incident.severity}
              </span>
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded text-xs font-medium border ${STATUS_COLORS[incident.status]}`}>
                {incident.status}
              </span>
            </div>
            <h1 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
              {incident.title}
            </h1>
          </div>

          {/* Quick status actions for engineers */}
          {isEngineerOrAbove && incident.status !== 'RESOLVED' && (
            <div className="flex items-center gap-2">
              {incident.status === 'OPEN' && (
                <button
                  onClick={() => handleTransition('INVESTIGATING')}
                  disabled={transitioning}
                  className="px-3 py-1.5 text-xs font-medium bg-amber-500/20 text-amber-300 border border-amber-500/30 hover:bg-amber-500/30 rounded-lg transition-colors"
                >
                  Start Investigating
                </button>
              )}
              {incident.status === 'INVESTIGATING' && (
                <button
                  onClick={() => handleTransition('MITIGATED')}
                  disabled={transitioning}
                  className="px-3 py-1.5 text-xs font-medium bg-blue-500/20 text-blue-300 border border-blue-500/30 hover:bg-blue-500/30 rounded-lg transition-colors"
                >
                  Mark Mitigated
                </button>
              )}
              {incident.status !== 'RESOLVED' && (
                <button
                  onClick={() => setTargetStatus('RESOLVED')}
                  className="px-3 py-1.5 text-xs font-medium bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg transition-colors"
                >
                  Resolve Incident
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {actionError && (
        <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-xs text-red-400">
          {actionError}
        </div>
      )}

      {/* Resolve Modal / Input */}
      {targetStatus === 'RESOLVED' && (
        <div className="p-4 bg-slate-900 border border-emerald-500/30 rounded-xl space-y-3">
          <h3 className="text-sm font-semibold text-white flex items-center gap-2">
            <CheckCircle className="w-4 h-4 text-emerald-400" />
            Resolve Incident {incident.incidentNumber}
          </h3>
          <p className="text-xs text-slate-400">
            A resolution summary is mandatory to complete the incident post-mortem trail.
          </p>
          <textarea
            rows={3}
            value={resolutionSummary}
            onChange={(e) => setResolutionSummary(e.target.value)}
            placeholder="Document root cause and exact remediation steps taken..."
            className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-500 text-xs"
          />
          <div className="flex justify-end gap-2">
            <button
              onClick={() => { setTargetStatus(''); setResolutionSummary(''); }}
              className="px-3 py-1.5 text-xs text-slate-400 hover:text-white"
            >
              Cancel
            </button>
            <button
              onClick={() => handleTransition('RESOLVED')}
              disabled={transitioning || !resolutionSummary.trim()}
              className="px-4 py-1.5 text-xs font-medium bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded-lg"
            >
              Confirm Resolution
            </button>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Details & Timeline */}
        <div className="lg:col-span-2 space-y-6">
          {/* Overview Card */}
          <div className="p-5 bg-slate-900/60 border border-slate-800 rounded-xl space-y-4">
            <h2 className="text-sm font-semibold text-slate-200">Incident Description</h2>
            <p className="text-sm text-slate-300 whitespace-pre-wrap leading-relaxed">
              {incident.description}
            </p>

            {incident.resolutionSummary && (
              <div className="pt-4 border-t border-slate-800">
                <h3 className="text-xs font-semibold text-emerald-400 uppercase tracking-wider mb-1">
                  Resolution Summary
                </h3>
                <p className="text-xs text-slate-300 leading-relaxed bg-emerald-950/20 border border-emerald-900/30 rounded-lg p-3">
                  {incident.resolutionSummary}
                </p>
              </div>
            )}
          </div>

          {/* Activity Notes / Timeline */}
          <div className="p-5 bg-slate-900/60 border border-slate-800 rounded-xl space-y-4">
            <h2 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
              <Clock className="w-4 h-4 text-slate-400" />
              Investigation Timeline & Notes ({incident.notes?.length || 0})
            </h2>

            {/* Note input */}
            {isEngineerOrAbove && incident.status !== 'RESOLVED' && (
              <form onSubmit={handleAddNote} className="space-y-2">
                <textarea
                  rows={2}
                  value={noteBody}
                  onChange={(e) => setNoteBody(e.target.value)}
                  placeholder="Add an update, metric observation, or investigation finding..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-500 text-xs"
                />
                <div className="flex justify-end">
                  <button
                    type="submit"
                    disabled={submittingNote || !noteBody.trim()}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-slate-800 hover:bg-slate-700 disabled:opacity-50 text-slate-200 rounded-lg transition-colors"
                  >
                    <Send className="w-3.5 h-3.5" />
                    Post Update
                  </button>
                </div>
              </form>
            )}

            {/* Notes List */}
            <div className="space-y-3 pt-2">
              {!incident.notes || incident.notes.length === 0 ? (
                <p className="text-xs text-slate-500 italic py-2">
                  No notes recorded yet.
                </p>
              ) : (
                incident.notes.map((note) => (
                  <div key={note.id} className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-lg text-xs space-y-1">
                    <div className="flex items-center justify-between text-slate-400">
                      <span className="font-semibold text-slate-300">{note.authorName}</span>
                      <span className="text-[11px] text-slate-500">
                        {new Date(note.createdAt).toLocaleString()}
                      </span>
                    </div>
                    <p className="text-slate-300 leading-relaxed whitespace-pre-wrap">
                      {note.noteBody}
                    </p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Context & Metadata */}
        <div className="space-y-6">
          <div className="p-5 bg-slate-900/60 border border-slate-800 rounded-xl space-y-4 text-xs">
            <h2 className="text-sm font-semibold text-slate-200">Incident Context</h2>

            <div className="space-y-3 divide-y divide-slate-800/60">
              <div className="pt-2 flex items-center justify-between">
                <span className="text-slate-400 flex items-center gap-1.5">
                  <Server className="w-3.5 h-3.5" /> Service:
                </span>
                <span className="font-semibold text-white">{incident.serviceName}</span>
              </div>

              <div className="pt-3 flex items-center justify-between">
                <span className="text-slate-400">Environment:</span>
                <span className="font-semibold text-white">{incident.environmentName}</span>
              </div>

              <div className="pt-3 flex items-center justify-between">
                <span className="text-slate-400 flex items-center gap-1.5">
                  <User className="w-3.5 h-3.5" /> Assignee:
                </span>
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-white">
                    {incident.assignee ? incident.assignee.fullName : 'Unassigned'}
                  </span>
                  {isEngineerOrAbove && !incident.assignee && (
                    <button
                      onClick={handleAssignToMe}
                      className="text-[11px] text-emerald-400 hover:underline"
                    >
                      Assign to me
                    </button>
                  )}
                </div>
              </div>

              <div className="pt-3 flex items-center justify-between">
                <span className="text-slate-400">Reported By:</span>
                <span className="text-slate-300">{incident.createdBy?.fullName}</span>
              </div>

              <div className="pt-3 flex items-center justify-between">
                <span className="text-slate-400">Created:</span>
                <span className="text-slate-300">{new Date(incident.createdAt).toLocaleString()}</span>
              </div>

              <div className="pt-3 flex items-center justify-between">
                <span className="text-slate-400">Last Modified:</span>
                <span className="text-slate-300">{new Date(incident.updatedAt).toLocaleString()}</span>
              </div>
            </div>
          </div>

          {/* AI Advisory Callout */}
          <div className="p-5 bg-gradient-to-br from-indigo-950/30 to-slate-900 border border-indigo-900/40 rounded-xl space-y-2 text-xs">
            <div className="flex items-center gap-2 text-indigo-400 font-semibold">
              <Activity className="w-4 h-4" />
              Automated Operations Advisory
            </div>
            <p className="text-slate-400 leading-relaxed">
              ForgeOps analyzes telemetry locally using deterministic rules to diagnose anomalies and recommend safe actions.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
