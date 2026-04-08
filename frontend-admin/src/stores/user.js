import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '');
  function setToken(t) {
    token.value = t;
    if (t) localStorage.setItem('admin_token', t);
    else localStorage.removeItem('admin_token');
  }
  return { token, setToken };
});
