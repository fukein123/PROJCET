package com.community.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApiErrorCode {

    REQUEST_INVALID(40000, HttpStatus.BAD_REQUEST, "请求参数不正确"),
    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "未登录或登录已失效"),
    FORBIDDEN(40300, HttpStatus.FORBIDDEN, "无权限访问该资源"),
    RESOURCE_NOT_FOUND(40400, HttpStatus.NOT_FOUND, "请求的资源不存在"),
    BUSINESS_CONFLICT(40900, HttpStatus.CONFLICT, "当前操作与业务状态冲突"),
    VALIDATION_FAILED(42200, HttpStatus.UNPROCESSABLE_ENTITY, "请求参数校验失败"),
    INTERNAL_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "系统异常，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    public static ApiErrorCode fromCode(Integer code) {
        if (code == null) {
            return BUSINESS_CONFLICT;
        }
        for (ApiErrorCode item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        if (code >= 1000) {
            return INTERNAL_ERROR;
        }
        return fromHttpStatus(code);
    }

    public static ApiErrorCode fromHttpStatus(int status) {
        return switch (status) {
            case 400 -> REQUEST_INVALID;
            case 401 -> UNAUTHORIZED;
            case 403 -> FORBIDDEN;
            case 404 -> RESOURCE_NOT_FOUND;
            case 409 -> BUSINESS_CONFLICT;
            case 422 -> VALIDATION_FAILED;
            case 500 -> INTERNAL_ERROR;
            default -> INTERNAL_ERROR;
        };
    }
}
