const helpApi = require('../../api/help');
const contactApi = require('../../api/contact');
const app = getApp();

Page({
  data: {
    latitude: null,
    longitude: null,
    address: '',
    addressAnon: false,
    urgencyLevel: 2,
    content: '',
    imageUrls: [],
    contacts: [],
    selectedContactId: null,
    submitting: false,
    showContact: false,
    step: 1 // 当前步骤指示
  },

  onLoad() {
    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      setTimeout(() => wx.switchTab({ url: '/pages/index/index' }), 1500);
      return;
    }
    this.autoGetLocation();
    this.loadContacts();
  },

  onShow() {
    if (app.globalData.token) this.loadContacts();
  },

  /**
   * 自动获取位置：首次使用弹出隐私说明，之后静默获取
   */
  autoGetLocation() {
    const locationConsented = wx.getStorageSync('location_consented');
    if (locationConsented) {
      this._doGetLocation();
    } else {
      wx.showModal({
        title: '位置授权说明',
        content: '我们需要获取您的位置以标注求助地点并推送给附近用户。位置仅用于本次求助，可选择匿名。',
        confirmText: '允许',
        cancelText: '暂不',
        success: (res) => {
          if (res.confirm) {
            wx.setStorageSync('location_consented', '1');
            this._doGetLocation();
          }
        }
      });
    }
  },

  _doGetLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({ latitude: res.latitude, longitude: res.longitude });
        this.reverseGeocode(res.latitude, res.longitude);
      },
      fail: () => {
        wx.showToast({ title: '请在设置中允许获取位置', icon: 'none' });
      }
    });
  },

  getLocation() {
    wx.setStorageSync('location_consented', '1');
    this._doGetLocation();
  },

  reverseGeocode(lat, lng) {
    const mapKey = app.globalData.mapKey || '';
    if (!mapKey) {
      this.setData({ address: '经度' + lng.toFixed(4) + ' 纬度' + lat.toFixed(4) });
      return;
    }
    wx.request({
      url: 'https://apis.map.qq.com/ws/geocoder/v1/',
      data: { location: lat + ',' + lng, key: mapKey },
      success: (res) => {
        if (res.data && res.data.result && res.data.result.address) {
          this.setData({ address: res.data.result.address });
        }
      },
      fail: () => {
        this.setData({ address: '经度' + lng.toFixed(4) + ' 纬度' + lat.toFixed(4) });
      }
    });
  },

  loadContacts() {
    contactApi.list().then((res) => {
      this.setData({ contacts: (res.data || []) });
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '加载联系人失败', icon: 'none' });
    });
  },

  onContentInput(e) {
    const content = e.detail.value;
    this.setData({ content, step: content.trim() ? 3 : 2 });
  },

  setUrgency(e) {
    this.setData({
      urgencyLevel: parseInt(e.currentTarget.dataset.level, 10),
      step: this.data.content.trim() ? 3 : 2
    });
  },

  onAnonChange(e) {
    this.setData({ addressAnon: e.detail.value });
  },

  toggleContact() {
    this.setData({ showContact: !this.data.showContact });
  },

  selectContact(e) {
    const id = e.currentTarget.dataset.id;
    this.setData({ selectedContactId: this.data.selectedContactId === id ? null : id });
  },

  goManageContacts() {
    wx.navigateTo({ url: '/pages/contacts/contacts' });
  },

  chooseImage() {
    wx.chooseMedia({
      count: 9 - this.data.imageUrls.length,
      mediaType: ['image'],
      success: (res) => {
        const tempFiles = (res.tempFiles || []).map(f => f.tempFilePath);
        this.setData({ imageUrls: this.data.imageUrls.concat(tempFiles) });
      }
    });
  },

  removeImage(e) {
    const idx = e.currentTarget.dataset.idx;
    const urls = this.data.imageUrls.filter((_, i) => i !== idx);
    this.setData({ imageUrls: urls });
  },

  submit() {
    const { latitude, longitude, content, imageUrls } = this.data;
    if (!content || !content.trim()) {
      wx.showToast({ title: '请填写求助描述', icon: 'none' });
      return;
    }
    if (!latitude || !longitude) {
      wx.showToast({ title: '请允许获取位置', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });

    const upload = (path) => {
      return new Promise((resolve, reject) => {
        wx.uploadFile({
          url: app.globalData.baseUrl + '/api/mp/upload/image',
          filePath: path,
          name: 'file',
          header: { Authorization: 'Bearer ' + app.globalData.token },
          success: (res) => {
            try {
              const d = JSON.parse(res.data);
              if (d.code === 200 && d.data) resolve(d.data);
              else reject(new Error(d.message || '上传失败'));
            } catch (e) { reject(e); }
          },
          fail: reject
        });
      });
    };

    let allUrls = [];
    const seq = (imageUrls || []).filter(p => p && typeof p === 'string' && !p.startsWith('http'));
    if (seq.length === 0) {
      allUrls = (imageUrls || []).filter(p => p && p.startsWith('http'));
      this.doPublish(allUrls);
      return;
    }

    Promise.all(seq.map(upload)).then((urls) => {
      allUrls = (this.data.imageUrls || []).map(p => p.startsWith('http') ? p : urls.shift()).filter(Boolean);
      this.doPublish(allUrls);
    }).catch((err) => {
      wx.showToast({ title: err.message || '上传失败', icon: 'none' });
      this.setData({ submitting: false });
    });
  },

  doPublish(imageUrls) {
    const { latitude, longitude, address, addressAnon, content, urgencyLevel, selectedContactId } = this.data;
    helpApi.publish({
      latitude,
      longitude,
      address: address || undefined,
      addressAnon: addressAnon ? 1 : 0,
      urgencyLevel,
      content: content.trim(),
      imageUrls,
      contactId: selectedContactId || undefined
    }).then(() => {
      wx.showToast({ title: '发布成功' });
      this.setData({ content: '', imageUrls: [], submitting: false, selectedContactId: null, step: 1 });
      wx.switchTab({ url: '/pages/index/index' });
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '发布失败，请稍后重试', icon: 'none' });
      this.setData({ submitting: false });
    });
  }
});
