package com.medical.medical_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.medical_system.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 医生排班 Mapper
 */
@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {

    /**
     * CAS 扣减号源：仅当剩余号源 > 0 时才扣减。
     * <p>
     * 返回影响行数：1 = 扣减成功；0 = 号源已扣空（或排班不存在）。
     * "查-判-改"压缩为一条带条件的原子 UPDATE，并发挂号不会扣成负数，
     * 是挂号事务防超卖的核心语句。#{remaining} 走 PreparedStatement 占位符。
     */
    @Update("UPDATE doctor_schedule SET remaining_slots = remaining_slots - 1 "
            + "WHERE id = #{scheduleId} AND remaining_slots > 0")
    int deductSlot(@Param("scheduleId") Long scheduleId);
}
