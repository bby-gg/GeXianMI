<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchDevices, fetchActiveAlerts } from '@/api'
import { isLoggedIn, logout as doLogout, getUser, isAdmin } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const onlineCount = ref(0)
const alertCount = ref(0)
const isCollapse = ref(false)
const showMobileMenu = ref(false)
const isMobile = ref(false)
const user = ref(getUser())
const isLoginPage = ref(route.path === '/login')

function checkScreen() {
  const small = window.innerWidth < 768
  isMobile.value = small
  isCollapse.value = window.innerWidth < 992
  if (!small) showMobileMenu.value = false
}

window.addEventListener('resize', checkScreen)
checkScreen()

function onNav() {
  showMobileMenu.value = false
}

function toggleMenu() {
  if (isMobile.value) {
    showMobileMenu.value = !showMobileMenu.value
  } else {
    isCollapse.value = !isCollapse.value
  }
}

let timer: ReturnType<typeof setInterval> | null = null

// 监听路由变化，更新登录页状态，离开登录页时刷新用户信息
watch(() => route.path, (path) => {
  isLoginPage.value = path === '/login'
  if (!isLoginPage.value) {
    // 从登录页进入业务页时，重新读取 localStorage 中的用户信息
    user.value = getUser()
    if (isLoggedIn() && onlineCount.value === 0) {
      loadDeviceCount()
      loadAlertCount()
    }
  }
})

onMounted(() => {
  if (!isLoginPage.value && isLoggedIn()) {
    loadDeviceCount()
    loadAlertCount()
    timer = setInterval(() => { loadDeviceCount(); loadAlertCount(); }, 30000)
  }
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function loadDeviceCount() {
  const devices = await fetchDevices()
  onlineCount.value = devices.filter(d => d.status === 'online').length
}

async function loadAlertCount() {
  try {
    const alerts = await fetchActiveAlerts()
    alertCount.value = alerts.length
  } catch { /* ignore */ }
}

function handleLogout() {
  doLogout()
  router.push('/login')
}

// ---- 侧栏菜单项 ----
interface MenuItem { path: string; icon: string; label: string; adminOnly?: boolean }

const allMenuItems: MenuItem[] = [
  { path: '/monitor',      icon: '📊', label: '实时监测' },
  { path: '/history',      icon: '📈', label: '历史数据' },
  { path: '/greenhouses',  icon: '🏠', label: '大棚管理' },
  { path: '/devices',      icon: '⚙️', label: '设备管理' },
  { path: '/alerts',       icon: '🔔', label: '告警记录' },
  { path: '/rules',        icon: '🔧', label: '告警规则', adminOnly: true },
  { path: '/access',       icon: '👥', label: '授权管理', adminOnly: true },
]

/** 根据角色过滤可见菜单项 */
const visibleMenuItems = computed(() =>
  allMenuItems.filter(item => !item.adminOnly || isAdmin())
)
</script>

<template>
  <!-- 登录页独立显示，不走布局壳 -->
  <div v-if="isLoginPage" class="login-shell">
    <router-view />
  </div>

  <!-- 业务页：侧边栏 + 顶栏 + 主内容区 -->
  <el-container v-else class="app-shell">
    <!-- 移动端遮罩 -->
    <div
      v-show="showMobileMenu"
      class="mobile-overlay"
      @click="showMobileMenu = false"
    ></div>

    <!-- 左侧菜单栏 -->
    <el-aside
      :width="isMobile && !showMobileMenu ? '0px' : isCollapse ? '68px' : '230px'"
      :class="{ 'mobile-aside': showMobileMenu }"
      class="app-sidebar"
    >
      <!-- Logo -->
      <div class="sidebar-logo">
        <span class="sidebar-logo-icon">🌿</span>
        <span v-show="!isCollapse" class="sidebar-logo-text">葛仙米监测</span>
      </div>

      <!-- 菜单 -->
      <el-menu
        :default-active="route.path"
        background-color="transparent"
        text-color="rgba(255,255,255,.7)"
        active-text-color="#fff"
        :collapse="isCollapse && !showMobileMenu"
        router
        @select="onNav"
      >
        <el-menu-item
          v-for="item in visibleMenuItems"
          :key="item.path"
          :index="item.path"
          class="sidebar-menu-item"
        >
          <div class="menu-item-inner">
            <span class="menu-icon">{{ item.icon }}</span>
            <span class="menu-label">{{ item.label }}</span>
            <span v-if="item.path === '/alerts' && alertCount > 0" class="alert-tag">{{ alertCount > 99 ? '99+' : alertCount }}</span>
          </div>
        </el-menu-item>
      </el-menu>

      <!-- 底部信息 -->
      <div v-show="!isCollapse" class="sidebar-footer">
        <div class="sidebar-footer-dot"></div>
        <span>系统运行中</span>
      </div>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container class="main-area">
      <!-- 顶部标题栏 -->
      <el-header class="top-header">
        <div class="top-header-left">
          <el-button text class="menu-toggle-btn" @click="toggleMenu">
            ☰
          </el-button>
          <span class="top-title">
            葛仙米生长环境智能监测系统
          </span>
        </div>
        <div class="top-header-right">
          <el-tag type="success" effect="plain" round size="small" class="online-tag">
            在线 {{ onlineCount }}
          </el-tag>
          <el-dropdown trigger="click">
            <span class="user-dropdown-trigger">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="user-name">{{ user?.username || '用户' }}</span>
              <span class="dropdown-arrow">▼</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <span style="font-size:12px;color:#909399">{{ user?.role === 'admin' ? '管理员' : '操作员' }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <span style="color:#f56c6c">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style>
/* 登录页壳：无任何布局，全屏白底 */
.login-shell {
  height: 100vh;
  width: 100vw;
}
/* ========== 全局重置 ========== */
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* ========== 布局壳 ========== */
.app-shell {
  height: 100vh;
}

/* ========== 侧栏 ========== */
.app-sidebar {
  background: linear-gradient(180deg, #001529 0%, #002140 100%) !important;
  overflow: hidden;
  transition: width .3s cubic-bezier(.4,0,.2,1);
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 64px;
  border-bottom: 1px solid rgba(255,255,255,.08);
  white-space: nowrap;
  overflow: hidden;
}
.sidebar-logo-icon {
  font-size: 22px;
  flex-shrink: 0;
}
.sidebar-logo-text {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  letter-spacing: .5px;
}

/* 菜单透明背景，继承侧栏渐变 */
.app-sidebar .el-menu {
  border-right: none !important;
  background: transparent !important;
  padding-top: 4px;
}
.sidebar-menu-item {
  margin: 2px 8px;
  border-radius: 6px;
  transition: background .2s;
}
.sidebar-menu-item:hover {
  background: rgba(255,255,255,.08) !important;
}
.sidebar-menu-item.is-active {
  background: rgba(64,158,255,.2) !important;
  color: #fff !important;
}
.menu-icon {
  font-size: 18px;
  flex-shrink: 0;
}

/* 菜单项内部：图标 + 文字 + 角标 水平排列 */
.menu-item-inner {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.menu-label {
  white-space: nowrap;
}

/* 告警角标 — 红色小圆标贴在文字右侧 */
.alert-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px; height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px; font-weight: 600;
  line-height: 1;
  flex-shrink: 0;
  animation: badge-pulse 2s ease-in-out infinite;
}
@keyframes badge-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.12); }
}

/* 侧栏底部状态 */
.sidebar-footer {
  position: absolute;
  bottom: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255,255,255,.45);
  font-size: 12px;
}
.sidebar-footer-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #67c23a;
  box-shadow: 0 0 6px rgba(103,194,58,.5);
}

