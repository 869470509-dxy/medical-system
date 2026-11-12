package com.medical.medical_system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 开方入参：一次挂号一张处方 + N 条明细
 */
@Data
public class PrescriptionCreateDTO {

    /** 挂号记录 id */
    @NotNull(message = "挂号记录不能为空")
    private Long recordId;

    /** 诊断结论 */
    @NotBlank(message = "诊断结论不能为空")
    @Size(max = 200, message = "诊断结论不能超过 200 字")
    private String diagnosis;

    /** 处方明细（至少一条） */
    @NotEmpty(message = "处方明细不能为空")
    @Valid
    private List<Item> items;

    /** 调试用：true 时在事务末尾抛出运行时异常，验证整体回滚 */
    private Boolean simulateError = false;

    @Data
    public static class Item {

        /** 药品 id */
        @NotNull(message = "药品不能为空")
        private Long drugId;

        /** 数量 */
        @NotNull(message = "药品数量不能为空")
        @Min(value = 1, message = "药品数量至少为 1")
        @Max(value = 999, message = "单种药品数量不能超过 999")
        private Integer quantity;

        /** 用法用量 */
        @Size(max = 40, message = "用法用量不能超过 40 字")
        private String dosage;
    }
}
