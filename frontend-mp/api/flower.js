const { request } = require('../utils/request');

function summary() {
  return request({ url: '/api/mp/flower/summary', method: 'GET' });
}

function logs(params) {
  return request({ url: '/api/mp/flower/logs', method: 'GET', data: params });
}

module.exports = { summary, logs };
