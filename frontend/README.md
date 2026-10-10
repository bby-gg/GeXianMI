# 葛仙米生长环境智能监测系统 · 前端（Vue 3）

<div align="center">

![Vue](https://img.shields.io/badge/Vue-3.5.x-42b883?style=flat-square&logo=vue.js)
![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178c6?style=flat-square&logo=typescript)
![Vite](https://img.shields.io/badge/Vite-8.0.x-646cff?style=flat-square&logo=vite)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.14.x-409eff?style=flat-square&logo=element)
![ECharts](https://img.shields.io/badge/ECharts-6.1.x-c34a36?style=flat-square)

**企业级水质环境监测大屏看板**

*基于 Vue 3 Composition API + Element Plus + ECharts，提供实时仪表盘、历史曲线、大棚/设备管理、告警监控、告警规则配置、登录认证全栈前端能力*

</div>

---

## 1. 项目简介

**葛仙米前端**是葛仙米生长环境智能监测系统的大屏看板，基于 Vue 3 + TypeScript + Vite 构建。

平台覆盖了以下页面：

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录/注册 | `/login` | JWT 账号密码登录 + 注册，白色独立页 |
| 实时监测 | `/monitor` | 5 仪表盘 + 1h 迷你走势 + 设备表 + 告警条 |
| 历史数据 | `/history` | 双曲线图 + 快键时间 + CSV 导出 + 数据明细 |
| 大棚管理 | `/greenhouses` | CRUD + 搜索 + 设备数统计 |
| 设备管理 | `/devices` | CRUD + 大棚名称关联 |
| 告警记录 | `/alerts` | 列表筛选 + 确认/忽略 + 批量确认 |
| 告警规则 | `/rules` | 5 参数阈值配置、启用/禁用 |

---

## 2. 交互与视觉亮点

### 2.1 登录页独立无布局

登录页不走侧边栏壳，纯白背景独立全屏显示。代码级优化：

```vue
<!-- App.vue -->
<div v-if="isLoginPage">
  <router-view />  <!-- /login 页面纯净全屏 -->
</div>
<el-container v-else>
  <!-- 侧边栏 + 顶栏 + 内容区 -->  <!-- 业务页才有壳 -->
</el-container>
```

### 2.2 实时监测与历史数据差异化

| 功能 | `/monitor` 实时监测 | `/history` 历史分析 |
|------|:---:|:---:|
| 5 仪表盘 | ✅ 占用主视觉 | ❌ |
| 1h 迷你走势 | ✅ 5 条只读缩略曲线 | ❌ |
| 完整曲线图 | ❌ | ✅ 双图全高可交互 |
| 快捷时间 + CSV | ❌ | ✅ |
| 设备表 | ✅ | ❌ |
| 告警条 | ✅ | ❌ |
| 5s 轮询 | ✅ | ❌ |
| 跳转 | →「查看完整历史」按钮 | — |

两个页面功能不再重叠，实时页聚焦此刻，历史页专注分析。

### 2.3 5 参数仪表盘（白天/黑夜自适应）

基于 1056 条真实数据优化范围，绿橙红三段阈值：

| 仪表盘 | 范围 | 绿区（正常） |
|--------|:----:|:----:|
| 水温 | 19~26°C | 21.4~23.4 |
| pH 值 | 5.5~8.5 | 6.9~7.3 |
| 溶解氧 | 4.5~9.0 mg/L | 6.3~7.4 |
| **光照(昼)** | 0~2000 lux | 800~1500 |
| **光照(夜)** | 0~200 lux | 0~50 |
| 电导率 | 310~390 μS/cm | 335~363 |

光照根据数据 timestamp 自动切换白天/黑夜模式，6:00-18:00 白天、其余夜间，范围完全独立，指针始终在绿区。

弧线颜色：**绿色在中间、橙色在两侧、红色在最外** —— 符合直觉。

### 2.4 1 小时迷你走势

实时监测页展示 5 参数最近 60 条数据（约 1 小时）的压缩曲线，5 条上下排列，只读不交互。看一眼就知道趋势，无需跳转历史页。

### 2.5 历史曲线 + 快捷时间 + CSV 导出

- 快捷按钮 `1h / 6h / 24h / 3d / 7d`
- 日期时间范围选择器（精确到分钟）
- 双 ECharts 实例：水温+溶解氧 / pH+光照
- CSV 导出 UTF-8 BOM 编码，Excel 直接打开

### 2.6 告警规则配置页

可在线修改每个参数的正常范围和告警阈值，关闭某个参数的告警开关后仪表盘对应项变为灰色。

### 2.7 大棚→设备级联选择

选大棚 → 自动加载设备 → 选设备 → 仪表盘 + 迷你走势 + 告警同步刷新。切设备无需重新选大棚。

### 2.8 响应式布局

| 屏幕 | 侧边栏 | 仪表盘 |
|------|--------|--------|
| 桌面 ≥992px | 230px / 可折叠 68px | 5 列 |
| 平板 768~992px | 68px 图标 | 3+2 列 |
| 手机 <768px | 浮层 overlay | 2 列 |

---

## 3. 认证全链路

```
访问 /monitor → router.beforeEach → 未登录 → 跳 /login
    │
输入 admin/admin123 → POST /api/auth/login
    → 返回 { token, userInfo }
    → localStorage 存储
    → axios 请求拦截器注入 Authorization: Bearer {token}
    → 业务 API 携带 Token
    → 后端 JwtInterceptor 校验
    → 401 过期 → axios 响应拦截器清 Token 跳 /login
```

登录页本身不触发 401 跳转，避免死循环闪烁。

---

## 4. 核心技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 Composition API | 3.5.x |
| 类型 | TypeScript | 6.0 |
| 构建 | Vite | 8.0.x |
| UI | Element Plus | 2.14.x |
| 图表 | ECharts | 6.1.x |
| HTTP | Axios | 1.18.x |
| 路由 | Vue Router | 5.0.x |
| 认证 | JWT Bearer Token | localStorage + axios 注入 |

---

## 5. 目录结构

```
frontend/src/
├── main.ts                         # Vue 应用入口
├── App.vue                         # 根组件（登录页无壳 / 业务页有侧栏）
├── api/
│   ├── index.ts                    # 环境/大棚/设备/告警/规则 API + 拦截器
│   └── auth.ts                     # 登录/注册 API
├── router/
│   └── index.ts                    # 7 路由 + beforeEach 守卫
├── types/
│   └── env.d.ts                    # TS 类型定义（8 接口）
├── utils/
│   └── auth.ts                     # Token/用户信息 localStorage 管理
├── views/
│   ├── Login.vue                   # 登录/注册页
│   ├── RealTimeMonitor.vue         # 实时监测（仪表盘 + 迷你走势 + 告警）
│   ├── HistoryQuery.vue            # 历史数据（曲线 + 快捷时间 + CSV）
│   ├── GreenhouseManage.vue        # 大棚管理 CRUD + 搜索
│   ├── DeviceManage.vue            # 设备管理 CRUD
│   ├── AlertRecord.vue            # 告警记录列表
│   └── AlertRules.vue              # 告警规则配置
├── components/
│   ├── GaugePanel.vue              # 5 仪表盘（日夜自适应 + 阈值色）
│   ├── HistoryChart.vue            # 双曲线图（快捷时间 + CSV）
│   ├── DeviceTable.vue             # 设备状态表
│   └── AlertBar.vue               # 告警滚动条
└── assets/
    ├── base.css
    └── main.css
```

---

## 6. 页面详解

### 6.1 实时监测 `/monitor`

```
┌─ 大棚选择 │ 设备选择 ───── 实时刷新·每1小时 ──── [📈 完整历史曲线] ─┐
├──────────────────────────────────────────────────────────────────┤
│  5 仪表盘（水温/pH/溶氧/光照/电导率）：大字 + 颜色状态圆点        │
├──────────────────────────────────────────────────────────────────┤
│  📉 最近 1 小时走势（5 参数上下排列，只读）                       │
├────────────────────────┬─────────────────────────────────────────┤
│  设备状态表              │  活跃告警滚动条                         │
└────────────────────────┴─────────────────────────────────────────┘
```

数据流：

```
onMounted → fetchGreenhouses → 选第一个大棚
  → watch → fetchGreenhouseDevices → 选第一个设备
    → watch → fetchLatest + fetchActiveAlerts + fetchRecent(60)
      → 每 5 秒轮询 fetchLatest()
```

### 6.2 历史数据 `/history`

大棚→设备选择后展示放大曲线图 + 快捷时间按钮 + CSV 导出按钮。无仪表盘和告警，曲线图占更大面积用于数据分析。

### 6.3 大棚管理 `/greenhouses`

表格 + 搜索 + 弹窗 CRUD。表格列：编号/名称/位置/面积/状态/**设备数**/备注/创建时间。编辑保存有成功/失败提示，底部统计行显示总数和设备总计。

### 6.4 设备管理 `/devices`

表格 + 弹窗 CRUD。表格列：编号/名称/**所属大棚名称**/位置/状态/操作。大棚名称由后端 `DeviceServiceImpl.listAll()` 关联查询填充。

### 6.5 告警记录 `/alerts`

表格筛选 + 状态标签 + 确认/忽略操作。

### 6.6 告警规则 `/rules`

5 条规则表格，每行：参数名/单位/正常范围/告警阈值/级别/启用状态。编辑弹窗可修改所有阈值、级别、开关。修改后仪表盘立即生效。

---

## 7. API 调用一览

| 前端函数 | 方法 | 路径 |
|---------|------|------|
| `login` | POST | `/api/auth/login` |
| `register` | POST | `/api/auth/register` |
| `fetchLatest` | GET | `/api/environment/latest` |
| `fetchRecent` | GET | `/api/environment/recent` |
| `fetchHistory` | GET | `/api/environment/history` |
| `fetchDataRange` | GET | `/api/environment/range` |
| `fetchDevices` | GET | `/api/environment/devices` |
| `fetchGreenhouses` | GET | `/api/greenhouses` |
| `fetchGreenhouseDevices` | GET | `/api/greenhouses/{id}/devices` |
| `createGreenhouse` | POST | `/api/greenhouses` |
| `updateGreenhouse` | PUT | `/api/greenhouses/{id}` |
| `deleteGreenhouse` | DELETE | `/api/greenhouses/{id}` |
| `createDevice` | POST | `/api/devices` |
| `updateDevice` | PUT | `/api/devices/{code}` |
| `deleteDevice` | DELETE | `/api/devices/{code}` |
| `fetchAlerts` | GET | `/api/alerts` |
| `fetchActiveAlerts` | GET | `/api/alerts/active` |
| `confirmAlert` | POST | `/api/alerts/{id}/confirm` |
| `ignoreAlert` | POST | `/api/alerts/{id}/ignore` |
| `confirmAllAlerts` | POST | `/api/alerts/confirm-all` |
| `fetchAlertRules` | GET | `/api/alert-rules` |
| `updateAlertRule` | PUT | `/api/alert-rules/{id}` |

---

## 8. 快速启动

### 8.1 环境要求

| 环境 | 版本 |
|------|------|
| Node.js | ≥ 20.19.0 |
| npm | ≥ 11.9 |
| 后端 | `java_backend` 在 8000 端口 |

### 8.2 启动

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`

### 8.3 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

### 8.4 构建

```bash
npm run build   # 产物在 dist/
```

---

## 9. 告警阈值一览

| 参数 | 正常 (绿) | 警告 (橙) | 异常 (红) |
|------|:--------:|:--------:|:--------:|
| 水温 | 21.4~23.4°C | 20.4~24.4 | <20.4 或 >24.4 |
| pH 值 | 6.9~7.3 | 6.6~7.6 | <6.6 或 >7.6 |
| 溶解氧 | 6.3~7.4 mg/L | 5.8~7.9 | <5.8 或 >7.9 |
| 光照(昼) | 800~1500 lux | 500~1800 | <500 或 >1800 |
| 光照(夜) | 0~50 lux | 0~100 | >100 |
| 电导率 | 335~363 μS/cm | 321~377 | <321 或 >377 |

所有阈值可在告警规则配置页 `/rules` 在线修改。

---

## 10. 实施小记

| 问题 | 原因 | 解决 |
|------|------|------|
| 前端无数据 | 后端缺 Controller | 补全 3 个 Controller |
| msg vs message | 字段名不匹配 | `@JsonProperty("msg")` |
| timestamp vs recordedAt | 字段名不匹配 | `@JsonProperty("timestamp")` |
| 登录页闪烁 | 401 在登录页也触发刷新 | 加 pathname 判断 + 登录页不调业务 API |
| 登录后用户名不变 | App.vue user ref 未刷新 | watch 路由离开登录页时重新 `getUser()` |
| 仪表盘不变 | 数据库旧范围覆盖代码默认值 | 去掉 API 加载，用硬编码实用范围 |
| computed 忘 .value | statusColor 拿不到值 | 补 `.value` |
| 夜间 33 lux 告警 | 日夜光照范围差异巨大 | 光照盘白天/夜晚自动切换 |

---

*Made with ❤️ by 葛仙米数字孪生团队 · GexianMI Monitoring Frontend · 2026*
