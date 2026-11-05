package com.medical.medical_system.exception;

/**
 * 业务异常
 * <p>
 * 与系统异常区分开：业务异常是"用户可以自己改正的错误"（手机号重复、记录不存在等），
 * 必须把明确原因回传给调用方；系统异常则统一兜底成 500，不暴露堆栈细节。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
