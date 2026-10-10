<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchOperators, fetchUserGreenhouses, fetchAllGreenhousesForAccess, saveUserAccess } from '@/api'
import type { OperatorInfo, GreenhouseInfo } from '@/api'

// ==================== 数据 ====================

const operators = ref<OperatorInfo[]>([])
const allGreenhouses = ref<GreenhouseInfo[]>([])
const selectedUserId = ref<number | null>(null)
const authorizedIds = ref<number[]>([])
const saving = ref(false)
const operatorFilter = ref('')
const changed = ref(false)

// ==================== 计算属性 ====================

const filteredOperators = computed(() => {
  if (!operatorFilter.value) return operators.value
  const kw = operatorFilter.value.toLowerCase()
  return operators.value.filter(o => o.username.toLowerCase().includes(kw))
})

const selectedOperator = computed(() =>
  operators.value.find(o => o.id === selectedUserId.value) ?? null
)

/** 已授权大棚数量（当前选中用户） */
const authorizedCount = computed(() => authorizedIds.value.length)

/** 大棚网格数据：带选中状态 */
const greenhouseCards = computed(() =>
  allGreenhouses.value.map(g => ({
    ...g,
    checked: authorizedIds.value.includes(g.id),
  }))
)

// ==================== 生命周期 ====================

onMounted(async () => {
  operators.value = await fetchOperators()
  allGreenhouses.value = await fetchAllGreenhousesForAccess()
  if (operators.value.length > 0) {
    selectedUserId.value = operators.value[0].id
  }
})

watch(selectedUserId, async (uid) => {
  changed.value = false
  if (!uid) { authorizedIds.value = []; return }
  authorizedIds.value = await fetchUserGreenhouses(uid)
})

// ==================== 方法 ====================

function toggleGreenhouse(ghId: number) {
  const idx = authorizedIds.value.indexOf(ghId)
  if (idx >= 0) {
    authorizedIds.value.splice(idx, 1)
  } else {
    authorizedIds.value.push(ghId)
  }
  changed.value = true
}

function selectAll() {
  authorizedIds.value = allGreenhouses.value.map(g => g.id)
  changed.value = true
}

function deselectAll() {
  authorizedIds.value = []
  changed.value = true
}

