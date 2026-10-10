<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchAlerts, confirmAlert, ignoreAlert, confirmAllAlerts } from '@/api'
import type { AlertInfo } from '@/types/env.d'
import { ElMessage } from 'element-plus'

const alerts = ref<AlertInfo[]>([])
const loading = ref(false)
const filterStatus = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => load())

async function load() {
  loading.value = true
  const params: any = { page: page.value, size: size.value }
  if (filterStatus.value) params.status = filterStatus.value
  const result = await fetchAlerts(params)
  alerts.value = result.records
  total.value = result.total
  loading.value = false
}

function onFilterChange() {
  page.value = 1
  load()
}

function onSizeChange() {
  page.value = 1
  load()
}

async function handleConfirm(row: AlertInfo) {
  if (await confirmAlert(row.id)) { ElMessage.success('已确认'); await load() }
}

async function handleIgnore(row: AlertInfo) {
  if (await ignoreAlert(row.id)) { ElMessage.success('已忽略'); await load() }
}

async function handleConfirmAll() {
  if (await confirmAllAlerts()) { ElMessage.success('已全部确认'); await load() }
}

function statusTag(s: string) {
  if (s === 'active') return 'danger'
  if (s === 'confirmed') return 'warning'
  if (s === 'resolved') return 'success'
  return 'info'
}
function statusText(s: string) {
  if (s === 'active') return '待处理'
  if (s === 'confirmed') return '已确认'
  if (s === 'resolved') return '已恢复'
  return '已忽略'
}
</script>

<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">🔔 告警记录</span>
        <div class="header-actions">
          <el-select v-model="filterStatus" placeholder="筛选状态" style="width: 120px" size="default" clearable @change="onFilterChange">
            <el-option label="待处理" value="active" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已忽略" value="ignored" />
            <el-option label="已恢复" value="resolved" />
          </el-select>
          <el-button type="primary" size="default" plain @click="handleConfirmAll">全部标记已读</el-button>
        </div>
      </div>
    </template>

    <div class="table-wrap">
      <el-table :data="alerts" v-loading="loading" stripe border style="width: 100%; min-width: 800px"
        empty-text="暂无告警记录">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="deviceId" label="设备" width="100" align="center" />
        <el-table-column prop="triggeredAt" label="触发时间" width="160" align="center" />
        <el-table-column prop="paramName" label="参数" width="80" align="center" />
        <el-table-column prop="value" label="异常值" width="100" align="center" />
        <el-table-column prop="message" label="描述" min-width="200" />
        <el-table-column label="级别" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.level === 'error' ? 'danger' : 'warning'" size="small" effect="dark" round>
              {{ scope.row.level === 'error' ? '严重' : '预警' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)" size="small" round>
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === 'active'">
              <el-button type="primary" size="small" link @click="handleConfirm(scope.row)">确认</el-button>
              <el-button size="small" link @click="handleIgnore(scope.row)">忽略</el-button>
            </template>
            <span v-else style="color: #c0c4cc; font-size: 13px">—</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="onSizeChange"
        />
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.page-card { border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.page-title { font-size: 18px; font-weight: 700; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.table-wrap { overflow-x: auto; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
@media (max-width: 768px) { .page-title { font-size: 16px; } }
</style>
