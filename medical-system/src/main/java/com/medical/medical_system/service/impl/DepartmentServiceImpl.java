package com.medical.medical_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.dto.DepartmentSaveDTO;
import com.medical.medical_system.entity.Department;
import com.medical.medical_system.exception.BusinessException;
import com.medical.medical_system.mapper.DepartmentMapper;
import com.medical.medical_system.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 科室业务实现
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
        implements DepartmentService {

    @Override
    public PageResult<Department> pageQuery(long pageNum, long pageSize, String deptName, Integer status) {
        LambdaQueryWrapper<Department> qw = Wrappers.<Department>lambdaQuery()
                // 条件成立才拼进 where，实现"多条件任意组合"
                .like(StringUtils.hasText(deptName), Department::getDeptName, deptName)
                .eq(status != null, Department::getStatus, status)
                .orderByDesc(Department::getId);
        Page<Department> page = new Page<>(pageNum, pageSize);
        return PageResult.of(baseMapper.selectPage(page, qw));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Department create(DepartmentSaveDTO dto) {
        Long dup = baseMapper.selectCount(Wrappers.<Department>lambdaQuery()
                .eq(Department::getDeptName, dto.getDeptName()));
        if (dup != null && dup > 0) {
            throw new BusinessException("科室名称「" + dto.getDeptName() + "」已存在，请勿重复建科");
        }
        Department entity = new Department();
        entity.setDeptCode(nextDeptCode());
        entity.setDeptName(dto.getDeptName());
        entity.setDeptDesc(dto.getDeptDesc());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        baseMapper.insert(entity);
        // 重新查一次，把数据库填充的 create_time/update_time 带回给前端
        return baseMapper.selectById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Department update(Long id, DepartmentSaveDTO dto) {
        Department exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "科室不存在，id=" + id);
        }
        Long dup = baseMapper.selectCount(Wrappers.<Department>lambdaQuery()
                .eq(Department::getDeptName, dto.getDeptName())
                .ne(Department::getId, id));
        if (dup != null && dup > 0) {
            throw new BusinessException("科室名称「" + dto.getDeptName() + "」已被其他科室占用");
        }
        Department entity = new Department();
        entity.setId(id);
        entity.setDeptName(dto.getDeptName());
        entity.setDeptDesc(dto.getDeptDesc());
        entity.setStatus(dto.getStatus());
        baseMapper.updateById(entity);
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Department exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "科室不存在，id=" + id);
        }
        // 先做业务校验，给出可读提示；数据库外键 RESTRICT 作为最后一道防线
        long doctorCount = baseMapper.countDoctorByDeptId(id);
        if (doctorCount > 0) {
            throw new BusinessException("该科室下仍有 " + doctorCount + " 名医生，请先调岗或删除医生后再删除科室");
        }
        baseMapper.deleteById(id);
    }

    /**
     * 生成科室编码：D + 5 位序号。
     * 用库里的最大序号 +1，而不是 count+1——删过数据后 count 会回落，容易撞上已存在的编码。
     */
    private String nextDeptCode() {
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.select("IFNULL(MAX(CAST(SUBSTRING(dept_code, 2) AS UNSIGNED)), 0) AS max_no");
        List<Object> objs = baseMapper.selectObjs(qw);
        long max = (objs == null || objs.isEmpty() || objs.get(0) == null)
                ? 0L : ((Number) objs.get(0)).longValue();
        return String.format("D%05d", max + 1);
    }
}
