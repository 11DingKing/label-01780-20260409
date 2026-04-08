import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useUserStore } from './user';

describe('useUserStore', () => {
  beforeEach(() => {
    vi.stubGlobal('localStorage', {
      getItem: vi.fn(() => null),
      setItem: vi.fn(),
      removeItem: vi.fn()
    });
    setActivePinia(createPinia());
  });

  it('initial token is empty when localStorage has nothing', () => {
    const store = useUserStore();
    expect(store.token).toBe('');
  });

  it('setToken saves to localStorage and updates token', () => {
    const store = useUserStore();
    store.setToken('abc');
    expect(store.token).toBe('abc');
    expect(localStorage.setItem).toHaveBeenCalledWith('admin_token', 'abc');
  });

  it('setToken empty removes from localStorage', () => {
    const store = useUserStore();
    store.setToken('x');
    store.setToken('');
    expect(store.token).toBe('');
    expect(localStorage.removeItem).toHaveBeenCalledWith('admin_token');
  });
});
