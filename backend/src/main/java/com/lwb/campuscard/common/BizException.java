package com.lwb.campuscard.common;

/**
 * 业务异常，由责任链与业务层抛出，统一被 GlobalExceptionHandler 兜住
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(ErrorCode.BIZ_ERROR, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
