package com.medical.medical_system.controller;

import com.medical.medical_system.common.Result;
import com.medical.medical_system.dto.ScheduleSaveDTO;
import com.medical.medical_system.service.ScheduleService;
import com.medical.medical_system.vo.ScheduleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生排班接口
 */
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /** GET /api/schedule/list?doctorId=1&date=2026-11-13 */
    @GetMapping("/list")
    public Result<List<ScheduleVO>> list(@RequestParam(required = false) Long doctorId,
                                         @RequestParam(required = false) LocalDate date) {
        return Result.ok(scheduleService.listByDoctor(doctorId, date));
    }

    /** GET /api/schedule/{id} */
    @GetMapping("/{id}")
    public Result<ScheduleVO> detail(@PathVariable Long id) {
        return Result.ok(scheduleService.detail(id));
    }

    /** POST /api/schedule */
    @PostMapping
    public Result<ScheduleVO> create(@Valid @RequestBody ScheduleSaveDTO dto) {
        return Result.ok(scheduleService.create(dto));
    }

    /** PUT /api/schedule/{id} */
    @PutMapping("/{id}")
    public Result<ScheduleVO> update(@PathVariable Long id, @Valid @RequestBody ScheduleSaveDTO dto) {
        return Result.ok(scheduleService.update(id, dto));
    }
}
