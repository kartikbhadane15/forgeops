import api from './client';

export const authApi = {
  login: (email, password) =>
    api.post('/auth/login', { email, password }),

  logout: () =>
    api.post('/auth/logout'),

  me: () =>
    api.get('/auth/me'),
};

export const usersApi = {
  list: (params) => api.get('/admin/users', { params }),
  create: (body) => api.post('/admin/users', body),
  updateRoles: (id, roles) => api.patch(`/admin/users/${id}/roles`, { roles }),
  updateStatus: (id, enabled) => api.patch(`/admin/users/${id}/status`, { enabled }),
};

export const servicesApi = {
  list: (params) => api.get('/services', { params }),
  get: (id) => api.get(`/services/${id}`),
  create: (body) => api.post('/services', body),
  update: (id, body) => api.put(`/services/${id}`, body),
  metrics: (id, params) => api.get(`/services/${id}/metrics`, { params }),
};

export const incidentsApi = {
  list: (params) => api.get('/incidents', { params }),
  get: (id) => api.get(`/incidents/${id}`),
  create: (body) => api.post('/incidents', body),
  patch: (id, body) => api.patch(`/incidents/${id}`, body),
  addNote: (id, noteBody) => api.post(`/incidents/${id}/notes`, { noteBody }),
  assign: (id, assigneeId) => api.post(`/incidents/${id}/assign`, { assigneeId }),
  transition: (id, body) => api.post(`/incidents/${id}/transitions`, body),
  triggerAnalysis: (id) => api.post(`/incidents/${id}/ai-analysis`, {}),
  getAnalyses: (id) => api.get(`/incidents/${id}/ai-analyses`),
};

export const approvalsApi = {
  list: (params) => api.get('/action-requests', { params }),
  get: (id) => api.get(`/action-requests/${id}`),
  create: (body) => api.post('/action-requests', body),
  approve: (id, comment) => api.post(`/action-requests/${id}/approve`, { comment }),
  reject: (id, comment) => api.post(`/action-requests/${id}/reject`, { comment }),
  execute: (id) => api.post(`/action-requests/${id}/execute`, {}),
};

export const dashboardApi = {
  summary: () => api.get('/dashboard/summary'),
  serviceHealth: () => api.get('/dashboard/service-health'),
  recentIncidents: () => api.get('/dashboard/recent-incidents'),
};

export const auditApi = {
  list: (params) => api.get('/audit-events', { params }),
};
