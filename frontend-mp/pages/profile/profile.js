const app = getApp();
const authApi = require('../../api/auth');
const flowerApi = require('../../api/flower');
const badgeApi = require('../../api/badge');
const experienceApi = require('../../api/experience');

Page({
  data: {
    user: null,
    summary: { redFlowerTotal: 0, badgeLevel: 0 },
    badges: [],
    myBadges: [],
    thanks: [],
    loading: true
  },

  onShow() {
    if (!app.globalData.token) {
      wx.reLaunch({ url: '/pages/index/index' });
      return;
    }
    this.loadAll();
  },

  loadAll() {
    this.setData({ loading: true });
    return Promise.all([
      authApi.getProfile().then(r => r.data),
      flowerApi.summary().then(r => r.data),
      badgeApi.my().then(r => r.data),
      experienceApi.thanks().then(r => (r && r.data) || [])
    ]).then(([user, summary, badgeData, thanks]) => {
      const badges = (badgeData && badgeData.badges) || [];
      const myBadges = (badgeData && badgeData.myBadges) || [];
      const myLevels = myBadges.map(b => b.badgeLevel);
      badges.forEach(b => { b.owned = myLevels.includes(b.level); });
      this.setData({
        user,
        summary: summary || { redFlowerTotal: 0, badgeLevel: 0 },
        badges,
        myBadges,
        thanks: Array.isArray(thanks) ? thanks : [],
        loading: false
      });
    }).catch(() => this.setData({ loading: false }));
  },

  onPullDownRefresh() {
    this.loadAll().then(() => wx.stopPullDownRefresh());
  },

  goEditProfile() {
    wx.navigateTo({ url: '/pages/edit-profile/edit-profile' });
  },

  goFlowerLog() {
    wx.navigateTo({ url: '/pages/flower-log/flower-log' });
  },

  goBadgeRule() {
    wx.navigateTo({ url: '/pages/badge-rules/badge-rules' });
  },

  goTimeline() {
    wx.navigateTo({ url: '/pages/timeline/timeline' });
  },

  goContacts() {
    wx.navigateTo({ url: '/pages/contacts/contacts' });
  },

  goMyHelps() {
    wx.navigateTo({ url: '/pages/my-helps/my-helps' });
  },

  goTipRecords() {
    wx.navigateTo({ url: '/pages/tip-records/tip-records' });
  },

  goPrivacy() {
    wx.navigateTo({ url: '/pages/privacy/privacy' });
  },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定退出吗？',
      success: (res) => {
        if (res.confirm) {
          app.clearToken();
          wx.reLaunch({ url: '/pages/index/index' });
        }
      }
    });
  }
});
