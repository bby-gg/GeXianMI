<script setup lang="ts">
import type { DeviceInfo } from '@/types/env.d'

defineProps<{ devices: DeviceInfo[] }>()

function statusType(s: string) {
  return s === 'online' ? 'success' : s === 'offline' ? 'info' : 'warning'
}
function statusText(s: string) {
  return s === 'online' ? '在线' : s === 'offline' ? '离线' : '维护中'
}
</script>

<template>
  <el-card shadow="hover" class="device-card">
    <template #header>
      <span class="device-title">🖥️ 设备状态</span>
    </template>
    <el-table :data="devices" stripe border style="width: 100%" max-height="300" empty-text="暂无设备数据">
      <el-table-column type="index" label="#" width="50" align="center" />
      <el-table-column prop="code" label="设备编号" align="center" min-width="110" />
      <el-table-column prop="name" label="名称" align="center" min-width="120" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)" size="small" effect="dark" round>
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后上报" width="140" align="center">
        <template #default>
          <span style="color: #909399; font-size: 13px">
            {{ new Date().toLocaleTimeString() }}
          </span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.device-card {
  border-radius: 8px;
}
.device-title {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
}
</style>
