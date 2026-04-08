<template>
  <div class="page-wrap">
    <div class="page-header">
      <h1 class="page-title">数据概览</h1>
      <span class="page-time">{{ currentDate }}</span>
    </div>

    <div class="stats-grid">
      <div class="stat-card" v-for="(item, i) in mainStats" :key="i" :style="{ animationDelay: i * 0.06 + 's' }">
        <div class="stat-top">
          <div class="stat-icon" :style="{ background: item.bg }">
            <span v-html="item.icon"></span>
          </div>
          <div class="stat-trend" v-if="item.trend">{{ item.trend }}</div>
        </div>
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-sub" v-if="item.sub">{{ item.sub }}</div>
      </div>
    </div>

    <div class="section-header">
      <h2 class="section-title">接口性能</h2>
      <span class="section-badge">最近 {{ perf.hours ?? 24 }}h</span>
    </div>

    <div class="perf-grid">
      <div class="perf-card" v-for="(item, i) in perfStats" :key="i" :style="{ animationDelay: (i * 0.06 + 0.3) + 's' }">
        <div class="perf-value">
          {{ item.value }}<span class="perf-unit">{{ item.unit }}</span>
        </div>
        <div class="perf-label">{{ item.label }}</div>
        <div class="perf-bar" v-if="item.bar !== undefined">
          <div class="perf-bar-fill" :style="{ width: item.bar + '%', background: item.barColor }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getStats, getPerfStats } from '../api/dashboard';

const stats = ref({});
const perf = ref({});

const currentDate = computed(() => {
  const d = new Date();
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日`;
});

const mainStats = computed(() => [
  {
    label: '总用户数',
    value: stats.value.totalUsers ?? '—',
    sub: `今日新增 ${stats.value.todayUsers ?? 0}`,
    bg: 'linear-gradient(135deg, #6366F1, #818CF8)',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
    trend: stats.value.todayUsers > 0 ? `+${stats.value.todayUsers}` : null
  },
  {
    label: '总求助数',
    value: stats.value.totalHelps ?? '—',
    sub: `进行中 ${stats.value.activeHelps ?? 0} · 今日 ${stats.value.todayHelps ?? 0}`,
    bg: 'linear-gradient(135deg, #D94F2B, #F17A4A)',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>'
  },
  {
    label: '打赏笔数',
    value: stats.value.totalOrders ?? '—',
    sub: `累计 ¥${stats.value.totalAmountYuan ?? 0}`,
    bg: 'linear-gradient(135deg, #F59E0B, #FBBF24)',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>'
  },
  {
    label: '推送次数',
    value: stats.value.pushCount ?? '—',
    sub: `平均到达率 ${stats.value.avgReachRate ?? 0}%`,
    bg: stats.value.avgReachRate < 95 ? 'linear-gradient(135deg, #EF4444, #F87171)' : 'linear-gradient(135deg, #10B981, #34D399)',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>',
    trend: stats.value.avgReachRate < 95 ? '⚠ 低于95%' : null
  }
]);

const perfStats = computed(() => {
  const avg = perf.value.avgMs ?? 0;
  const p95 = perf.value.p95Ms ?? 0;
  const p99 = perf.value.p99Ms ?? 0;
  return [
    { label: '平均响应', value: avg || '—', unit: avg ? 'ms' : '', bar: Math.min(avg / 5, 100), barColor: avg > 500 ? '#EF4444' : '#10B981' },
    { label: 'P95 响应', value: p95 || '—', unit: p95 ? 'ms' : '', bar: Math.min(p95 / 5, 100), barColor: p95 > 500 ? '#F59E0B' : '#10B981' },
    { label: 'P99 响应', value: p99 || '—', unit: p99 ? 'ms' : '', bar: Math.min(p99 / 10, 100), barColor: p99 > 1500 ? '#EF4444' : '#6366F1' },
    { label: '慢请求 (>500ms)', value: perf.value.slowCount ?? 0, unit: '次' }
  ];
});

onMounted(() => {
  getStats().then(res => { stats.value = res?.data ?? res ?? {}; });
  getPerfStats(24).then(res => { perf.value = res?.data ?? res ?? {}; });
});
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 28px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--c-text);
  margin: 0;
}
.page-time {
  font-size: 13px;
  color: var(--c-text-muted);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 36px;
}

.stat-card {
  background: var(--c-surface);
  border-radius: var(--radius-lg);
  padding: 22px;
  border: 1px solid var(--c-border);
  transition: all 0.25s ease;
  animation: cardUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) backwards;
}
.stat-card:hover {
  border-color: var(--c-border-hover);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

@keyframes cardUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-trend {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-success);
  background: rgba(16, 185, 129, 0.08);
  padding: 2px 8px;
  border-radius: 6px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--c-text);
  line-height: 1;
  margin-bottom: 6px;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 13px;
  color: var(--c-text-secondary);
  margin-bottom: 4px;
}
.stat-sub {
  font-size: 12px;
  color: var(--c-text-muted);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--c-text);
  margin: 0;
}
.section-badge {
  font-size: 11px;
  color: var(--c-text-muted);
  background: var(--c-surface-raised);
  padding: 3px 10px;
  border-radius: 20px;
  border: 1px solid var(--c-border);
}

.perf-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.perf-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--c-border);
  animation: cardUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) backwards;
}
.perf-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--c-text);
  font-variant-numeric: tabular-nums;
}
.perf-unit {
  font-size: 12px;
  font-weight: 400;
  color: var(--c-text-muted);
  margin-left: 2px;
}
.perf-label {
  font-size: 12px;
  color: var(--c-text-secondary);
  margin-top: 4px;
  margin-bottom: 12px;
}
.perf-bar {
  height: 4px;
  background: var(--c-surface-raised);
  border-radius: 2px;
  overflow: hidden;
}
.perf-bar-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.8s cubic-bezier(0.22, 1, 0.36, 1);
}

@media (max-width: 1200px) {
  .stats-grid, .perf-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
