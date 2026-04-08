const { request } = require('../utils/request');

function list(params) {
  return request({ url: '/api/mp/help/list', method: 'GET', data: params });
}

function detail(id) {
  return request({ url: '/api/mp/help/' + id, method: 'GET' });
}

function nearby(params) {
  return request({ url: '/api/mp/help/nearby', method: 'GET', data: params });
}

function myList(params) {
  return request({ url: '/api/mp/help/my', method: 'GET', data: params });
}

function publish(data) {
  return request({ url: '/api/mp/help', method: 'POST', data });
}

function close(id) {
  return request({ url: '/api/mp/help/' + id + '/close', method: 'POST' });
}

module.exports = { list, detail, nearby, myList, publish, close };
