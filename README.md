# 动物替代实验数字孪生原型系统

面向「动物替代实验」场景的 Web 原型：前端录入与展示、后端提供实验数据管理与模拟生理指标可视化；当前阶段以 **前后端分离骨架** 与 **Mock/简化业务** 为主，便于扩展真实仿真引擎。

---

## 一、系统设计概览

### 1.1 架构风格

| 层次 | 说明 |
|------|------|
| **表现层** | 浏览器端 SPA（Vue 3 + Vue Router），门户、登录与后台管理分区路由；Axios 调用 REST API。 |
| **应用层** | Spring Boot 提供 REST 接口：认证（注册/登录/JWT）、实验 CRUD；统一响应体 `Result`。 |
| **持久层** | MyBatis XML Mapper 访问 MySQL；实体与表字段驼峰映射。 |
| **安全** | 密码 **BCrypt** 存储；登录成功后签发 **JWT**；Spring Security 仅提供 `BCryptPasswordEncoder` 与放行策略，业务侧完成校验（可按需改为过滤器统一验签）。 |

### 1.2 业务数据流（概念）

```
用户注册/登录 → JWT 存前端 localStorage
     ↓
后台管理：实验列表 CRUD → MySQL `experiment` 表
     ↓
大屏/图表：前端请求列表数据 + 本地 Mock 生成生理指标折线（后续可替换为后端仿真接口）
```

### 1.3 模块划分（后端包结构）

- `controller`：HTTP 入口（`AuthController`、`ExperimentController`）
- `service` / `service.impl`：业务逻辑
- `mapper`：MyBatis 接口；`resources/mapper/*.xml`：SQL
- `entity`：与表对应的领域对象
- `common`：统一响应 `Result`
- `config`：Security、启动时默认管理员种子等
- `util`：JWT 工具类

前端（与后端并列目录时，一般为 `../frontend`）：`views`（门户/登录/后台）、`router`（路由与登录守卫）、`api`（Axios 封装）。

---

## 二、数据库设计

数据库名建议：**`digital_twin_db`**，字符集 **utf8mb4**。初始化脚本：`src/main/resources/db/init.sql`。

### 2.1 表 `user`（用户）

MySQL 中 **`user` 为保留字**，建表与 SQL 中建议使用反引号 `` `user` ``。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 主键 |
| `username` | VARCHAR(50) UNIQUE | 登录账号 |
| `password` | VARCHAR(100) | **BCrypt 哈希**，禁止存明文 |
| `nickname` | VARCHAR(50) | 展示用昵称 |
| `created_time` | DATETIME | 注册时间 |

**说明**

- 首次启动应用时，若不存在用户名为 `admin` 的记录，会自动创建默认管理员：**用户名 `admin`，密码 `admin123`**（密码以 BCrypt 写入数据库）。
- 手工 `INSERT` 用户时，`password` 必须为 BCrypt 字符串，否则无法通过登录校验。

### 2.2 表 `experiment`（实验方案台账）

数字孪生场景下的 **实验方法/方案** 登记，不含实物实验的「阶段状态」字段。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 主键 |
| `name` | VARCHAR(100) | 方案名称 |
| `animal_type` | ENUM | `MOUSE` / `RABBIT` / `FROG`（与训练语料物种一致） |
| `chemical_name` | VARCHAR(100) | 化学物质（与 `dataset_raw` 中名称一致，便于仿真拉取训练集） |
| `description` | TEXT | 方案说明 |
| `created_time` | DATETIME | 创建时间 |
| `updated_time` | DATETIME | 更新时间（自动更新） |

脚本中带若干 **示例 INSERT**，便于联调。

### 2.3 表 `dataset_raw`（历史原始数据 / 训练语料）

多物种数字孪生沙盒的 **算法训练语料**，一条记录表示某物种在某化学物质、剂量与环境温度下，某指标的 **真实观测值**。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK | 主键 |
| `animal_type` | ENUM | `MOUSE` / `RABBIT` / `FROG` |
| `chemical_name` | VARCHAR(200) | 化学物质名称 |
| `dosage` | DECIMAL(18,6) | 剂量 |
| `indicator_name` | VARCHAR(100) | 指标名称 |
| `indicator_value` | DECIMAL(18,6) | 真实指标数值 |
| `temperature` | DECIMAL(5,2) | 实验环境温度（℃），用于筛选 |
| `create_time` | DATETIME | 入库时间 |

建表脚本中对关键字段附有 **中文 COMMENT**；并建立 `(animal_type, chemical_name)`、`temperature`、`create_time` 等索引便于检索与训练抽样。

### 2.4 表 `simulation_record`（用户仿真记录）

保存用户在沙盒中的 **一次预测**：目标物种/物质、输入剂量、所选模型与预测结果；`user_id` 外键关联 `` `user` ``。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK | 主键 |
| `user_id` | BIGINT FK → `user.id` | 操作人 |
| `target_animal` | ENUM | 目标物种：`MOUSE` / `RABBIT` / `FROG` |
| `target_chemical` | VARCHAR(200) | 目标化学物质 |
| `input_dosage` | DECIMAL(18,6) | 输入预测剂量 |
| `selected_model` | ENUM | `LINEAR`（简单线性回归）、`POLYNOMIAL`（二次多项式回归）、`LOGARITHMIC`（对数回归） |
| `predicted_value` | DECIMAL(18,6) | 预测结果 |
| `create_time` | DATETIME | 记录创建时间 |

外键策略：`ON DELETE RESTRICT`，避免误删用户时遗留孤儿记录逻辑冲突；`ON UPDATE CASCADE`。

### 2.5 表关系小结

- `user` ← `simulation_record.user_id`（外键）
- `experiment` 仍与 `user` **无强制外键**（历史原型表）
- `dataset_raw` 为全局语料，当前 **不绑定用户**

执行 `init.sql` 时 **必须先删 `simulation_record` 再删 `user`**，脚本已按依赖顺序写好 `DROP`。

---

## 三、配置说明（节选）

连接信息见 `src/main/resources/application.yml`：

- **数据源**：URL、用户名、密码需与本地 MySQL 一致。
- **JWT**：`jwt.secret`、`jwt.expiration`（默认约 24 小时）。

修改数据库账号或 JWT 密钥后需重启后端。

---

## 四、快速运行（概要）

1. 创建库并执行 `init.sql`。
2. 后端：在项目根目录执行 `mvn spring-boot:run`（默认端口 **8080**）。
3. 前端：在 `frontend` 目录执行 `npm install` 与 `npm run dev`（默认 **5173**，以终端输出为准）。

详细接口路径与演示账号以前端页面提示与 `AuthController` / `ExperimentController` 为准。

---

## 五、扩展方向（非当前必须）

- 实验与用户的关联字段、权限角色表。
- 将 JWT 校验下沉为 Spring Security 过滤器，保护 `/api/**` 除白名单外的接口。
- 为 `dataset_raw` / `simulation_record` 增加 MyBatis 实体与 REST API，串联前端沙盒与真实模型服务。