async function handleSave() {
  if (!selectedUserId.value) return
  saving.value = true
  try {
    await saveUserAccess(selectedUserId.value, authorizedIds.value)
    changed.value = false
    ElMessage.success('授权已保存')
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

// ==================== 工具函数 ====================

function statusClass(status: string) {
  return status === 'normal' ? 'dot--green' : status === 'maintenance' ? 'dot--orange' : 'dot--gray'
}

function statusLabel(status: string) {
  return status === 'normal' ? '运行中' : status === 'maintenance' ? '维护中' : '已关闭'
}
</script>

<template>
  <div class="access-page">
    <!-- ====== 顶部统计卡片 ====== -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #ecf5ff; color: #409eff;">👤</div>
          <div class="stat-info">
            <div class="stat-num">{{ operators.length }}</div>
            <div class="stat-label">操作员总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f0f9eb; color: #67c23a;">🏠</div>
          <div class="stat-info">
            <div class="stat-num">{{ allGreenhouses.length }}</div>
            <div class="stat-label">大棚总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c;">📋</div>
          <div class="stat-info">
            <div class="stat-num">{{ operators.filter(o => authorizedIds.length > 0 && selectedUserId === o.id).length || authorizedCount }}</div>
            <div class="stat-label">当前已授权大棚</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #fef0f0; color: #f56c6c;">⚙️</div>
          <div class="stat-info">
            <div class="stat-num">{{ allGreenhouses.length - authorizedCount }}</div>
            <div class="stat-label">未授权大棚</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ====== 主体：左右分栏 ====== -->
    <el-row :gutter="20" class="main-row">
      <!-- 左侧：操作员列表 -->
      <el-col :xs="24" :lg="7">
        <div class="panel panel--operators">
          <div class="panel__header">
            <span class="panel__title">操作员列表</span>
            <el-input
              v-model="operatorFilter"
              placeholder="搜索用户名…"
              size="small"
              clearable
              :prefix-icon="'Search'"
              style="width: 160px;"
            />
          </div>

          <div class="operator-list" v-if="filteredOperators.length > 0">
            <div
              v-for="op in filteredOperators"
              :key="op.id"
              class="operator-card"
              :class="{ 'operator-card--active': selectedUserId === op.id }"
              @click="selectedUserId = op.id"
            >
              <div class="operator-card__avatar">
                {{ op.username.charAt(0).toUpperCase() }}
              </div>
              <div class="operator-card__body">
                <div class="operator-card__name">{{ op.username }}</div>
                <div class="operator-card__role">操作员</div>
              </div>
              <div class="operator-card__arrow">›</div>
            </div>
          </div>

          <div v-else class="panel__empty">
            <div class="empty-icon">🔍</div>
            <div class="empty-text">未找到匹配的操作员</div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：大棚授权区 -->
      <el-col :xs="24" :lg="17">
        <div class="panel panel--greenhouses">
          <!-- 头部 -->
          <div class="panel__header">
            <div class="panel__title-row">
              <span class="panel__title">
                大棚授权
              </span>
              <span v-if="selectedOperator" class="panel__subtitle">
                {{ selectedOperator.username }} 当前可查看
                <strong>{{ authorizedCount }}</strong> 个大棚
              </span>
            </div>

            <div v-if="selectedUserId" class="panel__actions">
              <el-button size="small" text @click="selectAll">全选</el-button>
              <el-button size="small" text @click="deselectAll">清空</el-button>
              <el-button
                type="primary"
                size="small"
                :loading="saving"
                :disabled="!changed"
                @click="handleSave"
              >
                保存
              </el-button>
            </div>
          </div>

          <!-- 大棚卡片网格 -->
          <div v-if="selectedUserId && greenhouseCards.length > 0" class="greenhouse-grid">
            <div
              v-for="gh in greenhouseCards"
              :key="gh.id"
              class="gh-card"
              :class="{ 'gh-card--checked': gh.checked }"
              @click="toggleGreenhouse(gh.id)"
            >
              <!-- 选中勾 -->
              <div class="gh-card__check">
                <span v-if="gh.checked">✓</span>
              </div>

              <!-- 内容 -->
              <div class="gh-card__body">
                <div class="gh-card__code">{{ gh.code }}</div>
                <div class="gh-card__name">{{ gh.name }}</div>
                <div class="gh-card__meta">
                  <span class="gh-card__loc" v-if="gh.location">
                    📍 {{ gh.location }}
                  </span>
                  <span class="gh-card__status">
                    <i class="dot" :class="statusClass(gh.status)"></i>
                    {{ statusLabel(gh.status) }}
                  </span>
                </div>
                <div class="gh-card__devices" v-if="gh.deviceCount !== undefined">
                  {{ gh.deviceCount }} 台设备
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="!selectedUserId" class="panel__empty panel__empty--large">
            <div class="empty-icon">👈</div>
            <div class="empty-text">请先选择左侧操作员</div>
            <div class="empty-desc">点击操作员后，可在此为其分配大棚访问权限</div>
          </div>

          <div v-else class="panel__empty panel__empty--large">
            <div class="empty-icon">🏠</div>
            <div class="empty-text">暂无可分配的大棚</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
/* ==================== 整体 ==================== */
.access-page {
  max-width: 1400px;
  margin: 0 auto;
}

/* ==================== 统计卡片 ==================== */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 10px;
  padding: 18px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
  border: 1px solid #ebeef5;
  transition: transform .15s, box-shadow .15s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,.06);
}
.stat-icon {
  width: 44px; height: 44px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.stat-info { min-width: 0; }
.stat-num {
  font-size: 24px; font-weight: 700; color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px; color: #909399;
  margin-top: 2px;
}

/* ==================== 面板通用 ==================== */
.main-row { align-items: stretch; }

.panel {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
  display: flex; flex-direction: column;
  overflow: hidden;
  height: 100%;
  min-height: 460px;
}
.panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f2f3f5;
  background: #fafbfc;
  flex-shrink: 0;
}
.panel__title-row {
  display: flex; flex-direction: column; gap: 2px;
}
.panel__title {
  font-size: 15px; font-weight: 700; color: #303133;
}
.panel__subtitle {
  font-size: 12px; color: #909399;
}
.panel__actions {
  display: flex; align-items: center; gap: 8px;
  flex-shrink: 0;
}
.panel__empty {
  flex: 1;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 48px 20px 40px;
}
.panel__empty--large {
  padding: 80px 20px;
}
.empty-icon {
  font-size: 40px; margin-bottom: 12px;
}
.empty-text {
  font-size: 14px; font-weight: 600; color: #909399;
}
.empty-desc {
  font-size: 12px; color: #c0c4cc; margin-top: 6px;
}

