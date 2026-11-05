package com.medical.medical_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.dto.DepartmentSaveDTO;
import com.medical.medical_system.entity.Department;

/**
 * 科室业务接口
 */
public interface DepartmentService extends IService<Department> {

    PageResult<Department> pageQuery(long pageNum, long pageSize, String deptName, Integer status);

    Department create(DepartmentSaveDTO dto);

    Department update(Long id, DepartmentSaveDTO dto);

    void remove(Long id);
}
