import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('forgeops_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors uniformly
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const errorResponse = error.response?.data || {
      message: error.message || 'An unexpected error occurred.',
      code: 'NETWORK_ERROR',
    };

    if (error.response?.status === 401 && !window.location.pathname.includes('/login')) {
      localStorage.removeItem('forgeops_token');
      localStorage.removeItem('forgeops_user');
      window.location.href = '/login';
    }

    return Promise.reject(errorResponse);
  }
);

export default api;
