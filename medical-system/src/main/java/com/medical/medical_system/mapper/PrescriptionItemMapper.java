package com.medical.medical_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.medical_system.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方明细 Mapper
 */
@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {
}
