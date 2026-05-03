-- 动物替代实验数字孪生原型系统 - 数据库初始化脚本
-- 执行前请先创建数据库：CREATE DATABASE digital_twin_db DEFAULT CHARACTER SET utf8mb4;

USE digital_twin_db;

DROP TABLE IF EXISTS sys_comment_like;
DROP TABLE IF EXISTS sys_comment;
DROP TABLE IF EXISTS simulation_record;
DROP TABLE IF EXISTS dataset_raw;
DROP TABLE IF EXISTS experiment;
DROP TABLE IF EXISTS `user`;

-- ─── 用户表 ─────────────────────────────────────────────
CREATE TABLE `user` (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username     VARCHAR(50) NOT NULL UNIQUE          COMMENT '登录账号',
    password     VARCHAR(100) NOT NULL                COMMENT 'BCrypt 加密密码',
    nickname     VARCHAR(50)                          COMMENT '昵称',
    role         VARCHAR(20) NOT NULL DEFAULT 'USER'  COMMENT '角色：ADMIN / USER',
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE / DISABLED',
    created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ─── 实验方案台账 / 申请单 ────────────────────────────────────────
-- 用户可创建草稿并提交审批；审批通过后可作为正式实验方案使用
CREATE TABLE experiment (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name           VARCHAR(100) NOT NULL                COMMENT '方案名称',
    animal_type    ENUM('MOUSE','RABBIT','FROG') NOT NULL COMMENT '实验动物',
    chemical_name  VARCHAR(100) NOT NULL                COMMENT '化学物质',
    indicator_name VARCHAR(100) NOT NULL                COMMENT '观测指标名称（与 dataset_raw 一致）',
    description    TEXT                                 COMMENT '方案说明',
    status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT / PENDING / APPROVED / REJECTED',
    submitted_by   BIGINT       NULL                    COMMENT '提交人 user.id',
    reviewed_by    BIGINT       NULL                    COMMENT '审批人 user.id',
    reviewed_time  DATETIME     NULL                    COMMENT '审批时间',
    review_comment VARCHAR(255) NULL                    COMMENT '审批意见',
    created_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_experiment_status (status),
    KEY idx_experiment_submitter (submitted_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验方案台账/申请单';

-- ─── 历史原始数据表（训练语料）────────────────────────────
CREATE TABLE dataset_raw (
    id               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    animal_type      ENUM('MOUSE','RABBIT','FROG') NOT NULL COMMENT '物种',
    chemical_name    VARCHAR(200)   NOT NULL                COMMENT '化学物质',
    dosage           DECIMAL(18, 6) NOT NULL                COMMENT '剂量',
    indicator_name   VARCHAR(100)   NOT NULL                COMMENT '指标名称',
    indicator_value  DECIMAL(18, 6) NOT NULL                COMMENT '观测数值',
    temperature      DECIMAL(5, 2)  NOT NULL                COMMENT '环境温度（℃）',
    create_time      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    PRIMARY KEY (id),
    KEY idx_dataset_main (animal_type, chemical_name, indicator_name),
    KEY idx_dataset_temperature (temperature)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史原始实验数据（训练语料）';

-- ─── 仿真记录表 ──────────────────────────────────────────
CREATE TABLE simulation_record (
    id               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id          BIGINT         NOT NULL                COMMENT '操作人',
    experiment_id    BIGINT         NULL                    COMMENT '关联实验方案（可选）',
    target_animal    ENUM('MOUSE','RABBIT','FROG') NOT NULL COMMENT '目标物种',
    target_chemical  VARCHAR(200)   NOT NULL                COMMENT '目标化学物质',
    indicator_name   VARCHAR(100)   NOT NULL                COMMENT '预测指标名称',
    input_dosage     DECIMAL(18, 6) NOT NULL                COMMENT '输入剂量',
    selected_model   ENUM('LINEAR','POLYNOMIAL','LOGARITHMIC') NOT NULL COMMENT '算法',
    predicted_value  DECIMAL(18, 6) NOT NULL                COMMENT '预测值',
    create_time      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sim_user (user_id),
    KEY idx_sim_experiment (experiment_id),
    CONSTRAINT fk_simulation_user       FOREIGN KEY (user_id)       REFERENCES `user` (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_simulation_experiment FOREIGN KEY (experiment_id) REFERENCES experiment (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仿真预测记录';

-- ─── 平台评论表 ──────────────────────────────────────────
CREATE TABLE sys_comment (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id            BIGINT       NOT NULL                COMMENT '评论用户ID，关联 user.id',
    experiment_id      BIGINT       NULL                    COMMENT '关联实验ID，可为空',
    root_id            BIGINT       NULL                    COMMENT '根评论ID；主评论为空，回复指向所属主评论',
    parent_id          BIGINT       NULL                    COMMENT '父评论ID；主评论为空，回复指向直接父评论',
    reply_to_user_id   BIGINT       NULL                    COMMENT '被回复用户ID，可为空',
    content            VARCHAR(1000) NOT NULL              COMMENT '评论内容（纯文本）',
    like_count         INT          NOT NULL DEFAULT 0      COMMENT '点赞数',
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING / APPROVED / REJECTED',
    reject_reason      VARCHAR(255) NULL                    COMMENT '驳回原因/审核备注',
    reviewed_by        BIGINT       NULL                    COMMENT '审核人 user.id',
    reviewed_time      DATETIME     NULL                    COMMENT '审核时间',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_comment_status_time (status, create_time),
    KEY idx_comment_user_time (user_id, create_time),
    KEY idx_comment_experiment (experiment_id),
    KEY idx_comment_root (root_id),
    KEY idx_comment_parent (parent_id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES `user` (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_comment_experiment FOREIGN KEY (experiment_id) REFERENCES experiment (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_comment_root FOREIGN KEY (root_id) REFERENCES sys_comment (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES sys_comment (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_comment_reply_to_user FOREIGN KEY (reply_to_user_id) REFERENCES `user` (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_comment_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES `user` (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台评论表';

-- ─── 评论点赞记录表 ────────────────────────────────────────
CREATE TABLE sys_comment_like (
    comment_id    BIGINT   NOT NULL COMMENT '评论ID',
    user_id       BIGINT   NOT NULL COMMENT '点赞用户ID',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (comment_id, user_id),
    KEY idx_comment_like_user (user_id),
    CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES sys_comment (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES `user` (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞记录表';

-- ─── 示例：实验方案 ───────────────────────────────────────
INSERT INTO experiment (name, animal_type, chemical_name, indicator_name, description, status) VALUES
('小鼠乙醇肝毒性初筛方案', 'MOUSE',  '乙醇',         '血清ALT(U/L)',  '基于历史 ALT 数据的数字孪生初筛', 'APPROVED'),
('兔角膜丙酮刺激评估方案', 'RABBIT', '丙酮',         '角膜刺激评分',  '角膜刺激评分数字孪生映射', 'APPROVED'),
('蛙皮肤 SDS 反应方案',    'FROG',   '十二烷基硫酸钠', '皮肤反应指数', '皮肤反应指数模拟', 'APPROVED');

-- ─── 训练语料（历史原始数据）────────────────────────────────
-- 设计说明（教学/原型用，数值在合理医学量级内，并体现剂量–反应关系）：
--   · 血清 ALT：急性肝损伤时常随暴露剂量上升；啮齿类参考区间约数十 U/L，重度升高可达数百。
--   · 角膜刺激：兔眼试验常用 0–4 分制，此处用连续分值近似，剂量增大刺激加重。
--   · 皮肤反应指数：表面活性剂 SDS 浓度越高，皮肤刺激性越强（两栖类皮肤通透性高）。
--   · 体重变化率(%)：亚急性毒性下多为负值（体重下降），剂量越高下降幅度越大。
--   · 温度：在动物房常见区间（约 20–26℃）内小幅波动，模拟饲养环境温度对代谢的轻微影响。

INSERT INTO dataset_raw (animal_type, chemical_name, dosage, indicator_name, indicator_value, temperature) VALUES
-- MOUSE + 乙醇 + 血清ALT：口服/腹腔乙醇肝毒性，低剂量 ALT 接近生理上限，高剂量明显升高
('MOUSE', '乙醇', 0.250000, '血清ALT(U/L)', 27.800000, 21.50),
('MOUSE', '乙醇', 0.500000, '血清ALT(U/L)', 31.200000, 22.00),
('MOUSE', '乙醇', 1.000000, '血清ALT(U/L)', 36.400000, 22.00),
('MOUSE', '乙醇', 1.500000, '血清ALT(U/L)', 42.100000, 22.50),
('MOUSE', '乙醇', 2.500000, '血清ALT(U/L)', 52.600000, 22.50),
('MOUSE', '乙醇', 4.000000, '血清ALT(U/L)', 68.300000, 23.00),
('MOUSE', '乙醇', 6.000000, '血清ALT(U/L)', 88.700000, 23.50),
('MOUSE', '乙醇', 8.500000, '血清ALT(U/L)', 112.400000, 24.00),
('MOUSE', '乙醇', 12.000000, '血清ALT(U/L)', 156.200000, 24.50),
-- MOUSE + 乙醇 + 体重变化率(%)：负值表示体重较基线下降百分比
('MOUSE', '乙醇', 0.500000, '体重变化率(%)', -0.800000, 22.00),
('MOUSE', '乙醇', 2.000000, '体重变化率(%)', -2.400000, 22.50),
('MOUSE', '乙醇', 4.500000, '体重变化率(%)', -5.100000, 23.00),
('MOUSE', '乙醇', 8.000000, '体重变化率(%)', -8.600000, 23.50),
('MOUSE', '乙醇', 14.000000, '体重变化率(%)', -12.300000, 24.00),
-- MOUSE + 二甲苯 + 血清ALT：芳香烃肝代谢负担，ALT 随剂量上升
('MOUSE', '二甲苯', 0.400000, '血清ALT(U/L)', 32.600000, 22.50),
('MOUSE', '二甲苯', 0.800000, '血清ALT(U/L)', 38.900000, 23.00),
('MOUSE', '二甲苯', 1.200000, '血清ALT(U/L)', 45.200000, 23.00),
('MOUSE', '二甲苯', 2.000000, '血清ALT(U/L)', 56.800000, 23.50),
('MOUSE', '二甲苯', 3.500000, '血清ALT(U/L)', 72.400000, 24.00),
('MOUSE', '二甲苯', 5.500000, '血清ALT(U/L)', 94.100000, 24.50),
('MOUSE', '二甲苯', 8.000000, '血清ALT(U/L)', 118.600000, 25.00),
-- MOUSE + 二甲苯 + 体重变化率
('MOUSE', '二甲苯', 1.000000, '体重变化率(%)', -1.200000, 23.00),
('MOUSE', '二甲苯', 3.000000, '体重变化率(%)', -3.800000, 23.50),
('MOUSE', '二甲苯', 6.000000, '体重变化率(%)', -7.500000, 24.00),
('MOUSE', '二甲苯', 10.000000, '体重变化率(%)', -11.200000, 24.50),
-- RABBIT + 丙酮 + 角膜刺激评分：挥发性溶剂眼表刺激，剂量（暴露强度代理）增大评分升高
('RABBIT', '丙酮', 0.100000, '角膜刺激评分', 0.350000, 22.00),
('RABBIT', '丙酮', 0.250000, '角膜刺激评分', 0.520000, 22.50),
('RABBIT', '丙酮', 0.500000, '角膜刺激评分', 0.780000, 23.50),
('RABBIT', '丙酮', 0.750000, '角膜刺激评分', 1.050000, 23.80),
('RABBIT', '丙酮', 1.000000, '角膜刺激评分', 1.280000, 24.00),
('RABBIT', '丙酮', 1.500000, '角膜刺激评分', 1.720000, 24.20),
('RABBIT', '丙酮', 2.000000, '角膜刺激评分', 2.050000, 24.00),
('RABBIT', '丙酮', 3.000000, '角膜刺激评分', 2.480000, 24.50),
('RABBIT', '丙酮', 4.500000, '角膜刺激评分', 2.920000, 25.00),
-- FROG + SDS + 皮肤反应指数：阴离子表面活性剂浓度越高刺激性越强
('FROG', '十二烷基硫酸钠', 0.020000, '皮肤反应指数', 0.120000, 19.50),
('FROG', '十二烷基硫酸钠', 0.040000, '皮肤反应指数', 0.210000, 20.00),
('FROG', '十二烷基硫酸钠', 0.060000, '皮肤反应指数', 0.280000, 20.00),
('FROG', '十二烷基硫酸钠', 0.080000, '皮肤反应指数', 0.340000, 20.50),
('FROG', '十二烷基硫酸钠', 0.100000, '皮肤反应指数', 0.410000, 20.00),
('FROG', '十二烷基硫酸钠', 0.150000, '皮肤反应指数', 0.580000, 21.00),
('FROG', '十二烷基硫酸钠', 0.200000, '皮肤反应指数', 0.720000, 21.00),
('FROG', '十二烷基硫酸钠', 0.300000, '皮肤反应指数', 0.980000, 21.50),
('FROG', '十二烷基硫酸钠', 0.450000, '皮肤反应指数', 1.250000, 22.00),
('FROG', '十二烷基硫酸钠', 0.600000, '皮肤反应指数', 1.480000, 22.50);
