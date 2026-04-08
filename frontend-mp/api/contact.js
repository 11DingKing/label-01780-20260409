const { request } = require('../utils/request');

function list() {
  return request({ url: '/api/mp/contacts', method: 'GET' });
}

function add(data) {
  return request({ url: '/api/mp/contacts', method: 'POST', data });
}

function update(id, data) {
  return request({ url: '/api/mp/contacts/' + id, method: 'PUT', data });
}

function remove(id) {
  return request({ url: '/api/mp/contacts/' + id, method: 'DELETE' });
}

module.exports = { list, add, update, remove };
