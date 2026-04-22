import axios from 'axios';

const api = axios.create({ 
  baseURL: 'http://localhost:8080',
  headers: { 'Cache-Control': 'no-cache', 'Pragma': 'no-cache' }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    if (err.response?.status === 401) {
      const refresh = localStorage.getItem('refreshToken');
      if (refresh) {
        try {
          const { data } = await axios.post('http://localhost:8080/api/auth/refresh', { refreshToken: refresh });
          const newToken = data.data.accessToken;
          localStorage.setItem('token', newToken);
          err.config.headers.Authorization = `Bearer ${newToken}`;
          return api(err.config);
        } catch {
          localStorage.clear();
          window.location.href = '/login';
        }
      } else {
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);

export default api;
