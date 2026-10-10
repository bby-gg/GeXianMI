import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, isAdmin } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/monitor',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { noAuth: true },
    },
    {
      path: '/monitor',
      name: 'monitor',
      component: () => import('@/views/RealTimeMonitor.vue'),
    },
    {
      path: '/history',
      name: 'history',
      component: () => import('@/views/HistoryQuery.vue'),
    },
    {
      path: '/screen',
      name: 'screen',
      component: () => import('@/views/BigScreen.vue'),
    },
    {
      path: '/greenhouses',
      name: 'greenhouses',
      component: () => import('@/views/GreenhouseManage.vue'),
    },
    {
      path: '/devices',
      name: 'devices',
      component: () => import('@/views/DeviceManage.vue'),
    },
    {
      path: '/alerts',
      name: 'alerts',
      component: () => import('@/views/AlertRecord.vue'),
    },
    {
      path: '/rules',
      name: 'rules',
      component: () => import('@/views/AlertRules.vue'),
      meta: { adminOnly: true },
    },
    {
      path: '/access',
      name: 'access',
      component: () => import('@/views/AccessManage.vue'),
      meta: { adminOnly: true },
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('@/views/UserManage.vue'),
      meta: { adminOnly: true },
    },
  ],
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  if (to.meta.noAuth) {
    // 已登录却访问登录页 → 直接跳到监测页
    if (to.path === '/login' && isLoggedIn()) {
      next('/monitor')
    } else {
      next()
    }
  } else if (!isLoggedIn()) {
    next('/login')
  } else if (to.meta.adminOnly && !isAdmin()) {
    next('/monitor')
  } else {
    next()
  }
})

export default router
