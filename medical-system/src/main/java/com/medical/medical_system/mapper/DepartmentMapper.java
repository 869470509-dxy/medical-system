package com.medical.medical_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.medical_system.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 科室 Mapper
 * <p>
 * 继承 BaseMapper 即可获得 insert / deleteById / updateById / selectById / selectPage 等 17 个通用方法，
 * 单表 CRUD 无需手写 XML。
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 统计该科室下的医生数量，用于删除前的业务校验。
     * 注意用 #{deptId} 而不是 ${deptId}：前者走 PreparedStatement 占位符，后者是字符串拼接，存在注入风险。
     */
    @Select("SELECT COUNT(*) FROM doctor WHERE dept_id = #{deptId}")
    long countDoctorByDeptId(@Param("deptId") Long deptId);
}
