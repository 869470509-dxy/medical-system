package com.medical.medical_system.vo;

import com.medical.medical_system.entity.DoctorSchedule;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班返回体
 */
@Data
public class ScheduleVO {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private String deptName;
    private LocalDate scheduleDate;

    /** 时段中文：上午 / 下午 / 夜间 */
    private String timeSlotText;
    private Integer totalSlots;
    private Integer remainingSlots;
    private BigDecimal fee;

    /** 状态中文：出诊 / 停诊 */
    private String statusText;

    public static ScheduleVO of(DoctorSchedule s, String doctorName, String deptName) {
        ScheduleVO vo = new ScheduleVO();
        vo.setId(s.getId());
        vo.setDoctorId(s.getDoctorId());
        vo.setDoctorName(doctorName);
        vo.setDeptName(deptName);
        vo.setScheduleDate(s.getScheduleDate());
        vo.setTimeSlotText(slotText(s.getTimeSlot()));
        vo.setTotalSlots(s.getTotalSlots());
        vo.setRemainingSlots(s.getRemainingSlots());
        vo.setFee(s.getFee());
        vo.setStatusText(s.getStatus() != null && s.getStatus() == 1 ? "出诊" : "停诊");
        return vo;
    }

    public static String slotText(Integer slot) {
        if (slot == null) {
            return "未知";
        }
        return switch (slot) {
            case 0 -> "上午";
            case 1 -> "下午";
            case 2 -> "夜间";
            default -> "未知";
        };
    }
}
