const helpApi = require('../../api/help');
const app = getApp();

// 默认位置：北京天安门（用于开发调试或定位失败时）
var DEFAULT_LAT = 39.9042;
var DEFAULT_LNG = 116.4074;

Page({
  data: {
    list: [],
    loading: false,
    lat: null,
    lng: null,
    locationTip: ''
  },

  onLoad() {
    if (!app.globalData.token) {
      wx.login({
        success: (res) => {
          if (res.code) {
            require('../../api/auth').login(res.code).then((d) => {
              if (d.data && d.data.token) {
                app.setToken(d.data.token);
                this.getLocationAndLoad();
              }
            });
          }
        }
      });
      return;
    }
    this.getLocationAndLoad();
  },

  onShow() {
    if (app.globalData.token && this.data.lat) this.fetchNearby();
  },

  onPullDownRefresh() {
    this.getLocationAndLoad().then(function() { wx.stopPullDownRefresh(); });
  },

  getLocationAndLoad() {
    var that = this;
    return new Promise(function(resolve) {
      wx.getLocation({
        type: 'gcj02',
        success: function(res) {
          that.setData({ lat: res.latitude, lng: res.longitude, locationTip: '' });
          that.fetchNearby().then(resolve);
        },
        fail: function() {
          // 定位失败，使用默认位置（北京）
          that.setData({
            lat: DEFAULT_LAT,
            lng: DEFAULT_LNG,
            locationTip: '未获取到位置，显示默认区域(北京)的求助'
          });
          that.fetchNearby().then(resolve);
        }
      });
    });
  },

  fetchNearby() {
    var that = this;
    that.setData({ loading: true });
    return helpApi.nearby({ lat: that.data.lat, lng: that.data.lng, radiusKm: 50, limit: 50 })
      .then(function(data) {
        that.setData({ list: (data.data || []), loading: false });
      })
      .catch(function() {
        that.setData({ loading: false });
      });
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/help-detail/help-detail?id=' + e.currentTarget.dataset.id });
  },

  goPublish() {
    wx.switchTab({ url: '/pages/publish/publish' });
  }
});
