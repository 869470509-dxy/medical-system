package com.medical.medical_system.vo;

import com.medical.medical_system.entity.Doctor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生返回对象
 * <p>
 * 额外带 deptName：避免前端为了显示"内科 / 吴建国"再去调一次科室接口（N+1 请求）。
 * 科室名称由 Service 批量查询后回填，不用表连接，减少一处 SQL 复杂度。
 */
@Data
public class DoctorVO {

    private Long id;
    private String doctorNo;
    private String doctorName;
    private Long deptId;
    private String deptName;
    private String title;
    private String phone;
    private LocalDateTime createTime;

    public static DoctorVO from(Doctor d) {
        DoctorVO vo = new DoctorVO();
        vo.setId(d.getId());
        vo.setDoctorNo(d.getDoctorNo());
        vo.setDoctorName(d.getDoctorName());
        vo.setDeptId(d.getDeptId());
        vo.setTitle(d.getTitle());
        vo.setPhone(d.getPhone());
        vo.setCreateTime(d.getCreateTime());
        return vo;
    }

    /** 列表脱敏：138****9001 */
    public void maskPhone() {
        if (phone != null && phone.length() == 11) {
            this.phone = phone.substring(0, 3) + "****" + phone.substring(7);
        }
    }
}
