package com.medical.medical_system.controller;

import com.medical.medical_system.common.Result;
import com.medical.medical_system.dto.RegisterCreateDTO;
import com.medical.medical_system.service.RegisterService;
import com.medical.medical_system.vo.RegisterVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 患者在线挂号接口
 * <p>
 * 只做参数接收与转发，业务校验与事务在 Service 层 —— Controller 层保持"薄"，
 * 便于复用与测试。
 */
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    /** POST /api/register —— 在线挂号（事务：重复校验 + 号源扣减 + 记录落库） */
    @PostMapping
    public Result<RegisterVO> register(@Valid @RequestBody RegisterCreateDTO dto) {
        return Result.ok(registerService.register(dto));
    }

    /** PUT /api/register/{id}/cancel —— 取消挂号并释放号源 */
    @PutMapping("/{id}/cancel")
    public Result<RegisterVO> cancel(@PathVariable Long id) {
        return Result.ok(registerService.cancel(id));
    }

    /** GET /api/register/byPatient/{patientId} —— 患者就诊史 */
    @GetMapping("/byPatient/{patientId}")
    public Result<List<RegisterVO>> byPatient(@PathVariable Long patientId) {
        return Result.ok(registerService.listByPatient(patientId));
    }
}
