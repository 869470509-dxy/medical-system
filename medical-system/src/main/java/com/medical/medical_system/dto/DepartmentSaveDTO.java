package com.medical.medical_system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 科室新增/编辑入参
 */
@Data
public class DepartmentSaveDTO {

    @NotBlank(message = "科室名称不能为空")
    @Size(max = 50, message = "科室名称不能超过 50 个字符")
    private String deptName;

    @Size(max = 200, message = "科室简介不能超过 200 个字符")
    private String deptDesc;

    @Min(value = 0, message = "状态只能是 0 停用 / 1 启用")
    @Max(value = 1, message = "状态只能是 0 停用 / 1 启用")
    private Integer status;
}
