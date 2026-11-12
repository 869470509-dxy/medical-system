-- =====================================================================
-- 实验三：业务逻辑与事务处理 —— 新增表与字段变更脚本
-- 1. doctor_schedule 医生排班表：挂号号源的载体，remaining_slots 由挂号事务 CAS 扣减
-- 2. drug 药品库存表：处方明细扣减库存的依据，CHECK 兜底防负库存
-- 3. prescription_item 增加 drug_id：处方明细关联药品，用于事务内扣减库存
-- 兼容性：可重复执行前请确认表不存在；register_record 的 schedule_id 允许为 NULL，
--         历史数据不受影响。
-- =====================================================================

-- 1. 医生排班表
CREATE TABLE doctor_schedule (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '排班主键',
    doctor_id       BIGINT      NOT NULL                COMMENT '医生 id',
    schedule_date   DATE        NOT NULL                COMMENT '排班日期',
    time_slot       TINYINT     NOT NULL                COMMENT '时段：0 上午，1 下午，2 夜间',
    total_slots     INT         NOT NULL                COMMENT '号源总数',
    remaining_slots INT         NOT NULL                COMMENT '剩余号源，挂号事务内 CAS 扣减',
    fee             DECIMAL(8,2) NOT NULL DEFAULT 0.00   COMMENT '本场次挂号费（元）',
    status          TINYINT     NOT NULL DEFAULT 0      COMMENT '状态：0 停诊，1 出诊',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule (doctor_id, schedule_date, time_slot) COMMENT '同一医生同日同时段唯一',
    CONSTRAINT ck_schedule_total  CHECK (total_slots > 0),
    CONSTRAINT ck_schedule_remain CHECK (remaining_slots >= 0 AND remaining_slots <= total_slots),
    CONSTRAINT ck_schedule_fee    CHECK (fee >= 0),
    CONSTRAINT ck_schedule_status CHECK (status IN (0, 1)),
    CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='医生排班表';

-- 2. 药品库存表
CREATE TABLE drug (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '药品主键',
    drug_code   VARCHAR(20)   NOT NULL                COMMENT '药品编码，业务唯一键',
    drug_name   VARCHAR(60)   NOT NULL                COMMENT '药品名称',
    spec        VARCHAR(40)   DEFAULT NULL            COMMENT '规格，如 0.25g*24 片',
    unit_price  DECIMAL(8,2)  NOT NULL DEFAULT 0.00   COMMENT '单价（元）',
    stock       INT           NOT NULL DEFAULT 0      COMMENT '当前库存数量',
    status      TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：0 停售，1 在售',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_drug_code (drug_code) COMMENT '药品编码唯一',
    CONSTRAINT ck_drug_price CHECK (unit_price >= 0),
    CONSTRAINT ck_drug_stock CHECK (stock >= 0),
    CONSTRAINT ck_drug_status CHECK (status IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='药品库存表';

-- 3. 处方明细关联药品（允许 NULL，兼容实验二已有数据）
ALTER TABLE prescription_item
    ADD COLUMN drug_id BIGINT DEFAULT NULL COMMENT '关联药品 id，开方时扣减库存' AFTER prescription_id,
    ADD CONSTRAINT fk_item_drug FOREIGN KEY (drug_id) REFERENCES drug (id)
        ON DELETE RESTRICT ON UPDATE CASCADE;

-- 4. 挂号记录关联排班（允许 NULL，兼容历史数据）
ALTER TABLE register_record
    ADD COLUMN schedule_id BIGINT DEFAULT NULL COMMENT '关联排班 id，号源扣减依据' AFTER doctor_id,
    ADD KEY idx_reg_schedule (schedule_id) COMMENT '按排班查号源消耗';

-- 5. 演示数据：三位医生未来 3 天的排班 + 6 种药品库存
INSERT INTO doctor_schedule (doctor_id, schedule_date, time_slot, total_slots, remaining_slots, fee, status) VALUES
(1, CURDATE(),                        0, 20, 20, 30.00, 1),
(1, CURDATE(),                        1, 15, 15, 30.00, 1),
(2, CURDATE(),                        0, 10, 10, 50.00, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 0, 10, 10, 50.00, 1),
(3, CURDATE(),                        2,  8,  8, 20.00, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 0, 12, 12, 20.00, 0);

INSERT INTO drug (drug_code, drug_name, spec, unit_price, stock, status) VALUES
('D1001', '阿莫西林胶囊',   '0.25g*24 粒', 15.80, 100, 1),
('D1002', '布洛芬缓释胶囊', '0.3g*20 粒',  22.50,  50, 1),
('D1003', '奥美拉唑肠溶胶囊','20mg*14 粒',  36.00,  30, 1),
('D1004', '连花清瘟颗粒',   '6g*10 袋',    19.90, 200, 1),
('D1005', '维生素C片',      '0.1g*100 片',  9.50,   0, 1),
('D1006', '板蓝根颗粒',     '10g*20 袋',   13.00,  80, 0);
