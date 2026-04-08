const app = getApp();
const { baseUrl } = require('./config');

function request(options) {
  const url = (options.url && options.url.startsWith('http')) ? options.url : baseUrl + (options.url || '');
  const header = {
    'Content-Type': 'application/json',
    ...(options.header || {})
  };
  if (app.globalData.token) {
    header.Authorization = 'Bearer ' + app.globalData.token;
  }
  return new Promise((resolve, reject) => {
    wx.request({
      ...options,
      url,
      header,
      success(res) {
        if (res.statusCode === 401) {
          app.clearToken();
          wx.reLaunch({ url: '/pages/index/index' });
          reject(new Error('请重新进入'));
          return;
        }
        if (res.data && res.data.code !== undefined && res.data.code !== 200) {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' });
          reject(res.data);
          return;
        }
        resolve(res.data);
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = { request };
