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
 * 药品库存表 drug
 * <p>
 * 开方事务按明细逐条 CAS 扣减 stock（带 stock >= quantity 条件），
 * 任何一种药扣减失败即整单回滚；ck_drug_stock CHECK 在数据库层兜底防负库存。
 */
@Data
@TableName("drug")
public class Drug {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 药品编码，业务唯一键 */
    private String drugCode;

    /** 药品名称 */
    private String drugName;

    /** 规格 */
    private String spec;

    /** 单价（元） */
    private BigDecimal unitPrice;

    /** 当前库存数量 */
    private Integer stock;

    /** 状态：0 停售，1 在售 */
    private Integer status;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
