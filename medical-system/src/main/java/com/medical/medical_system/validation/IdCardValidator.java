package com.medical.medical_system.validation;

import com.medical.medical_system.util.IdCardUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 身份证校验器实现
 * <p>
 * 注意两点：
 * 1. null 值直接放行——是否允许为空交给 @NotBlank 决定，校验器只负责"有值时的正确性"，
 *    否则空值和非法值会报同一句提示，用户分不清是漏填还是填错。
 * 2. 把具体失败原因写回校验上下文，前端能收到"校验位错误，末位应为 4"这类可操作的提示。
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String reason = IdCardUtils.diagnose(value);
        if (reason == null) {
            return true;
        }
        // 关掉注解上那句笼统的默认提示，换成具体原因
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(reason).addConstraintViolation();
        return false;
    }
}