/* ==================== 操作员列表 ==================== */
.operator-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.operator-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all .15s;
  margin-bottom: 2px;
  border: 1px solid transparent;
}
.operator-card:hover {
  background: #f5f7fa;
}
.operator-card--active {
  background: #ecf5ff;
  border-color: #c6e2ff;
  box-shadow: 0 0 0 1px rgba(64,158,255,.15);
}
.operator-card__avatar {
  width: 38px; height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a0cfff 0%, #409eff 100%);
  color: #fff;
  font-size: 16px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.operator-card__body { flex: 1; min-width: 0; }
.operator-card__name {
  font-size: 14px; font-weight: 600; color: #303133;
}
.operator-card__role {
  font-size: 12px; color: #909399; margin-top: 2px;
}
.operator-card__arrow {
  font-size: 18px; color: #c0c4cc;
  transition: transform .15s;
}
.operator-card--active .operator-card__arrow {
  color: #409eff;
}

/* ==================== 大棚卡片网格 ==================== */
.greenhouse-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
  padding: 16px 20px;
  overflow-y: auto;
  align-content: start;
}

.gh-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 16px;
  border-radius: 10px;
  border: 2px solid #ebeef5;
  background: #fff;
  cursor: pointer;
  transition: all .18s;
  user-select: none;
}
.gh-card:hover {
  border-color: #c6e2ff;
  box-shadow: 0 4px 14px rgba(64,158,255,.1);
}
.gh-card--checked {
  border-color: #409eff;
  background: #f5f9ff;
  box-shadow: 0 0 0 1px rgba(64,158,255,.2), 0 2px 10px rgba(64,158,255,.08);
}
.gh-card--checked:hover {
  border-color: #337ecc;
}

/* 选中勾 */
.gh-card__check {
  position: absolute;
  top: 10px; right: 10px;
  width: 22px; height: 22px;
  border-radius: 50%;
  border: 2px solid #dcdfe6;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
  color: #fff;
  background: #fff;
  transition: all .15s;
}
.gh-card--checked .gh-card__check {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.gh-card__body {
  flex: 1;
  padding-right: 22px;
}

.gh-card__code {
  font-size: 11px; font-weight: 700; color: #409eff;
  text-transform: uppercase; letter-spacing: .5px;
  margin-bottom: 4px;
}
.gh-card__name {
  font-size: 15px; font-weight: 600; color: #303133;
  margin-bottom: 8px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.gh-card__meta {
  display: flex; align-items: center; gap: 12px;
  font-size: 12px; color: #909399;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.gh-card__loc {
  display: flex; align-items: center; gap: 3px;
}
.gh-card__status {
  display: flex; align-items: center; gap: 4px;
}
.gh-card__devices {
  font-size: 12px; color: #c0c4cc;
  margin-top: 4px;
}

/* 状态圆点 */
.dot {
  display: inline-block;
  width: 7px; height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot--green  { background: #67c23a; }
.dot--orange { background: #e6a23c; }
.dot--gray   { background: #c0c4cc; }

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
  .greenhouse-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}
@media (max-width: 992px) {
  .panel--operators { margin-bottom: 16px; min-height: auto; }
  .operator-list { max-height: 240px; }
}
@media (max-width: 640px) {
  .stats-row .el-col { margin-bottom: 10px; }
  .stats-row .el-col:nth-child(odd) { padding-right: 6px; }
  .stats-row .el-col:nth-child(even) { padding-left: 6px; }
  .stat-card { padding: 14px 12px; gap: 10px; }
  .stat-num { font-size: 20px; }
  .stat-icon { width: 36px; height: 36px; font-size: 16px; }

  .greenhouse-grid {
    grid-template-columns: 1fr;
  }
  .panel__header {
    flex-direction: column;
    align-items: flex-start;
  }
  .panel__actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
