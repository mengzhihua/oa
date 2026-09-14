package com.oa.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class) public R<Void> biz(BizException e) { return R.fail(400, e.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) public R<Void> valid(MethodArgumentNotValidException e) {
        return R.fail(400, e.getBindingResult().getFieldErrors().stream().map(FieldError::toString).collect(Collectors.joining("; ")));
    }
    @ExceptionHandler(DuplicateKeyException.class) public R<Void> duplicate(DuplicateKeyException e) { return R.fail(400, "数据已存在"); }
    @ExceptionHandler(Exception.class) public R<Void> other(Exception e) { log.error("系统异常", e); return R.fail(500, e.getMessage() == null ? "系统异常" : e.getMessage()); }
}
