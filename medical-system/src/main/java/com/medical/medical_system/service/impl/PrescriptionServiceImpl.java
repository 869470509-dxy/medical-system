package com.medical.medical_system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.medical_system.dto.PrescriptionCreateDTO;
import com.medical.medical_system.entity.Department;
import com.medical.medical_system.entity.Doctor;
import com.medical.medical_system.entity.Drug;
import com.medical.medical_system.entity.Patient;
import com.medical.medical_system.entity.Prescription;
import com.medical.medical_system.entity.PrescriptionItem;
import com.medical.medical_system.entity.RegisterRecord;
import com.medical.medical_system.exception.BusinessException;
import com.medical.medical_system.mapper.DepartmentMapper;
import com.medical.medical_system.mapper.DoctorMapper;
import com.medical.medical_system.mapper.DrugMapper;
import com.medical.medical_system.mapper.PatientMapper;
import com.medical.medical_system.mapper.PrescriptionItemMapper;
import com.medical.medical_system.mapper.PrescriptionMapper;
import com.medical.medical_system.mapper.RegisterRecordMapper;
import com.medical.medical_system.service.PrescriptionService;
import com.medical.medical_system.vo.PrescriptionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 电子处方业务实现
 * <p>
 * 事务边界设计：开方一共四步——处方头落库、N 条明细落库、每种药 CAS 扣库存、
 * 挂号状态置为已就诊——全部包在一个 @Transactional(rollbackFor = Exception.class) 里。
 * 其中任何一步抛出异常（包括第 6 步刻意注入的模拟异常），前面已执行的 insert 与
 * 扣减全部回滚，挂号状态也不会被误改，保证"处方-库存-挂号"三方一致。
 * <p>
 * 分层职责：Controller 只收参数；本类做业务编排、校验与事务；库存 CAS 语句下沉 Mapper。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription>
        implements PrescriptionService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PrescriptionItemMapper itemMapper;
    private final DrugMapper drugMapper;
    private final RegisterRecordMapper recordMapper;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionVO create(PrescriptionCreateDTO dto) {
        // ---- 1. 挂号记录校验：存在且待就诊，一次挂号一张处方 ----------
        RegisterRecord record = recordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException(404, "挂号记录不存在，id=" + dto.getRecordId());
        }
        if (record.getStatus() == null || record.getStatus() != 0) {
            throw new BusinessException("该挂号记录不在待就诊状态，不能开方");
        }
        Long dup = baseMapper.selectCount(Wrappers.<Prescription>lambdaQuery()
                .eq(Prescription::getRecordId, dto.getRecordId()));
        if (dup != null && dup > 0) {
            throw new BusinessException(409, "该挂号已开具过处方，一次就诊只能开一张处方");
        }

        // ---- 2. 预校验全部药品：在售、明细不重复 ---------------------
        for (PrescriptionCreateDTO.Item item : dto.getItems()) {
            Drug drug = drugMapper.selectById(item.getDrugId());
            if (drug == null) {
                throw new BusinessException(404, "药品不存在，id=" + item.getDrugId());
            }
            if (drug.getStatus() == null || drug.getStatus() != 1) {
                throw new BusinessException("药品【" + drug.getDrugName() + "】已停售，请调整处方");
            }
        }
        long distinctDrugs = dto.getItems().stream().map(PrescriptionCreateDTO.Item::getDrugId).distinct().count();
        if (distinctDrugs < dto.getItems().size()) {
            throw new BusinessException("处方明细存在重复药品，请合并数量后重新提交");
        }

        // ---- 3. 处方头落库 ------------------------------------------
        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo(nextPrescriptionNo());
        prescription.setRecordId(dto.getRecordId());
        prescription.setDiagnosis(dto.getDiagnosis());
        baseMapper.insert(prescription);

        // ---- 4. 明细落库 + 逐条 CAS 扣库存 ---------------------------
        for (PrescriptionCreateDTO.Item item : dto.getItems()) {
            Drug drug = drugMapper.selectById(item.getDrugId());

            PrescriptionItem it = new PrescriptionItem();
            it.setPrescriptionId(prescription.getId());
            it.setDrugId(drug.getId());
            it.setDrugName(drug.getDrugName());   // 名称/单价开方时快照，药品改名不影响历史处方
            it.setSpec(drug.getSpec());
            it.setDosage(item.getDosage());
            it.setQuantity(item.getQuantity());
            it.setUnitPrice(drug.getUnitPrice());
            itemMapper.insert(it);

            // CAS 扣库存：影响行数 0 = 库存不足，抛异常触发整个事务回滚
            int rows = drugMapper.deductStock(drug.getId(), item.getQuantity());
            if (rows == 0) {
                throw new BusinessException(409, "药品【" + drug.getDrugName()
                        + "】库存不足，当前库存 " + drug.getStock() + "，处方已整体取消");
            }
        }

        // ---- 5. 挂号状态流转为已就诊 ---------------------------------
        record.setStatus(1);
        recordMapper.updateById(record);

        // ---- 6. 手动模拟异常：验证事务回滚效果（调试开关）------------
        if (Boolean.TRUE.equals(dto.getSimulateError())) {
            // 抛出非 BusinessException 的运行时异常，模拟"程序在事务中途崩溃"
            throw new IllegalStateException("【模拟异常】事务即将回滚：本次开方的处方/明细/库存扣减/挂号状态应全部撤销");
        }

        log.info("开方成功: no={}, items={}", prescription.getPrescriptionNo(), dto.getItems().size());
        return toVO(prescription, record);
    }

    @Override
    public PrescriptionVO findByRecord(Long recordId) {
        Prescription prescription = baseMapper.selectOne(Wrappers.<Prescription>lambdaQuery()
                .eq(Prescription::getRecordId, recordId));
        if (prescription == null) {
            throw new BusinessException(404, "该挂号记录尚未开具处方，recordId=" + recordId);
        }
        RegisterRecord record = recordMapper.selectById(recordId);
        return toVO(prescription, record);
    }

    // ---------------------------------------------------------------- 私有

    private String nextPrescriptionNo() {
        return "RX" + LocalDateTime.now().format(NO_FMT)
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

    private PrescriptionVO toVO(Prescription p, RegisterRecord record) {
        PrescriptionVO vo = new PrescriptionVO();
        vo.setId(p.getId());
        vo.setPrescriptionNo(p.getPrescriptionNo());
        vo.setRecordId(p.getRecordId());
        vo.setDiagnosis(p.getDiagnosis());
        vo.setCreateTime(p.getCreateTime());
        if (record != null) {
            vo.setRegisterNo(record.getRegisterNo());
            Patient patient = patientMapper.selectById(record.getPatientId());
            vo.setPatientName(patient == null ? null : patient.getPatientName());
            Doctor doctor = doctorMapper.selectById(record.getDoctorId());
            vo.setDoctorName(doctor == null ? null : doctor.getDoctorName());
            if (doctor != null && doctor.getDeptId() != null) {
                Department dept = departmentMapper.selectById(doctor.getDeptId());
                vo.setDoctorName((doctor.getDoctorName() == null ? "" : doctor.getDoctorName())
                        + (dept == null ? "" : "（" + dept.getDeptName() + "）"));
            }
        }
        List<PrescriptionItem> items = itemMapper.selectList(Wrappers.<PrescriptionItem>lambdaQuery()
                .eq(PrescriptionItem::getPrescriptionId, p.getId())
                .orderByAsc(PrescriptionItem::getId));
        vo.setItems(items.stream().map(PrescriptionVO.ItemVO::of).toList());
        BigDecimal total = items.stream()
                .map(it -> it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalAmount(total);
        return vo;
    }
}
