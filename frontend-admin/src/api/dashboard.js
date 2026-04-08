import request from './request';

export function getStats() {
  return request.get('/api/admin/dashboard/stats').then((r) => r.data);
}

export function getPerfStats(hours = 24) {
  return request.get('/api/admin/dashboard/perf', { params: { hours } }).then((r) => r.data);
}
