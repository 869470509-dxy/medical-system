package com.medical.medical_system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 患者新增/编辑入参
 * <p>
 * 刻意不接收 patientNo：编号属于系统生成的业务键，不能让调用方随便指定，
 * 否则会出现两个患者抢同一个编号的脏数据。
 */
@Data
public class PatientSaveDTO {

    @NotBlank(message = "患者姓名不能为空")
    @Size(max = 20, message = "患者姓名不能超过 20 个字符")
    private String patientName;

    /** 由 @IdCard 执行格式 + 校验位 + 日期真实性三重校验 */
    @NotBlank(message = "身份证号不能为空")
    @com.medical.medical_system.validation.IdCard
    private String idCard;

    @NotBlank(message = "手机号不能为空")
    @jakarta.validation.constraints.Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确，应为 1 开头的 11 位数字")
    private String phone;

    @Min(value = 0, message = "性别取值只能是 0 未知 / 1 男 / 2 女")
    @Max(value = 2, message = "性别取值只能是 0 未知 / 1 男 / 2 女")
    private Integer gender;

    @Size(max = 120, message = "联系地址不能超过 120 个字符")
    private String address;
}
