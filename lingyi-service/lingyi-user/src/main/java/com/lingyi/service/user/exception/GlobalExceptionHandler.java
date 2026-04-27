package com.lingyi.service.user.exception;

import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.domain.Result;
import com.lingyi.common.web.exception.BizException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        return new Result<>(ex.getCode(), ex.getMessage(), null, Instant.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        return new Result<>(ErrorCode.BAD_REQUEST.getCode(), message, null, Instant.now());
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        return new Result<>(ErrorCode.BAD_REQUEST.getCode(), message, null, Instant.now());
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public Result<Void> handleBadRequestException(Exception ex) {
        return Result.fail(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
}
