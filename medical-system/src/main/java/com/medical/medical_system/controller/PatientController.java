package com.medical.medical_system.controller;

import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.common.Result;
import com.medical.medical_system.dto.PatientSaveDTO;
import com.medical.medical_system.service.PatientService;
import com.medical.medical_system.util.PageUtil;
import com.medical.medical_system.vo.PatientVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者管理接口
 */
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * 多条件组合分页查询：姓名 + 手机号前缀 + 性别
     * GET /api/patient/page?pageNum=1&pageSize=5&patientName=张&phone=138&gender=1
     */
    @GetMapping("/page")
    public Result<PageResult<PatientVO>> page(
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer gender) {
        return Result.ok(patientService.pageQuery(
                PageUtil.pageNum(pageNum), PageUtil.pageSize(pageSize), patientName, phone, gender));
    }

    @GetMapping("/{id}")
    public Result<PatientVO> detail(@PathVariable Long id) {
        return Result.ok(patientService.detail(id));
    }

    @PostMapping
    public Result<PatientVO> create(@RequestBody @Valid PatientSaveDTO dto) {
        return Result.ok(patientService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<PatientVO> update(@PathVariable Long id, @RequestBody @Valid PatientSaveDTO dto) {
        return Result.ok(patientService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        patientService.remove(id);
        return Result.ok();
    }
}
