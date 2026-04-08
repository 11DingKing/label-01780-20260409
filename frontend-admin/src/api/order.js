import request from './request';

export function list(params) {
  return request.get('/api/admin/order/list', { params }).then((r) => r.data);
}

export function queryWxOrder(orderNo) {
  return request.get('/api/admin/order/' + orderNo + '/query').then((r) => r.data);
}

export function reconcile(orderNo) {
  return request.post('/api/admin/order/' + orderNo + '/reconcile').then((r) => r.data);
}

export function downloadBill(billDate) {
  return request.get('/api/admin/order/bill', { params: { billDate } }).then((r) => r.data);
}
