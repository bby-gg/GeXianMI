<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { fetchLatest, fetchGreenhouses, fetchGreenhouseDevices, fetchActiveAlerts, fetchRecent } from '@/api'
import type { EnvRecord, DeviceInfo, GreenhouseInfo, AlertInfo } from '@/types/env.d'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])
import GaugePanel from '@/components/GaugePanel.vue'
import DeviceTable from '@/components/DeviceTable.vue'
import AlertBar from '@/components/AlertBar.vue'

const router = useRouter()
const greenhouses = ref<GreenhouseInfo[]>([])
const devices = ref<DeviceInfo[]>([])
const selectedGreenhouse = ref<number | null>(null)
const selectedDevice = ref('')
const latestData = ref<EnvRecord | null>(null)
const alertMessages = ref<AlertInfo[]>([])
let timer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  greenhouses.value = await fetchGreenhouses()
  if (greenhouses.value.length > 0) selectedGreenhouse.value = greenhouses.value[0]?.id ?? null
})
watch(selectedGreenhouse, async (ghId) => {
  if (!ghId) { devices.value = []; selectedDevice.value = ''; return }
  devices.value = await fetchGreenhouseDevices(ghId)
  if (devices.value.length > 0) selectedDevice.value = devices.value[0]?.code ?? ''
  else selectedDevice.value = ''
})
let deviceGen = 0
watch(selectedDevice, (id) => {
  deviceGen++  // 切换设备后，丢弃仍在途的旧请求响应
  if (id) { fetchData(id); loadMiniChart(id) }
})

const POLL_MS = 10_000
const MINI_REFRESH_EVERY = 6  // 迷你走势约每 1 分钟刷新一次，避免频繁重拉 60 条
let pollCount = 0
onMounted(() => {
  timer = setInterval(() => {
    if (selectedDevice.value) {
      fetchData(selectedDevice.value)
      pollCount++
      if (pollCount >= MINI_REFRESH_EVERY) {
        pollCount = 0
        loadMiniChart(selectedDevice.value)
      }
    }
  }, POLL_MS)
})
onUnmounted(() => { if (timer) clearInterval(timer); miniChart?.dispose() })

async function fetchData(deviceId: string) {
  const gen = deviceGen
  const [data, alerts] = await Promise.all([fetchLatest(deviceId), fetchActiveAlerts(deviceId)])
  if (gen !== deviceGen) return  // 已切换设备，丢弃过期响应
  if (data) latestData.value = data
  alertMessages.value = alerts
}

// ====== 迷你走势：按钮切换，默认只显示水温 ======
const miniRef = ref<HTMLDivElement>()
let miniChart: echarts.ECharts | null = null
let lastDeviceId = ''

const P = [
  { key: 'waterTemp' as const,       name: '水温',   color: '#5470c6', unit: '°C' },
  { key: 'ph' as const,              name: 'pH值',   color: '#91cc75', unit: '' },
  { key: 'dissolvedOxygen' as const, name: '溶解氧', color: '#fac858', unit: 'mg/L' },
  { key: 'lightIntensity' as const,  name: '光照',   color: '#ee6666', unit: 'lux' },
  { key: 'conductivity' as const,    name: '电导率', color: '#fc8452', unit: 'μS/cm' },
]

const vis = ref({ waterTemp: true, ph: false, dissolvedOxygen: false, lightIntensity: false, conductivity: false })

async function loadMiniChart(deviceId: string) {
  const gen = deviceGen
  try {
    const fresh = await fetchRecent(deviceId, 60)
    if (gen !== deviceGen) return  // 已切换设备，丢弃过期响应
    if (fresh.length === 0) return
    updateMiniChart(fresh, deviceId !== lastDeviceId)
    lastDeviceId = deviceId
  } catch { /* ignore */ }
}

function toggleParam(key: keyof typeof vis.value) {
  vis.value[key] = !vis.value[key]
  if (lastDeviceId) updateMiniChartFromCache()
}

let miniCache: EnvRecord[] | null = null

function updateMiniChartFromCache() {
  if (miniCache && miniCache.length > 0) {
    updateMiniChart(miniCache, true) // 重建图表结构
  }
}

