package com.medical.medical_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.medical_system.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 医生 Mapper
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /** 统计该医生名下的挂号记录数，删除前校验引用关系 */
    @Select("SELECT COUNT(*) FROM register_record WHERE doctor_id = #{doctorId}")
    long countRegisterByDoctorId(@Param("doctorId") Long doctorId);
}
