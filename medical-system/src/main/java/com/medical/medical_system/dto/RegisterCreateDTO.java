package com.medical.medical_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 挂号入参
 * <p>
 * 挂号费、挂号时间、单号均由服务端生成，前端只提交三个决策字段。
 */
@Data
public class RegisterCreateDTO {

    /** 患者 id */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /** 医生 id */
    @NotNull(message = "医生不能为空")
    private Long doctorId;

    /** 排班 id（决定日期、时段与号源） */
    @NotNull(message = "排班不能为空")
    private Long scheduleId;
}
