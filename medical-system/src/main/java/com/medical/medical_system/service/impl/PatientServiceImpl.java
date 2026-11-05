package com.medical.medical_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.dto.PatientSaveDTO;
import com.medical.medical_system.entity.Patient;
import com.medical.medical_system.exception.BusinessException;
import com.medical.medical_system.mapper.PatientMapper;
import com.medical.medical_system.service.PatientService;
import com.medical.medical_system.util.IdCardUtils;
import com.medical.medical_system.vo.PatientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 患者业务实现
 */
@Service
@RequiredArgsConstructor
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Override
    public PageResult<PatientVO> pageQuery(long pageNum, long pageSize,
                                           String patientName, String phone, Integer gender) {
        LambdaQueryWrapper<Patient> qw = Wrappers.<Patient>lambdaQuery()
                .like(StringUtils.hasText(patientName), Patient::getPatientName, patientName)
                .likeRight(StringUtils.hasText(phone), Patient::getPhone, phone)
                .eq(gender != null, Patient::getGender, gender)
                .orderByDesc(Patient::getId);
        IPage<Patient> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<PatientVO> vos = page.getRecords().stream().map(PatientVO::masked).toList();
        return PageResult.of(page, vos);
    }

    @Override
    public PatientVO detail(Long id) {
        Patient patient = baseMapper.selectById(id);
        if (patient == null) {
            throw new BusinessException(404, "患者不存在，id=" + id);
        }
        return PatientVO.full(patient);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientVO create(PatientSaveDTO dto) {
        String idCard = dto.getIdCard().trim().toUpperCase();
        String phone = dto.getPhone().trim();

        // 业务层先查重，给出"是哪一项重复"的明确提示；数据库唯一索引作为并发下的兜底
        Long dupIdCard = baseMapper.selectCount(Wrappers.<Patient>lambdaQuery()
                .eq(Patient::getIdCard, idCard));
        if (dupIdCard != null && dupIdCard > 0) {
            throw new BusinessException("身份证号 " + idCard + " 已建档，请勿重复录入");
        }
        Long dupPhone = baseMapper.selectCount(Wrappers.<Patient>lambdaQuery()
                .eq(Patient::getPhone, phone));
        if (dupPhone != null && dupPhone > 0) {
            throw new BusinessException("手机号 " + phone + " 已被其他患者登记");
        }

        Patient entity = new Patient();
        entity.setPatientNo(nextPatientNo());
        entity.setPatientName(dto.getPatientName());
        entity.setIdCard(idCard);
        entity.setPhone(phone);
        // 未显式填写性别时，按身份证第 17 位自动推导，减少录入遗漏
        entity.setGender(dto.getGender() != null && dto.getGender() != 0
                ? dto.getGender() : IdCardUtils.getGender(idCard));
        entity.setAddress(dto.getAddress());
        baseMapper.insert(entity);
        return detail(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientVO update(Long id, PatientSaveDTO dto) {
        Patient exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "患者不存在，id=" + id);
        }
        String idCard = dto.getIdCard().trim().toUpperCase();
        String phone = dto.getPhone().trim();

        Long dupIdCard = baseMapper.selectCount(Wrappers.<Patient>lambdaQuery()
                .eq(Patient::getIdCard, idCard)
                .ne(Patient::getId, id));
        if (dupIdCard != null && dupIdCard > 0) {
            throw new BusinessException("身份证号 " + idCard + " 已被其他患者占用");
        }
        Long dupPhone = baseMapper.selectCount(Wrappers.<Patient>lambdaQuery()
                .eq(Patient::getPhone, phone)
                .ne(Patient::getId, id));
        if (dupPhone != null && dupPhone > 0) {
            throw new BusinessException("手机号 " + phone + " 已被其他患者登记");
        }

        Patient entity = new Patient();
        entity.setId(id);
        entity.setPatientName(dto.getPatientName());
        entity.setIdCard(idCard);
        entity.setPhone(phone);
        entity.setGender(dto.getGender() != null && dto.getGender() != 0
                ? dto.getGender() : IdCardUtils.getGender(idCard));
        entity.setAddress(dto.getAddress());
        baseMapper.updateById(entity);
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Patient exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "患者不存在，id=" + id);
        }
        long regCount = baseMapper.countRegisterByPatientId(id);
        if (regCount > 0) {
            throw new BusinessException("该患者存在 " + regCount + " 条挂号记录，为保证就诊记录完整不允许删除");
        }
        baseMapper.deleteById(id);
    }

    /** 生成患者编号：P + 11 位序号 */
    private String nextPatientNo() {
        QueryWrapper<Patient> qw = new QueryWrapper<>();
        qw.select("IFNULL(MAX(CAST(SUBSTRING(patient_no, 2) AS UNSIGNED)), 0) AS max_no");
        List<Object> objs = baseMapper.selectObjs(qw);
        long max = (objs == null || objs.isEmpty() || objs.get(0) == null)
                ? 0L : ((Number) objs.get(0)).longValue();
        return String.format("P%011d", max + 1);
    }
}
