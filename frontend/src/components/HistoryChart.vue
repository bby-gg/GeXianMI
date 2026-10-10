<script setup lang="ts">
/**
 * 历史曲线图 — 两个独立 ECharts 实例，上下排列
 * 上图: 水温(左轴) + 溶解氧(右轴)
 * 下图: pH 值(左轴) + 光照(右轴)
 * 每个实例仅 2 条 Y 轴，标签绝对不重叠
 *
 * 时间选择：始终使用日期时间范围选择器，用户可以自由选择起止时间
 * 无数据时：在图表上方显示警告提示
 */
import { ref, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { fetchHistory, fetchDataRange, type DataRange } from '@/api'
import type { EnvRecord } from '@/types/env.d'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

console.log('[HistoryChart] 组件加载')

const props = defineProps<{
  deviceId: string
  showTimePicker?: boolean   // 已废弃，现在始终显示日期选择器
  bare?: boolean
}>()

const topRef = ref<HTMLDivElement>()
const bottomRef = ref<HTMLDivElement>()
const dateRange = ref<[Date, Date] | null>(null)
const noDataWarning = ref('')
const rangeInfo = ref<DataRange | null>(null)
const cachedRecords = ref<EnvRecord[]>([])

let topChart: echarts.ECharts | null = null
let bottomChart: echarts.ECharts | null = null
let ro1: ResizeObserver | null = null
let ro2: ResizeObserver | null = null

// ---- 颜色 ----
const C = {
  waterTemp:       '#5470c6',
  dissolvedOxygen: '#fac858',
  ph:              '#91cc75',
  lightIntensity:  '#ee6666',
}

// ---- 初始化 ECharts ----
function initCharts() {
  if (topRef.value && !topChart) {
    topChart = echarts.init(topRef.value)
    ro1 = new ResizeObserver(() => topChart?.resize())
    ro1.observe(topRef.value)
    console.log('[HistoryChart] 上图 ECharts 初始化完成')
  }
  if (bottomRef.value && !bottomChart) {
    bottomChart = echarts.init(bottomRef.value)
    ro2 = new ResizeObserver(() => bottomChart?.resize())
    ro2.observe(bottomRef.value)
    console.log('[HistoryChart] 下图 ECharts 初始化完成')
  }
}

// deviceId 变化时重新加载数据
watch(() => props.deviceId, (newId) => {
  console.log('[HistoryChart] deviceId 变化:', newId)
  if (newId) { loadRangeInfo(); loadHistory() }
})

onMounted(async () => {
  console.log('[HistoryChart] onMounted, deviceId:', props.deviceId, '| topRef:', !!topRef.value, '| bottomRef:', !!bottomRef.value)
  initCharts()
  if (props.deviceId) {
    await loadRangeInfo()
    // 如果有数据范围，默认展示全部；否则兜底最近 24 小时
    if (rangeInfo.value?.first && rangeInfo.value?.last) {
      dateRange.value = [new Date(rangeInfo.value.first), new Date(rangeInfo.value.last)]
    } else if (!dateRange.value) {
      const now = new Date()
      dateRange.value = [new Date(now.getTime() - 24 * 3600_000), now]
    }
    loadHistory()
  }
})

onUnmounted(() => {
  ro1?.disconnect()
  ro2?.disconnect()
  topChart?.dispose()
  bottomChart?.dispose()
})

/** 格式化日期为 "YYYY-MM-DD HH:MM:SS" */
function fmt(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function getTimeParams(): { start: string; end: string } {
  if (dateRange.value && dateRange.value.length === 2) {
    return { start: fmt(dateRange.value[0]), end: fmt(dateRange.value[1]) }
  }
  const now = new Date()
  return {
    start: fmt(new Date(now.getTime() - 24 * 3600_000)),
    end: fmt(now),
  }
}

/** 快捷时间按钮 */
const timePresets = [
  { label: '1h',  ms: 3600_000 },
  { label: '6h',  ms: 6 * 3600_000 },
  { label: '24h', ms: 24 * 3600_000 },
  { label: '3d',  ms: 3 * 24 * 3600_000 },
  { label: '7d',  ms: 7 * 24 * 3600_000 },
]
const activePreset = ref('24h')

function applyPreset(ms: number, label: string) {
  activePreset.value = label
  const now = new Date()
  dateRange.value = [new Date(now.getTime() - ms), now]
  loadHistory()
}

/** CSV 导出 */
function exportCSV() {
  const records = cachedRecords.value
  if (records.length === 0) return
  const headers = ['时间', '水温(°C)', 'pH值', '溶解氧(mg/L)', '光照(lux)', '电导率(μS/cm)']
  const rows = records.map(r => [
    r.timestamp, r.waterTemp, r.ph, r.dissolvedOxygen, r.lightIntensity, r.conductivity,
  ].join(','))
  const csv = '﻿' + headers.join(',') + '\n' + rows.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `环境数据_${props.deviceId}_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

/** 友好的日期显示：去掉秒，中文年月日 */
function friendlyDate(s: string | null): string {
  if (!s) return ''
  const d = new Date(s)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

async function loadRangeInfo() {
  if (!props.deviceId) return
  try {
    rangeInfo.value = await fetchDataRange(props.deviceId)
    if (rangeInfo.value) {
      console.log('[HistoryChart] 数据范围:', rangeInfo.value.first, '~', rangeInfo.value.last, '共', rangeInfo.value.count, '条')
    }
  } catch (e) {
    console.error('[HistoryChart] 获取数据范围失败:', e)
  }
}

async function loadHistory() {
  if (!props.deviceId) return
  console.log('[HistoryChart] loadHistory 开始, deviceId:', props.deviceId)
  const { start, end } = getTimeParams()
  console.log('[HistoryChart] 时间范围:', start, '~', end)
  try {
    const records = await fetchHistory(props.deviceId, start, end)
    cachedRecords.value = records
    console.log('[HistoryChart] 获取到记录数:', records.length)
    renderCharts(records, start, end)
  } catch (e) {
    console.error('[HistoryChart] 加载失败:', e)
  }
}

function hexToRgba(hex: string, alpha: number): string {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r},${g},${b},${alpha})`
}

function areaFor(color: string) {
  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: hexToRgba(color, .2) },
    { offset: 1, color: hexToRgba(color, 0) },
  ])
}

