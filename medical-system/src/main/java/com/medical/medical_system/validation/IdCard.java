package com.medical.medical_system.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 身份证号校验注解
 * <p>
 * 用法：字段上加 @IdCard(message = "...")，由 IdCardValidator 执行真校验（含校验位）。
 * 相比直接写 @Pattern，好处是：正则只能管形状，校验位和日期真实性交给校验器。
 */
@Documented
@Constraint(validatedBy = IdCardValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdCard {

    String message() default "身份证号格式或校验位不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
