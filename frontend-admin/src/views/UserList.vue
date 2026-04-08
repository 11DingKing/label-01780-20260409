<template>
  <div class="page-wrap">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <span class="page-count">共 {{ total }} 位用户</span>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="nickName" label="昵称" show-overflow-tooltip />
        <el-table-column label="小红花">
          <template #default="{ row }">{{ row.redFlowerTotal ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="勋章等级">
          <template #default="{ row }">{{ row.badgeLevel > 0 ? 'Lv.' + row.badgeLevel : '—' }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">{{ row.status === 1 ? '正常' : '禁用' }}</template>
        </el-table-column>
        <el-table-column label="注册时间">
          <template #default="{ row }">{{ formatDate(row.createTime, true) }}</template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <span class="act del" v-if="row.status===1" @click="toggleStatus(row.id,0)">封禁</span>
            <span class="act ok" v-else @click="toggleStatus(row.id,1)">解封</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @current-change="fetch" @size-change="onSizeChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { list as fetchList, updateStatus } from '../api/user';
import { formatDate } from '../utils/format';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false), tableData = ref([]), page = ref(1), size = ref(20), total = ref(0);

function fetch() {
  loading.value = true;
  fetchList({ page: page.value, size: size.value }).then(r => { const d = r?.data||r; tableData.value = d?.records||[]; total.value = d?.total||0; }).finally(() => loading.value = false);
}
function onSizeChange(v) { size.value = v; page.value = 1; fetch(); }
function toggleStatus(id, s) {
  const l = s === 0 ? '封禁' : '解封';
  ElMessageBox.confirm(`确定${l}该用户？`,'确认').then(() => updateStatus(id,s).then(() => { ElMessage.success('操作成功'); fetch(); })).catch(()=>{});
}
onMounted(fetch);
</script>

<style scoped>
.page-header { display:flex; align-items:baseline; justify-content:space-between; margin-bottom:16px; }
.page-title { font-size:20px; font-weight:700; margin:0; }
.page-count { font-size:13px; color:#999; }
.table-card { background:#fff; border-radius:8px; border:1px solid #eee; overflow:hidden; }
.pager { display:flex; justify-content:flex-end; padding:12px 16px; border-top:1px solid #f0f0f0; }
.act { cursor:pointer; margin-right:12px; font-size:13px; }
.act:hover { opacity:0.7; }
.act.del { color:#DC2626; }
.act.ok { color:#059669; }
</style>
