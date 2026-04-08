const tipApi = require('../../api/tip');

Page({
  data: {
    list: [],
    loading: false,
    page: 1,
    size: 20,
    hasMore: false
  },

  onLoad() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.setData({ page: 1, list: [] });
    this.loadData().then(() => wx.stopPullDownRefresh());
  },

  loadData() {
    this.setData({ loading: true });
    return tipApi.records({ page: this.data.page, size: this.data.size }).then(res => {
      const data = res.data || res;
      const records = (data.records || data || []).map(item => ({
        ...item,
        amountYuan: (item.amountCents / 100).toFixed(2),
        statusText: item.status === 1 ? '已支付' : item.status === 0 ? '待支付' : item.status === 2 ? '已关闭' : '已退款',
        createTimeStr: (item.createTime || '').replace('T', ' ').substring(0, 16)
      }));
      const list = this.data.page === 1 ? records : this.data.list.concat(records);
      this.setData({
        list,
        loading: false,
        hasMore: records.length >= this.data.size
      });
    }).catch(() => this.setData({ loading: false }));
  },

  loadMore() {
    this.setData({ page: this.data.page + 1 });
    this.loadData();
  }
});
