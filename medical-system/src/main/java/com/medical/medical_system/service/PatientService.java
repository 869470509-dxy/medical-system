package com.medical.medical_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.dto.PatientSaveDTO;
import com.medical.medical_system.entity.Patient;
import com.medical.medical_system.vo.PatientVO;

/**
 * 患者业务接口
 */
public interface PatientService extends IService<Patient> {

    PageResult<PatientVO> pageQuery(long pageNum, long pageSize, String patientName, String phone, Integer gender);

    PatientVO detail(Long id);

    PatientVO create(PatientSaveDTO dto);

    PatientVO update(Long id, PatientSaveDTO dto);

    void remove(Long id);
}
