import React, { useState, useEffect } from 'react';
import { 
  Bot, 
  Sparkles, 
  ShieldAlert, 
  AlertCircle, 
  CheckCircle, 
  Play, 
  HelpCircle,
  ArrowRight,
  Info
} from 'lucide-react';
import { incidentsApi, approvalsApi } from '../../api/endpoints';
import { useAuth } from '../../context/AuthContext';

const RISK_BADGES = {
  LOW: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
  MEDIUM: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
  HIGH: 'bg-red-500/10 text-red-400 border-red-500/30',
  CRITICAL: 'bg-red-600/20 text-red-300 border-red-500/50',
};

export default function AnalysisPanel({ incidentId, isClosed }) {
  const { user, hasRole } = useAuth();
  const isEngineerOrAbove = hasRole(['ROLE_ENGINEER', 'ROLE_APPROVER', 'ROLE_ADMIN']);

  const [analyses, setAnalyses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);
  const [error, setError] = useState(null);
  const [actionSuccess, setActionSuccess] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [requestingAction, setRequestingAction] = useState(false);

  const fetchAnalyses = async () => {
    try {
      const res = await incidentsApi.getAnalyses(incidentId);
      setAnalyses(res.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalyses();
  }, [incidentId]);

  const handleRunAnalysis = async () => {
    setRunning(true);
    setError(null);
    setActionSuccess(null);
    setActionError(null);
    try {
      const res = await incidentsApi.triggerAnalysis(incidentId);
      setAnalyses([res.data.data, ...analyses]);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to trigger automated analysis.');
    } finally {
      setRunning(false);
    }
  };

  const handleProposeAction = async (action) => {
    setRequestingAction(true);
    setActionSuccess(null);
    setActionError(null);
    try {
      const idempotencyKey = `req-${incidentId}-${action.actionType}-${Date.now()}`;
      await approvalsApi.create({
        incidentId,
        targetServiceId: action.targetServiceId,
        actionType: action.actionType,
        reason: action.reason,
        riskLevel: action.riskLevel,
        idempotencyKey,
      });
      setActionSuccess(`Action request for "${action.actionType}" submitted to Approvals Inbox.`);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to propose action.');
    } finally {
      setRequestingAction(false);
    }
  };

  const latest = analyses[0];

  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-800 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-lg bg-indigo-500/10 border border-indigo-500/20 text-indigo-400">
            <Bot className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              Rule-Based Operational Advisory
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                100% Offline
              </span>
            </h2>
            <p className="text-xs text-slate-400">
              Deterministic heuristic evaluation of telemetry anomalies
            </p>
          </div>
        </div>

        {isEngineerOrAbove && !isClosed && (
          <button
            onClick={handleRunAnalysis}
            disabled={running}
            className="flex items-center gap-2 px-3 py-1.5 text-xs font-medium bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-lg transition-colors shadow-lg shadow-indigo-950/40"
          >
            <Sparkles className="w-3.5 h-3.5" />
            {running ? 'Evaluating Metrics...' : 'Run Advisory Analysis'}
          </button>
        )}
      </div>

      {error && (
        <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-xs text-red-400">
          {error}
        </div>
      )}

      {actionSuccess && (
        <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-lg text-xs text-emerald-400 flex items-center gap-2">
          <CheckCircle className="w-4 h-4 shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {actionError && (
        <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-xs text-red-400 flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      {loading ? (
        <div className="py-6 text-center text-xs text-slate-500">Loading analysis reports...</div>
      ) : !latest ? (
        <div className="py-8 text-center text-xs text-slate-500 space-y-2">
          <p>No automated analysis has been run for this incident yet.</p>
          {isEngineerOrAbove && !isClosed && (
            <p className="text-slate-400">
              Click <span className="text-indigo-400 font-medium">"Run Advisory Analysis"</span> to evaluate live telemetry against operational failure heuristics.
            </p>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {/* Header Summary Card */}
          <div className="p-4 bg-slate-950/70 border border-slate-800/80 rounded-lg space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs text-slate-400">
                Generated {new Date(latest.createdAt).toLocaleTimeString()} by{' '}
                <span className="text-slate-300 font-medium">{latest.requestedByName}</span>
              </span>
              <span className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold border ${RISK_BADGES[latest.riskLevel] || RISK_BADGES.LOW}`}>
                Risk Rating: {latest.riskLevel}
              </span>
            </div>
            <p className="text-xs text-slate-200 leading-relaxed font-sans">
              {latest.summary}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Possible Causes */}
            <div className="p-4 bg-slate-950/40 border border-slate-800/60 rounded-lg space-y-2">
              <h3 className="text-xs font-semibold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                <AlertCircle className="w-3.5 h-3.5 text-amber-400" />
                Inferred Root Causes
              </h3>
              <ul className="space-y-1.5 text-xs text-slate-400">
                {latest.possibleCauses.map((cause, i) => (
                  <li key={i} className="flex items-start gap-1.5 leading-relaxed">
                    <span className="text-amber-400/70 font-mono">•</span>
                    <span>{cause}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Recommended Investigation Steps */}
            <div className="p-4 bg-slate-950/40 border border-slate-800/60 rounded-lg space-y-2">
              <h3 className="text-xs font-semibold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                <HelpCircle className="w-3.5 h-3.5 text-blue-400" />
                Investigation Steps
              </h3>
              <ul className="space-y-1.5 text-xs text-slate-400">
                {latest.investigationSteps.map((step, i) => (
                  <li key={i} className="flex items-start gap-1.5 leading-relaxed">
                    <span className="text-blue-400/70 font-mono">{i + 1}.</span>
                    <span>{step}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Proposed Remediation Actions */}
          <div className="space-y-2.5">
            <h3 className="text-xs font-semibold text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
              <ShieldAlert className="w-3.5 h-3.5 text-emerald-400" />
              Proposed Remediation Actions (Human Gate Required)
            </h3>

            {latest.proposedActions.length === 0 ? (
              <p className="text-xs text-slate-500 italic">No automated actions proposed for this state.</p>
            ) : (
              <div className="space-y-2">
                {latest.proposedActions.map((act, idx) => (
                  <div
                    key={idx}
                    className="p-3.5 bg-slate-950 border border-slate-800 rounded-lg flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:border-slate-700 transition-colors"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-xs font-bold text-emerald-400">
                          {act.actionType}
                        </span>
                        <span className={`text-[10px] px-1.5 py-0.2 rounded border ${RISK_BADGES[act.riskLevel]}`}>
                          {act.riskLevel}
                        </span>
                        <span className="text-xs text-slate-500">
                          Target: <span className="text-slate-300">{act.targetServiceName}</span>
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 leading-relaxed">
                        {act.reason}
                      </p>
                      {act.rollbackStrategy && (
                        <p className="text-[11px] text-slate-500 italic">
                          Rollback: {act.rollbackStrategy}
                        </p>
                      )}
                    </div>

                    {isEngineerOrAbove && !isClosed && (
                      <button
                        onClick={() => handleProposeAction(act)}
                        disabled={requestingAction}
                        className="self-start sm:self-auto shrink-0 flex items-center gap-1 px-3 py-1.5 text-xs font-medium bg-emerald-600/20 text-emerald-300 border border-emerald-500/30 hover:bg-emerald-600/30 disabled:opacity-50 rounded-lg transition-colors"
                      >
                        Propose Action
                        <ArrowRight className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Air-gap / Limitations Notice */}
          <div className="p-3 bg-slate-950/40 border border-slate-800/60 rounded-lg text-[11px] text-slate-500 space-y-1">
            <div className="flex items-center gap-1.5 text-slate-400 font-medium">
              <Info className="w-3 h-3" /> Engine Safeguards & Limitations:
            </div>
            <ul className="list-disc list-inside space-y-0.5">
              {latest.limitations.map((lim, i) => (
                <li key={i}>{lim}</li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
}
