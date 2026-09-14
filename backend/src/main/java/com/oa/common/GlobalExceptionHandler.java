package com.oa.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public R<Void> biz(BizException exception) {
        return R.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> valid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::toString)
                .collect(Collectors.joining("; "));
        return R.fail(400, message);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> duplicate() {
        return R.fail(400, "编码已存在，请勿重复");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> other(Exception exception) {
        log.error("系统异常", exception);
        return R.fail(500, "系统内部错误");
    }
}
