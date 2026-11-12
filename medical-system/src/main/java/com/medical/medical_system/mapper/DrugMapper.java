package com.medical.medical_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.medical_system.entity.Drug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 药品库存 Mapper
 */
@Mapper
public interface DrugMapper extends BaseMapper<Drug> {

    /**
     * CAS 扣减药品库存：仅当库存充足时才扣减。
     * <p>
     * 返回影响行数：1 = 扣减成功；0 = 库存不足。开方事务逐条明细调用，
     * 任一条返回 0 即抛出库存不足异常并整体回滚。
     */
    @Update("UPDATE drug SET stock = stock - #{quantity} "
            + "WHERE id = #{drugId} AND stock >= #{quantity}")
    int deductStock(@Param("drugId") Long drugId, @Param("quantity") Integer quantity);
}
