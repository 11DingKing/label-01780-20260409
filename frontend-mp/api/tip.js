const { request } = require('../utils/request');

function create(data) {
  return request({ url: '/api/mp/tip/create', method: 'POST', data });
}

function records(params) {
  return request({ url: '/api/mp/tip/records', method: 'GET', data: params });
}

module.exports = { create, records };
