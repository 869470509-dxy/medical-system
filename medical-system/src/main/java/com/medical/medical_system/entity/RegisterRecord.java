package com.medical.medical_system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 挂号记录表 register_record
 */
@Data
@TableName("register_record")
public class RegisterRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 挂号单号，业务唯一键 */
    private String registerNo;

    /** 患者 id */
    private Long patientId;

    /** 医生 id */
    private Long doctorId;

    /** 关联排班 id，号源扣减依据 */
    private Long scheduleId;

    /** 挂号时间 */
    private LocalDateTime registerTime;

    /** 状态：0 待就诊，1 已就诊，2 已取消 */
    private Integer status;

    /** 挂号费（元） */
    private BigDecimal fee;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
