package com.medical.medical_system.vo;

import com.medical.medical_system.entity.Patient;
import com.medical.medical_system.util.IdCardUtils;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者返回对象
 * <p>
 * 比实体多出三类信息：
 * 1. birthDate / age —— 由身份证号实时推导，不落库（满足第三范式，且年龄不会失真）；
 * 2. genderText —— 把 0/1/2 翻译成"未知/男/女"，避免每个前端各写一套映射；
 * 3. 列表场景对身份证、手机号做脱敏，符合医疗数据的隐私要求。
 */
@Data
public class PatientVO {

    private Long id;
    private String patientNo;
    private String patientName;
    private String idCard;
    private String phone;
    private Integer gender;
    private String genderText;
    private String address;
    private LocalDate birthDate;
    private Integer age;
    private LocalDateTime createTime;

    /** 列表页：敏感字段脱敏 */
    public static PatientVO masked(Patient p) {
        return build(p, true);
    }

    /** 详情/编辑回显：返回完整号码 */
    public static PatientVO full(Patient p) {
        return build(p, false);
    }

    private static PatientVO build(Patient p, boolean mask) {
        PatientVO vo = new PatientVO();
        vo.setId(p.getId());
        vo.setPatientNo(p.getPatientNo());
        vo.setPatientName(p.getPatientName());
        vo.setIdCard(mask ? maskIdCard(p.getIdCard()) : p.getIdCard());
        vo.setPhone(mask ? maskPhone(p.getPhone()) : p.getPhone());
        vo.setGender(p.getGender());
        vo.setGenderText(genderText(p.getGender()));
        vo.setAddress(p.getAddress());
        vo.setBirthDate(IdCardUtils.getBirthDate(p.getIdCard()));
        vo.setAge(IdCardUtils.getAge(p.getIdCard()));
        vo.setCreateTime(p.getCreateTime());
        return vo;
    }

    private static String genderText(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    /** 110101********4514 */
    private static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    /** 138****8001 */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