/* ========== 顶部标题栏 ========== */
.top-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
}
.top-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.top-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  white-space: nowrap;
}
.menu-toggle-btn {
  font-size: 22px;
  padding: 4px 6px;
  color: #606266;
}
.menu-toggle-btn:hover {
  color: #409eff;
}
.top-header-right {
  flex-shrink: 0;
  display: flex; align-items: center; gap: 16px;
}
.online-tag { font-size: 12px; }
.user-dropdown-trigger {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 8px; border-radius: 6px; transition: background .2s;
}
.user-dropdown-trigger:hover { background: #f5f7fa; }
.user-name { font-size: 14px; color: #303133; }
.dropdown-arrow { font-size: 10px; color: #c0c4cc; }

/* ========== 主内容区 ========== */
.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* ========== 移动端遮罩 ========== */
.mobile-overlay {
  display: none;
}

/* ========== 平板 ========== */
@media (max-width: 992px) {
  .top-title { font-size: 16px; }
  .main-content { padding: 14px; }
}

/* ========== 手机端 ========== */
@media (max-width: 768px) {
  .top-header { padding: 0 12px; height: 56px; }
  .top-title { font-size: 14px; }
  .main-content { padding: 10px; }
  .sidebar-logo { height: 56px; }

  /* 侧栏浮层 */
  .mobile-aside {
    position: fixed !important;
    top: 0; left: 0; bottom: 0;
    z-index: 2000;
    width: 230px !important;
    box-shadow: 4px 0 24px rgba(0,0,0,.2);
  }
  .mobile-overlay {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 1999;
    background: rgba(0,0,0,.4);
  }
}
</style>
