package com.medical.medical_system.controller;

import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.common.Result;
import com.medical.medical_system.dto.DoctorSaveDTO;
import com.medical.medical_system.service.DoctorService;
import com.medical.medical_system.util.PageUtil;
import com.medical.medical_system.vo.DoctorVO;
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
 * 医生管理接口
 */
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * 多条件组合分页查询：医生姓名 + 科室 id + 科室名称，三个条件可任意组合
     * GET /api/doctor/page?pageNum=1&pageSize=5&doctorName=吴&deptId=1&deptName=内
     */
    @GetMapping("/page")
    public Result<PageResult<DoctorVO>> page(
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String deptName) {
        return Result.ok(doctorService.pageQuery(
                PageUtil.pageNum(pageNum), PageUtil.pageSize(pageSize), doctorName, deptId, deptName));
    }

    @GetMapping("/{id}")
    public Result<DoctorVO> detail(@PathVariable Long id) {
        return Result.ok(doctorService.detail(id));
    }

    @PostMapping
    public Result<DoctorVO> create(@RequestBody @Valid DoctorSaveDTO dto) {
        return Result.ok(doctorService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<DoctorVO> update(@PathVariable Long id, @RequestBody @Valid DoctorSaveDTO dto) {
        return Result.ok(doctorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        doctorService.remove(id);
        return Result.ok();
    }
}
