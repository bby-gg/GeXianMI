<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { fetchGreenhouses, createGreenhouse, updateGreenhouse, deleteGreenhouse } from '@/api'
import type { GreenhouseInfo, GreenhouseCreate, GreenhouseUpdate } from '@/types/env.d'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isAdmin } from '@/utils/auth'

const greenhouses = ref<GreenhouseInfo[]>([])
const loading = ref(false)
const searchText = ref('')

// 搜索过滤
const filteredList = computed(() => {
  if (!searchText.value) return greenhouses.value
  const kw = searchText.value.toLowerCase()
  return greenhouses.value.filter(g =>
    g.name.toLowerCase().includes(kw) || g.code.toLowerCase().includes(kw) || (g.location || '').toLowerCase().includes(kw)
  )
})

// ---- 对话框 ----
const dialogVisible = ref(false)
const dialogTitle = ref('新增大棚')
const isEdit = ref(false)
const editingId = ref(0)
const formLoading = ref(false)

const emptyForm = (): GreenhouseCreate => ({ code: '', name: '', location: '', area: undefined, notes: '' })
const form = ref<GreenhouseCreate>(emptyForm())

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    greenhouses.value = await fetchGreenhouses()
  } catch {
    ElMessage.error('加载大棚列表失败')
  }
  loading.value = false
}

function openCreate() {
  isEdit.value = false; dialogTitle.value = '新增大棚'; editingId.value = 0
  form.value = emptyForm()
  dialogVisible.value = true
}

function openEdit(row: GreenhouseInfo) {
  isEdit.value = true; dialogTitle.value = '编辑大棚'; editingId.value = row.id
  form.value = { code: row.code, name: row.name, location: row.location, area: row.area ?? undefined, notes: row.notes }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.code?.trim()) { ElMessage.warning('请输入大棚编号'); return }
  if (!form.value.name?.trim()) { ElMessage.warning('请输入大棚名称'); return }
  formLoading.value = true
  try {
    if (isEdit.value) {
      const data: GreenhouseUpdate = {
        name: form.value.name,
        location: form.value.location,
        area: form.value.area,
        notes: form.value.notes,
        status: undefined,
      }
      const result = await updateGreenhouse(editingId.value, data)
      if (result) {
        ElMessage.success('大棚信息已更新')
      } else {
        ElMessage.error('更新失败，请稍后重试')
        return
      }
    } else {
      const result = await createGreenhouse(form.value)
      if (result) {
        ElMessage.success('大棚已创建')
      } else {
        ElMessage.error('创建失败，请稍后重试')
        return
      }
    }
    dialogVisible.value = false
    await load()
  } catch {
    ElMessage.error('操作失败，请检查网络连接')
  } finally { formLoading.value = false }
}

async function handleDelete(row: GreenhouseInfo) {
  if ((row.deviceCount ?? 0) > 0) {
    ElMessage.warning(`「${row.name}」下有 ${row.deviceCount} 台设备，请先移除设备后再删除`)
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除大棚「${row.name}」吗？此操作不可恢复。`, '确认删除', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消',
    })
  } catch { return }

  const r = await deleteGreenhouse(row.id)
  if (r.ok) {
    ElMessage.success(`已删除「${row.name}」`)
    await load()
  } else {
    ElMessage.error(r.msg || '删除失败')
  }
}

function statusText(s: string) {
  return s === 'normal' ? '正常' : s === 'maintenance' ? '维护中' : '已关闭'
}
function statusType(s: string): 'success' | 'warning' | 'info' {
  return s === 'normal' ? 'success' : s === 'maintenance' ? 'warning' : 'info'
}
function formatDate(s: string | null) {
  if (!s) return '—'
  return new Date(s).toLocaleDateString('zh-CN')
}
</script>

<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">🏠 大棚管理</span>
        <div class="header-right">
          <el-input v-model="searchText" placeholder="搜索编号/名称/位置" clearable size="default" style="width: 220px" prefix-icon="Search" />
          <el-button v-if="isAdmin()" type="primary" @click="openCreate">+ 新增大棚</el-button>
        </div>
      </div>
    </template>

    <el-table :data="filteredList" v-loading="loading" stripe border style="width: 100%; min-width: 900px"
      empty-text="暂无大棚数据">
      <el-table-column type="index" label="#" width="50" align="center" />
      <el-table-column prop="code" label="编号" width="100" />
      <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="location" label="位置" width="100" />
      <el-table-column prop="area" label="面积(m²)" width="100" align="center">
        <template #default="scope">
          {{ scope.row.area ?? '—' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)" size="small" effect="dark" round>
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deviceCount" label="设备数" width="80" align="center">
        <template #default="scope">
          <span :style="{ color: scope.row.deviceCount ? '#303133' : '#c0c4cc', fontWeight: scope.row.deviceCount ? 600 : 400 }">
            {{ scope.row.deviceCount ?? 0 }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="notes" label="备注" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <span :style="{ color: scope.row.notes ? '#606266' : '#c0c4cc' }">{{ scope.row.notes || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="120" align="center">
        <template #default="scope">
          <span style="font-size:12px;color:#909399">{{ formatDate(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="isAdmin()" label="操作" width="160" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-popconfirm title="确定删除该大棚吗?" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 底部统计 -->
    <div class="table-footer">
      <span>共 <strong>{{ filteredList.length }}</strong> 个大棚，
            设备总计 <strong>{{ filteredList.reduce((s, g) => s + (g.deviceCount ?? 0), 0) }}</strong> 台</span>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" label-width="80px" label-position="right">
        <el-form-item label="编号" required>
          <el-input v-model="form.code" :disabled="isEdit" placeholder="如 GH-A" maxlength="32" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="如 A区葛仙米养殖大棚" maxlength="64" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="如 A区" maxlength="128" />
        </el-form-item>
        <el-form-item label="面积(m²)">
          <el-input-number v-model="form.area" :min="0" :precision="1" style="width:100%" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="3" placeholder="选填，如大棚用途、特殊说明等" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.page-card { border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.page-title { font-size: 18px; font-weight: 700; color: #303133; }
.header-right { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.table-footer { margin-top: 12px; font-size: 13px; color: #909399; }

@media (max-width: 768px) {
  .page-title { font-size: 16px; }
  .header-right { flex-direction: column; align-items: stretch; }
  .header-right .el-input { width: 100% !important; }
}
</style>
