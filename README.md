# 葛仙米生长环境智能监测系统 — 实施参考

## 一、项目目标

做一个 Web 端环境监测系统：
- 后端接收 JSON 环境数据，存入 MySQL
- 前端大屏看板展示仪表盘和历史曲线图
- 后续扩展：硬件设备联动、图像识别

---

## 二、技术栈

| 类别 | 选型 | 版本 | 用途 |
|------|------|------|------|
| 后端框架 | FastAPI | 0.124 | 写接口，接收/返回 JSON |
| 启动服务 | Uvicorn | 0.33 | 把 FastAPI 跑起来 |
| ORM | SQLAlchemy | 2.0 | Python 语法操作 MySQL |
| 数据库驱动 | PyMySQL | 1.1 | Python 连接 MySQL |
| 数据库 | MySQL | 8.0 | 存所有数据 |
| 前端框架 | Vue 3 + TypeScript | 最新 | 大屏看板页面 |
| UI 组件库 | Element Plus | 最新 | 菜单、按钮、表格、布局 |
| 图表库 | ECharts | 最新 | 仪表盘、折线图 |
| Python 环境 | Anaconda | `D:\anaconda_envs\jjb` | 项目独立环境 |
| 数据库工具 | DataGrip | — | 可视化操作数据库 |

---

## 三、数据流

```
模拟脚本 ──POST JSON──→ FastAPI 接口 ──SQLAlchemy──→ MySQL 存入
                                                        │
前端看板 ←──GET JSON─── FastAPI 接口 ←──SQLAlchemy── MySQL 查出
```

---

## 四、数据库设计

### 库名：`gexianmi`

### 第一阶段（现在）：`environment_records` 环境数据记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT 自增 | 主键 |
| `device_id` | VARCHAR(32) NOT NULL | 设备编号 |
| `recorded_at` | DATETIME(3) NOT NULL | 传感器采集时间 |
| `water_temp` | DECIMAL(5,2) | 水温 °C |
| `ph` | DECIMAL(4,2) | pH 值 |
| `dissolved_oxygen` | DECIMAL(5,2) | 溶解氧 mg/L |
| `light_intensity` | DECIMAL(8,2) | 光照强度 lux |
| `conductivity` | DECIMAL(6,2) | 电导率 μS/cm |
| `created_at` | DATETIME(3) DEFAULT NOW() | 入库时间 |

**索引**：`INDEX idx_device_time (device_id, recorded_at)`

---

### 第二阶段（硬件联动时加）：`devices` + `control_logs`

**`devices` 设备注册表**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT 自增 | 主键 |
| `device_id` | VARCHAR(32) UNIQUE | 设备唯一编号 |
| `device_type` | VARCHAR(20) | sensor / actuator |
| `device_name` | VARCHAR(64) | 中文名，如「1号塘水温传感器」 |
| `location` | VARCHAR(128) | 安装位置 |
| `status` | VARCHAR(16) | online / offline / error |
| `registered_at` | DATETIME | 注册时间 |

**`control_logs` 控制记录表**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT 自增 | 主键 |
| `actuator_id` | VARCHAR(32) | 执行器编号 |
| `command` | VARCHAR(32) | turn_on / turn_off / set_value |
| `set_value` | DECIMAL(8,2) | 设定值 |
| `trigger_reason` | VARCHAR(16) | manual / auto_rule |
| `executed_at` | DATETIME | 执行时间 |
| `result` | VARCHAR(16) | success / failed |

---

### 第三阶段（图像识别时加）：`image_analysis_records`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT 自增 | 主键 |
| `device_id` | VARCHAR(32) | 拍摄设备编号 |
| `captured_at` | DATETIME | 拍摄时间 |
| `image_url` | VARCHAR(256) | 图片存储路径 |
| `growth_stage` | VARCHAR(32) | 生长阶段 |
| `health_status` | VARCHAR(32) | 正常 / 偏黄 / 病害 |
| `coverage_rate` | DECIMAL(5,2) | 覆盖率 % |
| `abnormal_flag` | TINYINT | 是否异常 0/1 |
| `ai_model_version` | VARCHAR(32) | 模型版本 |
| `raw_result_json` | JSON | 模型原始输出 |
| `created_at` | DATETIME | 入库时间 |

---

### 所有表通过 `device_id` 关联

```
                    ┌──────────────────┐
                    │     devices      │  第二阶段
                    └──┬───────────┬───┘
                       │           │
           ┌───────────▼──┐  ┌─────▼───────────┐
           │environment_  │  │ control_logs     │  第二阶段
           │  records     │  └─────────────────┘
           │  (现在)       │
           └──────────────┘
                       
           ┌──────────────────────┐
           │image_analysis_records│  第三阶段
           └──────────────────────┘
```

