/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.common.config;

import cn.edu.sdu.qd.oj.common.enums.ApiExceptionEnum;
import cn.edu.sdu.qd.oj.common.exception.ApiException;
import cn.edu.sdu.qd.oj.common.entity.ResponseResult;
import cn.edu.sdu.qd.oj.common.exception.InternalApiException;
import cn.edu.sdu.qd.oj.common.util.RegexUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通用异常拦截器
 *
 * @author zhangt2333
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlerConfig {

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseResult> handleException(MissingServletRequestParameterException e) {
        log.error("", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
                .body(ResponseResult.fail(HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(InternalApiException.class)
    public ResponseEntity<ResponseResult> handleException(InternalApiException e) {
        log.warn("", e);
        return ResponseEntity.status(e.code)
                .body(ResponseResult.fail(e.code, e.message));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseResult> handleException(ApiException e) {
        log.warn("", Optional.ofNullable(e.getException()).orElse(e));
        return ResponseEntity.status(e.code)
                             .body(ResponseResult.fail(e.code, e.message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseResult> exception(MethodArgumentNotValidException e) {
        log.warn("{} {}", e.getMessage(), e.getStackTrace()[0]);
        String message = e.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), message));
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseResult> handleException(HttpMessageNotReadableException e) {
        log.warn("{} {}", e.getMessage(), e.getStackTrace()[0]);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "参数错误"));
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ResponseResult> handleException(JsonMappingException e) {
        log.warn("{} {}", e.getMessage(), e.getStackTrace()[0]);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), "JSON 格式有误"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ResponseResult> handleException(DuplicateKeyException e) {
        List<StackTraceElement> onlyOjStack = Arrays.stream(e.getStackTrace())
                                                    .filter(stack -> stack.getClassName().contains("cn.edu.sdu.qd.oj"))
                                                    .collect(Collectors.toList());
        log.warn("{}\n{}", e.getMessage(), onlyOjStack);
        String duplicateEntry = RegexUtils.regexFind(e.getMessage(), "Duplicate entry (.*?) for key");
        return ResponseEntity.status(ApiExceptionEnum.ENTITY_EXISTS.code)
                             .body(ResponseResult.fail(ApiExceptionEnum.ENTITY_EXISTS.code, duplicateEntry + " 已存在"));
    }

}