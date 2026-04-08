const app = getApp();

Page({
  data: { step: 0 },

  onSwipe(e) {
    this.setData({ step: e.detail.current });
  },

  start() {
    wx.setStorageSync('guide_done', '1');
    wx.switchTab({ url: '/pages/index/index' });
  },

  goPrivacy() {
    wx.navigateTo({ url: '/pages/privacy/privacy' });
  }
});
