package com.medical.medical_system.vo;

import com.medical.medical_system.entity.PrescriptionItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方返回体（含明细）
 */
@Data
public class PrescriptionVO {

    private Long id;
    private String prescriptionNo;
    private Long recordId;
    private String registerNo;
    private String patientName;
    private String doctorName;
    private String diagnosis;
    private LocalDateTime createTime;

    /** 处方总金额（元） = Σ 明细数量 × 单价 */
    private BigDecimal totalAmount;

    private List<ItemVO> items;

    @Data
    public static class ItemVO {
        private Long id;
        private Long drugId;
        private String drugName;
        private String spec;
        private String dosage;
        private Integer quantity;
        private BigDecimal unitPrice;
        /** 小计 = 数量 × 单价 */
        private BigDecimal subtotal;

        public static ItemVO of(PrescriptionItem it) {
            ItemVO vo = new ItemVO();
            vo.setId(it.getId());
            vo.setDrugId(it.getDrugId());
            vo.setDrugName(it.getDrugName());
            vo.setSpec(it.getSpec());
            vo.setDosage(it.getDosage());
            vo.setQuantity(it.getQuantity());
            vo.setUnitPrice(it.getUnitPrice());
            vo.setSubtotal(it.getUnitPrice() == null || it.getQuantity() == null
                    ? null : it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())));
            return vo;
        }
    }
}
