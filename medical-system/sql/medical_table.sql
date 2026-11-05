-- =====================================================================
--  医疗系统数据库建表脚本  medical_table.sql
--  课程：Java Web 开发            实验二：医疗系统数据库设计与基础 CRUD
--  学号：2024403316               姓名：董欣雨
--  适用：MySQL 8.0.16+（CHECK 约束自 8.0.16 起真正生效）
--  说明：脚本可重复执行，自动按外键依赖倒序清理旧表，直接运行无报错
-- =====================================================================

CREATE DATABASE IF NOT EXISTS medical_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE medical_system;

SET NAMES utf8mb4;

-- 按外键依赖倒序删除：明细 -> 处方 -> 挂号 -> 医生 -> 患者 -> 科室
-- 注意：重复执行会清空这 6 张表的数据，录入演示数据后请勿再跑本段
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS prescription_item;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS register_record;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS department;
SET FOREIGN_KEY_CHECKS = 1;


-- ---------------------------------------------------------------------
-- 1. 科室表 department
--    无外键依赖，必须最先创建
-- ---------------------------------------------------------------------
CREATE TABLE department (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '科室主键',
    dept_code   CHAR(6)      NOT NULL                COMMENT '科室编码，业务唯一键，如 D00001',
    dept_name   VARCHAR(50)  NOT NULL                COMMENT '科室名称',
    dept_desc   VARCHAR(200) DEFAULT NULL            COMMENT '科室简介',
    status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1 启用，0 停用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code) COMMENT '科室编码唯一',
    UNIQUE KEY uk_dept_name (dept_name) COMMENT '科室名称唯一，禁止重复建科',
    CONSTRAINT ck_dept_status CHECK (status IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='科室表';


-- ---------------------------------------------------------------------
-- 2. 患者表 patient
--    设计要点：年龄、出生日期均可由身份证号唯一推导，按第三范式不再冗余存储，
--              由应用层解析后填充到返回对象，避免随时间推移产生数据漂移
-- ---------------------------------------------------------------------
CREATE TABLE patient (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '患者主键',
    patient_no   CHAR(12)    NOT NULL                COMMENT '患者编号，业务唯一键，如 P00000000001',
    patient_name VARCHAR(20) NOT NULL                COMMENT '患者姓名',
    id_card      CHAR(18)    NOT NULL                COMMENT '身份证号，18 位，唯一',
    phone        CHAR(11)    NOT NULL                COMMENT '手机号，11 位，唯一',
    gender       TINYINT     NOT NULL DEFAULT 0      COMMENT '性别：0 未知，1 男，2 女（可自述，故独立存储）',
    address      VARCHAR(120) DEFAULT NULL           COMMENT '联系地址',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_patient_no (patient_no) COMMENT '患者编号唯一',
    UNIQUE KEY uk_patient_id_card (id_card) COMMENT '身份证号唯一，一人一档',
    UNIQUE KEY uk_patient_phone (phone) COMMENT '手机号唯一，作为就诊联系方式',
    KEY idx_patient_name (patient_name) COMMENT '支持按姓名模糊检索',
    CONSTRAINT ck_patient_gender CHECK (gender IN (0, 1, 2))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='患者表';


-- ---------------------------------------------------------------------
-- 3. 医生表 doctor
--    外键 dept_id 引用科室；职称、工号、电话均只依赖医生主键
-- ---------------------------------------------------------------------
CREATE TABLE doctor (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '医生主键',
    doctor_no   CHAR(8)     NOT NULL                COMMENT '医生工号，业务唯一键，如 DOC00001',
    doctor_name VARCHAR(20) NOT NULL                COMMENT '医生姓名',
    dept_id     BIGINT      NOT NULL                COMMENT '所属科室 id',
    title       VARCHAR(30) DEFAULT NULL            COMMENT '职称：主任医师/副主任医师/主治医师/住院医师',
    phone       CHAR(11)    NOT NULL                COMMENT '联系电话，11 位，唯一',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_doctor_no (doctor_no) COMMENT '工号唯一',
    UNIQUE KEY uk_doctor_phone (phone) COMMENT '电话唯一',
    KEY idx_doctor_dept (dept_id) COMMENT '外键列建索引，加速按科室检索',
    KEY idx_doctor_name (doctor_name) COMMENT '支持按姓名检索',
    CONSTRAINT fk_doctor_dept FOREIGN KEY (dept_id) REFERENCES department (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='医生表';


-- ---------------------------------------------------------------------
-- 4. 挂号记录表 register_record
--    患者与医生的多对多业务凭据；挂号费、状态只依赖挂号主键
-- ---------------------------------------------------------------------
CREATE TABLE register_record (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '挂号主键',
    register_no   VARCHAR(20) NOT NULL                COMMENT '挂号单号，业务唯一键',
    patient_id    BIGINT      NOT NULL                COMMENT '患者 id',
    doctor_id     BIGINT      NOT NULL                COMMENT '医生 id',
    register_time DATETIME    NOT NULL                COMMENT '挂号时间',
    status        TINYINT     NOT NULL DEFAULT 0      COMMENT '状态：0 待就诊，1 已就诊，2 已取消',
    fee           DECIMAL(8, 2) NOT NULL DEFAULT 0.00 COMMENT '挂号费（元）',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_register_no (register_no) COMMENT '挂号单号唯一',
    KEY idx_reg_patient (patient_id, register_time) COMMENT '按患者查就诊史',
    KEY idx_reg_doctor (doctor_id, register_time) COMMENT '按医生查当日号源',
    CONSTRAINT ck_reg_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT ck_reg_fee CHECK (fee >= 0),
    CONSTRAINT fk_reg_patient FOREIGN KEY (patient_id) REFERENCES patient (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_reg_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='挂号记录表';


-- ---------------------------------------------------------------------
-- 5. 电子处方表 prescription
--    设计要点：只保留 record_id，开方医生通过挂号记录关联获得。
--              若再存 doctor_id 将形成 record_id -> doctor_id 的传递依赖，
--              一旦改号会出现两处医生不一致的更新异常，故不冗余存储
-- ---------------------------------------------------------------------
CREATE TABLE prescription (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '处方主键',
    prescription_no VARCHAR(20)  NOT NULL                COMMENT '处方单号，业务唯一键',
    record_id       BIGINT       NOT NULL                COMMENT '关联挂号记录 id',
    diagnosis       VARCHAR(200) DEFAULT NULL            COMMENT '诊断结论',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开方时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_prescription_no (prescription_no) COMMENT '处方单号唯一',
    UNIQUE KEY uk_prescription_record (record_id) COMMENT '一次挂号对应一张处方',
    CONSTRAINT fk_prescription_record FOREIGN KEY (record_id) REFERENCES register_record (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='电子处方表';


-- ---------------------------------------------------------------------
-- 6. 处方明细表 prescription_item
--    设计要点：一张处方含多种药品，若把药名、剂量塞进一个 TEXT 字段即违反第一范式
--              （字段不可再分）。拆出明细表后每个字段都是原子值。
--    级联删除：明细是处方的组成部分（弱实体），随处方删除
-- ---------------------------------------------------------------------
CREATE TABLE prescription_item (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细主键',
    prescription_id BIGINT        NOT NULL                COMMENT '所属处方 id',
    drug_name       VARCHAR(60)   NOT NULL                COMMENT '药品名称',
    spec            VARCHAR(40)   DEFAULT NULL            COMMENT '规格，如 0.25g*24 片',
    dosage          VARCHAR(40)   DEFAULT NULL            COMMENT '用法用量，如 1 片/次，3 次/日',
    quantity        INT           NOT NULL DEFAULT 1      COMMENT '数量',
    unit_price      DECIMAL(8, 2) NOT NULL DEFAULT 0.00   COMMENT '单价（元）',
    PRIMARY KEY (id),
    KEY idx_item_prescription (prescription_id) COMMENT '按处方查明细',
    CONSTRAINT ck_item_quantity CHECK (quantity > 0),
    CONSTRAINT ck_item_price CHECK (unit_price >= 0),
    CONSTRAINT fk_item_prescription FOREIGN KEY (prescription_id) REFERENCES prescription (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='处方明细表';


-- ---------------------------------------------------------------------
-- 可选：演示数据（用于分页查询截图，可按需删除本段）
-- ---------------------------------------------------------------------
INSERT INTO department (dept_code, dept_name, dept_desc, status) VALUES
('D00001', '内科',   '心血管、呼吸、消化等内科常见病诊疗', 1),
('D00002', '外科',   '普外、骨科、创伤等手术治疗', 1),
('D00003', '儿科',   '0-14 岁儿童疾病诊疗', 1),
('D00004', '妇产科', '妇科疾病与围产期保健', 1),
('D00005', '眼科',   '视力矫正与眼底疾病诊疗', 1);

INSERT INTO patient (patient_no, patient_name, id_card, phone, gender, address) VALUES
('P00000000001', '张伟', '110101199003074514', '13800138001', 1, '北京市东城区'),
('P00000000002', '李静', '310101199205126623', '13800138002', 2, '上海市黄浦区'),
('P00000000003', '王强', '440106198811203517', '13800138003', 1, '广州市天河区'),
('P00000000004', '赵敏', '510107199507158829', '13800138004', 2, '成都市武侯区'),
('P00000000005', '陈晨', '330106200002291216', '13800138005', 1, '杭州市西湖区'),
('P00000000006', '刘洋', '420106199312054431', '13800138006', 1, '武汉市江岸区'),
('P00000000007', '孙丽', '370102199809237727', '13800138007', 2, '济南市历下区'),
('P00000000008', '周涛', '610113198504186636', '13800138008', 1, '西安市雁塔区');

INSERT INTO doctor (doctor_no, doctor_name, dept_id, title, phone) VALUES
('DOC00001', '吴建国', 1, '主任医师',   13900139001),
('DOC00002', '徐雅琴', 1, '主治医师',   13900139002),
('DOC00003', '马海涛', 2, '副主任医师', 13900139003),
('DOC00004', '何秀兰', 3, '主任医师',   13900139004),
('DOC00005', '林嘉怡', 4, '主治医师',   13900139005);

INSERT INTO register_record (register_no, patient_id, doctor_id, register_time, status, fee) VALUES
('R20261105000001', 1, 1, '2026-11-05 08:30:00', 1, 50.00),
('R20261105000002', 2, 2, '2026-11-05 09:10:00', 1, 30.00),
('R20261105000003', 3, 3, '2026-11-05 09:45:00', 0, 40.00),
('R20261105000004', 4, 4, '2026-11-05 10:20:00', 0, 50.00);

INSERT INTO prescription (prescription_no, record_id, diagnosis, create_time) VALUES
('RX20261105000001', 1, '原发性高血压 2 级（中危）', '2026-11-05 09:05:00'),
('RX20261105000002', 2, '急性上呼吸道感染',         '2026-11-05 09:40:00');

INSERT INTO prescription_item (prescription_id, drug_name, spec, dosage, quantity, unit_price) VALUES
(1, '苯磺酸氨氯地平片', '5mg*14 片', '1 片/次，1 次/日，晨服', 2, 28.50),
(1, '缬沙坦胶囊',       '80mg*7 粒', '1 粒/次，1 次/日',       1, 35.00),
(2, '连花清瘟胶囊',     '0.35g*24 粒', '4 粒/次，3 次/日',     2, 19.80),
(2, '布洛芬缓释胶囊',   '0.3g*20 粒', '1 粒/次，2 次/日',      1, 15.60);


-- ---------------------------------------------------------------------
-- 建表结果自检：应返回 6 行，TABLE_ROWS 为估算值仅供参考
-- ---------------------------------------------------------------------
SELECT TABLE_NAME AS '表名', TABLE_COMMENT AS '说明', ENGINE AS '引擎', TABLE_COLLATION AS '字符集'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'medical_system'
ORDER BY TABLE_NAME;

-- 外键约束清单（应返回 5 条）
SELECT CONSTRAINT_NAME AS '约束名', TABLE_NAME AS '子表',
       REFERENCED_TABLE_NAME AS '父表', DELETE_RULE AS '删除规则'
FROM information_schema.REFERENTIAL_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = 'medical_system'
ORDER BY TABLE_NAME;
