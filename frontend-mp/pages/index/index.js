const helpApi = require('../../api/help');
const app = getApp();

Page({
  data: {
    list: [],
    loading: true,
    page: 1,
    hasMore: true,
    showGuide: false
  },

  onLoad() {
    if (!wx.getStorageSync('guide_done')) {
      wx.navigateTo({ url: '/pages/guide/guide' });
      return;
    }
    // 新手引导完成后，首次进入首页显示功能引导提示
    if (!wx.getStorageSync('index_guide_done')) {
      this.setData({ showGuide: true });
    }
    this.loadList(true);
  },

  onShow() {
    if (!app.globalData.token) {
      if (!wx.getStorageSync('privacy_agreed')) {
        wx.showModal({
          title: '隐私政策',
          content: '欢迎使用互助求助。在使用前，请阅读并同意我们的隐私政策。我们将依据《个人信息保护法》保护您的个人信息。',
          confirmText: '同意',
          cancelText: '查看详情',
          success: (res) => {
            if (res.confirm) {
              wx.setStorageSync('privacy_agreed', '1');
              this.doLogin();
            } else {
              wx.navigateTo({ url: '/pages/privacy/privacy' });
            }
          }
        });
        return;
      }
      this.doLogin();
      return;
    }
    this.loadList(true);
  },

  onPullDownRefresh() {
    this.loadList(true).then(function() { wx.stopPullDownRefresh(); });
  },

  onReachBottom() {
    if (this.data.loading || !this.data.hasMore) return;
    this.loadList(false);
  },

  doLogin() {
    wx.login({
      success: (res) => {
        if (!res.code) {
          wx.showToast({ title: '登录失败', icon: 'none' });
          return;
        }
        require('../../api/auth').login(res.code).then((data) => {
          if (data.data && data.data.token) {
            app.setToken(data.data.token);
            app.globalData.userInfo = data.data;
            this.loadList(true);
          }
        }).catch(function() {});
      }
    });
  },

  loadList(forceRefresh) {
    if (!app.globalData.token) return Promise.resolve();
    var page = forceRefresh ? 1 : this.data.page;
    this.setData({ loading: true });
    return helpApi.list({ page: page, size: 20 }).then((res) => {
      var records = (res.data && res.data.records) || [];
      var list = page === 1 ? records : (this.data.list || []).concat(records);
      this.setData({
        list: list,
        hasMore: records.length >= 20,
        page: page + 1,
        loading: false
      });
    }).catch(function() { this.setData({ loading: false }); }.bind(this));
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/help-detail/help-detail?id=' + e.currentTarget.dataset.id });
  },

  goPublish() {
    if (!app.globalData.token) {
      this.doLogin();
      return;
    }
    wx.switchTab({ url: '/pages/publish/publish' });
  },

  dismissGuide() {
    wx.setStorageSync('index_guide_done', '1');
    this.setData({ showGuide: false });
  }
});
