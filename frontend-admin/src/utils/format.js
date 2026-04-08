/**
 * 格式化日期：2024-02-21 或 2024-02-21 12:30
 */
export function formatDate(value, withTime = false) {
  if (value == null || value === '') return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '—';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const str = `${y}-${m}-${day}`;
  if (withTime) {
    const h = String(d.getHours()).padStart(2, '0');
    const min = String(d.getMinutes()).padStart(2, '0');
    return `${str} ${h}:${min}`;
  }
  return str;
}
