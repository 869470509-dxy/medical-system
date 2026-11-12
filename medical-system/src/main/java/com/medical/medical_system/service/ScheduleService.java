package com.medical.medical_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.medical_system.dto.ScheduleSaveDTO;
import com.medical.medical_system.entity.DoctorSchedule;
import com.medical.medical_system.vo.ScheduleVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生排班业务
 */
public interface ScheduleService extends IService<DoctorSchedule> {

    /** 新增排班（同医生同日同时段唯一） */
    ScheduleVO create(ScheduleSaveDTO dto);

    /** 修改号源总数 / 状态 */
    ScheduleVO update(Long id, ScheduleSaveDTO dto);

    /** 按医生查排班列表，可按日期过滤 */
    List<ScheduleVO> listByDoctor(Long doctorId, LocalDate date);

    /** 排班详情 */
    ScheduleVO detail(Long id);
}
