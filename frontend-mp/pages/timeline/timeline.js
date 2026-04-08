const experienceApi = require('../../api/experience');

Page({
  data: {
    tab: 'auto',
    list: [], page: 1, hasMore: true, loading: false,
    notes: [], notesPage: 1, notesHasMore: true, notesLoading: false,
    showNoteForm: false,
    noteTitle: '',
    noteContent: '',
    editingNoteId: null,
    noteSubmitting: false
  },

  onLoad() { this.loadTimeline(); },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ tab });
    if (tab === 'auto' && this.data.list.length === 0) this.loadTimeline();
    if (tab === 'notes' && this.data.notes.length === 0) this.loadNotes();
  },

  // ===== 系统记录 =====
  loadTimeline() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });
    experienceApi.timeline({ page: this.data.page, size: 20 }).then((res) => {
      const records = (res.data && res.data.records) || [];
      const list = this.data.page === 1 ? records : this.data.list.concat(records);
      this.setData({ list, hasMore: records.length >= 20, page: this.data.page + 1, loading: false });
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '加载失败', icon: 'none' });
      this.setData({ loading: false });
    });
  },

  // ===== 用户经历 =====
  loadNotes() {
    if (this.data.notesLoading) return;
    this.setData({ notesLoading: true });
    experienceApi.getNotes({ page: this.data.notesPage, size: 20 }).then((res) => {
      const records = (res.data && res.data.records) || [];
      const notes = this.data.notesPage === 1 ? records : this.data.notes.concat(records);
      this.setData({ notes, notesHasMore: records.length >= 20, notesPage: this.data.notesPage + 1, notesLoading: false });
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '加载失败', icon: 'none' });
      this.setData({ notesLoading: false });
    });
  },

  showAddNote() {
    this.setData({ showNoteForm: true, noteTitle: '', noteContent: '', editingNoteId: null });
  },

  editNote(e) {
    const idx = e.currentTarget.dataset.idx;
    const note = this.data.notes[idx];
    this.setData({
      showNoteForm: true,
      noteTitle: note.title,
      noteContent: note.content,
      editingNoteId: note.id
    });
  },

  hideNoteForm() {
    this.setData({ showNoteForm: false });
  },

  onNoteTitle(e) { this.setData({ noteTitle: e.detail.value }); },
  onNoteContent(e) { this.setData({ noteContent: e.detail.value }); },

  submitNote() {
    const { noteTitle, noteContent, editingNoteId } = this.data;
    if (!noteTitle.trim() || !noteContent.trim()) {
      wx.showToast({ title: '请填写标题和内容', icon: 'none' });
      return;
    }
    this.setData({ noteSubmitting: true });

    const promise = editingNoteId
      ? experienceApi.updateNote(editingNoteId, { title: noteTitle, content: noteContent })
      : experienceApi.createNote({ title: noteTitle, content: noteContent });

    promise.then(() => {
      wx.showToast({ title: editingNoteId ? '已保存' : '已发布' });
      this.setData({ showNoteForm: false, noteSubmitting: false, notes: [], notesPage: 1, notesHasMore: true });
      this.loadNotes();
    }).catch((err) => {
      wx.showToast({ title: (err && err.message) || '操作失败', icon: 'none' });
      this.setData({ noteSubmitting: false });
    });
  },

  deleteNote(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '删除后不可恢复',
      success: (res) => {
        if (!res.confirm) return;
        experienceApi.deleteNote(id).then(() => {
          wx.showToast({ title: '已删除' });
          this.setData({ notes: this.data.notes.filter(n => n.id !== id) });
        }).catch((err) => {
          wx.showToast({ title: (err && err.message) || '删除失败', icon: 'none' });
        });
      }
    });
  },

  onReachBottom() {
    if (this.data.tab === 'auto') this.loadTimeline();
    else this.loadNotes();
  }
});
