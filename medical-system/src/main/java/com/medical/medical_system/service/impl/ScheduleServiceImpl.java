package com.medical.medical_system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.medical_system.dto.ScheduleSaveDTO;
import com.medical.medical_system.entity.Department;
import com.medical.medical_system.entity.Doctor;
import com.medical.medical_system.entity.DoctorSchedule;
import com.medical.medical_system.exception.BusinessException;
import com.medical.medical_system.mapper.DepartmentMapper;
import com.medical.medical_system.mapper.DoctorMapper;
import com.medical.medical_system.mapper.DoctorScheduleMapper;
import com.medical.medical_system.service.ScheduleService;
import com.medical.medical_system.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生排班业务实现
 * <p>
 * 职责单一：只管排班的增改查。号源扣减不在本类发生——它属于挂号事务的
 * 一个步骤，由 RegisterServiceImpl 在事务内调用 Mapper 的 CAS 语句完成，
 * 保证"谁的事务谁负责"。
 */
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule>
        implements ScheduleService {

    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public ScheduleVO create(ScheduleSaveDTO dto) {
        // 同医生同日同时段唯一：业务层先查重给出可读提示，唯一索引并发兜底
        Long dup = baseMapper.selectCount(Wrappers.<DoctorSchedule>lambdaQuery()
                .eq(DoctorSchedule::getDoctorId, dto.getDoctorId())
                .eq(DoctorSchedule::getScheduleDate, dto.getScheduleDate())
                .eq(DoctorSchedule::getTimeSlot, dto.getTimeSlot()));
        if (dup != null && dup > 0) {
            throw new BusinessException(409, "该医生在此日期时段已有排班，不可重复排班");
        }
        checkDoctorExists(dto.getDoctorId());
        if (dto.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("排班日期不能早于今天");
        }

        DoctorSchedule s = new DoctorSchedule();
        applyDto(s, dto);
        s.setRemainingSlots(dto.getTotalSlots()); // 新排班剩余 = 总数
        baseMapper.insert(s);
        return toVO(s);
    }

    @Override
    public ScheduleVO update(Long id, ScheduleSaveDTO dto) {
        DoctorSchedule s = baseMapper.selectById(id);
        if (s == null) {
            throw new BusinessException(404, "排班不存在，id=" + id);
        }
        // 已被挂号消耗的号源不允许改到"剩余 > 总数"以下：重设总数不得小于已消耗量
        int used = dto.getTotalSlots() != null && s.getTotalSlots() != null
                ? s.getTotalSlots() - s.getRemainingSlots() : 0;
        if (dto.getTotalSlots() != null && dto.getTotalSlots() < used) {
            throw new BusinessException("号源总数不能小于已被挂号消耗的数量（已消耗 " + used + " 个）");
        }
        applyDto(s, dto);
        if (dto.getTotalSlots() != null) {
            s.setRemainingSlots(dto.getTotalSlots() - used);
        }
        baseMapper.updateById(s);
        return toVO(s);
    }

    @Override
    public List<ScheduleVO> listByDoctor(Long doctorId, LocalDate date) {
        List<DoctorSchedule> list = baseMapper.selectList(Wrappers.<DoctorSchedule>lambdaQuery()
                .eq(doctorId != null, DoctorSchedule::getDoctorId, doctorId)
                .eq(date != null, DoctorSchedule::getScheduleDate, date)
                .orderByAsc(DoctorSchedule::getScheduleDate)
                .orderByAsc(DoctorSchedule::getTimeSlot));
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public ScheduleVO detail(Long id) {
        DoctorSchedule s = baseMapper.selectById(id);
        if (s == null) {
            throw new BusinessException(404, "排班不存在，id=" + id);
        }
        return toVO(s);
    }

    // ---------------------------------------------------------------- 私有

    private void applyDto(DoctorSchedule s, ScheduleSaveDTO dto) {
        if (dto.getDoctorId() != null) {
            s.setDoctorId(dto.getDoctorId());
        }
        s.setScheduleDate(dto.getScheduleDate());
        s.setTimeSlot(dto.getTimeSlot());
        s.setTotalSlots(dto.getTotalSlots());
        s.setFee(dto.getFee());
        if (dto.getStatus() != null) {
            s.setStatus(dto.getStatus());
        }
    }

    private void checkDoctorExists(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在，id=" + doctorId);
        }
    }

    private ScheduleVO toVO(DoctorSchedule s) {
        Doctor doctor = doctorMapper.selectById(s.getDoctorId());
        String deptName = null;
        if (doctor != null && doctor.getDeptId() != null) {
            Department dept = departmentMapper.selectById(doctor.getDeptId());
            deptName = dept == null ? null : dept.getDeptName();
        }
        return ScheduleVO.of(s, doctor == null ? null : doctor.getDoctorName(), deptName);
    }
}
