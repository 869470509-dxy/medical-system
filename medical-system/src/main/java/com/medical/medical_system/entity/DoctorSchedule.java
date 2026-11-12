package com.medical.medical_system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生排班表 doctor_schedule
 * <p>
 * 号源的业务载体：挂号事务通过 CAS 扣减 remaining_slots（带 remaining_slots > 0 条件），
 * 并发挂号也不会把号源扣成负数；数据库层 ck_schedule_remain CHECK 再兜底一道。
 */
@Data
@TableName("doctor_schedule")
public class DoctorSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 医生 id */
    private Long doctorId;

    /** 排班日期 */
    private LocalDate scheduleDate;

    /** 时段：0 上午，1 下午，2 夜间 */
    private Integer timeSlot;

    /** 号源总数 */
    private Integer totalSlots;

    /** 剩余号源，挂号事务内 CAS 扣减 */
    private Integer remainingSlots;

    /** 本场次挂号费（元） */
    private BigDecimal fee;

    /** 状态：0 停诊，1 出诊 */
    private Integer status;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
