# Handover - Digital Twin Animal Replacement Prototype (Backend)

## 当前目标：我们正在解决什么问题？
- 构建「动物替代实验数字孪生原型系统」后端能力，支持：
  - 用户注册/登录（BCrypt + JWT）
  - 实验方案台账（experiment）CRUD
  - 仿真沙盒：基于历史训练数据（dataset_raw）做回归预测（LINEAR / POLYNOMIAL / LOGARITHMIC），并把预测结果落库（simulation_record）
- 关键业务：预测不是“展示曲线”，而是由 `commons-math3` 回归模型基于训练集得出的一个峰值/指标值。

## 技术架构：目前代码的关键结构和依赖
- 核心技术栈
  - Spring Boot 3.x / Java 17
  - MySQL + MyBatis（XML mapper + mapper 接口）
  - Spring Security：`BCryptPasswordEncoder`
  - JWT：`jjwt`（在 util 层生成/解析）
  - 训练拟合/回归：`org.apache.commons:commons-math3`
- 关键模块/文件
  - `controller`
    - `AuthController`：`POST /api/auth/login`、`POST /api/auth/register`
    - `ExperimentController`：`GET/POST/PUT/DELETE /api/experiments...`
    - `SimulationController`：`POST /api/simulation/run`
  - `config`
    - `SecurityConfig`：当前项目安全配置（接口访问策略见“注意事项”）
    - `AdminUserInitializer`：启动时自动创建默认管理员（admin/admin123）
  - `service`
    - `UserService` / `UserServiceImpl`：注册、查用户、存在性校验
    - `ExperimentServiceImpl`：实验分页列表、创建/更新/删除
    - `SimulationEngineServiceImpl`：三种回归算法的训练、预测、记录落库
  - `mapper`（MyBatis）
    - `UserMapper`（`user` 表）
    - `ExperimentMapper`（`experiment` 表 + 分页排序）
    - `DatasetRawMapper`（`dataset_raw` 表训练集筛选：物种+化学物质+指标名+温度区间）
    - `SimulationRecordMapper`（simulation_record 落库）
  - `entity` / `dto`
    - `Experiment`：`id/name/animalType/chemicalName/indicatorName/...`
    - `DatasetRaw`：训练语料（包含 `indicatorName`、`indicatorValue`、`temperature`）
    - `SimulationRecord`：预测记录（包含 `indicatorName`）
    - `SimulationRequest`：仿真请求 DTO（包含 `indicatorName`）
  - 数据初始化
    - `src/main/resources/db/init.sql`：建表 + 示例数据（训练语料 + experiment 台账 + 用户表）

## 已完成的工作：最近修复的 Bug 或实现的 Feature
- 认证体系完成
  - 注册/登录从 mock 切换为真实落库：
    - 注册：`UserService.register()` 使用 BCrypt 编码密码写入 `user`
    - 登录：`BCryptPasswordEncoder.matches()` 校验密码并生成 JWT
  - 启动种子：`AdminUserInitializer` 自动创建默认管理员账户。
- 数据模型对齐（指标维度）
  - `experiment` 增加 `indicator_name`：并与前端下拉枚举硬编码值保持一致
  - `dataset_raw` 训练筛选增加 `indicator_name` 精确过滤：避免混用不同量纲指标导致回归失真
  - `simulation_record` 增加 `indicator_name`：用于前端动态显示纵轴/指标名称
- 数字孪生仿真引擎
  - `SimulationEngineServiceImpl` 支持三种算法并落库：
    - LINEAR：`SimpleRegression`（x=dosage, y=indicatorValue）
    - POLYNOMIAL：`PolynomialCurveFitter.create(2)`（degree=2）
    - LOGARITHMIC：y = a + b ln(x)，训练时用 ln(dosage) 线性化，预测时同样 ln(targetDosage)
  - 预测记录写入：
    - userId、experimentId（可选）、targetAnimal、targetChemical、indicatorName、selectedModel、predictedValue
    - predictedValue 按 SCALE=6 舍入。
- 实验管理列表增强（分页 + 排序）
  - `ExperimentController` 支持 `pageNum/pageSize/sortBy/order/name`
  - 服务端白名单映射：
    - sortBy 仅允许 `id` 与 `createdTime`
    - 防止前端随意拼接字段名。

## 未竟的事项：下一步需要立即执行的任务是什么？
- 安全收尾（建议优先）
  - 目前 `SecurityConfig` 的授权策略可能仍偏“宽松”（`permitAll` 相关逻辑需要统一校验方式）。
  - 下一步应统一：对需要登录/鉴权的接口从 JWT 解析出用户身份，并在 service 层做 userId 一致性校验（尤其是 simulation 记录写入、仿真历史查询等）。
- 权限管理（RBAC）
  - 按你之前讨论，建议在完成功能后再统一上 RBAC：最小可行是“ADMIN / USER”两级；再演进到完整 RBAC。
- 仿真展示与业务语义进一步完善
  - 前端目前用指数衰减公式“从峰值生成 12 小时曲线”（可作为可视化示意）；若未来要更真实时序，需要明确后端是否也返回时序或需要新的模型。
- 测试与交付
  - 补充关键路径测试：
    - 分页排序 SQL 正确性
    - 仿真引擎在训练集不足时的错误处理
    - 登录/注册的成功与失败用例

## 核心逻辑说明：有哪些复杂的逻辑是新对话框可能理解错的？
1. **仿真是“峰值预测”，不是“12小时曲线预测”**
   - 后端 `SimulationEngineServiceImpl` 只计算 `predictedValue`（一个标量）。
   - 前端的 12 小时衰减曲线是基于峰值的可视化生成（exp 衰减），不等同于后端回归预测每个时间点。

2. **训练集筛选必须包含 `indicatorName`**
   - 训练集查询不仅依赖 `animalType/chemicalName/温度区间`，还必须过滤 `indicator_name`。
   - 否则可能把 ALT 与刺激评分等不同量纲指标混在一起拟合，导致预测失真。

3. **LOGARITHMIC 的数学域约束**
   - 回归形式 y = a + b ln(x) 要求 x>0。
   - 代码会忽略 `dosage <= 0` 的训练样本，并在有效样本数不足时抛异常。

4. **experimentId 是可选**
   - 仿真请求允许 `experimentId` 为 null（例如从仿真沙盒直接发起，不绑定台账）。
   - 落库时 `simulation_record.experiment_id` 允许 null。

5. **分页排序的实现是“白名单映射”**
   - 前端传的 `sortBy`/`order` 不会直接拼接 SQL 字段名。
   - service 将其映射为真实列名（例如 createdTime → created_time），避免 SQL 注入或错误字段。

