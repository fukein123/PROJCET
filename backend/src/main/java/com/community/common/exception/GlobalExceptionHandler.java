package com.community.common.exception;

import com.community.common.web.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return failure(ex.getHttpStatus().value(), ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return failure(ApiErrorCode.VALIDATION_FAILED, message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBind(BindException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return failure(ApiErrorCode.VALIDATION_FAILED, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return failure(ApiErrorCode.VALIDATION_FAILED, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        return failure(ApiErrorCode.REQUEST_INVALID, "请求参数格式不正确");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return failure(ApiErrorCode.FORBIDDEN, "无权限访问该资源");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled system exception", ex);
        return failure(ApiErrorCode.INTERNAL_ERROR, "系统异常，请稍后重试");
    }

    private ResponseEntity<ApiResponse<Void>> failure(ApiErrorCode errorCode, String message) {
        return failure(errorCode.getHttpStatus().value(), errorCode.getCode(), message);
    }

    private ResponseEntity<ApiResponse<Void>> failure(int httpStatus, int code, String message) {
        return ResponseEntity
                .status(httpStatus)
                .body(ApiResponse.fail(code, message));
    }
}
