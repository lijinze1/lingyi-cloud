package com.lingyi.ai.diagnosis.exception;

import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.domain.Result;
import com.lingyi.common.web.exception.BizException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        return new Result<>(ex.getCode(), ex.getMessage(), null, java.time.Instant.now());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, HttpMessageNotReadableException.class})
    public Result<Void> handleBadRequestException() {
        return Result.fail(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException() {
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
}
