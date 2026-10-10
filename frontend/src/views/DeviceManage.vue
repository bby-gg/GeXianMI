<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchDevices, fetchGreenhouses, createDevice, updateDevice, deleteDevice } from '@/api'
import type { DeviceInfo, DeviceCreate, DeviceUpdate, GreenhouseInfo } from '@/types/env.d'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isAdmin } from '@/utils/auth'

const devices = ref<DeviceInfo[]>([])
const greenhouses = ref<GreenhouseInfo[]>([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const isEdit = ref(false)
const editingCode = ref('')
const formLoading = ref(false)

const form = ref<DeviceCreate>({ code: '', name: '', greenhouseId: undefined, location: '', notes: '' })

onMounted(async () => {
  greenhouses.value = await fetchGreenhouses()
  await loadDevices()
})

async function loadDevices() {
  loading.value = true
  devices.value = await fetchDevices()
  loading.value = false
}

function openCreate() {
  isEdit.value = false; dialogTitle.value = '新增设备'; editingCode.value = ''
  form.value = { code: '', name: '', greenhouseId: undefined, location: '', notes: '' }
  dialogVisible.value = true
}

function openEdit(row: DeviceInfo) {
  isEdit.value = true; dialogTitle.value = '编辑设备'; editingCode.value = row.code
  form.value = {
    code: row.code, name: row.name,
    greenhouseId: row.greenhouseId ?? undefined,
    location: row.location, notes: row.notes,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.code || !form.value.name) { ElMessage.warning('编号和名称不能为空'); return }
  formLoading.value = true
  try {
    if (isEdit.value) {
      const data: DeviceUpdate = {
        name: form.value.name, greenhouseId: form.value.greenhouseId,
        location: form.value.location, notes: form.value.notes,
      }
      if (await updateDevice(editingCode.value, data)) ElMessage.success('已更新')
    } else {
      if (await createDevice(form.value)) ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await loadDevices()
  } finally { formLoading.value = false }
}

async function handleDelete(row: DeviceInfo) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name} (${row.code})」？`, '确认删除', { type: 'warning' })
  } catch { return }
  if (await deleteDevice(row.code)) { ElMessage.success('已删除'); await loadDevices() }
}

function statusType(s: string) {
  return s === 'online' ? 'success' : s === 'offline' ? 'info' : 'warning'
}
function statusText(s: string) {
  return s === 'online' ? '在线' : s === 'offline' ? '离线' : '维护中'
}
</script>

<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">⚙️ 设备管理</span>
        <el-button v-if="isAdmin()" type="primary" @click="openCreate">+ 新增设备</el-button>
      </div>
    </template>

    <div class="table-wrap">
      <el-table :data="devices" v-loading="loading" stripe border style="width: 100%; min-width: 800px"
        empty-text="暂无设备">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="code" label="设备编号" min-width="110" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="greenhouseName" label="所属大棚" min-width="130" />
        <el-table-column prop="location" label="位置" min-width="100" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)" size="small" effect="dark" round>
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <span :style="{ color: scope.row.notes ? '#606266' : '#c0c4cc' }">{{ scope.row.notes || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small"
              @click="$router.push({ path: '/history', query: { deviceId: scope.row.code } })">数据</el-button>
            <template v-if="isAdmin()">
              <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="设备编号" required>
          <el-input v-model="form.code" :disabled="isEdit" placeholder="如 B-005" maxlength="32" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="如 A区-5号筒" maxlength="64" />
        </el-form-item>
        <el-form-item label="所属大棚">
          <el-select v-model="form.greenhouseId" placeholder="选择大棚" clearable style="width: 100%">
            <el-option v-for="gh in greenhouses" :key="gh.id" :label="gh.name" :value="gh.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="form.location" placeholder="如 A区大棚内" maxlength="128" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.page-card { border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.page-title { font-size: 18px; font-weight: 700; color: #303133; }
.table-wrap { overflow-x: auto; }
@media (max-width: 768px) { .page-title { font-size: 16px; } }
</style>
