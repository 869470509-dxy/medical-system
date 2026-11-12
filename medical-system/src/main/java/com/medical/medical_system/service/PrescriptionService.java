package com.medical.medical_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.medical_system.dto.PrescriptionCreateDTO;
import com.medical.medical_system.entity.Prescription;
import com.medical.medical_system.vo.PrescriptionVO;

/**
 * 电子处方业务
 */
public interface PrescriptionService extends IService<Prescription> {

    /**
     * 开具处方（事务方法）：挂号校验 → 处方落库 → 明细落库 → 药品库存逐条 CAS 扣减，
     * 任一环节失败整体回滚。simulateError=true 用于手动模拟异常，验证回滚效果。
     */
    PrescriptionVO create(PrescriptionCreateDTO dto);

    /** 按挂号记录查处方 */
    PrescriptionVO findByRecord(Long recordId);
}
