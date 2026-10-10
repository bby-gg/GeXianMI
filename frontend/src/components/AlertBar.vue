<script setup lang="ts">
import type { AlertInfo } from '@/types/env.d'

defineProps<{ messages: AlertInfo[] }>()
</script>

<template>
  <el-card shadow="hover" class="alert-card">
    <template #header>
      <span class="alert-title">⚠️ 告警信息</span>
    </template>
    <div v-if="messages.length === 0" class="alert-empty">
      <span class="alert-empty-icon">✅</span>
      <span>所有指标正常</span>
    </div>
    <el-scrollbar v-else max-height="260px">
      <el-alert
        v-for="a in messages"
        :key="a.id"
        :title="a.message"
        :type="a.level === 'error' ? 'error' : 'warning'"
        show-icon
        :closable="false"
        class="alert-item"
      />
    </el-scrollbar>
  </el-card>
</template>

<style scoped>
.alert-card {
  border-left: 4px solid #e6a23c;
  border-radius: 8px;
}
.alert-title {
  font-size: 17px;
  font-weight: 700;
  color: #e6a23c;
}
.alert-empty {
  display: flex; align-items: center; justify-content: center;
  gap: 8px; padding: 28px 0; color: #67c23a; font-size: 15px;
}
.alert-empty-icon { font-size: 22px; }
.alert-item { margin-bottom: 8px; border-radius: 6px; }
</style>
