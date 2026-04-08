<template>
  <div class="page-wrap">
    <div class="page-header">
      <h1 class="page-title">交易记录</h1>
      <div class="header-actions">
        <el-date-picker v-model="billDate" type="date" placeholder="对账日期" size="default" value-format="YYYY-MM-DD" style="width:150px" />
        <el-button size="default" @click="doBill" :disabled="!billDate">下载账单</el-button>
        <el-select v-model="statusFilter" placeholder="全部状态" clearable size="default" style="width:120px" @change="onFilterChange">
          <el-option label="待支付" :value="0" /><el-option label="已支付" :value="1" /><el-option label="已关闭" :value="2" /><el-option label="已退款" :value="3" />
        </el-select>
      </div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="orderNo" label="订单号" show-overflow-tooltip />
        <el-table-column prop="helpId" label="求助ID" />
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column label="金额">
          <template #default="{ row }">¥{{ (row.amountCents / 100).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">{{ statusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="wxTransactionId" label="微信交易号" show-overflow-tooltip />
        <el-table-column label="创建时间">
          <template #default="{ row }">{{ formatDate(row.createTime, true) }}</template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <span class="act" @click="queryWx(row.orderNo)">查询</span>
            <span class="act" v-if="row.status===0" @click="doReconcile(row.orderNo)">补单</span>
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
import { list as fetchList, queryWxOrder, reconcile, downloadBill } from '../api/order';
import { formatDate } from '../utils/format';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false), tableData = ref([]), page = ref(1), size = ref(20), total = ref(0), statusFilter = ref(null), billDate = ref('');
const statusLabels = { 0:'待支付', 1:'已支付', 2:'已关闭', 3:'已退款' };
function statusLabel(s) { return statusLabels[s] ?? '未知'; }

function fetch() {
  loading.value = true;
  const p = { page: page.value, size: size.value };
  if (statusFilter.value !== null && statusFilter.value !== '') p.status = statusFilter.value;
  fetchList(p).then(r => { const d = r?.data||r; tableData.value = d?.records||[]; total.value = d?.total||0; }).finally(() => loading.value = false);
}
function onSizeChange(v) { size.value = v; page.value = 1; fetch(); }
function onFilterChange() { page.value = 1; fetch(); }
function queryWx(no) {
  queryWxOrder(no).then(r => { const d = r?.data||r; ElMessageBox.alert(`状态: ${d?.trade_state||'未知'}\n交易号: ${d?.transaction_id||'—'}`,'微信支付查询'); }).catch(() => ElMessage.error('查询失败'));
}
function doReconcile(no) {
  ElMessageBox.confirm('确定补单？','确认').then(() => reconcile(no).then(r => { ElMessage.success(r?.data||r||'完成'); fetch(); })).catch(()=>{});
}
function doBill() {
  if (!billDate.value) return;
  downloadBill(billDate.value).then(r => { const d = r?.data||r; if(d) ElMessageBox.alert('账单已获取','账单'); }).catch(() => ElMessage.error('下载失败'));
}
onMounted(fetch);
</script>

<style scoped>
.page-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; flex-wrap:wrap; gap:12px; }
.page-title { font-size:20px; font-weight:700; margin:0; }
.header-actions { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.table-card { background:#fff; border-radius:8px; border:1px solid #eee; overflow:hidden; }
.pager { display:flex; justify-content:flex-end; padding:12px 16px; border-top:1px solid #f0f0f0; }
.act { color:var(--c-brand); cursor:pointer; margin-right:12px; font-size:13px; }
.act:hover { opacity:0.7; }
</style>
