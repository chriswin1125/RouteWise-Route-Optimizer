const API_BASE = (import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '').replace(/\/$/, '') + '/api';

/**
 * Universal fetch wrapper handling authorization headers and standard error reporting.
 */
async function request(endpoint, options = {}) {
  const token = localStorage.getItem('routewise_token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const config = {
    ...options,
    headers,
  };

  const response = await fetch(`${API_BASE}${endpoint}`, config);

  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || data?.error || `Request failed with status ${response.status}`;
    const error = new Error(errorMsg);
    error.status = response.status;
    error.details = data;
    throw error;
  }

  return data;
}

export const api = {
  // Auth
  login: (credentials) => request('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
  register: (payload) => request('/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
  getCurrentUser: () => request('/auth/me'),
  updatePreferences: (prefs) => request('/auth/preferences', { method: 'PUT', body: JSON.stringify(prefs) }),

  // Routes
  optimizeRoute: (payload) => request('/routes/optimize', { method: 'POST', body: JSON.stringify(payload) }),
  compareRoutes: (payload) => request('/routes/compare', { method: 'POST', body: JSON.stringify(payload) }),
  shortestPath: (payload) => request('/routes/shortest-path', { method: 'POST', body: JSON.stringify(payload) }),
  saveRoute: (payload) => request('/routes', { method: 'POST', body: JSON.stringify(payload) }),
  getRoutes: (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return request(`/routes${query ? `?${query}` : ''}`);
  },
  getRouteById: (id) => request(`/routes/${id}`),
  updateRoute: (id, payload) => request(`/routes/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteRoute: (id) => request(`/routes/${id}`, { method: 'DELETE' }),

  // Deliveries
  createDelivery: (payload) => request('/deliveries', { method: 'POST', body: JSON.stringify(payload) }),
  getDeliveries: (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return request(`/deliveries${query ? `?${query}` : ''}`);
  },
  getPendingDeliveries: () => request('/deliveries/pending'),
  getDeliveryById: (id) => request(`/deliveries/${id}`),
  updateDelivery: (id, payload) => request(`/deliveries/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  updateDeliveryStatus: (id, status) => request(`/deliveries/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
  deleteDelivery: (id) => request(`/deliveries/${id}`, { method: 'DELETE' }),

  // Locations
  searchLocations: (query) => request(`/locations/search?q=${encodeURIComponent(query)}`),
  geocodeAddress: (address) => request('/locations/geocode', { method: 'POST', body: JSON.stringify({ address }) }),
  reverseGeocode: (lat, lon) => request(`/locations/reverse?lat=${lat}&lon=${lon}`),

  // Dashboard
  getDashboardSummary: () => request('/dashboard/summary'),
  getDashboardAnalytics: () => request('/dashboard/analytics'),

  // Algorithms
  traceDijkstra: (payload) => request('/algorithms/dijkstra/trace', { method: 'POST', body: JSON.stringify(payload) }),
  traceAStar: (payload) => request('/algorithms/a-star/shortest-path', { method: 'POST', body: JSON.stringify(payload) }),
  getSampleGraph: () => request('/algorithms/sample-graph'),
};
