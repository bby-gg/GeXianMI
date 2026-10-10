# 葛仙米生长环境智能监测系统 · 后端（Spring Boot）

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6db33f?style=flat-square&logo=springboot)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-1e90ff?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![JWT](https://img.shields.io/badge/JWT-0.12.6-000000?style=flat-square&logo=jsonwebtokens)
![Maven](https://img.shields.io/badge/Maven-3.9+-c71a36?style=flat-square&logo=apachemaven)

**企业级水质环境监测 REST API 服务**

*基于 Spring Boot 3.4.1 + MyBatis-Plus 3.5.7，提供环境数据采集、大棚设备管理、告警监控、告警规则配置、用户认证等全栈后端能力*

</div>

---

## 1. 项目简介

**葛仙米后端服务**是葛仙米生长环境智能监测系统的 Java 后端，基于 Spring Boot 3.4.1 构建，内嵌 Tomcat 容器，提供完整的 RESTful API。

平台覆盖了以下业务域：

| 模块 | 说明 |
|------|------|
| 认证授权 | JWT 登录/注册、BCrypt 密码加密、路由拦截校验 |
| 环境数据 | 传感器数据上报、最新值查询、历史曲线、数据范围 |
| 大棚管理 | 养殖大棚 CRUD、棚下设备关联、设备数统计 |
| 设备管理 | 设备注册/编辑/删除、大棚名称关联填充 |
| 告警监控 | 活跃告警查询、单条确认/忽略、批量确认 |
| 告警规则 | 5 参数阈值配置、启用/禁用、级别设定 |
| 数据模拟 | Java 原生 HttpClient 模拟传感器定时上报 |

---

## 2. 架构设计

### 2.1 三层架构 + JWT 全链路防护

```
Controller → Service → Mapper → MySQL
     ↑                      ↑
  JWT 拦截器          MyBatis-Plus
     校验层               自动 CRUD
```

所有 `/api/**` 请求经过 JWT 拦截器校验（登录/注册接口除外），无 Token 或过期自动返回 401。

### 2.2 命名风格自动转换

MySQL 下划线命名（`water_temp`、`device_id`）→ MyBatis-Plus `map-underscore-to-camel-case: true` → Java 驼峰（`waterTemp`、`deviceId`）。无需手写 ResultMap。

### 2.3 @JsonProperty 前后端字段对齐

| 后端 Java 字段 | 前端 JSON 字段 | 注解 |
|:---|:---|:---|
| `recordedAt` | `timestamp` | `@JsonProperty("timestamp")` |
| `message` | `msg` | `@JsonProperty("msg")` |

### 2.4 多格式时间解析

前端可能传空格分隔（`2026-07-11 00:00:00`）或 T 分隔（ISO），后端 `parseTime()` 自动尝试 4 种格式兜底。

### 2.5 MetaObjectHandler 自动填充

`createdAt` / `updatedAt` 由 `MetaObjectHandlerConfig` 自动填充，INSERT/UPDATE 时无需手动设值。

### 2.6 全局异常处理

`GlobalExceptionHandler` 拦截所有异常，统一包装为 `{code, msg, data}`。区分 400（参数校验/业务）、401（未登录）、500（服务器错误）。

---

## 3. 核心技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.4.1 |
| Web 层 | Spring Web MVC | 6.2.1 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0 |
| 连接池 | HikariCP | 5.x |
| JWT | jjwt-api | 0.12.6 |
| 密码加密 | spring-security-crypto | 6.4.2 |
| 构建工具 | Maven | 3.9+ |
| 语言 | Java | 17 LTS |
| 工具 | Lombok | 1.18.x |
| 校验 | Jakarta Validation | 3.x |

---

## 4. 目录结构

```
java_backend/
├── pom.xml                                    # Maven 依赖配置
├── sql/
│   ├── init_users.sql                         # 用户表 DDL
│   ├── init_alert_rules.sql                   # 告警规则表 DDL + 种子数据
│   ├── init_db.py                             # 自动建表 + 默认管理员
│   ├── clean_and_seed.py                      # 数据清洗 + 整齐模拟数据
│   └── run_init.bat                           # 一键初始化批处理
└── src/main/java/com/gexianmi/
    ├── GexianmiApplication.java               # 启动类
    ├── common/
    │   ├── Result.java                        # 统一响应 {code, data, msg}
    │   └── GlobalExceptionHandler.java        # 全局异常处理
    ├── config/
    │   ├── WebConfig.java                     # CORS 跨域
    │   ├── MyBatisPlusConfig.java             # 分页插件
    │   ├── JwtInterceptor.java                # JWT 请求拦截器
    │   ├── InterceptorConfig.java             # 拦截器注册
    │   └── MetaObjectHandlerConfig.java       # created_at/updated_at 自动填充
    ├── controller/
    │   ├── AuthController.java                # 登录/注册/密码
    │   ├── EnvironmentController.java         # 环境数据 (6 端点)
    │   ├── GreenhouseController.java          # 大棚 CRUD + 设备列表
    │   ├── DeviceController.java              # 设备 CRUD
    │   ├── AlertController.java               # 告警查询/确认/忽略
    │   └── AlertRuleController.java           # 告警规则配置
    ├── dto/
    │   ├── EnvironmentDataDTO.java            # 数据上报请求体
    │   └── LoginDTO.java                      # 登录/注册请求体
    ├── entity/
    │   ├── EnvironmentRecord.java             # 环境记录
    │   ├── Greenhouse.java                    # 大棚 (+设备数)
    │   ├── Device.java                        # 设备 (+大棚名)
    │   ├── Alert.java                         # 告警
    │   ├── AlertRule.java                     # 告警规则
    │   └── User.java                          # 用户
    ├── mapper/
    │   ├── EnvironmentRecordMapper.java
    │   ├── GreenhouseMapper.java
    │   ├── DeviceMapper.java
    │   ├── AlertMapper.java
    │   ├── AlertRuleMapper.java
    │   └── UserMapper.java
    ├── service/
    │   ├── EnvironmentService.java + impl/
    │   ├── GreenhouseService.java   + impl/
    │   ├── DeviceService.java       + impl/
    │   ├── AlertService.java        + impl/
    │   ├── AlertRuleService.java    + impl/
    │   └── AuthService.java         + impl/
    ├── simulator/
    │   └── DataSimulator.java                 # 传感器数据模拟器
    └── utils/
        └── JwtUtil.java                       # JWT 生成/解析/校验
```

---

## 5. 数据库设计

### 5.1 表一览

| 表 | 说明 | 数据量 |
|------|------|:---:|
| `users` | 用户认证 | ~3 |
| `greenhouses` | 大棚信息 | 2 |
| `devices` | 设备注册 | 7 |
| `environment_records` | 环境数据记录 | 1056 |
| `alerts` | 告警记录 | ~2 |
| `alert_rules` | 告警规则配置 | 5 |

### 5.2 表关联

```
        ┌──────────────────┐
        │    users          │  认证
        └──────────────────┘

        ┌──────────────────┐
        │   greenhouses    │  大棚
        └──┬───────────────┘
           │ 1:N
        ┌──▼──────────────────┐
        │   devices           │  设备
        └──┬─────────┬────────┘
           │ 1:N     │ 1:N
    ┌──────▼───┐  ┌──▼──────────┐
    │ env_records│  │   alerts   │
    └──────────┘  └─────────────┘

    ┌──────────────┐
    │ alert_rules  │  规则配置（独立表）
    └──────────────┘
```

### 5.3 environment_records

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `device_id` | VARCHAR(32) | 设备编号 |
| `recorded_at` | DATETIME(3) | 采集时间 |
| `water_temp` | DECIMAL(5,2) | 水温 °C |
| `ph` | DECIMAL(4,2) | pH 值 |
| `dissolved_oxygen` | DECIMAL(5,2) | 溶解氧 mg/L |
| `light_intensity` | DECIMAL(8,2) | 光照 lux |
| `conductivity` | DECIMAL(6,2) | 电导率 μS/cm |
| `created_at` | DATETIME(3) | 入库时间 |

索引：`INDEX idx_device_time (device_id, recorded_at)`

### 5.4 users

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `username` | VARCHAR(32) UNIQUE | 用户名 |
| `password_hash` | VARCHAR(128) | BCrypt 密文 |
| `role` | VARCHAR(16) | admin / operator / viewer |
| `last_login_at` | DATETIME | 最后登录 |
| `created_at` | DATETIME(3) | 创建时间 |

### 5.5 alert_rules

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `param` | VARCHAR(32) UNIQUE | 参数名（waterTemp 等） |
| `param_name` | VARCHAR(32) | 中文名 |
| `unit` | VARCHAR(16) | 单位 |
| `min_val` / `max_val` | DECIMAL(8,2) | 正常范围 |
| `threshold_low` / `threshold_high` | DECIMAL(8,2) | 告警阈值 |
| `level` | VARCHAR(16) | warning / error |
| `enabled` | TINYINT(1) | 0/1 禁用/启用 |

---

## 6. 接口一览

### 6.1 认证模块

| 方法 | 路径 | 认证 | 说明 |
|------|------|:---:|------|
| POST | `/api/auth/login` | 否 | 登录 |
| POST | `/api/auth/register` | 否 | 注册 |
| GET | `/api/auth/me?userId=` | 是 | 用户信息 |
| PUT | `/api/auth/password` | 是 | 修改密码 |

### 6.2 环境数据模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/environment/data` | 上报数据 |
| GET | `/api/environment/latest?deviceId=` | 最新一条 |
| GET | `/api/environment/recent?deviceId=&limit=` | 最近 N 条 |
| GET | `/api/environment/history?deviceId=&start=&end=` | 时间范围查询 |
| GET | `/api/environment/range?deviceId=` | 起止时间 + 总条数 |
| GET | `/api/environment/devices` | 所有设备 |

### 6.3 大棚管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/greenhouses` | 列表（含设备数） |
| POST | `/api/greenhouses` | 新建 |
| PUT | `/api/greenhouses/{id}` | 编辑 |
| DELETE | `/api/greenhouses/{id}` | 删除 |
| GET | `/api/greenhouses/{id}/devices` | 棚下设备 |

### 6.4 设备管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/devices` | 列表（含大棚名称） |
| POST | `/api/devices` | 注册 |
| PUT | `/api/devices/{code}` | 编辑 |
| DELETE | `/api/devices/{code}` | 删除 |

### 6.5 告警模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/alerts` | 条件查询 |
| GET | `/api/alerts/active` | 活跃告警 |
| POST | `/api/alerts/{id}/confirm` | 确认 |
| POST | `/api/alerts/{id}/ignore` | 忽略 |
| POST | `/api/alerts/confirm-all` | 批量确认 |

### 6.6 告警规则

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/alert-rules` | 5 条规则 |
| PUT | `/api/alert-rules/{id}` | 更新阈值 |

---

## 7. 统一响应格式

```json
{
  "code": 200,
  "data": {},
  "msg": "success"
}
```

| code | 含义 |
|:---:|------|
| 200 | 成功 |
| 400 | 参数错误 / 业务异常 |
| 401 | 未登录或 Token 过期 |
| 500 | 服务器内部错误 |

---

## 8. JWT 认证流程

```
POST /api/auth/login { username, password }
    → BCrypt 验证 → 生成 JWT（24h）
    → 返回 { token, userInfo }

后续请求 Header: Authorization: Bearer {token}
JwtInterceptor 拦截校验 → 通过放行 / 失败 401
```

---

## 9. 快速启动

### 9.1 环境要求

| 环境 | 版本 |
|------|------|
| JDK | 17 LTS |
| Maven | 3.9+ |
| MySQL | 8.0+ |

### 9.2 数据库初始化

```bash
# 方式一：Python 脚本
python java_backend/sql/init_db.py          # 用户表 + admin
python java_backend/sql/clean_and_seed.py   # 整齐模拟数据

# 方式二：DataGrip 执行 SQL 文件
# java_backend/sql/init_users.sql
# java_backend/sql/init_alert_rules.sql
```

### 9.3 启动

```bash
cd java_backend
mvn spring-boot:run

# 或 IDEA 中直接运行 GexianmiApplication.java
```

默认端口 8000。

### 9.4 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

---

## 10. 实施小记

| 问题 | 原因 | 解决 |
|------|------|------|
| PaginationInnerInterceptor 找不到 | MP 3.5.9+ 移除 | 锁定 3.5.7 |
| JDBC utf8mb4 报错 | Connector/J 9.x 不支持 | 改 `characterEncoding=UTF-8` |
| 外键约束失败 | device_id 有 FK | 用 `A-001` 等已有设备 |
| updated_at NOT NULL | 缺 MetaObjectHandler | 新增配置类 |
| 注册 401 | 拦截器没放行 | 加 `/register` 白名单 |
| Long→int 编译错 | selectCount 返回 Long | 用 `.intValue()` |

---

*Made with ❤️ by 葛仙米数字孪生团队 · GexianMI Monitoring Backend · 2026*
