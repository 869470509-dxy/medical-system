package com.medical.medical_system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 处方明细表 prescription_item
 */
@Data
@TableName("prescription_item")
public class PrescriptionItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属处方 id */
    private Long prescriptionId;

    /** 关联药品 id，开方时据此扣减库存 */
    private Long drugId;

    /** 药品名称（下单时快照） */
    private String drugName;

    /** 规格 */
    private String spec;

    /** 用法用量 */
    private String dosage;

    /** 数量 */
    private Integer quantity;

    /** 单价（元，开方时快照） */
    private BigDecimal unitPrice;
}
