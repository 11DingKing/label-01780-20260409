<template>
  <div class="page-wrap">
    <div class="page-header">
      <h1 class="page-title">求助列表</h1>
      <el-select v-model="statusFilter" placeholder="全部状态" clearable size="default" style="width: 130px" @change="onFilterChange">
        <el-option label="已发布" :value="1" />
        <el-option label="已关闭" :value="2" />
        <el-option label="已删除" :value="0" />
      </el-select>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column label="紧急程度">
          <template #default="{ row }">{{ row.urgencyLevel === 1 ? '高' : row.urgencyLevel === 2 ? '中' : '低' }}</template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="address" label="位置" show-overflow-tooltip />
        <el-table-column label="状态">
          <template #default="{ row }">{{ row.status === 1 ? '已发布' : row.status === 2 ? '已关闭' : '已删除' }}</template>
        </el-table-column>
        <el-table-column label="发布时间">
          <template #default="{ row }">{{ formatDate(row.publishTime, true) }}</template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <span class="act" v-if="row.status===1" @click="changeStatus(row.id,2)">关闭</span>
            <span class="act" v-if="row.status===2" @click="changeStatus(row.id,1)">恢复</span>
            <span class="act del" v-if="row.status!==0" @click="changeStatus(row.id,0)">删除</span>
            <span class="act" v-if="row.status===1" @click="triggerPush(row.id)">推送</span>
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
import { list as fetchList, updateStatus } from '../api/help';
import { triggerPush as apiTriggerPush } from '../api/push';
import { formatDate } from '../utils/format';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false), tableData = ref([]), page = ref(1), size = ref(20), total = ref(0), statusFilter = ref(null);

function fetch() {
  loading.value = true;
  const p = { page: page.value, size: size.value };
  if (statusFilter.value !== null && statusFilter.value !== '') p.status = statusFilter.value;
  fetchList(p).then(r => { const d = r?.data||r; tableData.value = d?.records||[]; total.value = d?.total||0; }).finally(() => loading.value = false);
}
function onSizeChange(v) { size.value = v; page.value = 1; fetch(); }
function onFilterChange() { page.value = 1; fetch(); }
function changeStatus(id, s) {
  const l = { 0:'删除', 1:'恢复', 2:'关闭' };
  ElMessageBox.confirm(`确定${l[s]}该求助？`,'确认').then(() => updateStatus(id,s).then(() => { ElMessage.success('操作成功'); fetch(); })).catch(()=>{});
}
function triggerPush(id) {
  ElMessageBox.confirm('确定推送？','确认').then(() => apiTriggerPush(id).then(() => ElMessage.success('推送已触发'))).catch(()=>{});
}
onMounted(fetch);
</script>

<style scoped>
.page-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.page-title { font-size:20px; font-weight:700; margin:0; }
.table-card { background:#fff; border-radius:8px; border:1px solid #eee; overflow:hidden; }
.pager { display:flex; justify-content:flex-end; padding:12px 16px; border-top:1px solid #f0f0f0; }
.act { color:var(--c-brand); cursor:pointer; margin-right:12px; font-size:13px; }
.act:hover { opacity:0.7; }
.act.del { color:#DC2626; }
</style>
