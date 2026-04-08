import { ElMessage, ElMessageBox } from 'element-plus';

const messageClass = 'app-message';
const boxClass = 'app-message-box';

/**
 * 自定义风格 Toast（基于 ElMessage，无原生弹窗）
 */
export function toast(msg, type = 'info') {
  return ElMessage({
    message: msg,
    type,
    customClass: messageClass,
    duration: 2800,
    showClose: true,
    offset: 24
  });
}

export function toastSuccess(msg) {
  return toast(msg, 'success');
}

export function toastWarning(msg) {
  return toast(msg, 'warning');
}

export function toastError(msg) {
  return toast(msg, 'error');
}

/**
 * 自定义风格确认框（替代原生 confirm）
 */
export function confirm(content, title = '提示', options = {}) {
  return ElMessageBox({
    title,
    message: content,
    customClass: boxClass,
    showCancelButton: true,
    confirmButtonText: options.confirmText || '确定',
    cancelButtonText: options.cancelText || '取消',
    type: options.type || 'warning',
    ...options
  });
}
