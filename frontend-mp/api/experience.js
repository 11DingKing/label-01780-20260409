const { request } = require('../utils/request');

function timeline(params) {
  return request({ url: '/api/mp/experience/timeline', method: 'GET', data: params });
}

function thanks() {
  return request({ url: '/api/mp/experience/thanks', method: 'GET' });
}

function getNotes(params) {
  return request({ url: '/api/mp/experience/notes', method: 'GET', data: params });
}

function createNote(data) {
  return request({ url: '/api/mp/experience/notes', method: 'POST', data });
}

function updateNote(id, data) {
  return request({ url: '/api/mp/experience/notes/' + id, method: 'PUT', data });
}

function deleteNote(id) {
  return request({ url: '/api/mp/experience/notes/' + id, method: 'DELETE' });
}

module.exports = { timeline, thanks, getNotes, createNote, updateNote, deleteNote };
