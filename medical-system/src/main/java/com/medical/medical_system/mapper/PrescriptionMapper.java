package com.medical.medical_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.medical_system.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电子处方 Mapper
 */
@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
}
