package com.medical.medical_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.medical_system.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 患者 Mapper
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

    /** 统计该患者名下的挂号记录数，删除前校验引用关系 */
    @Select("SELECT COUNT(*) FROM register_record WHERE patient_id = #{patientId}")
    long countRegisterByPatientId(@Param("patientId") Long patientId);
}
