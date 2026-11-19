import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Eye, EyeOff, AlertCircle, Cpu } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const DEMO_USERS = [
  { label: 'Viewer', email: 'viewer@forgeops.local', role: 'Read-only' },
  { label: 'Engineer', email: 'engineer@forgeops.local', role: 'Create incidents, propose actions' },
  { label: 'Approver', email: 'approver@forgeops.local', role: 'Approve/reject action requests' },
  { label: 'Admin', email: 'admin@forgeops.local', role: 'Full platform access' },
];

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, loading, error } = useAuth();
  const [email, setEmail] = useState('engineer@forgeops.local');
  const [password, setPassword] = useState('Password123!');
  const [showPass, setShowPass] = useState(false);
  const [localError, setLocalError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLocalError('');
    if (!email.trim() || !password.trim()) {
      setLocalError('Email and password are required.');
      return;
    }
    try {
      await login(email.trim(), password);
      navigate('/');
    } catch (err) {
      navigate('/');
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md space-y-6">
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="flex justify-center">
            <div className="w-14 h-14 rounded-2xl bg-cyan-600/20 border border-cyan-500/40 flex items-center justify-center">
              <Shield className="w-7 h-7 text-cyan-400" />
            </div>
          </div>
          <h1 className="text-2xl font-bold text-slate-100">ForgeOps</h1>
          <p className="text-sm text-slate-400">Enterprise Infrastructure Operations Platform</p>
        </div>

        {/* Login Form */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-5">
          <h2 className="text-base font-semibold text-slate-200">Sign in to your account</h2>

          {(localError || error) && (
            <div className="flex items-start gap-2 p-3 rounded-lg bg-rose-950/40 border border-rose-700/40 text-rose-300 text-sm">
              <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{localError || error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5">Email address</label>
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="user@forgeops.local"
                autoComplete="email"
                className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5">Password</label>
              <div className="relative">
                <input
                  type={showPass ? 'text' : 'password'}
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  autoComplete="current-password"
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2.5 pr-10 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50"
                />
                <button
                  type="button"
                  onClick={() => setShowPass(v => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
                  aria-label={showPass ? 'Hide password' : 'Show password'}
                >
                  {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 rounded-lg bg-cyan-600 hover:bg-cyan-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold text-sm transition-colors"
            >
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
        </div>

        {/* Demo Users */}
        <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-xs font-semibold text-slate-400">
            <Cpu className="w-3.5 h-3.5 text-cyan-400" />
            <span>Demo Accounts — Password: <code className="font-mono text-cyan-400">Password123!</code></span>
          </div>
          <div className="grid gap-2">
            {DEMO_USERS.map(u => (
              <button
                key={u.email}
                onClick={() => { setEmail(u.email); setPassword('Password123!'); setLocalError(''); }}
                className="text-left p-2.5 rounded-lg border border-slate-800 hover:border-slate-600 hover:bg-slate-800/60 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold text-slate-200">{u.label}</span>
                  <span className="text-[10px] font-mono text-cyan-400">{u.email}</span>
                </div>
                <p className="text-[11px] text-slate-500 mt-0.5">{u.role}</p>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
