package com.medical.medical_system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班新增 / 编辑入参
 */
@Data
public class ScheduleSaveDTO {

    /** 医生 id */
    @NotNull(message = "医生不能为空")
    private Long doctorId;

    /** 排班日期 */
    @NotNull(message = "排班日期不能为空")
    private LocalDate scheduleDate;

    /** 时段：0 上午，1 下午，2 夜间 */
    @NotNull(message = "时段不能为空")
    @Min(value = 0, message = "时段取值只能是 0 上午 / 1 下午 / 2 夜间")
    @Max(value = 2, message = "时段取值只能是 0 上午 / 1 下午 / 2 夜间")
    private Integer timeSlot;

    /** 号源总数 */
    @NotNull(message = "号源总数不能为空")
    @Min(value = 1, message = "号源总数至少为 1")
    @Max(value = 200, message = "号源总数不能超过 200")
    private Integer totalSlots;

    /** 挂号费（元） */
    @NotNull(message = "挂号费不能为空")
    @Min(value = 0, message = "挂号费不能为负数")
    private BigDecimal fee;

    /** 状态：0 停诊，1 出诊 */
    private Integer status;
}
