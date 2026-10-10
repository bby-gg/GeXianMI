<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { fetchDevices, fetchActiveAlerts, fetchLatest } from '@/api'
import type { DeviceInfo, AlertInfo } from '@/types/env.d'

echarts.use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

const devices = ref<DeviceInfo[]>([])
const alerts = ref<AlertInfo[]>([])
const now = ref('')
const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
let timer: ReturnType<typeof setInterval> | null = null
let clockTimer: ReturnType<typeof setInterval> | null = null

const onlineCount = computed(() => devices.value.filter(d => d.status === 'online').length)
const errorCount = computed(() => alerts.value.filter(a => a.level === 'error').length)

onMounted(async () => {
  initChart()
  await refresh()
  timer = setInterval(refresh, 30000)
  clockTimer = setInterval(updateClock, 1000)
  updateClock()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (clockTimer) clearInterval(clockTimer)
  chart?.dispose()
  window.removeEventListener('resize', handleResize)
})

function updateClock() {
  const d = new Date()
  const p = (n: number) => (n < 10 ? '0' + n : '' + n)
  now.value = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

function handleResize() { chart?.resize() }

function initChart() {
  if (chartRef.value && !chart) {
    chart = echarts.init(chartRef.value)
  }
}

async function refresh() {
  try {
    devices.value = await fetchDevices()
    alerts.value = await fetchActiveAlerts()
    await loadCompare()
  } catch { /* 单个接口失败不影响整体展示 */ }
}

/** 各设备最新水温对比柱状图 */
async function loadCompare() {
  const names: string[] = []
  const values: number[] = []
  for (const d of devices.value) {
    try {
      const latest = await fetchLatest(d.code)
      names.push(d.name || d.code)
      values.push(latest?.waterTemp ?? 0)
    } catch {
      names.push(d.name || d.code)
      values.push(0)
    }
  }
  if (!chart) return
  chart.setOption({
    grid: { left: 50, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: names, axisLabel: { color: '#c8d3e0', fontSize: 12 } },
    yAxis: { type: 'value', name: '°C', axisLabel: { color: '#c8d3e0' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,.08)' } } },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    series: [{
      name: '水温', type: 'bar', barWidth: 26,
      itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
      data: values,
      label: { show: true, position: 'top', color: '#c8d3e0', formatter: '{c}°C' },
    }],
  }, true)
}
</script>

<template>
  <div class="screen">
    <header class="screen-header">
      <h1 class="screen-title">🌿 葛仙米生长环境智能监测大屏</h1>
      <span class="screen-clock">{{ now }}</span>
    </header>

    <el-row :gutter="16" class="kpi-row">
      <el-col :xs="12" :sm="6">
        <div class="kpi-card">
          <div class="kpi-num">{{ devices.length }}</div>
          <div class="kpi-label">设备总数</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="kpi-card kpi--green">
          <div class="kpi-num">{{ onlineCount }}</div>
          <div class="kpi-label">在线设备</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="kpi-card kpi--orange">
          <div class="kpi-num">{{ alerts.length }}</div>
          <div class="kpi-label">活跃告警</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="kpi-card kpi--red">
          <div class="kpi-num">{{ errorCount }}</div>
          <div class="kpi-label">严重告警</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="main-row">
      <el-col :xs="24" :lg="16">
        <div class="panel">
          <div class="panel-title">各设备最新水温对比</div>
          <div ref="chartRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="panel">
          <div class="panel-title">实时告警</div>
          <div v-if="alerts.length === 0" class="empty">✅ 所有指标正常</div>
          <div v-else class="alert-list">
            <div v-for="a in alerts.slice(0, 8)" :key="a.id" class="alert-item"
              :class="{ 'alert-item--error': a.level === 'error' }">
              <span class="alert-dot" :class="a.level === 'error' ? 'dot-red' : 'dot-orange'"></span>
              <span class="alert-msg">{{ a.message }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.screen {
  min-height: calc(100vh - 40px);
  background: linear-gradient(160deg, #0b1c33 0%, #12294a 55%, #0e2238 100%);
  border-radius: 12px;
  padding: 20px;
  color: #e6f1ff;
}
.screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px 20px;
  border-bottom: 1px solid rgba(255,255,255,.12);
}
.screen-title {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #6dd5fa, #ffffff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.screen-clock { font-size: 16px; color: #9fb8d4; font-variant-numeric: tabular-nums; }

.kpi-row { margin-top: 20px; }
.kpi-card {
  background: rgba(255,255,255,.04);
  border: 1px solid rgba(255,255,255,.1);
  border-radius: 10px;
  padding: 20px;
  text-align: center;
}
.kpi-num { font-size: 34px; font-weight: 700; color: #fff; line-height: 1.1; }
.kpi-label { font-size: 13px; color: #9fb8d4; margin-top: 6px; }
.kpi--green .kpi-num { color: #67c23a; }
.kpi--orange .kpi-num { color: #e6a23c; }
.kpi--red .kpi-num { color: #f56c6c; }

.main-row { margin-top: 16px; }
.panel {
  background: rgba(255,255,255,.04);
  border: 1px solid rgba(255,255,255,.1);
  border-radius: 10px;
  padding: 16px;
  height: 100%;
}
.panel-title { font-size: 16px; font-weight: 600; color: #d6e5ff; margin-bottom: 12px; }
.chart { width: 100%; height: 360px; }

.empty { text-align: center; color: #67c23a; padding: 60px 0; font-size: 15px; }
.alert-list { max-height: 360px; overflow-y: auto; }
.alert-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; margin-bottom: 8px;
  background: rgba(230,162,60,.1); border-radius: 8px;
  border-left: 3px solid #e6a23c;
}
.alert-item--error {
  background: rgba(245,108,108,.12);
  border-left-color: #f56c6c;
  animation: flash 1s ease-in-out infinite;
}
.alert-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-orange { background: #e6a23c; }
.dot-red { background: #f56c6c; }
.alert-msg { font-size: 13px; color: #e6f1ff; }

@keyframes flash {
  0%, 100% { opacity: 1; }
  50% { opacity: .45; }
}

@media (max-width: 992px) {
  .screen-title { font-size: 20px; }
  .chart { height: 300px; }
}
</style>
