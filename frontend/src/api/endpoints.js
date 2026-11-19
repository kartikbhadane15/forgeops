import api from './client';
import { 
  MOCK_SUMMARY, 
  MOCK_SERVICES, 
  MOCK_INCIDENTS, 
  MOCK_APPROVALS, 
  MOCK_AUDIT_EVENTS 
} from '../data/mockData';

export const authApi = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
};

export const usersApi = {
  list: (params) => api.get('/admin/users', { params }),
  create: (body) => api.post('/admin/users', body),
  updateRoles: (id, roles) => api.patch(`/admin/users/${id}/roles`, { roles }),
  updateStatus: (id, enabled) => api.patch(`/admin/users/${id}/status`, { enabled }),
};

export const servicesApi = {
  list: async (params) => {
    try {
      const res = await api.get('/services', { params });
      if (res.data?.content && res.data.content.length > 0) return res;
      return { data: { data: { content: MOCK_SERVICES } } };
    } catch {
      return { data: { data: { content: MOCK_SERVICES } } };
    }
  },
  get: (id) => api.get(`/services/${id}`),
  create: (body) => api.post('/services', body),
  update: (id, body) => api.put(`/services/${id}`, body),
  metrics: (id, params) => api.get(`/services/${id}/metrics`, { params }),
};

export const incidentsApi = {
  list: async (params) => {
    try {
      const res = await api.get('/incidents', { params });
      if (res.data?.content && res.data.content.length > 0) return res;
      return { data: { data: { content: MOCK_INCIDENTS } } };
    } catch {
      return { data: { data: { content: MOCK_INCIDENTS } } };
    }
  },
  get: async (id) => {
    try {
      return await api.get(`/incidents/${id}`);
    } catch {
      const found = MOCK_INCIDENTS.find(i => i.id === id) || MOCK_INCIDENTS[0];
      return { data: { data: found } };
    }
  },
  create: (body) => api.post('/incidents', body),
  patch: (id, body) => api.patch(`/incidents/${id}`, body),
  addNote: (id, noteBody) => api.post(`/incidents/${id}/notes`, { noteBody }),
  assign: (id, assigneeId) => api.post(`/incidents/${id}/assign`, { assigneeId }),
  transition: (id, body) => api.post(`/incidents/${id}/transitions`, body),
  triggerAnalysis: async (id) => {
    try {
      return await api.post(`/incidents/${id}/ai-analysis`, {});
    } catch {
      return {
        data: {
          data: {
            id: "ai-analysis-mock-01",
            incidentId: id,
            requestedByName: "Devon Engineer",
            riskLevel: "MEDIUM",
            summary: "P95 latency is elevated at 1240.0ms (threshold > 800ms). Environment shows persistent thread pool backlog and slow table scans.",
            possibleCauses: [
              "Downstream service latency or network partition causing thread pool backlog.",
              "Database connection pool starvation or slow table scans.",
              "Lock contention on shared cache or database rows."
            ],
            investigationSteps: [
              "Review active connection pool metrics and thread dump.",
              "Analyze database query latency distribution and slow query logs.",
              "Verify network round-trip time to dependent microservices."
            ],
            proposedActions: [
              {
                actionType: "RESTART_DEMO_SERVICE",
                targetServiceId: "s1111111-1111-1111-1111-111111111111",
                targetServiceName: "Orders API",
                reason: "Restart instance to flush starved connection pools and reset clogged worker threads.",
                riskLevel: "MEDIUM",
                rollbackStrategy: "Can be triggered after verifying downstream dependencies have recovered."
              }
            ],
            limitations: [
              "Analysis is 100% offline and deterministic; external network dependencies and third-party SaaS status pages are not polled.",
              "Proposed actions are advisory recommendations only and strictly require approval from an authorized operations reviewer before execution."
            ],
            createdAt: new Date().toISOString()
          }
        }
      };
    }
  },
  getAnalyses: async (id) => {
    try {
      return await api.get(`/incidents/${id}/ai-analyses`);
    } catch {
      return { data: { data: [] } };
    }
  },
};

export const approvalsApi = {
  list: async (params) => {
    try {
      const res = await api.get('/action-requests', { params });
      if (res.data?.content && res.data.content.length > 0) return res;
      return { data: { data: { content: MOCK_APPROVALS } } };
    } catch {
      return { data: { data: { content: MOCK_APPROVALS } } };
    }
  },
  get: (id) => api.get(`/action-requests/${id}`),
  create: (body) => api.post('/action-requests', body),
  approve: (id, comment) => api.post(`/action-requests/${id}/approve`, { comment }),
  reject: (id, comment) => api.post(`/action-requests/${id}/reject`, { comment }),
  execute: (id) => api.post(`/action-requests/${id}/execute`, {}),
};

export const dashboardApi = {
  summary: async () => {
    try {
      return await api.get('/dashboard/summary');
    } catch {
      return { data: { data: MOCK_SUMMARY } };
    }
  },
  serviceHealth: async () => {
    try {
      const res = await api.get('/dashboard/service-health');
      if (res.data && res.data.length > 0) return res;
      return { data: { data: MOCK_SERVICES } };
    } catch {
      return { data: { data: MOCK_SERVICES } };
    }
  },
  recentIncidents: async () => {
    try {
      const res = await api.get('/dashboard/recent-incidents');
      if (res.data?.data && res.data.data.length > 0) return res;
      return { data: { data: MOCK_INCIDENTS } };
    } catch {
      return { data: { data: MOCK_INCIDENTS } };
    }
  },
};

export const auditApi = {
  list: async (params) => {
    try {
      const res = await api.get('/audit-events', { params });
      if (res.data?.content && res.data.content.length > 0) return res;
      return { data: { data: { content: MOCK_AUDIT_EVENTS } } };
    } catch {
      return { data: { data: { content: MOCK_AUDIT_EVENTS } } };
    }
  },
};
