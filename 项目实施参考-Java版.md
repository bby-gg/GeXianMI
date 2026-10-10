# 葛仙米生长环境智能监测系统 — 实施参考（Java 后端版）

## 一、项目目标

做一个 Web 端环境监测系统：
- 后端用 **Java Spring Boot** 接收 JSON 环境数据，存入 MySQL
- 前端大屏看板展示仪表盘和历史曲线图
- 后续扩展：硬件设备联动、图像识别

---

## 二、技术栈

| 类别 | 选型 | 版本 | 用途 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.4.x | 写 REST 接口，接收/返回 JSON |
| Web 层 | Spring Web (MVC) | — | 内嵌 Tomcat，处理 HTTP 请求 |
| ORM | MyBatis-Plus | 3.5.x | 简化 CRUD，条件构造器 |
| 数据库驱动 | MySQL Connector/J | 9.x | Java 连接 MySQL |
| 连接池 | HikariCP | — | Spring Boot 默认连接池 |
| 数据库 | MySQL | 8.0 | 存所有数据 |
| 构建工具 | Maven | 3.9+ | 依赖管理、打包 |
| JDK | JDK 17 | 17 LTS | Java 运行环境 |
| 前端框架 | Vue 3 + TypeScript | 最新 | 大屏看板页面 |
| UI 组件库 | Element Plus | 最新 | 菜单、按钮、表格、布局 |
| 图表库 | ECharts | 最新 | 仪表盘、折线图 |
| IDE | IntelliJ IDEA | 2024+ | Java 开发 |
| 数据库工具 | DataGrip / Navicat | — | 可视化操作数据库 |

---

## 三、数据流

```
模拟脚本 ──POST JSON──→ Spring Boot 接口 ──MyBatis-Plus──→ MySQL 存入
                                                                │
前端看板 ←──GET JSON─── Spring Boot 接口 ←──MyBatis-Plus── MySQL 查出
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

## 五、后端结构（Spring Boot）

### 项目目录结构

```
backend/
  pom.xml                          ← Maven 配置（依赖、插件）
  src/
    main/
      java/
        com/gexianmi/
          GexianmiApplication.java          ← Spring Boot 启动类
          config/
            WebConfig.java                  ← 跨域 CORS 配置
            MyBatisPlusConfig.java          ← MyBatis-Plus 分页插件配置
          controller/
            EnvironmentController.java      ← 环境数据接口（POST + GET）
            DeviceController.java           ← 设备管理接口（第二阶段）
          entity/
            EnvironmentRecord.java          ← 环境记录实体类
            Device.java                     ← 设备实体类（第二阶段）
          mapper/
            EnvironmentRecordMapper.java    ← 环境记录 Mapper 接口
            DeviceMapper.java               ← 设备 Mapper 接口（第二阶段）
          service/
            EnvironmentService.java         ← 环境数据业务接口
            impl/
              EnvironmentServiceImpl.java   ← 环境数据业务实现
          dto/
            EnvironmentDataDTO.java         ← POST 请求体 DTO
            EnvironmentLatestVO.java        ← GET 响应 VO
          common/
            Result.java                     ← 统一响应格式 {code, data, message}
      resources/
        application.yml                     ← Spring Boot 配置文件
        db/
          migration/
            V1__init_environment_records.sql ← 数据库初始化 SQL
    test/
      java/
        com/gexianmi/
          GexianmiApplicationTests.java     ← 启动测试
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
  },
  "message": "success"
}
```

---

### 关键代码骨架

#### 1. `pom.xml` — Maven 依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
    </parent>

    <groupId>com.gexianmi</groupId>
    <artifactId>gexianmi-backend</artifactId>
    <version>1.0.0</version>
    <name>gexianmi-backend</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Web (REST 接口 + 内嵌 Tomcat) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- MyBatis-Plus (ORM) -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>3.5.9</version>
        </dependency>

        <!-- MySQL 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok (简化 getter/setter) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- 参数校验 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 2. `application.yml` — 配置文件

```yaml
server:
  port: 8000

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gexianmi?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: 3523048584
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 15

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true   # 数据库下划线 → Java 驼峰自动映射
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发时打印 SQL
  global-config:
    db-config:
      id-type: auto                       # 主键自增
```

#### 3. `GexianmiApplication.java` — 启动类

```java
package com.gexianmi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GexianmiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GexianmiApplication.class, args);
    }
}
```

#### 4. `WebConfig.java` — 跨域配置

```java
package com.gexianmi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")          // 所有 /api/ 开头的接口
                .allowedOrigins("http://localhost:5173")  // 前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

#### 5. `EnvironmentRecord.java` — 实体类

