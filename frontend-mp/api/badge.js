const { request } = require('../utils/request');

function list() {
  return request({ url: '/api/mp/badge/list', method: 'GET' });
}

function my() {
  return request({ url: '/api/mp/badge/my', method: 'GET' });
}

module.exports = { list, my };
