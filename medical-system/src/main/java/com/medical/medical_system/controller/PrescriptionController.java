package com.medical.medical_system.controller;

import com.medical.medical_system.common.Result;
import com.medical.medical_system.dto.PrescriptionCreateDTO;
import com.medical.medical_system.service.PrescriptionService;
import com.medical.medical_system.vo.PrescriptionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 电子处方接口
 */
@RestController
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * POST /api/prescription —— 开具处方
     * <p>
     * 事务内完成：处方落库 → 明细落库 → 药品库存逐条 CAS 扣减 → 挂号状态流转。
     * 请求体 simulateError=true 可手动模拟事务中途异常，验证整体回滚效果
     * （响应 500 + 通用提示，数据库无任何残留变更）。
     */
    @PostMapping
    public Result<PrescriptionVO> create(@Valid @RequestBody PrescriptionCreateDTO dto) {
        return Result.ok(prescriptionService.create(dto));
    }

    /** GET /api/prescription/byRecord/{recordId} —— 按挂号查处方 */
    @GetMapping("/byRecord/{recordId}")
    public Result<PrescriptionVO> findByRecord(@PathVariable Long recordId) {
        return Result.ok(prescriptionService.findByRecord(recordId));
    }
}
