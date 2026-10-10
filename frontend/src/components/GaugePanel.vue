<script setup lang="ts">
import { watch, ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { EnvRecord } from '@/types/env.d'

const props = defineProps<{ data: EnvRecord | null }>()

const gaugeContainers = Array.from({ length: 5 }, () => ref<HTMLDivElement>())

type GaugeDef = { key: keyof EnvRecord; name: string; unit: string; min: number; max: number; normal: [number, number]; warn: [number, number] }

const baseDefs: GaugeDef[] = [
  { key: 'waterTemp',       name:'水温',   unit:'°C',    min:19, max:26,  normal:[21.4,23.4],warn:[20.4,24.4] },
  { key: 'ph',              name:'pH值',   unit:'',      min:5.5,max:8.5, normal:[6.9,7.3],  warn:[6.6,7.6]   },
  { key: 'dissolvedOxygen', name:'溶解氧', unit:'mg/L',  min:4.5,max:9.0, normal:[6.3,7.4],  warn:[5.8,7.9]   },
  { key: 'lightIntensity',  name:'光照',   unit:'lux',   min:0,  max:2000,normal:[800,1500],warn:[500,1800]  },
  { key: 'conductivity',    name:'电导率', unit:'μS/cm', min:310,max:390, normal:[335,363],  warn:[321,377]  },
]

// 光照盘根据数据时间戳动态生成
const gaugeDefs = computed<GaugeDef[]>(() => {
  const ts = props.data?.timestamp
  const h = ts ? parseInt(ts.slice(11, 13)) : undefined
  const isDay = h !== undefined && !isNaN(h) && h >= 6 && h < 18
  return baseDefs.map(d => {
    if (d.key === 'lightIntensity') {
      return isDay
        ? { ...d, name: '光照(昼)', max: 2000, normal: [800,1500] as [number,number], warn: [500,1800] as [number,number] }
        : { ...d, name: '光照(夜)', max: 200,  normal: [0,50] as [number,number],    warn: [0,100] as [number,number] }
    }
    return { ...d }
  })
})

const charts: (echarts.ECharts | null)[] = Array(5).fill(null)

onMounted(() => { nextTick(() => initAllCharts()) })
onUnmounted(() => { charts.forEach(c => c?.dispose()); window.removeEventListener('resize', handleResize) })
watch(() => props.data, () => updateAllCharts(), { deep: true })

function initAllCharts() {
  gaugeContainers.forEach((ref, i) => { if (ref.value) charts[i] = echarts.init(ref.value) })
  updateAllCharts()
  window.addEventListener('resize', handleResize)
}
function handleResize() { charts.forEach(c => c?.resize()) }

const GREEN = '#67c23a'; const ORANGE = '#e6a23c'; const RED = '#f56c6c'

function getAxisColor(g: GaugeDef) {
  const rng = g.max - g.min
  const n0 = (g.normal[0] - g.min) / rng; const n1 = (g.normal[1] - g.min) / rng
  const w0 = (g.warn[0] - g.min) / rng;   const w1 = (g.warn[1] - g.min) / rng
  const segs: [number, string][] = []
  if (w0 < n0) segs.push([w0, RED], [n0, ORANGE])
  segs.push([n1, GREEN])
  if (w1 > n1) segs.push([w1, ORANGE])
  segs.push([1, RED])
  return segs
}

function updateAllCharts() {
  const defs = gaugeDefs.value
  gaugeContainers.forEach((ref, i) => {
    const chart = charts[i]
    if (!chart || !ref.value) return
    const g = defs[i]
    const val = props.data ? (props.data[g.key] as number) ?? 0 : 0
    const w = ref.value.clientWidth
    const detailSize = w < 140 ? 18 : w < 180 ? 22 : 26
    const labelSize  = w < 140 ? 10 : w < 180 ? 11 : 12
    chart.setOption({
      series: [{
        type: 'gauge', radius: '88%', min: g.min, max: g.max,
        startAngle: 210, endAngle: -30, center: ['50%','54%'],
        axisLine: { show: true, lineStyle: { width: 20, color: getAxisColor(g) } },
        pointer: { length: '52%', width: 6, itemStyle: { color: '#303133' } },
        axisTick: { distance: -20, length: 6, lineStyle: { width: 1.5, color: '#888' } },
        splitLine: { distance: -26, length: 16, lineStyle: { width: 3, color: '#555' } },
        axisLabel: { distance: 32, fontSize: labelSize, color: '#666' },
        detail: { valueAnimation: true, fontSize: detailSize, fontWeight: 'bold', color: '#303133',
          offsetCenter: [0, '58%'], formatter: (v: number) => `${v} ${g.unit}` },
        title: { show: false },
        data: [{ value: Number(val) }],
      }],
    }, true)
  })
}

function statusColor(i: number): string {
  if (!props.data) return '#c0c4cc'
  const g = gaugeDefs.value[i]
  const val = (props.data[g.key] as number) ?? 0
  if (val < g.warn[0] || val > g.warn[1]) return RED
  if (val < g.normal[0] || val > g.normal[1]) return ORANGE
  return GREEN
}
function statusLabel(i: number): string {
  if (!props.data) return '等待数据'
  const g = gaugeDefs.value[i]
  const val = props.data[g.key] as number
  if (val === null || val === undefined) return '—'
  return `${val} ${g.unit}`
}
function updatedTime(): string {
  if (!props.data?.timestamp) return ''
  return props.data.timestamp.replace('T',' ').slice(0,19)
}
</script>

<template>
  <el-card shadow="hover" class="gauge-card">
    <template #header>
      <div class="gauge-header">
        <span class="gauge-card-title">实时仪表盘</span>
        <span v-if="updatedTime()" class="gauge-update-time">最后更新: {{ updatedTime() }}</span>
      </div>
    </template>
    <div class="gauge-grid">
      <div
        v-for="(g, i) in gaugeDefs"
        :key="g.key"
        class="gauge-cell"
        :class="{ 'gauge-cell--warning': statusColor(i) === '#e6a23c', 'gauge-cell--danger': statusColor(i) === '#f56c6c' }"
        :title="`点击查看 ${g.name} 详情`"
        @click="() => {}"
      >
        <div class="gauge-badge">{{ g.name }}</div>
        <div :ref="(el) => { if (el) gaugeContainers[i].value = el as HTMLDivElement }" class="gauge-chart"></div>
        <div class="gauge-status" :style="{ color: statusColor(i) }">
          <span class="gauge-dot" :style="{ background: statusColor(i) }"></span>
          {{ statusLabel(i) }}
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.gauge-card { border-radius: 8px; }
.gauge-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.gauge-card-title { font-size: 17px; font-weight: 700; color: #303133; }
.gauge-update-time { font-size: 12px; color: #909399; }
.gauge-grid { display: flex; justify-content: space-around; align-items: stretch; flex-wrap: wrap; gap: 16px 8px; }
.gauge-cell {
  flex: 1 1 18%; min-width: 150px; max-width: 270px;
  display: flex; flex-direction: column; align-items: center;
  padding: 12px 8px 8px; border-radius: 12px;
  background: #fff; border: 2px solid #e8ecf1;
  cursor: pointer; user-select: none;
  transition: all .2s ease;
  position: relative; overflow: hidden;
}
.gauge-cell::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
  background: #67c23a; border-radius: 12px 12px 0 0;
  transition: background .3s;
}
.gauge-cell--warning::before { background: #e6a23c; }
.gauge-cell--danger::before { background: #f56c6c; }
.gauge-cell:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,.1); border-color: #409eff; }
.gauge-cell:active { transform: translateY(0); }

.gauge-badge {
  font-size: 13px; font-weight: 600; color: #fff;
  background: #409eff; padding: 2px 12px; border-radius: 10px;
  margin-bottom: 6px; letter-spacing: 1px;
}
.gauge-chart { width: 100%; height: 170px; flex-shrink: 0; }
.gauge-status { display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 700; margin-top: 2px; }
.gauge-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
@media (max-width: 992px) { .gauge-cell { flex: 1 1 30%; min-width: 120px; } .gauge-chart { height: 160px; } .gauge-name { font-size: 14px; } }
@media (max-width: 768px) { .gauge-cell { flex: 1 1 45%; min-width: 45%; max-width: none; } .gauge-chart { height: 200px; } .gauge-name { font-size: 16px; } }
</style>
