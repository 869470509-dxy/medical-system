package com.medical.medical_system.controller;

import com.medical.medical_system.common.PageResult;
import com.medical.medical_system.common.Result;
import com.medical.medical_system.dto.DepartmentSaveDTO;
import com.medical.medical_system.entity.Department;
import com.medical.medical_system.service.DepartmentService;
import com.medical.medical_system.util.PageUtil;
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
 * 科室管理接口
 * <p>
 * 分层职责：Controller 只做参数接收、触发校验、包装结果；
 * 业务规则（重名判断、删除前置条件）全部放在 Service，避免控制层越写越厚。
 */
@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 多条件组合分页查询
     * GET /api/department/page?pageNum=1&pageSize=5&deptName=内&status=1
     */
    @GetMapping("/page")
    public Result<PageResult<Department>> page(
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) Integer status) {
        return Result.ok(departmentService.pageQuery(
                PageUtil.pageNum(pageNum), PageUtil.pageSize(pageSize), deptName, status));
    }

    /** 按 id 查询详情 */
    @GetMapping("/{id}")
    public Result<Department> detail(@PathVariable Long id) {
        Department department = departmentService.getById(id);
        if (department == null) {
            return Result.fail(404, "科室不存在，id=" + id);
        }
        return Result.ok(department);
    }

    /** 新增科室 */
    @PostMapping
    public Result<Department> create(@RequestBody @Valid DepartmentSaveDTO dto) {
        return Result.ok(departmentService.create(dto));
    }

    /** 编辑科室 */
    @PutMapping("/{id}")
    public Result<Department> update(@PathVariable Long id, @RequestBody @Valid DepartmentSaveDTO dto) {
        return Result.ok(departmentService.update(id, dto));
    }

    /** 删除科室 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        departmentService.remove(id);
        return Result.ok();
    }
}
