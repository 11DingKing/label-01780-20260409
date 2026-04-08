<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <div class="login-container">
      <div class="login-hero">
        <div class="hero-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" fill="currentColor"/>
          </svg>
        </div>
        <h1 class="hero-title">互助求助</h1>
        <p class="hero-desc">紧急互助管理平台</p>
      </div>

      <div class="login-card">
        <h2 class="card-title">登录管理后台</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" size="large" clearable>
              <template #prefix>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="submit">
              <template #prefix>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              </template>
            </el-input>
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="submit">
            {{ loading ? '登录中...' : '进入后台' }}
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../api/auth';
import { useUserStore } from '../stores/user';
import { toastSuccess, toastWarning } from '../utils/message';

const router = useRouter();
const store = useUserStore();
const formRef = ref(null);
const loading = ref(false);
const form = reactive({ username: '', password: '' });
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

function submit() {
  formRef.value?.validate((valid) => {
    if (!valid) return;
    loading.value = true;
    login(form)
      .then((data) => {
        const token = data?.data?.token || data?.token;
        if (token) {
          store.setToken(token);
          toastSuccess('登录成功');
          router.replace('/');
        } else {
          toastWarning('登录失败');
        }
      })
      .finally(() => { loading.value = false; });
  });
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1A1A2E;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.bg-shape-1 {
  width: 500px;
  height: 500px;
  background: #D94F2B;
  top: -15%;
  right: -10%;
  animation: float1 12s ease-in-out infinite;
}

.bg-shape-2 {
  width: 400px;
  height: 400px;
  background: #F17A4A;
  bottom: -10%;
  left: -8%;
  animation: float2 15s ease-in-out infinite;
}

.bg-shape-3 {
  width: 250px;
  height: 250px;
  background: #F59E0B;
  top: 40%;
  left: 50%;
  opacity: 0.2;
  animation: float3 10s ease-in-out infinite;
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 20px) scale(1.05); }
}
@keyframes float2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -30px) scale(1.08); }
}
@keyframes float3 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-20px, 15px); }
}

.login-container {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: containerIn 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

@keyframes containerIn {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.login-hero {
  text-align: center;
  margin-bottom: 36px;
}

.hero-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, #D94F2B, #F17A4A);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto 20px;
  box-shadow: 0 8px 32px rgba(217, 79, 43, 0.35);
}

.hero-title {
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 6px;
  letter-spacing: 0.02em;
}

.hero-desc {
  color: rgba(255,255,255,0.45);
  font-size: 14px;
  margin: 0;
  font-weight: 400;
}

.login-card {
  width: 380px;
  background: rgba(255,255,255,0.06);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 20px;
  padding: 36px 32px;
}

.card-title {
  color: rgba(255,255,255,0.85);
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 28px;
  text-align: center;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.06) !important;
  box-shadow: 0 0 0 1px rgba(255,255,255,0.1) inset !important;
  border-radius: 10px !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255,255,255,0.2) inset !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(217, 79, 43, 0.4) inset !important;
}

.login-form :deep(.el-input__inner) {
  color: #fff !important;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255,255,255,0.35) !important;
}

.login-form :deep(.el-form-item__error) {
  color: #F17A4A !important;
}

.submit-btn {
  width: 100%;
  height: 44px !important;
  font-size: 15px !important;
  border-radius: 10px !important;
  margin-top: 8px;
}
</style>