---

## 五、后端结构

### 文件清单

```
backend/
  ├── main.py          ← FastAPI 主文件，两个接口
  ├── database.py      ← 数据库连接配置
  ├── models.py        ← 表结构定义（SQLAlchemy 模型）
  └── simulator.py     ← 数据模拟脚本（每5秒发一条假数据）
```

### 两个接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/environment/data` | POST | 接收环境数据 JSON，写入数据库 |
| `/api/environment/latest?deviceId=xxx` | GET | 返回最新一条环境数据 |

### POST 请求 JSON 格式

```json
{
  "deviceId": "pond-01",
  "timestamp": "2026-06-15 14:30:00",
  "waterTemp": 22.5,
  "ph": 7.2,
  "dissolvedOxygen": 6.8,
  "lightIntensity": 1200,
  "conductivity": 350
}
```

### GET 响应 JSON 格式

```json
{
  "code": 200,
  "data": {
    "deviceId": "pond-01",
    "timestamp": "2026-06-15 14:30:00",
    "waterTemp": 22.5,
    "ph": 7.2,
    "dissolvedOxygen": 6.8,
    "lightIntensity": 1200,
    "conductivity": 350
  }
}
```

### 启动命令

```bash
conda activate D:\anaconda_envs\jjb
cd backend
uvicorn main:app --reload --port 8000
```

访问 `http://localhost:8000/docs` 查看 Swagger 接口文档。

---

## 六、前端结构

### 文件清单

```
frontend/
  src/
    views/
      Dashboard.vue         ← 大屏看板主页面（整体布局容器）
      RealTimeMonitor.vue   ← 实时监测页面
      HistoryQuery.vue      ← 历史数据查询页面
      DeviceManage.vue      ← 设备管理页面
      AlertRecord.vue       ← 告警记录页面
    components/
      GaugePanel.vue        ← 仪表盘组（水温、pH、溶氧、光照、电导率）
      HistoryChart.vue      ← 历史曲线图（支持时间范围切换）
      DeviceTable.vue       ← 设备状态列表（el-table）
      AlertBar.vue          ← 告警信息滚动条
    api/
      index.ts              ← 封装后端请求
    router/
      index.ts              ← 路由配置
    types/
      env.d.ts              ← TypeScript 类型定义
  package.json
  tsconfig.json
```

### 整体布局（Element Plus 菜单栏 + 内容区）

```
┌────────────────────────────────────────────────────────────┐
│  葛仙米生长环境智能监测系统          在线设备 5  用户名     │  ← el-header
├──────────┬─────────────────────────────────────────────────┤
│          │                                                 │
│ 实时监测  │           主内容区 <router-view>                 │
│ 历史数据  │                                                  │
│ 设备管理  │     ┌──────────────────────────────┐            │
│ 告警记录  │     │                              │            │
│          │     │  仪表盘 + 曲线图 + 设备表      │            │
│          │     │   (根据菜单切换不同页面)        │            │
│          │     │                              │            │
│          │     └──────────────────────────────┘            │
│          │                                                 │
├──────────┴─────────────────────────────────────────────────┤
│  el-aside (el-menu)       el-main (<router-view>)           │
└────────────────────────────────────────────────────────────┘
```

- **el-container / el-aside / el-header / el-main** → Element Plus 布局容器
- **el-menu** → 左侧竖排菜单，el-menu-item 配合 `<router-link>` 切换页面
- **el-button** → 页面内的操作按钮（查询、刷新、导出等）
- **el-table** → 设备列表、告警记录表格
- **ECharts** → 仪表盘 `gauge`、曲线图 `line`，嵌在页面内

### 核心页面

| 页面 | 路由路径 | 内容 |
|------|---------|------|
| 实时监测 | `/monitor` | 仪表盘 + 实时曲线 + 告警滚动条 |
| 历史数据 | `/history` | 时间范围选择（el-date-picker）+ 多参数曲线图 |
| 设备管理 | `/devices` | el-table 设备列表、在线状态、详情 |
| 告警记录 | `/alerts` | el-table 告警列表、筛选、确认 |

### 实时监测页布局（/monitor）

