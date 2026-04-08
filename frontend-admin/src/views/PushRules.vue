<template>
  <div class="page-wrap">
    <div class="page-header">
      <h1 class="page-title">推送规则</h1>
      <el-button type="primary" size="default" @click="showAddDialog">+ 新增规则</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="rules" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="name" label="名称" show-overflow-tooltip />
        <el-table-column label="半径(km)">
          <template #default="{ row }">{{ row.radiusKm }}</template>
        </el-table-column>
        <el-table-column label="紧急程度">
          <template #default="{ row }">{{ (row.urgencyLevels||'').split(',').map(l => l==='1'?'高':l==='2'?'中':'低').join('、') }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">{{ row.enabled === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="创建时间">
          <template #default="{ row }">{{ formatDate(row.createTime, true) }}</template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <span class="act" @click="editRule(row)">编辑</span>
            <span class="act del" @click="removeRule(row.id)">删除</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="section-header">
      <h2 class="section-title">推送日志</h2>
      <el-button size="small" @click="fetchLogs">刷新</el-button>
    </div>

    <div class="table-card">
      <el-table :data="pushLogs" empty-text="暂无推送记录">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="helpId" label="求助ID" />
        <el-table-column prop="totalCount" label="推送数" />
        <el-table-column prop="successCount" label="成功" />
        <el-table-column prop="failCount" label="失败" />
        <el-table-column label="到达率">
          <template #default="{ row }">{{ (row.reachRate * 100).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column label="时间">
          <template #default="{ row }">{{ formatDate(row.createTime, true) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑规则' : '新增规则'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如：默认推送规则" /></el-form-item>
        <el-form-item label="半径(km)"><el-input-number v-model="form.radiusKm" :min="1" :max="500" /></el-form-item>
        <el-form-item label="紧急程度">
          <el-checkbox-group v-model="form.urgencyList">
            <el-checkbox :value="'1'">高</el-checkbox><el-checkbox :value="'2'">中</el-checkbox><el-checkbox :value="'3'">低</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { getRules, saveRule, deleteRule, getPushLogs } from '../api/push';
import { formatDate } from '../utils/format';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false), rules = ref([]), pushLogs = ref([]), dialogVisible = ref(false), editingId = ref(null);
const form = reactive({ name:'', radiusKm:10, urgencyList:['1','2','3'], enabled:1 });

function fetch() {
  loading.value = true;
  getRules().then(r => { const d = r?.data??r; rules.value = Array.isArray(d)?d:(d?.list||[]); }).finally(() => loading.value = false);
}
function showAddDialog() { editingId.value=null; Object.assign(form,{name:'',radiusKm:10,urgencyList:['1','2','3'],enabled:1}); dialogVisible.value=true; }
function editRule(row) {
  editingId.value=row.id;
  Object.assign(form,{ name:row.name||'', radiusKm:row.radiusKm||10, urgencyList:row.urgencyLevels?row.urgencyLevels.split(','):['1','2','3'], enabled:row.enabled??1 });
  dialogVisible.value=true;
}
function saveForm() {
  const d = { id:editingId.value||undefined, name:form.name, radiusKm:form.radiusKm, urgencyLevels:form.urgencyList.join(','), enabled:form.enabled };
  saveRule(d).then(() => { ElMessage.success('保存成功'); dialogVisible.value=false; fetch(); });
}
function removeRule(id) { ElMessageBox.confirm('确定删除？','确认').then(() => deleteRule(id).then(() => { ElMessage.success('已删除'); fetch(); })).catch(()=>{}); }
function fetchLogs() { getPushLogs().then(r => { const d = r?.data??r; pushLogs.value = Array.isArray(d)?d:[]; }).catch(()=>{}); }
onMounted(() => { fetch(); fetchLogs(); });
</script>

<style scoped>
.page-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.page-title { font-size:20px; font-weight:700; margin:0; }
.section-header { display:flex; align-items:center; justify-content:space-between; margin:24px 0 12px; }
.section-title { font-size:16px; font-weight:600; margin:0; }
.table-card { background:#fff; border-radius:8px; border:1px solid #eee; overflow:hidden; }
.act { color:var(--c-brand); cursor:pointer; margin-right:12px; font-size:13px; }
.act:hover { opacity:0.7; }
.act.del { color:#DC2626; }
</style>
