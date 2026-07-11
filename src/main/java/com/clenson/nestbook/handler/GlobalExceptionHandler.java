package com.clenson.nestbook.handler;

import com.clenson.nestbook.core.exception.BizException;
import com.clenson.nestbook.core.exception.ErrorCode;
import com.clenson.nestbook.core.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException ex) {
        log.warn("Business request rejected: code={}", ex.getErrorCode().code());
        return ApiResponse.fail(ex.getErrorCode());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<Void> handleValidationException(Exception ex) {
        log.warn("Request validation failed", ex);
        return ApiResponse.fail(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("Unhandled request exception", ex);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR);
    }
}
