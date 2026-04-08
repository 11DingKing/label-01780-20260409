import axios from 'axios';
import { useUserStore } from '../stores/user';
import { toastWarning, toastError } from '../utils/message';

const baseURL = import.meta.env.VITE_API_BASE !== undefined && import.meta.env.VITE_API_BASE !== ''
  ? import.meta.env.VITE_API_BASE
  : (import.meta.env.PROD ? '' : 'http://localhost:8080');

const request = axios.create({
  baseURL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
});

request.interceptors.request.use((config) => {
  const store = useUserStore();
  if (store.token) config.headers.Authorization = 'Bearer ' + store.token;
  return config;
});

request.interceptors.response.use(
  (res) => {
    const data = res.data;
    if (data && data.code !== undefined && data.code !== 200) {
      toastWarning(data.message || '请求失败');
      return Promise.reject(data);
    }
    return res;
  },
  (err) => {
    if (err.response && err.response.status === 401) {
      const store = useUserStore();
      store.setToken('');
      window.location.href = '/login';
    } else {
      toastError(err.response?.data?.message || err.message || '网络异常');
    }
    return Promise.reject(err);
  }
);

export default request;
