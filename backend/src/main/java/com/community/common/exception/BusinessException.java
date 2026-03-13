package com.community.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;
    private final HttpStatus httpStatus;

    public BusinessException(String message) {
        this(ApiErrorCode.BUSINESS_CONFLICT, message);
    }

    public BusinessException(Integer code, String message) {
        this(ApiErrorCode.fromCode(code), message);
    }

    public BusinessException(ApiErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
