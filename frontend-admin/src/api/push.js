import request from './request';

export function getRules() {
  return request.get('/api/admin/push/rules').then((r) => r.data);
}

export function saveRule(data) {
  return request.post('/api/admin/push/rules', data).then((r) => r.data);
}

export function deleteRule(id) {
  return request.delete('/api/admin/push/rules/' + id).then((r) => r.data);
}

export function triggerPush(helpId) {
  return request.post('/api/admin/push/trigger', { helpId }).then((r) => r.data);
}

export function getPushLogs(helpId) {
  const params = helpId ? { helpId } : {};
  return request.get('/api/admin/push/logs', { params }).then((r) => r.data);
}
