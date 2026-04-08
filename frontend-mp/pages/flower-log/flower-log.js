const flowerApi = require('../../api/flower');

Page({
  data: { list: [], page: 1, hasMore: true, loading: false },

  onLoad() {
    this.load();
  },

  load() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });
    flowerApi.logs({ page: this.data.page, size: 20 }).then((res) => {
      const records = (res.data && res.data.records) || [];
      const list = this.data.page === 1 ? records : this.data.list.concat(records);
      this.setData({
        list,
        hasMore: records.length >= 20,
        page: this.data.page + 1,
        loading: false
      });
    }).catch(() => this.setData({ loading: false }));
  },

  onReachBottom() { this.load(); }
});
