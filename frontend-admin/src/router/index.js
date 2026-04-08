import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '../stores/user';

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'help', name: 'Help', component: () => import('../views/HelpList.vue') },
      { path: 'users', name: 'Users', component: () => import('../views/UserList.vue') },
      { path: 'orders', name: 'Orders', component: () => import('../views/OrderList.vue') },
      { path: 'push', name: 'Push', component: () => import('../views/PushRules.vue') }
    ]
  }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const store = useUserStore();
  if (to.meta.public) return next();
  if (!store.token) return next('/login');
  next();
});

export default router;
