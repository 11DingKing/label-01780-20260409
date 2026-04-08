import { describe, it, expect } from 'vitest';
import { formatDate } from './format';

describe('formatDate', () => {
  it('returns — for null or empty', () => {
    expect(formatDate(null)).toBe('—');
    expect(formatDate(undefined)).toBe('—');
    expect(formatDate('')).toBe('—');
  });

  it('returns — for invalid date string', () => {
    expect(formatDate('not-a-date')).toBe('—');
  });

  it('formats date without time', () => {
    expect(formatDate('2024-02-21')).toBe('2024年2月21日');
    expect(formatDate('2024-12-01')).toBe('2024年12月1日');
    expect(formatDate(new Date(2024, 0, 15))).toBe('2024年1月15日');
  });

  it('formats date with time when withTime true', () => {
    const str = formatDate('2024-02-21T14:30:00', true);
    expect(str).toMatch(/2024年2月21日 \d{2}:\d{2}/);
    expect(str).toContain('2024年2月21日');
  });

  it('pads hours and minutes with zero', () => {
    const d = new Date(2024, 1, 21, 9, 5);
    const str = formatDate(d, true);
    expect(str).toBe('2024年2月21日 09:05');
  });
});