/** 构建单图 option：左轴曲线 + 右轴曲线 */
function buildOption(
  times: string[],
  leftName: string, leftColor: string, leftData: (number | null)[],
  rightName: string, rightColor: string, rightData: (number | null)[],
  showXLabel: boolean,
) {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: 'rgba(255,255,255,.96)',
      borderColor: '#e0e0e0',
      textStyle: { color: '#333', fontSize: 12 },
    },
    legend: {
      bottom: 0,
      textStyle: { fontSize: 12, color: '#555' },
      itemWidth: 22, itemHeight: 3,
      data: [leftName, rightName],
    },
    grid: { left: 10, right: 10, top: 28, bottom: 42, containLabel: true },
    xAxis: {
      type: 'category', data: times,
      axisLabel: showXLabel
        ? { rotate: 30, fontSize: 11, color: '#888', margin: 6 }
        : { show: false },
      axisLine: { lineStyle: { color: '#ddd' } },
      axisTick: { show: false },
      boundaryGap: false,
    },
    yAxis: [
      {
        type: 'value',
        name: leftName,
        nameTextStyle: { fontSize: 12, color: leftColor },
        nameGap: 12,
        axisLabel: { fontSize: 12, color: leftColor, margin: 6 },
        axisLine: { show: true, lineStyle: { color: leftColor } },
        splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
      },
      {
        type: 'value',
        name: rightName,
        nameTextStyle: { fontSize: 12, color: rightColor },
        nameGap: 12,
        axisLabel: { fontSize: 12, color: rightColor, margin: 6 },
        axisLine: { show: true, lineStyle: { color: rightColor } },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: leftName, type: 'line', yAxisIndex: 0,
        data: leftData, smooth: true,
        symbol: 'none', lineStyle: { width: 2.5, color: leftColor },
        areaStyle: { color: areaFor(leftColor) },
      },
      {
        name: rightName, type: 'line', yAxisIndex: 1,
        data: rightData, smooth: true,
        symbol: 'none', lineStyle: { width: 2.5, color: rightColor },
        areaStyle: { color: areaFor(rightColor) },
      },
    ],
  }
}

function renderCharts(records: EnvRecord[], start: string, end: string) {
  console.log('[HistoryChart] renderCharts 被调用, 记录数:', records.length)
  if (!topChart || !bottomChart) {
    console.warn('[HistoryChart] renderCharts 跳过: ECharts 未初始化')
    return
  }

  if (records.length === 0) {
    // 设置醒目的无数据提示
    noDataWarning.value = `${start} 至 ${end} 范围内没有环境数据，请调整查询时间`
    const empty = {
      title: { text: '暂无数据', left: 'center', top: 'center',
        textStyle: { color: '#999', fontSize: 14, fontWeight: 'normal' } },
    }
    topChart.setOption(empty, true)
    bottomChart.setOption({}, true)
    return
  }

  // 有数据 — 清除警告
  noDataWarning.value = ''

  const times = records.map((r) => r.timestamp.slice(11, 19))

  // 上图：水温(左) + 溶解氧(右)
  topChart.setOption(buildOption(
    times,
    '水温 °C', C.waterTemp, records.map(r => r.waterTemp),
    '溶解氧 mg/L', C.dissolvedOxygen, records.map(r => r.dissolvedOxygen),
    false,
  ), true)

  // 下图：pH(左) + 光照(右)
  bottomChart.setOption(buildOption(
    times,
    'pH 值', C.ph, records.map(r => r.ph),
    '光照 lux', C.lightIntensity, records.map(r => r.lightIntensity),
    true,
  ), true)

  console.log('[HistoryChart] 图表渲染完成')
}

