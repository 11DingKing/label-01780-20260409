const contactApi = require('../../api/contact');

Page({
  data: {
    contacts: [],
    loading: true,
    showAdd: false,
    editId: null,
    form: { nameEnc: '', phoneEnc: '', relation: '' }
  },

  onLoad() { this.load(); },

  load() {
    this.setData({ loading: true });
    contactApi.list().then((res) => {
      this.setData({ contacts: res.data || [], loading: false });
    }).catch(() => this.setData({ loading: false }));
  },

  showAddForm() {
    this.setData({ showAdd: true, editId: null, form: { nameEnc: '', phoneEnc: '', relation: '' } });
  },

  editContact(e) {
    const c = this.data.contacts.find(i => i.id === e.currentTarget.dataset.id);
    if (!c) return;
    this.setData({
      showAdd: true,
      editId: c.id,
      form: { nameEnc: c.nameEnc || '', phoneEnc: c.phoneEnc || '', relation: c.relation || '' }
    });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ ['form.' + field]: e.detail.value });
  },

  save() {
    const { form, editId } = this.data;
    if (!form.nameEnc && !form.phoneEnc) {
      wx.showToast({ title: '请填写姓名或电话', icon: 'none' });
      return;
    }
    const promise = editId ? contactApi.update(editId, form) : contactApi.add(form);
    promise.then(() => {
      wx.showToast({ title: editId ? '已更新' : '已添加' });
      this.setData({ showAdd: false });
      this.load();
    });
  },

  cancel() {
    this.setData({ showAdd: false });
  },

  deleteContact(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定删除该联系人？',
      success: (res) => {
        if (res.confirm) {
          contactApi.remove(id).then(() => {
            wx.showToast({ title: '已删除' });
            this.load();
          });
        }
      }
    });
  }
});
