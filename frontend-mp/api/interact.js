const { request } = require('../utils/request');

function bless(helpId) {
  return request({ url: '/api/mp/interact/bless?helpId=' + helpId, method: 'POST' });
}
function share(helpId) {
  return request({ url: '/api/mp/interact/share?helpId=' + helpId, method: 'POST' });
}

module.exports = { bless, share };
