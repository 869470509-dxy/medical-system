package com.medical.medical_system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者表 patient
 * <p>
 * 注意：本表刻意不存 age（年龄）和 birth_date（出生日期）。
 * 二者都能由 id_card 唯一推导，存进来就形成 id -> id_card -> birth_date 的传递依赖，
 * 不满足第三范式；更实际的问题是年龄每年都会变，入库值必然逐渐失真。
 * 因此改在返回给前端的 PatientVO 里按身份证实时计算。
 */
@Data
@TableName("patient")
public class Patient {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者编号，业务唯一键 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 身份证号，唯一，一人一档 */
    private String idCard;

    /** 手机号，唯一 */
    private String phone;

    /** 性别：0 未知，1 男，2 女 */
    private Integer gender;

    /** 联系地址 */
    private String address;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
