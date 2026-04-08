const helpApi = require('../../api/help');
const interactApi = require('../../api/interact');
const tipApi = require('../../api/tip');
const app = getApp();

Page({
  data: {
    id: null,
    help: null,
    images: [],
    contact: null,
    hasBlessed: false,
    hasShared: false,
    loading: true,
    blessLoading: false,
    shareLoading: false,
    tipLoading: false,
    blessAnim: false,
    shareAnim: false,
    tipAnim: false,
    showFlowerRain: false,
    flowerItems: [],
    // 打赏面板
    showTip: false,
    tipAmounts: [1, 5, 10, 20, 50, 100],
    selectedAmount: 5,
    customAmount: '',
    finalAmount: 5
  },

  onLoad(options) {
    const id = options.id;
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      return;
    }
    this.setData({ id });
    this.loadDetail();
  },

  loadDetail() {
    helpApi.detail(this.data.id).then((res) => {
      const help = (res.data && res.data.help) || res.data;
      const images = (res.data && res.data.images) || [];
      const contact = (res.data && res.data.contact) || null;
      if (!help) return;
      this.setData({ help, images, contact, loading: false });
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '加载失败，请重试', icon: 'none' });
      this.setData({ loading: false });
    });
  },

  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    const urls = this.data.images.map(i => i.url);
    wx.previewImage({ current: url, urls });
  },

  // 分享给好友
  onShareAppMessage() {
    const help = this.data.help || {};
    const content = (help.content || '').substring(0, 30);
    return {
      title: '紧急求助：' + content,
      path: '/pages/help-detail/help-detail?id=' + this.data.id,
      imageUrl: this.data.images.length > 0 ? this.data.images[0].url : ''
    };
  },

  // 分享到朋友圈
  onShareTimeline() {
    const help = this.data.help || {};
    const content = (help.content || '').substring(0, 30);
    return {
      title: '紧急求助：' + content,
      query: 'id=' + this.data.id
    };
  },

  triggerFlowerRain(count) {
    const items = [];
    for (let i = 0; i < (count > 10 ? 10 : count + 5); i++) {
      items.push({ left: Math.random() * 90 + 5, delay: Math.random() * 1.5 });
    }
    this.setData({ showFlowerRain: true, flowerItems: items });
    setTimeout(() => this.setData({ showFlowerRain: false, flowerItems: [] }), 3000);
  },

  bless() {
    if (this.data.blessLoading || this.data.hasBlessed) return;
    this.setData({ blessLoading: true });
    interactApi.bless(this.data.id).then(() => {
      this.setData({ hasBlessed: true, blessLoading: false, blessAnim: true });
      wx.showToast({ title: '祝福成功 +1🌸', icon: 'none' });
      this.triggerFlowerRain(1);
      setTimeout(() => this.setData({ blessAnim: false }), 600);
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '祝福失败，请稍后重试', icon: 'none' });
      this.setData({ blessLoading: false });
    });
  },

  share() {
    if (this.data.shareLoading || this.data.hasShared) return;
    this.setData({ shareLoading: true });
    interactApi.share(this.data.id).then(() => {
      this.setData({ hasShared: true, shareLoading: false, shareAnim: true });
      wx.showToast({ title: '转发成功 +2🌸', icon: 'none' });
      this.triggerFlowerRain(2);
      setTimeout(() => this.setData({ shareAnim: false }), 600);
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '转发失败，请稍后重试', icon: 'none' });
      this.setData({ shareLoading: false });
    });
  },

  showTipPanel() {
    this.setData({ showTip: true });
  },

  hideTipPanel() {
    this.setData({ showTip: false });
  },

  selectAmount(e) {
    const amount = parseInt(e.currentTarget.dataset.amount, 10);
    this.setData({ selectedAmount: amount, customAmount: '', finalAmount: amount });
  },

  onCustomAmount(e) {
    const val = parseFloat(e.detail.value);
    if (val && val >= 1 && val <= 100) {
      this.setData({ customAmount: e.detail.value, selectedAmount: null, finalAmount: Math.floor(val) });
    } else {
      this.setData({ customAmount: e.detail.value });
    }
  },

  confirmTip() {
    const amount = this.data.finalAmount;
    if (!amount || amount < 1 || amount > 100) {
      wx.showToast({ title: '金额需在1-100元之间', icon: 'none' });
      return;
    }
    if (this.data.tipLoading) return;
    this.setData({ tipLoading: true });
    const amountCents = amount * 100;

    tipApi.create({ helpId: parseInt(this.data.id, 10), amountCents }).then((res) => {
      const data = res.data || res;
      const payParams = data.payParams || {};
      this.setData({ showTip: false });

      // 沙箱模式：支付未配置时后端自动完成，前端直接展示成功
      if (payParams.sandbox === 'true') {
        wx.showToast({ title: '(沙箱)打赏成功 +' + amount + '🌸', icon: 'none' });
        this.setData({ tipLoading: false, tipAnim: true });
        this.triggerFlowerRain(amount);
        setTimeout(() => this.setData({ tipAnim: false }), 600);
        return;
      }

      wx.requestPayment({
        timeStamp: payParams.timeStamp,
        nonceStr: payParams.nonceStr,
        package: payParams.package,
        signType: payParams.signType || 'RSA',
        paySign: payParams.paySign,
        success: () => {
          wx.showToast({ title: '打赏成功 +' + amount + '🌸', icon: 'none' });
          this.setData({ tipLoading: false, tipAnim: true });
          this.triggerFlowerRain(amount);
          setTimeout(() => this.setData({ tipAnim: false }), 600);
        },
        fail: (err) => {
          if (err.errMsg && err.errMsg.indexOf('cancel') === -1) {
            wx.showToast({ title: '支付失败', icon: 'none' });
          }
          this.setData({ tipLoading: false });
        }
      });
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '创建打赏订单失败', icon: 'none' });
      this.setData({ tipLoading: false });
    });
  }
});
