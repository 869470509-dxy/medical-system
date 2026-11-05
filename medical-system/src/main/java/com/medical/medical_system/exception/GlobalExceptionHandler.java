package com.medical.medical_system.exception;

import com.medical.medical_system.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 目标：无论发生什么异常，前端拿到的都是统一的 Result 结构 + 准确的 HTTP 状态码，
 * 并且不把 MyBatis/MySQL 的原始堆栈直接抛给调用方（既影响体验，也泄露表结构）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务校验失败：手机号重复、记录不存在等 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    /** @Valid 校验失败：把所有字段错误拼成一句可读提示 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Void>> handleValidation(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(400, msg));
    }

    /** 请求体不是合法 JSON，或枚举/日期格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(400, "请求体格式错误，请检查 JSON 语法与字段类型"));
    }

    /** 唯一索引冲突：把数据库的约束名翻译成人话 */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException e) {
        String raw = e.getMessage() == null ? "" : e.getMessage();
        String friendly = "数据已存在，违反唯一约束";
        if (raw.contains("uk_patient_id_card")) {
            friendly = "该身份证号已建档，不可重复录入";
        } else if (raw.contains("uk_patient_phone") || raw.contains("uk_doctor_phone")) {
            friendly = "该手机号已被占用";
        } else if (raw.contains("uk_dept_code")) {
            friendly = "科室编码已存在";
        } else if (raw.contains("uk_dept_name")) {
            friendly = "科室名称已存在";
        } else if (raw.contains("uk_doctor_no")) {
            friendly = "医生工号已存在";
        } else if (raw.contains("uk_patient_no")) {
            friendly = "患者编号已存在";
        }
        log.warn("唯一约束冲突: {}", raw);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Result.fail(409, friendly));
    }

    /** 外键约束、CHECK 约束等完整性错误 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleIntegrity(DataIntegrityViolationException e) {
        String raw = e.getMessage() == null ? "" : e.getMessage();
        String friendly = "数据完整性校验未通过";
        if (raw.contains("fk_doctor_dept") || raw.contains("fk_reg_patient") || raw.contains("fk_reg_doctor")) {
            friendly = "关联数据不存在或仍被引用，请先检查科室/患者/医生的有效性";
        } else if (raw.contains("ck_")) {
            friendly = "字段取值超出允许范围，请检查性别、状态、金额等字段";
        }
        log.warn("完整性约束异常: {}", raw);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Result.fail(409, friendly));
    }

    /** 兜底：未预料的异常，日志留全栈，响应只给通用提示 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOthers(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(500, "服务器内部错误，请联系管理员"));
    }
}