function updateMiniChart(records: EnvRecord[], fullInit: boolean) {
  if (!miniRef.value) return
  miniCache = records
  const sorted = [...records].sort((a, b) => (a.timestamp ?? '').localeCompare(b.timestamp ?? ''))
  const times = sorted.map(r => r.timestamp?.slice(11, 16) ?? '')
  const v = vis.value

  const active = P.filter(p => v[p.key])
  if (active.length === 0) { miniChart?.clear(); return }

  // Y 轴左-右交替，同侧轴的偏移量按数量动态分配，避免多轴标签重叠
  let leftCount = 0, rightCount = 0
  const AXIS_GAP = 48
  const yAxes = active.map((p, i) => {
    const vals = sorted.map(r => r[p.key] ?? 0) as number[]
    const mn = Math.min(...vals), mx = Math.max(...vals)
    const pad = Math.max((mx - mn) * 0.3, 0.3)
    const onRight = i % 2 === 1
    const offset = onRight ? rightCount++ * AXIS_GAP : leftCount++ * AXIS_GAP
    return {
      type: 'value' as const, min: +(mn - pad).toFixed(1), max: +(mx + pad).toFixed(1), splitNumber: 3,
      position: onRight ? 'right' as const : 'left' as const, offset,
      name: `${p.name}(${p.unit})`,
      nameLocation: onRight ? 'end' as const : 'start' as const,
      nameTextStyle: { fontSize: 10, color: p.color, fontWeight: 600 as const },
      axisLabel: { fontSize: 9, color: p.color },
      axisLine: { show: true, lineStyle: { color: p.color, width: 1 } },
      splitLine: { show: false }, axisTick: { show: false },
    }
  })

  const series = active.map((p, i) => {
    const data = sorted.map(r => r[p.key] ?? 0) as number[]
    return { name: p.name, type: 'line', smooth: true, symbol: 'none', yAxisIndex: i, data, lineStyle: { width: 2, color: p.color } }
  })

  if (!miniChart) { miniChart = echarts.init(miniRef.value); window.addEventListener('resize', () => miniChart?.resize()) }
  if (!miniChart) return

  if (fullInit) {
    miniChart.setOption({
      color: active.map(p => p.color),
      grid: { top: 12, bottom: 32, left: 12, right: 12 },
      xAxis: { type: 'category', data: times, axisLabel: { fontSize: 10, color: '#999' }, axisTick: { show: false }, axisLine: { lineStyle: { color: '#ddd' } } },
      yAxis: yAxes, series, tooltip: { trigger: 'axis' },
      legend: { bottom: 0, data: active.map(p => p.name), textStyle: { fontSize: 11 }, itemWidth: 20, itemHeight: 2 },
    }, true)
  } else {
    miniChart.setOption({ series, xAxis: { data: times }, yAxis: yAxes }, false)
  }
}
</script>

<template>
  <div class="monitor-page">
    <div class="device-bar">
      <span class="bar-label">大棚</span>
      <el-select v-model="selectedGreenhouse" placeholder="选择大棚" style="width:200px">
        <el-option v-for="gh in greenhouses" :key="gh.id" :label="gh.name" :value="gh.id" />
      </el-select>
      <span class="bar-label">设备</span>
      <el-select v-model="selectedDevice" placeholder="选择设备" style="width:220px" :disabled="!selectedGreenhouse">
        <el-option v-for="d in devices" :key="d.code" :label="`${d.name} (${d.code})`" :value="d.code" />
      </el-select>
      <span class="bar-hint">传感器上报 / 每 10 秒刷新</span>
      <el-button v-if="selectedDevice" size="small" type="primary" plain class="go-history-btn"
        @click="router.push({ path: '/history', query: { deviceId: selectedDevice } })">
        完整历史曲线
      </el-button>
    </div>

    <GaugePanel :data="latestData" />

    <el-card shadow="hover" class="mini-card">
      <template #header>
        <div class="mini-header">
          <span class="mini-title">最近 1 小时走势</span>
          <div class="mini-toggles">
            <button
              v-for="p in P"
              :key="p.key"
              class="mini-btn"
              :class="{ on: vis[p.key] }"
              :style="{ borderColor: vis[p.key] ? p.color : '#dcdfe6', backgroundColor: vis[p.key] ? p.color + '14' : '#fff', color: vis[p.key] ? p.color : '#909399' }"
              @click="toggleParam(p.key)">
              <span class="btn-dot" :style="{ background: p.color }"></span>
              {{ p.name }}
            </button>
          </div>
        </div>
      </template>
      <div v-show="selectedDevice" ref="miniRef" class="mini-chart"></div>
      <div v-show="!selectedDevice" class="mini-empty">请先选择大棚和设备</div>
    </el-card>

    <el-row :gutter="16" class="bottom-row">
      <el-col :xs="24" :md="16"><DeviceTable :devices="devices" /></el-col>
      <el-col :xs="24" :md="8"><AlertBar :messages="alertMessages" /></el-col>
    </el-row>
  </div>
</template>

<style scoped>
.device-bar { display:flex; align-items:center; gap:10px; margin-bottom:16px; padding:12px 16px; background:#fff; border-radius:8px; box-shadow:0 1px 4px rgba(0,0,0,.04); }
.bar-label { font-weight:600; color:#303133; }
.bar-hint { color:#909399; font-size:13px; margin-left:auto; }
.go-history-btn { flex-shrink:0; }

.mini-card { margin-top:16px; border-radius:8px; }
.mini-header { display:flex; align-items:center; gap:12px; flex-wrap:wrap; }
.mini-title { font-size:16px; font-weight:700; color:#303133; }
.mini-toggles { display:flex; gap:6px; flex-wrap:wrap; }

.mini-btn {
  display:inline-flex; align-items:center; gap:4px;
  padding:3px 10px; border-radius:14px; border:2px solid #dcdfe6;
  background:#fff; font-size:12px; font-weight:600;
  cursor:pointer; transition:all .2s; outline:none;
}
.mini-btn:hover { transform:translateY(-1px); box-shadow:0 2px 8px rgba(0,0,0,.08); }
.mini-btn.on {}
.btn-dot { display:inline-block; width:8px; height:8px; border-radius:50%; flex-shrink:0; }

.mini-chart { width:100%; height:380px; }
.mini-empty { text-align:center; color:#909399; padding:60px 0; }
.bottom-row { margin-top:16px; }

@media (max-width:768px) {
  .device-bar { flex-wrap:wrap; gap:8px; padding:10px 12px; }
  .bar-hint { display:none; }
  .mini-chart { height:300px; }
  .mini-toggles { gap:4px; }
  .mini-btn { padding:2px 8px; font-size:11px; }
  .bottom-row .el-col:last-child { margin-top:16px; }
}
</style>
