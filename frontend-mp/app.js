// 紧急求助互助小程序 - 入口
const config = require('./utils/config');

App({
  globalData: {
    token: '',
    userInfo: null,
    baseUrl: config.baseUrl,
    mapKey: config.mapKey || ''
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      this.fetchProfile();
    }
  },

  fetchProfile() {
    if (!this.globalData.token) return;
    wx.request({
      url: this.globalData.baseUrl + '/api/mp/auth/profile',
      method: 'GET',
      header: { Authorization: 'Bearer ' + this.globalData.token },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          this.globalData.userInfo = res.data.data;
        }
      }
    });
  },

  setToken(token) {
    this.globalData.token = token;
    wx.setStorageSync('token', token);
  },

  clearToken() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
  }
});