defineExpose({ loadHistory })
</script>

<template>
  <!-- ====== 卡片版（/monitor 页面使用） ====== -->
  <el-card v-if="!bare" shadow="hover" class="chart-card">
    <template #header>
      <div class="chart-header">
        <span class="chart-title">📈 环境参数曲线</span>
        <div class="chart-actions">
          <el-button-group size="small" class="preset-group">
            <el-button
              v-for="p in timePresets" :key="p.label"
              :type="activePreset === p.label ? 'primary' : ''"
              @click="applyPreset(p.ms, p.label)"
            >{{ p.label }}</el-button>
          </el-button-group>
          <el-date-picker
            v-model="dateRange" type="datetimerange"
            range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间"
            size="small" style="max-width: 340px"
            format="YYYY-MM-DD HH:mm"
            @change="() => { activePreset = ''; loadHistory(); }"
          />
          <el-button size="small" type="success" plain @click="exportCSV" :disabled="cachedRecords.length === 0">
            📥 导出 CSV
          </el-button>
        </div>
      </div>
    </template>

    <!-- 数据范围提示 -->
    <el-alert
      v-if="rangeInfo?.first" type="info" :closable="false" class="range-info-bar"
    >
      <template #title>
        <span>📋 该设备记录了 <strong>{{ friendlyDate(rangeInfo.first) }}</strong> 至 <strong>{{ friendlyDate(rangeInfo.last) }}</strong> 的数据，共 <strong>{{ rangeInfo.count }}</strong> 条记录</span>
      </template>
    </el-alert>
    <el-alert
      v-else-if="rangeInfo" type="info" :closable="false" class="range-info-bar"
      title="📋 该设备暂无任何数据"
    />

    <!-- 无数据警告 -->
    <el-alert
      v-if="noDataWarning" :title="noDataWarning"
      type="warning" show-icon :closable="false"
      class="no-data-alert"
    />

    <div ref="topRef" class="chart-half"></div>
    <div ref="bottomRef" class="chart-half"></div>
  </el-card>

  <!-- ====== 裸版（/history 页面使用） ====== -->
  <div v-else>
    <!-- 数据范围提示 -->
    <el-alert
      v-if="rangeInfo?.first" type="info" :closable="false" class="range-info-bar"
    >
      <template #title>
        <span>📋 该设备记录了 <strong>{{ friendlyDate(rangeInfo.first) }}</strong> 至 <strong>{{ friendlyDate(rangeInfo.last) }}</strong> 的数据，共 <strong>{{ rangeInfo.count }}</strong> 条记录</span>
      </template>
    </el-alert>
    <el-alert
      v-else-if="rangeInfo" type="info" :closable="false" class="range-info-bar"
      title="📋 该设备暂无任何数据"
    />

    <div class="chart-header bare-header">
      <div class="chart-actions">
        <el-button-group size="small" class="preset-group">
          <el-button
            v-for="p in timePresets" :key="p.label"
            :type="activePreset === p.label ? 'primary' : ''"
            @click="applyPreset(p.ms, p.label)"
          >{{ p.label }}</el-button>
        </el-button-group>
        <el-date-picker
          v-model="dateRange" type="datetimerange"
          range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间"
          size="small" style="max-width: 340px"
          format="YYYY-MM-DD HH:mm"
          @change="() => { activePreset = ''; loadHistory(); }"
        />
        <el-button size="small" type="success" plain @click="exportCSV" :disabled="cachedRecords.length === 0">
          📥 导出 CSV
        </el-button>
      </div>
    </div>

    <!-- 无数据警告 -->
    <el-alert
      v-if="noDataWarning" :title="noDataWarning"
      type="warning" show-icon :closable="false"
      class="no-data-alert"
    />

    <div ref="topRef" class="chart-half"></div>
    <div ref="bottomRef" class="chart-half"></div>
  </div>
</template>

<style scoped>
.chart-card { margin-top: 16px; }
.chart-header {
  display: flex; justify-content: space-between;
  align-items: center; flex-wrap: wrap; gap: 10px;
}
.bare-header { margin-bottom: 8px; justify-content: flex-end; }
.chart-title { font-size: 17px; font-weight: 700; color: #303133; }
.chart-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.preset-group { flex-shrink: 0; }

/* 数据范围提示条 */
.range-info-bar { margin-bottom: 10px; }

/* 无数据警告条 */
.no-data-alert { margin-bottom: 10px; }

/* 每个子图独立占一半高度 */
.chart-half { width: 100%; height: 260px; }

@media (max-width: 992px)  { .chart-half { height: 220px; } }
@media (max-width: 768px) {
  .chart-title { font-size: 15px; }
  .chart-half { height: 210px; }
}
</style>