```java
package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("environment_records")
public class EnvironmentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceId;

    private LocalDateTime recordedAt;

    private BigDecimal waterTemp;

    private BigDecimal ph;

    private BigDecimal dissolvedOxygen;

    private BigDecimal lightIntensity;

    private BigDecimal conductivity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

#### 6. `EnvironmentRecordMapper.java` — Mapper 接口

```java
package com.gexianmi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gexianmi.entity.EnvironmentRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnvironmentRecordMapper extends BaseMapper<EnvironmentRecord> {
    // 继承 BaseMapper 后，自带 insert、selectById、selectList 等基础 CRUD
    // 复杂查询可在这里自定义方法 + XML
}
```

#### 7. `EnvironmentDataDTO.java` — 请求体 DTO

```java
package com.gexianmi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EnvironmentDataDTO {

    @NotBlank(message = "设备编号不能为空")
    private String deviceId;

    @NotNull(message = "采集时间不能为空")
    private String timestamp;  // "2026-06-15 14:30:00"

    @DecimalMin(value = "0.0", message = "水温不能为负")
    @DecimalMax(value = "60.0", message = "水温超出合理范围")
    private BigDecimal waterTemp;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "14.0")
    private BigDecimal ph;

    @DecimalMin(value = "0.0")
    private BigDecimal dissolvedOxygen;

    @DecimalMin(value = "0.0")
    private BigDecimal lightIntensity;

    @DecimalMin(value = "0.0")
    private BigDecimal conductivity;
}
```

#### 8. `Result.java` — 统一响应

```java
package com.gexianmi.common;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private T data;
    private String message;

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, data, "success");
    }

    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(200, data, message);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, null, message);
    }
}
```

#### 9. `EnvironmentServiceImpl.java` — 核心业务

```java
package com.gexianmi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gexianmi.dto.EnvironmentDataDTO;
import com.gexianmi.entity.EnvironmentRecord;
import com.gexianmi.mapper.EnvironmentRecordMapper;
import com.gexianmi.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {

    private final EnvironmentRecordMapper mapper;

    @Override
    @Transactional
    public void saveData(EnvironmentDataDTO dto) {
        EnvironmentRecord record = new EnvironmentRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setRecordedAt(LocalDateTime.parse(dto.getTimestamp(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        record.setWaterTemp(dto.getWaterTemp());
        record.setPh(dto.getPh());
        record.setDissolvedOxygen(dto.getDissolvedOxygen());
        record.setLightIntensity(dto.getLightIntensity());
        record.setConductivity(dto.getConductivity());

        mapper.insert(record);
    }

    @Override
    public EnvironmentRecord getLatest(String deviceId) {
        LambdaQueryWrapper<EnvironmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentRecord::getDeviceId, deviceId)
               .orderByDesc(EnvironmentRecord::getRecordedAt)
               .last("LIMIT 1");
        return mapper.selectOne(wrapper);
    }
}
```

#### 10. `EnvironmentController.java` — 控制器

```java
package com.gexianmi.controller;

import com.gexianmi.common.Result;
import com.gexianmi.dto.EnvironmentDataDTO;
import com.gexianmi.entity.EnvironmentRecord;
import com.gexianmi.service.EnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/environment")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;

    /**
     * 接收环境数据
     * POST /api/environment/data
     */
    @PostMapping("/data")
    public Result<Void> receiveData(@Valid @RequestBody EnvironmentDataDTO dto) {
        environmentService.saveData(dto);
        return Result.ok(null, "数据已保存");
    }

    /**
     * 查询最新一条环境数据
     * GET /api/environment/latest?deviceId=pond-01
     */
    @GetMapping("/latest")
    public Result<EnvironmentRecord> getLatest(@RequestParam String deviceId) {
        EnvironmentRecord record = environmentService.getLatest(deviceId);
        return Result.ok(record);
    }
}
```

---

### 启动命令

```bash
# 开发环境直接运行
cd backend
mvn spring-boot:run

# 或者打包成 jar 运行
mvn clean package -DskipTests
java -jar target/gexianmi-backend-1.0.0.jar
```

访问 `http://localhost:8000/api/environment/latest?deviceId=pond-01` 测试接口。

> **注意**：不像 FastAPI 自带 Swagger，Spring Boot 需要额外引入 `springdoc-openapi-starter-webmvc-ui` 才能在 `http://localhost:8000/swagger-ui.html` 看到 Swagger 文档。初期开发可以先不加，用 Postman 或浏览器直接测试。

---

## 六、前端结构

（与 Python 版完全相同，无需改动）

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

### 方案一：用 Java 写模拟器（推荐，统一技术栈）

新建 `backend/src/main/java/com/gexianmi/simulator/DataSimulator.java`：

```java
package com.gexianmi.simulator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 环境数据模拟器 —— 每 5 秒向接口 POST 一条随机数据
 * 手动运行 main 方法即可
 */
public class DataSimulator {

    private static final String URL = "http://localhost:8000/api/environment/data";
    private static final String[] DEVICE_IDS = {"pond-01", "pond-02"};
    private static final Random RANDOM = new Random();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws InterruptedException {
        System.out.println("模拟器启动，每 5 秒发送一条数据...");
        while (true) {
            for (String deviceId : DEVICE_IDS) {
                String json = buildJson(deviceId);
                sendPost(json);
                Thread.sleep(5000);
            }
        }
    }

    private static String buildJson(String deviceId) {
        double waterTemp = round(20.0 + RANDOM.nextDouble() * 5.0);      // 20-25
        double ph = round(6.5 + RANDOM.nextDouble() * 1.0);              // 6.5-7.5
        double doxy = round(5.0 + RANDOM.nextDouble() * 3.0);            // 5.0-8.0
        double light = round(800 + RANDOM.nextDouble() * 700);           // 800-1500
        double cond = round(300 + RANDOM.nextDouble() * 100);            // 300-400
        String timestamp = LocalDateTime.now().format(FMT);

        return String.format(
            "{\"deviceId\":\"%s\",\"timestamp\":\"%s\"," +
            "\"waterTemp\":%.1f,\"ph\":%.1f,\"dissolvedOxygen\":%.1f," +
            "\"lightIntensity\":%.0f,\"conductivity\":%.0f}",
            deviceId, timestamp, waterTemp, ph, doxy, light, cond
        );
    }

    private static void sendPost(String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());
            System.out.println("发送: " + json);
            System.out.println("响应: " + response.body());
        } catch (Exception e) {
            System.err.println("发送失败: " + e.getMessage());
        }
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
```

### 方案二：继续用 Python 脚本模拟（零成本迁移）

保持原来的 `simulator.py` 不变，它只负责 HTTP POST，和后端语言无关。

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
| 2 | 用 Spring Initializr 生成项目骨架 | start.spring.io | pom.xml、启动类就绪 |
| 3 | 写 application.yml、实体类、Mapper | IntelliJ IDEA | 启动不报错 |
| 4 | 写 Service + Controller（两个接口） | IntelliJ IDEA | 启动后 Postman 测试 |
| 5 | 用 Postman 手动发一条 POST | Postman | MySQL 里查数据是否插入 |
| 6 | 跑 DataSimulator.main() | IntelliJ IDEA | MySQL 里数据持续增长 |
| 7 | 搭前端 Dashboard | VS Code | 页面出来，仪表盘和曲线图 |
| 8 | 前后端联调（注意跨域） | 两端都启动 | 仪表盘实时刷新 |

---

## 九、Python 版 vs Java 版对照

| 对比项 | Python (FastAPI) | Java (Spring Boot) |
|--------|-----------------|-------------------|
| 后端框架 | FastAPI 0.124 | Spring Boot 3.4.x |
| ORM | SQLAlchemy 2.0 | MyBatis-Plus 3.5.x |
| 数据库驱动 | PyMySQL | MySQL Connector/J |
| 启动方式 | `uvicorn main:app` | `mvn spring-boot:run` |
| 接口文档 | `/docs` (自带 Swagger) | `/swagger-ui.html` (需加 springdoc) |
| 异步支持 | `async/await` 原生支持 | `@Async` 注解 + 线程池 |
| 类型校验 | Pydantic | `@Valid` + Jakarta Validation |
| 打包 | 不需要，源码直接跑 | `mvn package` 打 jar |
| 部署 | 需装 Python 环境 + 依赖 | 有 JRE 就能跑（fat jar 自带 Tomcat） |
| 启动速度 | 快（解释型） | 较慢（编译 + JVM 启动） |
| 运行时性能 | 中等 | 高（JIT 编译优化） |
| 前端 | Vue 3 + Element Plus + ECharts | 完全相同 |

---

## 十、环境信息速查

| 项目 | Python 版 | Java 版 |
|------|-----------|---------|
| JDK 版本 | — | JDK 17 LTS |
| 构建工具 | — | Maven 3.9+ |
| IDE | VS Code | IntelliJ IDEA 2024+ |
| MySQL 安装目录 | `C:\Program Files\MySQL\MySQL Server 8.0\` | 相同 |
| Node.js 版本 | v24.14.0 | 相同 |
| 后端端口 | 8000 | 8000 |
| 前端端口 | 5173 (Vite 默认) | 相同 |

---

## 十一、关键约定

- 前后端 JSON 字段统一用驼峰命名（`waterTemp`、`deviceId`）
- 数据库字段用下划线命名（`water_temp`、`device_id`）
- MyBatis-Plus 开启 `map-underscore-to-camel-case: true` 自动转换命名风格
- 接口返回格式统一为 `{code: 200, data: {...}, message: "success"}`
- 所有表通过 `device_id` 串联
- 后续加表不碰现有表结构
- Controller 只做参数接收和返回，业务逻辑放 Service 层
- 实体类用 Lombok `@Data` 省去 getter/setter 手写
- 数据库连接池用 HikariCP（Spring Boot 默认，零配置）
