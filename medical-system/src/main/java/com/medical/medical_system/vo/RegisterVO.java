package com.medical.medical_system.vo;

import com.medical.medical_system.entity.RegisterRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 挂号记录返回体
 */
@Data
public class RegisterVO {

    private Long id;
    private String registerNo;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String deptName;
    private Long scheduleId;
    private String scheduleDate;
    private String timeSlotText;
    private LocalDateTime registerTime;

    /** 状态中文：待就诊 / 已就诊 / 已取消 */
    private String statusText;
    private BigDecimal fee;

    public static RegisterVO of(RegisterRecord r, String patientName,
                                String doctorName, String deptName,
                                String scheduleDate, String timeSlotText) {
        RegisterVO vo = new RegisterVO();
        vo.setId(r.getId());
        vo.setRegisterNo(r.getRegisterNo());
        vo.setPatientId(r.getPatientId());
        vo.setPatientName(patientName);
        vo.setDoctorId(r.getDoctorId());
        vo.setDoctorName(doctorName);
        vo.setDeptName(deptName);
        vo.setScheduleId(r.getScheduleId());
        vo.setScheduleDate(scheduleDate);
        vo.setTimeSlotText(timeSlotText);
        vo.setRegisterTime(r.getRegisterTime());
        vo.setFee(r.getFee());
        vo.setStatusText(statusText(r.getStatus()));
        return vo;
    }

    public static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待就诊";
            case 1 -> "已就诊";
            case 2 -> "已取消";
            default -> "未知";
        };
    }
}
