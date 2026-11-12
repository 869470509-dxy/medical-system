package com.medical.medical_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.medical_system.entity.RegisterRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 挂号记录 Mapper
 */
@Mapper
public interface RegisterRecordMapper extends BaseMapper<RegisterRecord> {
}
