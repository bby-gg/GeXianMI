<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchUsers, updateUserRole, resetUserPassword } from '@/api'
import type { UserAccount } from '@/api'

const users = ref<UserAccount[]>([])
const loading = ref(false)

const ROLE_OPTIONS = [
  { value: 'admin', label: '管理员' },
  { value: 'operator', label: '操作员' },
  { value: 'viewer', label: '查看者' },
]

onMounted(() => load())

async function load() {
  loading.value = true
  try { users.value = await fetchUsers() } catch { ElMessage.error('加载用户失败') }
  loading.value = false
}

function roleLabel(role: string) {
  return ROLE_OPTIONS.find(o => o.value === role)?.label ?? role
}

async function handleRoleChange(row: UserAccount, role: string) {
  if (await updateUserRole(row.id, role)) {
    ElMessage.success(`${row.username} 角色已改为「${roleLabel(role)}」`)
    await load()
  } else {
    ElMessage.error('更新失败')
  }
}

async function handleResetPassword(row: UserAccount) {
  const { value } = await ElMessageBox.prompt(
    `为 ${row.username} 设置新密码（至少 6 位）`,
    '重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPattern: /^.{6,}$/,
      inputErrorMessage: '密码至少 6 位',
    },
  )
  if (value && await resetUserPassword(row.id, value)) {
    ElMessage.success('密码已重置')
  }
}

function formatTime(t: string | null) {
  if (!t) return '—'
  return t.replace('T', ' ').slice(0, 19)
}
</script>

<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">👤 用户管理</span>
        <span class="page-hint">管理账号角色、重置密码</span>
      </div>
    </template>

    <el-table :data="users" v-loading="loading" stripe border style="width: 100%; min-width: 760px" empty-text="暂无用户">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column label="角色" width="180" align="center">
        <template #default="scope">
          <el-select :model-value="scope.row.role" size="small" style="width: 120px"
            @change="handleRoleChange(scope.row, $event)">
            <el-option v-for="o in ROLE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="最后登录" width="180" align="center">
        <template #default="scope">{{ formatTime(scope.row.lastLoginAt) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="180" align="center">
        <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="handleResetPassword(scope.row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.page-card { border-radius: 8px; }
.page-header { display: flex; justify-content: space-between; align-items: baseline; flex-wrap: wrap; gap: 10px; }
.page-title { font-size: 18px; font-weight: 700; color: #303133; }
.page-hint { font-size: 13px; color: #909399; }
</style>
