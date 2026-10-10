<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { fetchGreenhouses, fetchGreenhouseDevices } from '@/api'
import type { DeviceInfo, GreenhouseInfo } from '@/types/env.d'
import HistoryChart from '@/components/HistoryChart.vue'

const greenhouses = ref<GreenhouseInfo[]>([])
const devices = ref<DeviceInfo[]>([])
const selectedGreenhouse = ref<number | null>(null)
const selectedDevice = ref('')

onMounted(async () => {
  greenhouses.value = await fetchGreenhouses()
  if (greenhouses.value.length > 0) {
    selectedGreenhouse.value = greenhouses.value[0]?.id ?? null
  }
})

watch(selectedGreenhouse, async (ghId) => {
  if (!ghId) { devices.value = []; selectedDevice.value = ''; return }
  devices.value = await fetchGreenhouseDevices(ghId)
  if (devices.value.length > 0) selectedDevice.value = devices.value[0]?.code ?? ''
  else selectedDevice.value = ''
})
</script>

<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">📈 历史数据查询</span>
        <div class="header-selectors">
          <el-select v-model="selectedGreenhouse" placeholder="大棚" style="width: 180px">
            <el-option v-for="gh in greenhouses" :key="gh.id" :label="gh.name" :value="gh.id" />
          </el-select>
          <el-select v-model="selectedDevice" placeholder="设备" style="width: 220px" :disabled="!selectedGreenhouse">
            <el-option v-for="d in devices" :key="d.code" :label="`${d.name} (${d.code})`" :value="d.code" />
          </el-select>
        </div>
      </div>
    </template>
    <HistoryChart v-if="selectedDevice" :device-id="selectedDevice" :show-time-picker="true" bare />
    <div v-else class="empty-hint">请先选择大棚和设备</div>
  </el-card>
</template>

<style scoped>
.page-card { border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.page-title { font-size: 18px; font-weight: 700; color: #303133; }
.header-selectors { display: flex; gap: 8px; }
.empty-hint { text-align: center; color: #909399; padding: 60px 0; font-size: 14px; }
@media (max-width: 768px) { .page-title { font-size: 16px; } .header-selectors { flex-direction: column; } }
</style>
