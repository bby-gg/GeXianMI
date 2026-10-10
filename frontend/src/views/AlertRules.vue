<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchAlertRules, updateAlertRule } from '@/api'
import type { AlertRule } from '@/types/env.d'
import { ElMessage } from 'element-plus'
import { isAdmin } from '@/utils/auth'

const rules = ref<AlertRule[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const editingRule = ref<AlertRule | null>(null)
const form = ref({ minVal: 0, maxVal: 0, thresholdLow: 0, thresholdHigh: 0, enabled: 1, level: 'warning' as 'warning' | 'error' })
const formLoading = ref(false)

onMounted(() => load())

async function load() {
  loading.value = true
  try { rules.value = await fetchAlertRules() } catch { ElMessage.error('加载规则失败') }
  loading.value = false
}

function openEdit(row: AlertRule) {
  editingRule.value = row
  form.value = {
    minVal: row.minVal ?? 0,
    maxVal: row.maxVal ?? 0,
    thresholdLow: row.thresholdLow ?? 0,
    thresholdHigh: row.thresholdHigh ?? 0,
    enabled: row.enabled,
    level: row.level,
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!editingRule.value) return
  formLoading.value = true
  try {
    const result = await updateAlertRule(editingRule.value.id, form.value)
    if (result) {
      ElMessage.success(`${editingRule.value.paramName} 规则已更新`)
      dialogVisible.value = false
      await load()
    } else {
      ElMessage.error('更新失败')
    }
  } catch { ElMessage.error('操作失败') }
  finally { formLoading.value = false }
}

function levelTag(l: string) {
  return l === 'error' ? 'danger' : 'warning'
}
function levelText(l: string) {
  return l === 'error' ? '严重' : '警告'
}
</script>

<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">🔧 告警规则配置</span>
        <span class="page-hint">修改规则后仪表盘阈值颜色和告警判断立刻生效</span>
      </div>
    </template>

    <el-table :data="rules" v-loading="loading" stripe border style="width: 100%; min-width: 900px"
      empty-text="暂无规则数据">
      <el-table-column prop="paramName" label="参数" width="100" align="center" />
      <el-table-column prop="unit" label="单位" width="70" align="center" />
      <el-table-column label="正常范围" width="180" align="center">
        <template #default="scope">
          <span class="range-green">{{ scope.row.minVal }} ~ {{ scope.row.maxVal }} {{ scope.row.unit }}</span>
        </template>
      </el-table-column>
      <el-table-column label="告警阈值" width="200" align="center">
        <template #default="scope">
          <span class="range-red">
            &lt; {{ scope.row.thresholdLow }} {{ scope.row.unit }}
            <span v-if="scope.row.thresholdHigh">或 &gt; {{ scope.row.thresholdHigh }} {{ scope.row.unit }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="级别" width="80" align="center">
        <template #default="scope">
          <el-tag :type="levelTag(scope.row.level)" size="small" effect="dark" round>
            {{ levelText(scope.row.level) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="启用" width="70" align="center">
        <template #default="scope">
          <span :style="{ color: scope.row.enabled ? '#67c23a' : '#c0c4cc', fontSize: '18px' }">
            {{ scope.row.enabled ? '✅' : '❌' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column v-if="isAdmin()" label="操作" width="100" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="`编辑规则 — ${editingRule?.paramName}`" width="440px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px" label-position="right">
        <el-form-item label="正常下限">
          <el-input-number v-model="form.minVal" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="正常上限">
          <el-input-number v-model="form.maxVal" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="告警下限">
          <el-input-number v-model="form.thresholdLow" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="告警上限">
          <el-input-number v-model="form.thresholdHigh" :precision="1" style="width:100%" />
          <span class="form-tip">留空 = 不设上限</span>
        </el-form-item>
        <el-form-item label="告警级别">
          <el-radio-group v-model="form.level">
            <el-radio value="warning">警告</el-radio>
            <el-radio value="error">严重</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.page-card { border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: baseline; flex-wrap: wrap; gap: 10px; }
.page-title { font-size: 18px; font-weight: 700; color: #303133; }
.page-hint { font-size: 13px; color: #909399; }
.range-green { color: #67c23a; font-weight: 600; font-size: 14px; }
.range-red { color: #f56c6c; font-weight: 600; font-size: 13px; }
.form-tip { font-size: 12px; color: #c0c4cc; margin-left: 8px; }
@media (max-width: 768px) { .page-title { font-size: 16px; } .page-hint { display: none; } }
</style>
