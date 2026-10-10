# Java 后端开发指南 — 葛仙米环境监测系统（学生版）

> **适用对象**：正在做课程设计/实训项目的同学，学过 Java 基础（面向对象、集合、异常），想系统学习 Spring Boot 后端开发。
>
> **本文档的目标**：让你理解 Spring Boot 后端的分层架构，知道每层干什么、怎么串联，能够独立开发新接口。

---

## 目录

1. [技术栈是什么、为什么选它们](#一技术栈是什么为什么选它们)
2. [环境准备：从零搭好开发环境](#二环境准备从零搭好开发环境)
3. [项目结构详解：每个文件是干什么的](#三项目结构详解每个文件是干什么的)
4. [Spring Boot 基础概念](#四spring-boot-基础概念)
5. [分层架构详解](#五分-层-架-构-详-解)
6. [核心接口开发全过程（从零写一个接口）](#六核心接口开发全过程从零写一个接口)
7. [JWT 认证：Token 是怎么工作的](#七jwt-认证token-是怎么工作的)
8. [统一响应与异常处理](#八统一响应与异常处理)
9. [数据库设计与 ORM](#九数据库设计与-orm)
10. [启动与调试](#十启动与调试)
11. [常见问题排查](#十一常见问题排查)

---

## 一、技术栈是什么、为什么选它们

| 技术 | 版本 | 它是干什么的 | 为什么选它 |
|------|------|-------------|-----------|
| **Java** | JDK 17 LTS | 编程语言 | 国内企业级开发标配，生态成熟，工作好找 |
| **Spring Boot** | 3.4.x | Java 后端框架，帮你快速搭接口 | 「约定优于配置」，90% 配置自动化，专注写业务 |
| **Spring Web (MVC)** | — | 处理 HTTP 请求（接收 JSON、返回 JSON） | Spring Boot 自带，内嵌 Tomcat 服务器 |
| **MyBatis-Plus** | 3.5.x | ORM 框架：用 Java 代码操作数据库 | MyBatis 增强版，单表 CRUD 不用写 SQL |
| **MySQL** | 8.0 | 关系型数据库，存数据 | 免费、稳定、中文资料多 |
| **HikariCP** | — | 数据库连接池 | Spring Boot 默认，业界最快 |
| **Maven** | 3.9+ | 构建工具：管理依赖、编译、打包 | Java 生态标准，`pom.xml` 声明依赖就行 |
| **Lombok** | — | 自动生成 getter/setter/构造器 | 省去大量样板代码 |
| **JWT** | 0.12.x | 无状态身份认证 | 适合前后端分离项目 |
| **IntelliJ IDEA** | 2024+ | Java 开发 IDE | Java 开发标配，智能提示无敌 |

### 这些技术之间是什么关系？

```
用户浏览器（前端 :5173）
        │
        │  HTTP Request（JSON）
        ▼
┌────────────────────────────────────────────────────┐
│              Spring Boot (内嵌 Tomcat)              │
│                                                    │
│  ┌──────────────────────────────────────────┐     │
│  │  Controller 层（接收请求、返回响应）         │     │
│  │      │                                    │     │
│  │      ▼                                    │     │
│  │  Service 层（业务逻辑、数据校验）            │     │
│  │      │                                    │     │
│  │      ▼                                    │     │
│  │  Mapper 层（数据库操作，MyBatis-Plus）      │     │
│  │      │                                    │     │
│  └──────┼────────────────────────────────────┘     │
│         │                                          │
└─────────┼──────────────────────────────────────────┘
          │
          │  JDBC（MySQL 驱动）
          ▼
    ┌──────────┐
    │  MySQL   │
    │ gexianmi │
    └──────────┘
```

---

## 二、环境准备：从零搭好开发环境

### 2.1 你需要安装什么

| 软件 | 怎么装 | 验证装好没 |
|------|--------|-----------|
| **JDK 17** | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/#java17) 或 [OpenJDK](https://adoptium.net/) 下载安装 | 终端输入 `java -version`，看到 `17.x.x` |
| **Maven** | [maven.apache.org](https://maven.apache.org/download.cgi) 下载，解压后配 PATH | 终端输入 `mvn -v`，看到版本号 |
| **MySQL 8.0** | [mysql.com](https://dev.mysql.com/downloads/mysql/) 下载安装，记住 root 密码 | 终端输入 `mysql -u root -p` 能连上 |
| **IntelliJ IDEA** | [jetbrains.com/idea](https://www.jetbrains.com/idea/) 下载 Community 版（免费） | 能打开就行 |
| **DataGrip** 或 **Navicat** | 数据库可视化工具（可选，方便看表和数据） | 能连上 MySQL |

### 2.2 IDEA 打开项目

```
1. 打开 IntelliJ IDEA
2. File → Open → 选择 java_backend 目录
3. IDEA 自动识别 pom.xml，开始下载依赖（右下角进度条）
4. 等待依赖下载完成（第一次可能 5-10 分钟，取决于网速）
```

### 2.3 修改数据库密码

打开 [src/main/resources/application.yml](src/main/resources/application.yml)：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gexianmi?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的MySQL密码    # ← 改成你自己的密码
```

### 2.4 创建数据库

```sql
-- 在 MySQL 命令行或 DataGrip 中执行
CREATE DATABASE IF NOT EXISTS gexianmi
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

---

## 三、项目结构详解：每个文件是干什么的

```
java_backend/
├── pom.xml                                  ← Maven 配置文件（依赖列表、插件）
│
├── sql/                                     ← 数据库初始化脚本
│   ├── init_users.sql                       ← 用户表数据
│   ├── init_alert_rules.sql                 ← 告警规则表数据
│   └── clean_and_seed.py                    ← Python 数据清理/填充脚本
│
└── src/
    ├── main/
    │   ├── java/com/gexianmi/               ← ★ Java 源代码（你主要写代码的地方）
    │   │   │
    │   │   ├── GexianmiApplication.java     ← Spring Boot 启动类（程序入口）
    │   │   │
    │   │   ├── controller/                  ← 控制器层：接收 HTTP 请求
    │   │   │   ├── EnvironmentController.java   ← 环境数据接口
    │   │   │   ├── DeviceController.java        ← 设备管理接口
    │   │   │   ├── GreenhouseController.java    ← 大棚管理接口
    │   │   │   ├── AlertController.java         ← 告警接口
    │   │   │   ├── AlertRuleController.java     ← 告警规则接口
    │   │   │   └── AuthController.java          ← 登录/注册接口
    │   │   │
    │   │   ├── service/                     ← 服务接口：定义业务方法
    │   │   │   ├── EnvironmentService.java
    │   │   │   ├── DeviceService.java
    │   │   │   └── ...
    │   │   │   └── impl/                    ← 服务实现：具体业务逻辑
    │   │   │       ├── EnvironmentServiceImpl.java
    │   │   │       ├── DeviceServiceImpl.java
    │   │   │       └── ...
    │   │   │
    │   │   ├── mapper/                      ← 数据访问层：操作数据库
    │   │   │   ├── EnvironmentRecordMapper.java
    │   │   │   ├── DeviceMapper.java
    │   │   │   └── ...
    │   │   │
    │   │   ├── entity/                      ← 实体类：对应数据库表
    │   │   │   ├── EnvironmentRecord.java
    │   │   │   ├── Device.java
    │   │   │   ├── Greenhouse.java
    │   │   │   ├── User.java
    │   │   │   ├── Alert.java
    │   │   │   └── AlertRule.java
    │   │   │
    │   │   ├── dto/                         ← 数据传输对象：前端传来的 JSON
    │   │   │   ├── EnvironmentDataDTO.java
    │   │   │   └── LoginDTO.java
    │   │   │
    │   │   ├── config/                      ← 配置类：Spring Boot 配置
    │   │   │   ├── WebConfig.java           ← 跨域 CORS 配置
    │   │   │   ├── InterceptorConfig.java   ← 拦截器注册
    │   │   │   ├── JwtInterceptor.java      ← JWT Token 验证拦截器
    │   │   │   ├── MyBatisPlusConfig.java   ← MyBatis-Plus 分页插件
    │   │   │   ├── MetaObjectHandlerConfig.java  ← 自动填充（创建时间等）
    │   │   │   └── RequireAdmin.java        ← 自定义注解（标记需要管理员权限）
    │   │   │
    │   │   ├── common/                      ← 通用类：全局工具
    │   │   │   ├── Result.java              ← 统一响应格式
    │   │   │   └── GlobalExceptionHandler.java  ← 全局异常处理
    │   │   │
    │   │   ├── utils/                       ← 工具类
    │   │   │   └── JwtUtil.java             ← JWT 生成与验证工具
    │   │   │
    │   │   └── simulator/                   ← 数据模拟器
    │   │       └── DataSimulator.java       ← 定时生成假数据发到接口
    │   │
    │   └── resources/                       ← 配置文件
    │       └── application.yml              ← Spring Boot 核心配置
    │
    └── test/                                ← 测试代码
        └── java/com/gexianmi/
            └── GexianmiApplicationTests.java
```

---

## 四、Spring Boot 基础概念

### 4.1 什么是「控制反转（IoC）」？

传统的写法是你自己 `new` 对象：

```java
// 传统写法（不用 Spring）
public class OrderService {
    private UserService userService = new UserService();  // 自己 new
}
```

Spring Boot 的写法是把对象的创建交给 Spring 管理：

```java
// Spring 写法
@Service
public class OrderService {
    // Spring 自动帮你 new 好、注入进来
    @Autowired  // 或使用 @RequiredArgsConstructor + final
    private UserService userService;
}
```

> **一句话理解**：你不用手动 `new` 对象，Spring 容器帮你管理所有对象的创建和销毁。你需要哪个，Spring 就给你注入哪个。

### 4.2 常用注解速查

| 注解 | 放在哪 | 作用 | 示例 |
|------|--------|------|------|
| `@SpringBootApplication` | 启动类 | 标记这是 Spring Boot 项目入口 | `GexianmiApplication` |
| `@RestController` | Controller 类 | 标记这是 REST 接口控制器 | `EnvironmentController` |
| `@RequestMapping("/api/xxx")` | Controller 类 | 设置接口的基础路径 | `@RequestMapping("/api/environment")` |
| `@GetMapping` | Controller 方法 | GET 请求 | `@GetMapping("/latest")` |
| `@PostMapping` | Controller 方法 | POST 请求 | `@PostMapping("/data")` |
| `@PutMapping` | Controller 方法 | PUT 请求 | `@PutMapping("/{id}")` |
| `@DeleteMapping` | Controller 方法 | DELETE 请求 | `@DeleteMapping("/{id}")` |
| `@Service` | Service 类 | 标记这是业务层组件 | `EnvironmentServiceImpl` |
| `@Mapper` | Mapper 接口 | 标记这是 MyBatis 数据访问接口 | `EnvironmentRecordMapper` |
| `@Data` | Entity/DTO 类 | Lombok：自动生成 getter/setter/toString | 实体类 |
| `@Autowired` | 字段/构造器 | 自动注入依赖 | （推荐用 `@RequiredArgsConstructor`） |
| `@Valid` | Controller 方法参数 | 开启参数校验 | `@Valid @RequestBody XxxDTO dto` |
| `@Transactional` | Service 方法 | 开启数据库事务 | `saveData()` |
| `@Configuration` | Config 类 | 标记这是配置类 | `WebConfig` |

### 4.3 一个请求的完整处理流程

```
前端发送：POST http://localhost:8000/api/environment/data
Body: { "deviceId": "pond-01", "waterTemp": 22.5, ... }

                │
                ▼
    ┌───────────────────────┐
    │  Tomcat（内嵌服务器）   │  接收 HTTP 请求
    └───────────┬───────────┘
                │
                ▼
    ┌───────────────────────┐
    │  DispatcherServlet    │  Spring 的前端控制器，分发请求
    └───────────┬───────────┘
                │
                ▼  匹配 @RequestMapping + @PostMapping
    ┌───────────────────────┐
    │  JwtInterceptor       │  拦截器：验证 Token → 通过
    └───────────┬───────────┘
                │
                ▼
    ┌───────────────────────────────────────┐
    │  EnvironmentController.receiveData()  │  Controller：接收参数
    │    - @Valid 校验 DTO 字段              │
    │    - 调用 Service                       │
    └───────────┬───────────────────────────┘
                │
                ▼
    ┌───────────────────────────────────────┐
    │  EnvironmentServiceImpl.saveData()    │  Service：业务逻辑
    │    - 校验设备是否存在                   │
    │    - DTO → Entity 转换                 │
    │    - 调用 Mapper.insert()              │
    └───────────┬───────────────────────────┘
                │
                ▼
    ┌───────────────────────────────────────┐
    │  EnvironmentRecordMapper.insert()     │  Mapper：数据库操作
    │    - MyBatis-Plus 生成 SQL              │
    │    - INSERT INTO environment_records... │
    └───────────┬───────────────────────────┘
                │
                ▼
           ┌──────────┐
           │  MySQL   │  数据存入
           └──────────┘

    原路返回：Result.ok("数据已保存")
    → HTTP 200 { "code": 200, "data": null, "message": "数据已保存" }
```

---

## 五、分 层 架 构 详 解

Spring Boot 项目遵循**三层架构**（也叫 MVC 模式）：

```
┌─────────────────────────────────────────────┐
│        Controller 层（控制器）                │
│  职责：接收 HTTP 请求、参数校验、返回响应        │
│  不管业务逻辑，不管数据库                       │
└──────────────────┬──────────────────────────┘
                   │ 调用
                   ▼
┌─────────────────────────────────────────────┐
│        Service 层（业务逻辑）                  │
│  职责：处理业务规则、数据校验、事务管理           │
│  Service 定义接口，ServiceImpl 实现             │
└──────────────────┬──────────────────────────┘
                   │ 调用
                   ▼
┌─────────────────────────────────────────────┐
│        Mapper 层（数据访问）                   │
│  职责：操作数据库（增删改查）                    │
│  继承 BaseMapper 后自动获得 CRUD 方法           │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
              ┌──────────┐
              │  MySQL   │
              └──────────┘
```

### 5.1 Entity（实体类）— 和数据库表一一对应

```java
package com.gexianmi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data                                    // Lombok：自动生成 getter/setter/toString/equals/hashCode
@TableName("environment_records")        // 对应数据库里的哪张表
public class EnvironmentRecord {

    @TableId(type = IdType.AUTO)         // 主键，自动增长
    private Long id;

    private String deviceId;             // 数据库字段是 device_id
                                         // MyBatis-Plus 会自动转换：device_id ↔ deviceId
    private LocalDateTime recordedAt;

    private BigDecimal waterTemp;

    private BigDecimal ph;

    private BigDecimal dissolvedOxygen;

    private BigDecimal lightIntensity;

    private BigDecimal conductivity;

    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createdAt;
}
```

> **为什么金额用 `BigDecimal` 而不是 `double`？** `double` 是浮点数，有精度问题（`0.1 + 0.2 = 0.30000000000000004`）。涉及小数运算尤其是金额、科学数据，必须用 `BigDecimal`。

### 5.2 Mapper（数据访问层）— 操作数据库

```java
package com.gexianmi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gexianmi.entity.EnvironmentRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper  // 告诉 Spring：这是一个 MyBatis Mapper
public interface EnvironmentRecordMapper extends BaseMapper<EnvironmentRecord> {
    // 继承 BaseMapper 后，自动拥有以下方法（不用写一行 SQL！）：
    //   insert(entity)           — 插入一条记录
    //   selectById(id)           — 根据主键查询
    //   selectList(wrapper)      — 条件查询（返回列表）
    //   selectOne(wrapper)       — 条件查询（返回一条）
    //   updateById(entity)       — 根据主键更新
    //   deleteById(id)           — 根据主键删除
    //   selectCount(wrapper)     — 根据条件计数

    // 复杂查询可以在这里自定义方法，配合 XML 写 SQL
    // 但单表 CRUD 90% 的情况用 BaseMapper 自带方法就够了
}
```

### 5.3 Service（业务逻辑层）— 核心业务

```java
// ============ Service 接口 ============
package com.gexianmi.service;

public interface EnvironmentService {
    void saveData(EnvironmentDataDTO dto);
    EnvironmentRecord getLatest(String deviceId);
    List<EnvironmentRecord> getRecent(String deviceId, int limit);
    List<EnvironmentRecord> getHistory(String deviceId, String start, String end);
}

// ============ Service 实现 ============
package com.gexianmi.service.impl;

@Service                                    // 告诉 Spring：这是一个 Service 组件
@RequiredArgsConstructor                    // Lombok：自动生成带 final 字段的构造器
public class EnvironmentServiceImpl implements EnvironmentService {

    private final EnvironmentRecordMapper mapper;  // final + RequiredArgsConstructor = 自动注入
    private final DeviceMapper deviceMapper;

    @Override
    @Transactional                          // 开启事务：要么全成功，要么全回滚
    public void saveData(EnvironmentDataDTO dto) {
        // 1. 业务校验：设备是否注册
        Device device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new RuntimeException("设备未注册: " + dto.getDeviceId());
        }

        // 2. DTO → Entity 转换
        EnvironmentRecord record = new EnvironmentRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setRecordedAt(LocalDateTime.parse(dto.getTimestamp(), FORMATTER));
        record.setWaterTemp(dto.getWaterTemp());
        record.setPh(dto.getPh());
        record.setDissolvedOxygen(dto.getDissolvedOxygen());
        record.setLightIntensity(dto.getLightIntensity());
        record.setConductivity(dto.getConductivity());

        // 3. 存入数据库
        mapper.insert(record);
    }

    @Override
    public EnvironmentRecord getLatest(String deviceId) {
        // MyBatis-Plus 的 LambdaQueryWrapper：用 Lambda 表达式构建查询条件
        // 好处：字段名是类型安全的，不会拼错字符串
        LambdaQueryWrapper<EnvironmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentRecord::getDeviceId, deviceId)    // WHERE device_id = ?
               .orderByDesc(EnvironmentRecord::getCreatedAt)     // ORDER BY created_at DESC
               .last("LIMIT 1");                                 // LIMIT 1
        return mapper.selectOne(wrapper);
    }
}
```

> **为什么 Service 要分接口和实现？** 这是「面向接口编程」原则。接口定义契约（有哪些方法），实现类写具体逻辑。好处是：以后想换实现（比如从 MySQL 切到 Redis 缓存），只要写新的 Impl 就行，调用方不用改。

### 5.4 Controller（控制器）— 对外暴露接口

```java
@RestController                             // = @Controller + @ResponseBody（返回 JSON）
@RequestMapping("/api/environment")         // 所有方法的基础路径
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;
    private final DeviceService deviceService;

    /**
     * 接收环境数据
     * POST /api/environment/data
     */
    @PostMapping("/data")
    public Result<Void> receiveData(@Valid @RequestBody EnvironmentDataDTO dto) {
        // @Valid：自动校验 DTO 里的 @NotBlank、@DecimalMin 等注解
        // @RequestBody：把 HTTP Body 的 JSON 自动转成 EnvironmentDataDTO 对象
        environmentService.saveData(dto);
        return Result.ok("数据已保存");
    }

    /**
     * 查询最新一条环境数据
     * GET /api/environment/latest?deviceId=pond-01
     */
    @GetMapping("/latest")
    public Result<EnvironmentRecord> getLatest(@RequestParam String deviceId) {
        // @RequestParam：从 URL 查询参数中取值（?deviceId=xxx）
        EnvironmentRecord record = environmentService.getLatest(deviceId);
        return Result.ok(record);
    }

    /**
     * 按时间范围查询历史数据
     * GET /api/environment/history?deviceId=A-001&start=2026-07-01T00:00:00&end=2026-07-11T23:59:59
     */
    @GetMapping("/history")
    public Result<List<EnvironmentRecord>> getHistory(
            @RequestParam String deviceId,
            @RequestParam(required = false) String start,   // required=false：可选参数
            @RequestParam(required = false) String end) {
        return Result.ok(environmentService.getHistory(deviceId, start, end));
    }
}
```

---

## 六、核心接口开发全过程（从零写一个接口）

假设你要新增一个「根据设备 ID 删除所有历史数据」的接口，下面是完整的开发步骤：

### Step 1：在 Mapper 中加方法

```java
// EnvironmentRecordMapper.java
@Mapper
public interface EnvironmentRecordMapper extends BaseMapper<EnvironmentRecord> {

    // 按设备 ID 删除
    default int deleteByDeviceId(String deviceId) {
        return this.delete(new LambdaQueryWrapper<EnvironmentRecord>()
                .eq(EnvironmentRecord::getDeviceId, deviceId));
    }
}
```

### Step 2：在 Service 接口中声明方法

```java
// EnvironmentService.java
public interface EnvironmentService {
    // ... 已有方法
    int clearData(String deviceId);   // 新增
}
```

### Step 3：在 ServiceImpl 中实现逻辑

```java
// EnvironmentServiceImpl.java
@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {

    private final EnvironmentRecordMapper mapper;

    @Override
    @Transactional
    public int clearData(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("设备 ID 不能为空");
        }
        return mapper.deleteByDeviceId(deviceId);
    }
}
```

### Step 4：在 Controller 中暴露接口

```java
// EnvironmentController.java
@DeleteMapping("/data/{deviceId}")                      // DELETE /api/environment/data/pond-01
public Result<Integer> clearData(@PathVariable String deviceId) {
    // @PathVariable：从 URL 路径中取值（/{deviceId}）
    int deletedCount = environmentService.clearData(deviceId);
    return Result.ok(deletedCount, "成功删除 " + deletedCount + " 条记录");
}
```

### Step 5：用 Postman 测试

```
DELETE http://localhost:8000/api/environment/data/pond-01
Header: Authorization: Bearer <你的Token>

期望返回：
{
  "code": 200,
  "data": 150,
  "message": "成功删除 150 条记录"
}
```

### 开发新接口的检查清单

```
□ Entity：数据库表对应的实体类有没有？
□ Mapper：BaseMapper 继承了吗？自定义方法加了吗？
□ Service 接口：新方法声明了吗？
□ ServiceImpl：业务逻辑写好了吗？@Transactional 加了吗？
□ Controller：请求路径和方法（GET/POST/PUT/DELETE）对吗？
□ @Valid 参数校验加了吗？
□ Result 统一包装了吗？
□ 用 Postman 测过了吗？
```

---

## 七、JWT 认证：Token 是怎么工作的

### 7.1 为什么需要 JWT？

HTTP 是无状态的——服务器不会记住「你是谁」。每次请求都是独立的，如果不带身份证明，服务器不知道这次请求和上次请求是不是同一个人。

JWT 的解决思路：**登录后给你一个加密的令牌（Token），以后每次请求都带着它。**

### 7.2 登录 → 获得 Token → 使用 Token 的完整流程

```
┌────────┐                    ┌────────────┐                    ┌──────────┐
│ 浏览器  │                    │ Spring Boot │                    │  MySQL   │
└───┬────┘                    └──────┬─────┘                    └────┬─────┘
    │                                │                               │
    │ ① POST /api/auth/login        │                               │
    │  {username, password}          │                               │
    │ ──────────────────────────────▶│                               │
    │                                │ ② 查询用户：                  │
    │                                │  SELECT * FROM users          │
    │                                │  WHERE username = ?           │
    │                                │ ──────────────────────────────▶│
    │                                │                               │
    │                                │ ③ 返回用户信息（含加密密码）     │
    │                                │ ◀──────────────────────────────│
    │                                │                               │
    │                                │ ④ 验证密码（BCrypt 比对）       │
    │                                │  生成 JWT Token                │
    │                                │  {sub: "zhangsan", role: "admin", exp: ...}│
    │                                │                               │
    │ ⑤ 返回 Token                  │                               │
    │ ◀──────────────────────────────│                               │
    │                                │                               │
    │ ⑥ 存 Token 到 localStorage     │                               │
    │                                │                               │
    │ ⑦ GET /api/environment/latest │                               │
    │  Header: Authorization:        │                               │
    │  Bearer <Token>                │                               │
    │ ──────────────────────────────▶│                               │
    │                                │ ⑧ JwtInterceptor 拦截请求      │
    │                                │  解析 Token → 验证签名          │
    │                                │  提取用户信息 → 放行            │
    │                                │                               │
    │ ⑨ 返回数据                     │                               │
    │ ◀──────────────────────────────│                               │
```

### 7.3 关键代码

**登录接口**（[AuthController.java](src/main/java/com/gexianmi/controller/AuthController.java)）：

```java
@PostMapping("/login")
public Result<Map<String, String>> login(@Valid @RequestBody LoginDTO dto) {
    // 1. 查用户
    User user = userMapper.selectOne(
        new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
    );
    // 2. 验密码（BCrypt）
    if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
        return Result.error(401, "用户名或密码错误");
    }
    // 3. 生成 Token
    String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
    // 4. 返回
    Map<String, String> result = new HashMap<>();
    result.put("token", token);
    result.put("username", user.getUsername());
    result.put("role", user.getRole());
    return Result.ok(result);
}
```

**JWT 拦截器**（[JwtInterceptor.java](src/main/java/com/gexianmi/config/JwtInterceptor.java)）：

```java
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 从请求头取 Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);  // 没带 Token → 401
            return false;
        }
        token = token.substring(7);  // 去掉 "Bearer " 前缀

        // 2. 验证 Token 是否有效
        try {
            Claims claims = jwtUtil.parse(token);
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", claims.get("role"));
            return true;  // 放行
        } catch (Exception e) {
            response.setStatus(401);  // Token 无效 → 401
            return false;
        }
    }
}
```

> **JWT 的安全性**：Token 里的信息是**签名**过的，不是加密的。意思是：别人可以看到 Token 里的内容（用户名等），但无法伪造——因为签名需要密钥。所以不要把密码、银行卡号等敏感信息放进 Token。

---

## 八、统一响应与异常处理

### 8.1 统一响应格式

本项目所有接口返回的 JSON 都是这种格式：

```java
// Result.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;        // 状态码：200 成功，400 参数错误，401 未登录，500 服务器错误
    private T data;          // 数据（泛型，可以是任何类型）
    private String message;  // 提示信息

    // 成功响应
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, data, "success");
    }

    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(200, data, message);
    }

    // 失败响应
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, null, message);
    }
}
```

### 8.2 全局异常处理

```java
// GlobalExceptionHandler.java
@RestControllerAdvice  // 拦截所有 Controller 抛出的异常
public class GlobalExceptionHandler {

    // 处理参数校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(400, msg);
    }

    // 处理其他未捕获异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}
```

> **好处**：不用在每个 Controller 方法里写 try-catch。任何地方抛出异常，`@RestControllerAdvice` 自动拦截并返回规范的错误 JSON。

---

## 九、数据库设计与 ORM

### 9.1 核心表结构

| 表名 | 用途 | 对应实体类 |
|------|------|-----------|
| `environment_records` | 环境数据记录 | `EnvironmentRecord` |
| `devices` | 设备注册表 | `Device` |
| `greenhouses` | 大棚信息 | `Greenhouse` |
| `users` | 用户表 | `User` |
| `alerts` | 告警记录 | `Alert` |
| `alert_rules` | 告警规则配置 | `AlertRule` |

### 9.2 命名转换规则

```
Java （驼峰命名）       数据库（下划线命名）
─────────────────      ────────────────────
deviceId          ↔    device_id
waterTemp         ↔    water_temp
dissolvedOxygen   ↔    dissolved_oxygen
createdAt         ↔    created_at
recordedAt        ↔    recorded_at

MyBatis-Plus 配置：
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  ← 这一行开启自动转换
```

### 9.3 LambdaQueryWrapper 常用条件

```java
// 用 Lambda 表达式构建查询，类型安全，不会拼错字段名
LambdaQueryWrapper<EnvironmentRecord> wrapper = new LambdaQueryWrapper<>();

wrapper.eq(EnvironmentRecord::getDeviceId, "pond-01")   // = 等于
wrapper.ne(EnvironmentRecord::getDeviceId, "pond-01")   // != 不等于
wrapper.gt(EnvironmentRecord::getWaterTemp, 25)          // > 大于
wrapper.ge(EnvironmentRecord::getWaterTemp, 25)          // >= 大于等于
wrapper.lt(EnvironmentRecord::getPh, 6.5)                // < 小于
wrapper.le(EnvironmentRecord::getPh, 7.5)                // <= 小于等于
wrapper.like(EnvironmentRecord::getDeviceId, "pond")     // LIKE 模糊
wrapper.between(EnvironmentRecord::getWaterTemp, 20, 25) // BETWEEN 区间
wrapper.in(EnvironmentRecord::getDeviceId, list)          // IN
wrapper.orderByDesc(EnvironmentRecord::getRecordedAt)     // ORDER BY DESC
wrapper.orderByAsc(EnvironmentRecord::getRecordedAt)      // ORDER BY ASC
wrapper.last("LIMIT 100")                                 // LIMIT
```

---

## 十、启动与调试

### 10.1 启动步骤

```bash
# 方式一：IDEA 内启动（推荐）
# 找到 GexianmiApplication.java → 右键 → Run 'GexianmiApplication'

# 方式二：命令行启动
cd java_backend
mvn spring-boot:run

# 方式三：打包后运行
mvn clean package -DskipTests
java -jar target/gexianmi-backend-1.0.0.jar
```

启动成功的标志：
```
Tomcat started on port 8000 (http) with context path ''
```

### 10.2 用 Postman 测试接口

```
1. 先登录获取 Token：
   POST http://localhost:8000/api/auth/login
   Body → raw → JSON:
   {
     "username": "admin",
     "password": "123456"
   }

2. 复制返回的 Token

3. 调用其他接口时带上 Token：
   GET http://localhost:8000/api/environment/latest?deviceId=pond-01
   Headers → Authorization: Bearer <粘贴Token>
```

### 10.3 查看日志

在 IDEA 的 `Run` 窗口中可以看到日志。MyBatis-Plus 配置了 SQL 日志打印：

```sql
-- 控制台会打印类似这样的 SQL：
==> Preparing: SELECT * FROM environment_records WHERE device_id = ? ORDER BY created_at DESC LIMIT 1
==> Parameters: pond-01(String)
<== Total: 1
```

这对调试非常有用——你发的请求，最终转换成了什么 SQL，一目了然。

### 10.4 IDEA 调试技巧

```
1. 打断点：在代码行号左边点击，出现红色圆点
2. Debug 模式启动：右键 → Debug（而不是 Run）
3. 程序执行到断点处会暂停
4. 此时可以：
   - 查看变量的值
   - Step Over（F8）：执行下一行
   - Step Into（F7）：进入方法内部
   - Resume（F9）：继续执行到下一个断点
```

---

## 十一、常见问题排查

### 11.1 启动报错

| 症状 | 可能原因 | 解决办法 |
|------|---------|---------|
| `Communications link failure` | MySQL 没启动 | 启动 MySQL 服务 |
| `Access denied for user` | 密码错误 | 检查 `application.yml` 的密码 |
| `Unknown database 'gexianmi'` | 数据库没建 | 执行 `CREATE DATABASE gexianmi` |
| `Table 'xxx' doesn't exist` | 表没建 | 执行 SQL 建表脚本 |
| `Port 8000 already in use` | 端口被占用 | 改 `application.yml` 的端口，或停掉占用进程 |
| `Cannot find bean` | 依赖注入失败 | 检查是否漏了 `@Service` / `@Mapper` / `@Component` 注解 |

### 11.2 接口 400（Bad Request）

```
原因：参数校验失败
查看控制台或返回的 message 字段，会告诉你具体哪个字段校验失败
例如："deviceId: 设备编号不能为空"
```

### 11.3 接口 401（Unauthorized）

```
检查清单：
□ 登录了吗？
□ Token 加对了吗？（Header: Authorization: Bearer xxx）
□ Token 过期了吗？（JWT 有过期时间）
□ 是不是访问了白名单之外的接口？
```

### 11.4 接口 500（Internal Server Error）

```
原因：后端代码抛异常了
排查步骤：
1. IDEA 控制台看完整异常堆栈
2. 最常见：
   - NullPointerException：某个对象是 null 就调用了它的方法
   - SQL 语法错误：自定义 SQL 写错了
   - 类型转换错误：字符串转日期格式不匹配
```

### 11.5 跨域问题（CORS）

```
症状：前端 Console 显示 "blocked by CORS policy"
检查：WebConfig.java 的跨域配置是否正确
  - allowedOrigins 是否包含前端地址（http://localhost:5173）
  - 接口路径是否在 allowedMapping 范围内（/api/**）
```

### 11.6 MyBatis-Plus 相关

| 症状 | 原因 | 解决办法 |
|------|------|---------|
| 查询结果字段为 null | 数据库字段名和 Java 属性名不匹配 | 确认 `map-underscore-to-camel-case: true` |
| `insert` 报主键冲突 | 主键策略不对 | 检查 `id-type: auto` 和数据库表是否设置自增 |
| 查出来的数据比预想的少 | 逻辑删除字段作怪 | 检查实体类是否有 `@TableLogic` 注解 |

### 11.7 @Transactional 事务不生效

```
常见原因：
1. 方法不是 public 的（Spring AOP 只能代理 public 方法）
2. 同一个类内部调用（this.xxx() 不会走代理）
3. 异常被 try-catch 吞掉了（事务只在 RuntimeException 时回滚）

正确用法：
@Transactional
public void saveData() {
    mapper.insert(record1);   // 如果这里成功
    mapper.insert(record2);   // 这里抛异常 → record1 自动回滚
    // 注意：不要 catch 异常！让异常抛出去事务才能回滚
}
```

---

## 附录 A：学习路线建议

```
第 1 步：Java 基础（2-4 周）
    ↓   类与对象、继承、接口、集合框架、异常处理
第 2 步：MySQL 基础（1-2 周）
    ↓   SELECT/INSERT/UPDATE/DELETE、JOIN、索引
第 3 步：JDBC 入门（3 天）
    ↓   了解 Java 怎么连数据库（后面 MyBatis-Plus 替你做）
第 4 步：Servlet / Spring MVC 概念（1 周）
    ↓   理解 HTTP 请求-响应模型
第 5 步：Spring Boot + MyBatis-Plus（2-3 周）
    ↓   跟着教程搭建项目、写 CRUD
第 6 步：本项目实战
    跑起来 → 加新接口 → 加校验 → 加缓存 → 理解架构
```

**推荐资源**：
- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [B 站 Spring Boot 教程](https://www.bilibili.com/)（搜「Spring Boot 3 最新教程」）
- [Lombok 注解速查](https://projectlombok.org/features/)

---

## 附录 B：Java 后端新增功能检查清单

当你要给项目加新功能时，按这个清单逐项检查：

```
新增一张表：
□ 在 MySQL 中建表
□ 创建 Entity 类（@TableName 对应表名）
□ 创建 Mapper 接口（继承 BaseMapper<Entity>）
□ 需要自动填充字段吗？（createdAt 等）→ 加 @TableField(fill = ...)

新增一个接口：
□ DTO（入参）定义了吗？
□ Service 接口声明方法
□ ServiceImpl 实现方法（加 @Transactional 如果需要）
□ Controller 加方法（路径、方法类型、@Valid、@RequestBody/@RequestParam）
□ 返回 Result<T> 包装
□ Postman 测试

新增安全控制：
□ 需要登录才能访问？→ 默认所有接口都会被 JwtInterceptor 拦截
□ 不需要登录？→ 在 InterceptorConfig 中加 excludePathPatterns
□ 需要管理员权限？→ Controller 方法上加 @RequireAdmin
```

---

> **最后的话**：Spring Boot 后端开发入门确实比前端要陡一些，因为它涉及的概念更多——IoC、AOP、事务、ORM、JWT 等。但一旦理解了分层架构的思想，之后写任何后端项目都是这个套路：Controller 收请求 → Service 做业务 → Mapper 操作数据库。把这个项目跑起来、加几个接口，比看十遍教程都管用。善用 IDEA 的 Debug 功能，它能让你「看到」代码是怎么一步步执行的。
