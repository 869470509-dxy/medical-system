package com.medical.medical_system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.medical_system.dto.RegisterCreateDTO;
import com.medical.medical_system.entity.Department;
import com.medical.medical_system.entity.Doctor;
import com.medical.medical_system.entity.DoctorSchedule;
import com.medical.medical_system.entity.Patient;
import com.medical.medical_system.entity.RegisterRecord;
import com.medical.medical_system.exception.BusinessException;
import com.medical.medical_system.mapper.DepartmentMapper;
import com.medical.medical_system.mapper.DoctorMapper;
import com.medical.medical_system.mapper.DoctorScheduleMapper;
import com.medical.medical_system.mapper.PatientMapper;
import com.medical.medical_system.mapper.RegisterRecordMapper;
import com.medical.medical_system.service.RegisterService;
import com.medical.medical_system.vo.RegisterVO;
import com.medical.medical_system.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 患者在线挂号业务实现
 * <p>
 * 事务边界设计：register() 的三个写相关步骤——重复校验、号源 CAS 扣减、挂号记录落库——
 * 全部在同一个 @Transactional 内完成。号源扣减失败抛异常 → 已执行的写操作全部回滚；
 * 挂号记录插入失败（如唯一键冲突）→ 已扣减的号源同样回滚，不存在"扣了号没挂上"的中间态。
 * <p>
 * 分层职责：Controller 只做参数接收与转发；本类做业务编排与校验；纯 SQL 条件更新
 * 下沉到 Mapper（CAS 扣号源），保证"查-判-改"的原子性且便于复用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterServiceImpl extends ServiceImpl<RegisterRecordMapper, RegisterRecord>
        implements RegisterService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final DoctorScheduleMapper scheduleMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO register(RegisterCreateDTO dto) {
        // ---- 1. 基础校验：三方数据都必须真实存在且有效 ----------------
        Patient patient = patientMapper.selectById(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException(404, "患者不存在，请先建档，id=" + dto.getPatientId());
        }
        Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在，id=" + dto.getDoctorId());
        }
        DoctorSchedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在或已删除，id=" + dto.getScheduleId());
        }
        if (!doctor.getId().equals(schedule.getDoctorId())) {
            throw new BusinessException("该排班不属于所选医生，请重新选择");
        }
        if (schedule.getStatus() == null || schedule.getStatus() != 1) {
            throw new BusinessException("该场次已停诊，请选择其他排班");
        }
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("该排班已过期，不能挂过往日期的号");
        }

        // ---- 2. 重复挂号校验：同患者 + 同排班 已有待就诊记录 ----------
        // 业务层先查给出可读提示；若挂号表后续加唯一索引，数据库层可再兜底并发
        Long dup = baseMapper.selectCount(Wrappers.<RegisterRecord>lambdaQuery()
                .eq(RegisterRecord::getPatientId, dto.getPatientId())
                .eq(RegisterRecord::getScheduleId, dto.getScheduleId())
                .eq(RegisterRecord::getStatus, 0));
        if (dup != null && dup > 0) {
            throw new BusinessException(409, "您已挂过 " + schedule.getScheduleDate()
                    + " " + ScheduleVO.slotText(schedule.getTimeSlot()) + " 该医生的号，请勿重复挂号");
        }

        // ---- 3. CAS 扣减号源：影响行数 0 即号源已满 -------------------
        int rows = scheduleMapper.deductSlot(schedule.getId());
        if (rows == 0) {
            throw new BusinessException(409, "号源已满，本场次仅剩 "
                    + schedule.getRemainingSlots() + " 个号源，请选择其他时段");
        }

        // ---- 4. 生成挂号记录 ----------------------------------------
        RegisterRecord record = new RegisterRecord();
        record.setRegisterNo(nextRegisterNo());
        record.setPatientId(dto.getPatientId());
        record.setDoctorId(dto.getDoctorId());
        record.setScheduleId(schedule.getId());
        record.setRegisterTime(LocalDateTime.now());
        record.setStatus(0);
        record.setFee(schedule.getFee());
        baseMapper.insert(record);
        log.info("挂号成功: no={}, patient={}, schedule={}",
                record.getRegisterNo(), patient.getPatientName(), schedule.getId());

        return toVO(record, patient, doctor, schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO cancel(Long id) {
        RegisterRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "挂号记录不存在，id=" + id);
        }
        if (record.getStatus() == null || record.getStatus() != 0) {
            throw new BusinessException("仅待就诊的挂号可以取消");
        }
        record.setStatus(2);
        baseMapper.updateById(record);

        // 释放号源：取消挂号必须把号源还回去，与扣减成对出现
        if (record.getScheduleId() != null) {
            DoctorSchedule schedule = scheduleMapper.selectById(record.getScheduleId());
            if (schedule != null) {
                schedule.setRemainingSlots(schedule.getRemainingSlots() + 1);
                scheduleMapper.updateById(schedule);
            }
        }
        Patient patient = patientMapper.selectById(record.getPatientId());
        Doctor doctor = doctorMapper.selectById(record.getDoctorId());
        DoctorSchedule schedule = record.getScheduleId() == null
                ? null : scheduleMapper.selectById(record.getScheduleId());
        return toVO(record, patient, doctor, schedule);
    }

    @Override
    public List<RegisterVO> listByPatient(Long patientId) {
        List<RegisterRecord> list = baseMapper.selectList(Wrappers.<RegisterRecord>lambdaQuery()
                .eq(RegisterRecord::getPatientId, patientId)
                .orderByDesc(RegisterRecord::getId));
        return list.stream().map(r -> {
            Doctor doctor = doctorMapper.selectById(r.getDoctorId());
            DoctorSchedule schedule = r.getScheduleId() == null
                    ? null : scheduleMapper.selectById(r.getScheduleId());
            return toVO(r, patientMapper.selectById(r.getPatientId()), doctor, schedule);
        }).toList();
    }

    // ---------------------------------------------------------------- 私有

    private String nextRegisterNo() {
        // 单号规则：REG + 时间戳 + 3 位随机数；数据库唯一键兜底并发撞号
        return "REG" + LocalDateTime.now().format(NO_FMT)
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

    private RegisterVO toVO(RegisterRecord r, Patient patient, Doctor doctor, DoctorSchedule schedule) {
        String deptName = null;
        if (doctor != null && doctor.getDeptId() != null) {
            Department dept = departmentMapper.selectById(doctor.getDeptId());
            deptName = dept == null ? null : dept.getDeptName();
        }
        return RegisterVO.of(r,
                patient == null ? null : patient.getPatientName(),
                doctor == null ? null : doctor.getDoctorName(),
                deptName,
                schedule == null ? null : schedule.getScheduleDate().toString(),
                schedule == null ? null : ScheduleVO.slotText(schedule.getTimeSlot()));
    }
}
