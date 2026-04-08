<template>
  <el-config-provider :locale="zhCn">
    <router-view v-slot="{ Component }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
</script>

<style>
.app-message {
  min-width: 300px !important;
  padding: 12px 18px !important;
  border-radius: 10px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), 0 2px 6px rgba(0, 0, 0, 0.06) !important;
  border: none !important;
  font-size: 13px !important;
  font-family: var(--font-cn) !important;
}
.app-message.el-message--success { background: #ECFDF5 !important; color: #065F46 !important; }
.app-message.el-message--warning { background: #FFFBEB !important; color: #92400E !important; }
.app-message.el-message--error { background: #FEF2F2 !important; color: #991B1B !important; }
.app-message.el-message--info { background: #F9FAFB !important; color: #374151 !important; }

.app-message-box { border-radius: var(--radius-lg) !important; box-shadow: 0 20px 60px rgba(0,0,0,0.15) !important; }
.app-message-box .el-message-box__header { padding: 24px 28px 12px !important; }
.app-message-box .el-message-box__title { font-size: 16px !important; font-weight: 600 !important; }
.app-message-box .el-message-box__content { padding: 8px 28px 20px !important; font-size: 14px !important; }
.app-message-box .el-message-box__btns { padding: 0 28px 24px !important; }
</style>
