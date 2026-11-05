package com.medical.medical_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 医生新增/编辑入参
 */
@Data
public class DoctorSaveDTO {

    @NotBlank(message = "医生姓名不能为空")
    @Size(max = 20, message = "医生姓名不能超过 20 个字符")
    private String doctorName;

    /** 必须指向一个真实存在的科室，由数据库外键 fk_doctor_dept 二次兜底 */
    @NotNull(message = "所属科室不能为空")
    @Min(value = 1, message = "科室 id 必须为正整数")
    private Long deptId;

    @Size(max = 30, message = "职称不能超过 30 个字符")
    private String title;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$",
            message = "联系电话格式不正确，应为 1 开头的 11 位数字")
    private String phone;
}
