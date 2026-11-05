package com.medical.medical_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.dto.DoctorSaveDTO;
import com.medical.medical_system.entity.Doctor;
import com.medical.medical_system.vo.DoctorVO;

/**
 * 医生业务接口
 */
public interface DoctorService extends IService<Doctor> {

    /**
     * 多条件组合分页
     *
     * @param doctorName 医生姓名，模糊匹配
     * @param deptId     科室 id，精确匹配
     * @param deptName   科室名称，模糊匹配（先翻译成 deptId 集合再过滤，避免表连接）
     */
    PageResult<DoctorVO> pageQuery(long pageNum, long pageSize, String doctorName, Long deptId, String deptName);

    DoctorVO detail(Long id);

    DoctorVO create(DoctorSaveDTO dto);

    DoctorVO update(Long id, DoctorSaveDTO dto);

    void remove(Long id);
}