```
┌────────┬────────┬────────┬────────┬────────┬───────┐
│ 水温仪表│pH 仪表 │溶氧仪表│光照仪表│电导率仪表│       │  ← GaugePanel
│ 22.5°C │   7.2 │6.8mg/L │1200lux│ 350μS │       │
├────────┴────────┴────────┴────────┴────────┴───────┤
│                                                    │
│        环境参数历史曲线  [1h] [6h] [24h]            │  ← HistoryChart + el-button
│        X轴：时间  │  多 Y 轴                       │
│                                                    │
├────────────────────────────┬───────────────────────┤
│  设备状态（el-table）       │  告警信息（AlertBar）  │
│  pond-01 ✅ 在线  14:25    │  ⚠ 14:30 水温超25°C  │
│  pond-02 ✅ 在线  14:28    │  ⚠ 14:25 pH 低于6.5   │
└────────────────────────────┴───────────────────────┘
```

### 各部分技术

| 区域 | 用什么 | 说明 |
|------|--------|------|
| 整体布局 | `el-container` / `el-aside` / `el-header` / `el-main` | 菜单 + 内容 |
| 左侧菜单 | `el-menu` + `el-menu-item` | 页面切换 |
| 操作按钮 | `el-button` | 查询、刷新、导出、时间切换 |
| 时间选择 | `el-date-picker` | 历史查询选时间范围 |
| 仪表盘 | ECharts `gauge` | 5 个圆形仪表盘 |
| 曲线图 | ECharts `line`（多 Y 轴） | 多参数折线 |
| 表格 | `el-table` | 设备列表、告警列表 |
| 告警 | `el-alert` + 滚动动画 | 超出阈值自动弹出 |

### 刷新策略

| 区域 | 刷新方式 |
|------|---------|
| 仪表盘 | 每 5 秒轮询 GET `/api/environment/latest` |
| 曲线图 | 用户点时间按钮（1h / 6h / 24h）触发查询 |
| 设备列表 | 每 30 秒轮询 GET `/api/devices` |


### 告警逻辑

在前端判断，超过阈值时仪表盘变红、告警栏显示：

| 参数 | 正常范围 | 告警条件 |
|------|---------|---------|
| 水温 | 15–25°C | <15 或 >25 |
| pH | 6.5–7.5 | <6.5 或 >7.5 |
| 溶解氧 | >5.0 mg/L | <5.0 |

### 启动命令

```bash
cd frontend
npm install
npm run dev
```

---

## 七、数据模拟脚本

### 模拟逻辑

- 用 `random.uniform()` 在合理范围内生成随机值
- 每 5 秒 POST 一条到 `http://localhost:8000/api/environment/data`
- 打印每次发送内容和返回结果

### 参数模拟范围

| 参数 | 范围 |
|------|------|
| waterTemp | 20.0 – 25.0 °C |
| ph | 6.5 – 7.5 |
| dissolvedOxygen | 5.0 – 8.0 mg/L |
| lightIntensity | 800 – 1500 lux |
| conductivity | 300 – 400 μS/cm |

---

## 八、实施顺序

| 序号 | 做什么 | 在哪做 | 验证方式 |
|------|--------|--------|---------|
| 1 | 建库建表 | DataGrip 连 MySQL | 表是否存在 |
| 2 | 写 database.py、models.py、main.py | VS Code | Swagger 页面出现 |
| 3 | 用 Swagger/Postman 手动发一条 POST | 浏览器 | MySQL 里查数据是否插入 |
| 4 | 跑 simulator.py | Anaconda Prompt | MySQL 里数据持续增长 |
| 5 | 写 simulators.py | VS Code | 正常运行即可 |
| 6 | 搭前端 Dashboard | VS Code | 页面出来，仪表盘和曲线图 |
| 7 | 前后端联调 | 两端都启动 | 仪表盘实时刷新 |

---

## 九、环境信息速查

| 项目 | 信息 |
|------|------|
| Anaconda 安装目录 | `D:\anaconda\` |
| 项目 Python 环境 | `D:\anaconda_envs\jjb` |
| Python 版本 | 3.8.20 |
| MySQL 安装目录 | `C:\Program Files\MySQL\MySQL Server 8.0\` |
| Node.js 版本 | v24.14.0 |
| 激活环境命令 | `conda activate D:\anaconda_envs\jjb` |

---

## 十、关键约定

- 前后端 JSON 字段统一用驼峰命名（`waterTemp`、`deviceId`）
- 数据库字段用下划线命名（`water_temp`、`device_id`）
- SQLAlchemy 在 Python 对象和数据库表之间自动转换命名
- 接口返回格式统一为 `{code: 200, data: {...}}`
- 所有表通过 `device_id` 串联
- 后续加表不碰现有表结构
