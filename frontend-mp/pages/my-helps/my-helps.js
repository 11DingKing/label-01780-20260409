const helpApi = require('../../api/help');

Page({
  data: { list: [], page: 1, hasMore: true, loading: false },

  onLoad() { this.load(); },

  load() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });
    helpApi.myList({ page: this.data.page, size: 20 }).then((res) => {
      const records = (res.data && res.data.records) || [];
      const list = this.data.page === 1 ? records : this.data.list.concat(records);
      this.setData({ list, hasMore: records.length >= 20, page: this.data.page + 1, loading: false });
    }).catch(() => this.setData({ loading: false }));
  },

  onReachBottom() { this.load(); },

  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true });
    this.load();
    wx.stopPullDownRefresh();
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/help-detail/help-detail?id=' + e.currentTarget.dataset.id });
  },

  closeHelp(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认关闭',
      content: '关闭后将不再接收互动',
      success: (res) => {
        if (res.confirm) {
          helpApi.close(id).then(() => {
            wx.showToast({ title: '已关闭' });
            this.setData({ page: 1, hasMore: true });
            this.load();
          });
        }
      }
    });
  }
});
