import request from './request';

export function login(data) {
  return request.post('/api/admin/auth/login', data).then((r) => r.data);
}
