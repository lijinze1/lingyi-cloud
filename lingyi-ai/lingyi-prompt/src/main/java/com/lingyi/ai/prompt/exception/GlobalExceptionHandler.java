package com.lingyi.ai.prompt.exception;

import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.domain.Result;
import com.lingyi.common.web.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        log.warn("Prompt service biz exception: code={}, message={}", ex.getCode(), ex.getMessage());
        return new Result<>(ex.getCode(), ex.getMessage(), null, java.time.Instant.now());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, HttpMessageNotReadableException.class})
    public Result<Void> handleBadRequestException(Exception ex) {
        log.warn("Prompt service bad request", ex);
        return new Result<>(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage(), null, java.time.Instant.now());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Prompt service unexpected exception", ex);
        String message = ex.getMessage() == null || ex.getMessage().isBlank()
                ? ErrorCode.INTERNAL_ERROR.getMessage()
                : ex.getMessage();
        return new Result<>(ErrorCode.INTERNAL_ERROR.getCode(), message, null, java.time.Instant.now());
    }
}
