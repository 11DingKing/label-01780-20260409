const { request } = require('../utils/request');

function login(code) {
  return request({ url: '/api/mp/auth/login', method: 'POST', data: { code } });
}

function getProfile() {
  return request({ url: '/api/mp/auth/profile', method: 'GET' });
}

function updateProfile(data) {
  return request({ url: '/api/mp/auth/profile', method: 'PUT', data });
}

module.exports = { login, getProfile, updateProfile };
