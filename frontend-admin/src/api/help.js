import request from './request';

export function list(params) {
  return request.get('/api/admin/help/list', { params }).then((r) => r.data);
}

export function updateStatus(id, status) {
  return request.post('/api/admin/help/' + id + '/status', { status }).then((r) => r.data);
}
