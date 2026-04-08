const authApi = require('../../api/auth');
const app = getApp();

Page({
  data: {
    nickName: '',
    avatarUrl: '',
    phoneEnc: '',
    phoneAnon: false,
    saving: false
  },

  onLoad() {
    this.loadProfile();
  },

  loadProfile() {
    authApi.getProfile().then((res) => {
      const u = res.data || {};
      this.setData({
        nickName: u.nickName || '',
        avatarUrl: u.avatarUrl || '',
        phoneEnc: u.phoneEnc || '',
        phoneAnon: u.phoneAnon === 1
      });
    });
  },

  onNickInput(e) { this.setData({ nickName: e.detail.value }); },
  onPhoneInput(e) { this.setData({ phoneEnc: e.detail.value }); },
  onPhoneAnonChange(e) { this.setData({ phoneAnon: e.detail.value }); },

  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.uploadAvatar(res.tempFiles[0].tempFilePath);
      },
      fail: () => {}
    });
  },

  uploadAvatar(path) {
    // 先显示本地临时图片
    this.setData({ avatarUrl: path });
    wx.uploadFile({
      url: app.globalData.baseUrl + '/api/mp/upload/image',
      filePath: path,
      name: 'file',
      header: { Authorization: 'Bearer ' + (app.globalData.token || '') },
      success: (uploadRes) => {
        try {
          var d = JSON.parse(uploadRes.data);
          if (d.code === 200 && d.data) {
            this.setData({ avatarUrl: d.data });
          }
        } catch (e) {
          // 上传解析失败，保留本地临时图片
        }
      },
      fail: () => {
        wx.showToast({ title: '头像上传失败，请检查网络', icon: 'none' });
      }
    });
  },

  save() {
    this.setData({ saving: true });
    authApi.updateProfile({
      nickName: this.data.nickName || undefined,
      avatarUrl: this.data.avatarUrl || undefined,
      phoneEnc: this.data.phoneEnc || undefined,
      phoneAnon: this.data.phoneAnon ? 1 : 0
    }).then(() => {
      wx.showToast({ title: '保存成功' });
      setTimeout(() => wx.navigateBack(), 1000);
    }).finally(() => this.setData({ saving: false }));
  }
});
