import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  CheckCircle2, 
  XCircle, 
  Play, 
  Clock, 
  ShieldAlert, 
  Filter, 
  AlertTriangle,
  RefreshCw,
  ExternalLink,
  MessageSquare
} from 'lucide-react';
import { approvalsApi } from '../../api/endpoints';
import { useAuth } from '../../context/AuthContext';

const STATUS_BADGES = {
  PENDING_APPROVAL: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
  APPROVED: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
  REJECTED: 'bg-red-500/10 text-red-400 border-red-500/30',
  EXECUTED: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
  FAILED: 'bg-rose-600/20 text-rose-300 border-rose-500/50',
};

const RISK_BADGES = {
  LOW: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
  MEDIUM: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
  HIGH: 'bg-red-500/10 text-red-400 border-red-500/30',
  CRITICAL: 'bg-red-600/20 text-red-300 border-red-500/50',
};

export default function ApprovalsPage() {
  const { user, hasRole } = useAuth();
  const isApproverOrAdmin = hasRole(['ROLE_APPROVER', 'ROLE_ADMIN']);

  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('');

  // Modals & Action Processing
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [modalType, setModalType] = useState(null); // 'approve' | 'reject' | 'execute' | 'result'
  const [comment, setComment] = useState('');
  const [processing, setProcessing] = useState(false);
  const [modalError, setModalError] = useState(null);

  const fetchRequests = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {};
      if (statusFilter) params.status = statusFilter;
      const res = await approvalsApi.list(params);
      setRequests(res.data.data.content || []);
    } catch (err) {
      setError('Failed to load action requests.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, [statusFilter]);

  const handleApprove = async () => {
    setProcessing(true);
    setModalError(null);
    try {
      await approvalsApi.approve(selectedRequest.id, comment);
      setModalType(null);
      setSelectedRequest(null);
      setComment('');
      fetchRequests();
    } catch (err) {
      setModalError(err.response?.data?.message || 'Approval failed.');
    } finally {
      setProcessing(false);
    }
  };

  const handleReject = async () => {
    if (!comment.trim()) {
      setModalError('A rejection justification is mandatory.');
      return;
    }
    setProcessing(true);
    setModalError(null);
    try {
      await approvalsApi.reject(selectedRequest.id, comment);
      setModalType(null);
      setSelectedRequest(null);
      setComment('');
      fetchRequests();
    } catch (err) {
      setModalError(err.response?.data?.message || 'Rejection failed.');
    } finally {
      setProcessing(false);
    }
  };

  const handleExecute = async () => {
    setProcessing(true);
    setModalError(null);
    try {
      const res = await approvalsApi.execute(selectedRequest.id);
      setSelectedRequest(res.data.data);
      setModalType('result');
      fetchRequests();
    } catch (err) {
      setModalError(err.response?.data?.message || 'Execution failed.');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Approvals & Action Execution</h1>
          <p className="text-sm text-slate-400 mt-1">
            Human verification gate and segregation of duties for automated remediation
          </p>
        </div>
        <button
          onClick={fetchRequests}
          className="self-start sm:self-auto flex items-center gap-2 px-3 py-2 text-sm bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 rounded-lg transition-colors"
        >
          <RefreshCw className="w-4 h-4" />
          Refresh Queue
        </button>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-3 p-4 bg-slate-900/60 border border-slate-800 rounded-xl">
        <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-500">
          <Filter className="w-3.5 h-3.5" />
          Status Filter:
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="text-xs bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-3 py-1.5 focus:border-slate-700 focus:outline-none"
        >
          <option value="">All Action Requests</option>
          <option value="PENDING_APPROVAL">Pending Approval</option>
          <option value="APPROVED">Approved (Ready to Execute)</option>
          <option value="EXECUTED">Executed</option>
          <option value="REJECTED">Rejected</option>
          <option value="FAILED">Failed</option>
        </select>
      </div>

      {/* List */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            Loading action requests...
          </div>
        ) : error ? (
          <div className="p-8 text-center text-rose-400 text-sm">{error}</div>
        ) : requests.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            No action requests matching current filter.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-300">
              <thead className="text-xs uppercase bg-slate-950/50 text-slate-400 border-b border-slate-800">
                <tr>
                  <th className="py-3 px-4">Action Type</th>
                  <th className="py-3 px-4">Incident</th>
                  <th className="py-3 px-4">Target Service</th>
                  <th className="py-3 px-4">Risk</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Requester</th>
                  <th className="py-3 px-4">Reviewer</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
                {requests.map((req) => {
                  const isRequester = user?.id === req.requestedBy?.id;

                  return (
                    <tr key={req.id} className="hover:bg-slate-800/30 transition-colors">
                      <td className="py-3 px-4 font-semibold text-emerald-400">
                        {req.actionType}
                      </td>
                      <td className="py-3 px-4 font-sans text-xs">
                        <Link
                          to={`/incidents/${req.incidentId}`}
                          className="text-white hover:text-emerald-400 flex items-center gap-1 font-medium"
                        >
                          <span className="font-mono text-emerald-400">{req.incidentNumber}</span>
                          <ExternalLink className="w-3 h-3 text-slate-500" />
                        </Link>
                      </td>
                      <td className="py-3 px-4 font-sans text-xs text-slate-200">
                        {req.targetServiceName}
                      </td>
                      <td className="py-3 px-4 font-sans">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold border ${RISK_BADGES[req.riskLevel]}`}>
                          {req.riskLevel}
                        </span>
                      </td>
                      <td className="py-3 px-4 font-sans">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${STATUS_BADGES[req.approvalStatus]}`}>
                          {req.approvalStatus}
                        </span>
                      </td>
                      <td className="py-3 px-4 font-sans text-xs text-slate-400">
                        {req.requestedBy?.fullName}
                      </td>
                      <td className="py-3 px-4 font-sans text-xs text-slate-400">
                        {req.reviewedBy ? req.reviewedBy.fullName : <span className="text-slate-600 italic">None</span>}
                      </td>
                      <td className="py-3 px-4 text-right font-sans">
                        <div className="flex items-center justify-end gap-2">
                          {req.approvalStatus === 'PENDING_APPROVAL' && (
                            <>
                              {isApproverOrAdmin ? (
                                isRequester ? (
                                  <span className="text-[11px] text-amber-400/80 italic" title="Segregation of Duties: You cannot approve your own request">
                                    Self-approval blocked
                                  </span>
                                ) : (
                                  <>
                                    <button
                                      onClick={() => { setSelectedRequest(req); setModalType('approve'); setComment('Approved for execution'); setModalError(null); }}
                                      className="px-2.5 py-1 text-xs font-medium bg-emerald-600/20 text-emerald-300 border border-emerald-500/30 hover:bg-emerald-600/30 rounded"
                                    >
                                      Approve
                                    </button>
                                    <button
                                      onClick={() => { setSelectedRequest(req); setModalType('reject'); setComment(''); setModalError(null); }}
                                      className="px-2.5 py-1 text-xs font-medium bg-red-600/20 text-red-300 border border-red-500/30 hover:bg-red-600/30 rounded"
                                    >
                                      Reject
                                    </button>
                                  </>
                                )
                              ) : (
                                <span className="text-[11px] text-slate-500">Requires Approver</span>
                              )}
                            </>
                          )}

                          {req.approvalStatus === 'APPROVED' && (
                            <button
                              onClick={() => { setSelectedRequest(req); setModalType('execute'); setModalError(null); }}
                              className="flex items-center gap-1 px-3 py-1 text-xs font-medium bg-emerald-600 hover:bg-emerald-500 text-white rounded shadow-sm transition-colors"
                            >
                              <Play className="w-3 h-3" />
                              Execute
                            </button>
                          )}

                          {(req.approvalStatus === 'EXECUTED' || req.approvalStatus === 'FAILED') && (
                            <button
                              onClick={() => { setSelectedRequest(req); setModalType('result'); }}
                              className="text-xs text-slate-400 hover:text-white underline"
                            >
                              View Result
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Review / Execute Modals */}
      {modalType && selectedRequest && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-lg w-full p-6 shadow-2xl space-y-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <ShieldAlert className="w-5 h-5 text-emerald-400" />
              {modalType === 'approve' && 'Approve Action Request'}
              {modalType === 'reject' && 'Reject Action Request'}
              {modalType === 'execute' && 'Execute Whitelisted Action'}
              {modalType === 'result' && 'Execution Output'}
            </h2>

            {modalError && (
              <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-xs text-red-400">
                {modalError}
              </div>
            )}

            <div className="p-3.5 bg-slate-950/70 border border-slate-800 rounded-lg text-xs space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-400">Action:</span>
                <span className="font-mono font-bold text-emerald-400">{selectedRequest.actionType}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Target Service:</span>
                <span className="text-white font-medium">{selectedRequest.targetServiceName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Reason:</span>
                <span className="text-slate-300 max-w-xs text-right">{selectedRequest.reason}</span>
              </div>
            </div>

            {/* Approve / Reject comment inputs */}
            {(modalType === 'approve' || modalType === 'reject') && (
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">
                  {modalType === 'reject' ? 'Rejection Reason (Mandatory)' : 'Review Comment'}
                </label>
                <textarea
                  rows={3}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder={modalType === 'reject' ? 'Explain why this action cannot be approved...' : 'Operational notes...'}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-emerald-500 text-xs"
                />
              </div>
            )}

            {/* Execute confirmation warning */}
            {modalType === 'execute' && (
              <p className="text-xs text-slate-300 leading-relaxed bg-amber-500/10 border border-amber-500/20 p-3 rounded-lg">
                ⚠️ Executing will apply safe simulated remediations against the service state and update environment health records.
              </p>
            )}

            {/* Result view */}
            {modalType === 'result' && (
              <div className="space-y-2">
                <label className="block text-xs font-semibold text-slate-400">Execution Log Output:</label>
                <pre className="p-3 bg-slate-950 border border-slate-800 rounded-lg font-mono text-xs text-emerald-300 whitespace-pre-wrap">
                  {selectedRequest.executionResult || 'No output recorded.'}
                </pre>
              </div>
            )}

            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <button
                type="button"
                onClick={() => { setModalType(null); setSelectedRequest(null); }}
                className="px-4 py-2 text-xs text-slate-400 hover:text-white"
              >
                Close
              </button>

              {modalType === 'approve' && (
                <button
                  type="button"
                  onClick={handleApprove}
                  disabled={processing}
                  className="px-4 py-2 text-xs font-medium bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded-lg"
                >
                  {processing ? 'Approving...' : 'Confirm Approval'}
                </button>
              )}

              {modalType === 'reject' && (
                <button
                  type="button"
                  onClick={handleReject}
                  disabled={processing}
                  className="px-4 py-2 text-xs font-medium bg-red-600 hover:bg-red-500 disabled:opacity-50 text-white rounded-lg"
                >
                  {processing ? 'Rejecting...' : 'Confirm Rejection'}
                </button>
              )}

              {modalType === 'execute' && (
                <button
                  type="button"
                  onClick={handleExecute}
                  disabled={processing}
                  className="px-4 py-2 text-xs font-medium bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded-lg"
                >
                  {processing ? 'Executing...' : 'Run Action Now'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
