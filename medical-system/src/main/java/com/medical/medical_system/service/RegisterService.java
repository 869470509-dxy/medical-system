package com.medical.medical_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.medical_system.dto.RegisterCreateDTO;
import com.medical.medical_system.entity.RegisterRecord;
import com.medical.medical_system.vo.RegisterVO;

/**
 * 患者在线挂号业务
 */
public interface RegisterService extends IService<RegisterRecord> {

    /**
     * 在线挂号（事务方法）：
     * 校验患者/医生/排班 → 重复挂号校验 → CAS 扣减号源 → 生成挂号记录，
     * 任一步失败整体回滚。
     */
    RegisterVO register(RegisterCreateDTO dto);

    /** 取消挂号：释放号源（与扣减在同一事务语义下成对出现） */
    RegisterVO cancel(Long id);

    /** 按患者查就诊史 */
    java.util.List<RegisterVO> listByPatient(Long patientId);
}
