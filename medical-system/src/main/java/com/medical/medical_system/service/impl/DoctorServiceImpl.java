package com.medical.medical_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.dto.DoctorSaveDTO;
import com.medical.medical_system.entity.Department;
import com.medical.medical_system.entity.Doctor;
import com.medical.medical_system.exception.BusinessException;
import com.medical.medical_system.mapper.DepartmentMapper;
import com.medical.medical_system.mapper.DoctorMapper;
import com.medical.medical_system.service.DoctorService;
import com.medical.medical_system.vo.DoctorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 医生业务实现
 */
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    private final DepartmentMapper departmentMapper;

    @Override
    public PageResult<DoctorVO> pageQuery(long pageNum, long pageSize,
                                          String doctorName, Long deptId, String deptName) {
        // 科室名称是模糊条件，先查出一批 deptId 再做 in 过滤；
        // 查不到说明条件自相矛盾，直接返回空页，省掉一次必然为空的 SQL
        List<Long> deptIds = null;
        if (StringUtils.hasText(deptName)) {
            deptIds = departmentMapper.selectList(Wrappers.<Department>lambdaQuery()
                            .like(Department::getDeptName, deptName))
                    .stream().map(Department::getId).toList();
            if (deptIds.isEmpty()) {
                return PageResult.empty(pageNum, pageSize);
            }
            if (deptId != null && !deptIds.contains(deptId)) {
                return PageResult.empty(pageNum, pageSize);
            }
        }

        LambdaQueryWrapper<Doctor> qw = Wrappers.<Doctor>lambdaQuery()
                .like(StringUtils.hasText(doctorName), Doctor::getDoctorName, doctorName)
                .eq(deptId != null, Doctor::getDeptId, deptId)
                .in(deptIds != null, Doctor::getDeptId, deptIds)
                .orderByDesc(Doctor::getId);

        IPage<Doctor> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<DoctorVO> vos = page.getRecords().stream().map(DoctorVO::from).toList();
        fillDeptName(vos);
        vos.forEach(DoctorVO::maskPhone);
        return PageResult.of(page, vos);
    }

    @Override
    public DoctorVO detail(Long id) {
        Doctor doctor = baseMapper.selectById(id);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在，id=" + id);
        }
        DoctorVO vo = DoctorVO.from(doctor);
        Department dept = departmentMapper.selectById(doctor.getDeptId());
        vo.setDeptName(dept == null ? null : dept.getDeptName());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DoctorVO create(DoctorSaveDTO dto) {
        requireDeptExists(dto.getDeptId());
        Long dupPhone = baseMapper.selectCount(Wrappers.<Doctor>lambdaQuery()
                .eq(Doctor::getPhone, dto.getPhone()));
        if (dupPhone != null && dupPhone > 0) {
            throw new BusinessException("联系电话 " + dto.getPhone() + " 已被其他医生使用");
        }
        Doctor entity = new Doctor();
        entity.setDoctorNo(nextDoctorNo());
        entity.setDoctorName(dto.getDoctorName());
        entity.setDeptId(dto.getDeptId());
        entity.setTitle(dto.getTitle());
        entity.setPhone(dto.getPhone());
        baseMapper.insert(entity);
        return detail(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DoctorVO update(Long id, DoctorSaveDTO dto) {
        Doctor exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "医生不存在，id=" + id);
        }
        requireDeptExists(dto.getDeptId());
        Long dupPhone = baseMapper.selectCount(Wrappers.<Doctor>lambdaQuery()
                .eq(Doctor::getPhone, dto.getPhone())
                .ne(Doctor::getId, id));
        if (dupPhone != null && dupPhone > 0) {
            throw new BusinessException("联系电话 " + dto.getPhone() + " 已被其他医生使用");
        }
        Doctor entity = new Doctor();
        entity.setId(id);
        entity.setDoctorName(dto.getDoctorName());
        entity.setDeptId(dto.getDeptId());
        entity.setTitle(dto.getTitle());
        entity.setPhone(dto.getPhone());
        baseMapper.updateById(entity);
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Doctor exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "医生不存在，id=" + id);
        }
        long regCount = baseMapper.countRegisterByDoctorId(id);
        if (regCount > 0) {
            throw new BusinessException("该医生存在 " + regCount + " 条挂号记录，为保证病历可追溯不允许删除");
        }
        baseMapper.deleteById(id);
    }

    private void requireDeptExists(Long deptId) {
        if (departmentMapper.selectById(deptId) == null) {
            throw new BusinessException("所属科室不存在，id=" + deptId);
        }
    }

    /** 批量回填科室名称：一次 selectBatchIds 解决，避免逐条查询产生 N+1 */
    private void fillDeptName(List<DoctorVO> vos) {
        if (vos.isEmpty()) {
            return;
        }
        List<Long> deptIds = vos.stream().map(DoctorVO::getDeptId).distinct().toList();
        Map<Long, String> nameMap = departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getDeptName));
        vos.forEach(v -> v.setDeptName(nameMap.get(v.getDeptId())));
    }

    /** 生成工号：DOC + 5 位序号，取库里最大值 +1 */
    private String nextDoctorNo() {
        QueryWrapper<Doctor> qw = new QueryWrapper<>();
        qw.select("IFNULL(MAX(CAST(SUBSTRING(doctor_no, 4) AS UNSIGNED)), 0) AS max_no");
        List<Object> objs = baseMapper.selectObjs(qw);
        long max = (objs == null || objs.isEmpty() || objs.get(0) == null)
                ? 0L : ((Number) objs.get(0)).longValue();
        return String.format("DOC%05d", max + 1);
    }
}
