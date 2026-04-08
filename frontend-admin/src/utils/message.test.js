import { describe, it, expect, vi, beforeEach } from 'vitest';
import { toast, toastSuccess, toastWarning, toastError, confirm } from './message';

vi.mock('element-plus', () => ({
  ElMessage: vi.fn(() => ({ close: vi.fn() })),
  ElMessageBox: vi.fn(() => Promise.resolve())
}));

describe('message utils', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('toast calls ElMessage with message and customClass', async () => {
    const { ElMessage } = await import('element-plus');
    toast('hello');
    expect(ElMessage).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'hello',
        type: 'info',
        customClass: 'app-message',
        duration: 2800,
        showClose: true,
        offset: 24
      })
    );
  });

  it('toastSuccess uses type success', async () => {
    const { ElMessage } = await import('element-plus');
    toastSuccess('成功');
    expect(ElMessage).toHaveBeenCalledWith(expect.objectContaining({ type: 'success', message: '成功' }));
  });

  it('toastWarning uses type warning', async () => {
    const { ElMessage } = await import('element-plus');
    toastWarning('警告');
    expect(ElMessage).toHaveBeenCalledWith(expect.objectContaining({ type: 'warning', message: '警告' }));
  });

  it('toastError uses type error', async () => {
    const { ElMessage } = await import('element-plus');
    toastError('错误');
    expect(ElMessage).toHaveBeenCalledWith(expect.objectContaining({ type: 'error', message: '错误' }));
  });

  it('confirm calls ElMessageBox with customClass and default texts', async () => {
    const { ElMessageBox } = await import('element-plus');
    confirm('确定删除？');
    expect(ElMessageBox).toHaveBeenCalledWith(
      expect.objectContaining({
        title: '提示',
        message: '确定删除？',
        customClass: 'app-message-box',
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    );
  });

  it('confirm accepts options override', async () => {
    const { ElMessageBox } = await import('element-plus');
    confirm('内容', '标题', { confirmText: '是的', cancelText: '不了' });
    expect(ElMessageBox).toHaveBeenCalledWith(
      expect.objectContaining({
        title: '标题',
        message: '内容',
        confirmButtonText: '是的',
        cancelButtonText: '不了'
      })
    );
  });
});
