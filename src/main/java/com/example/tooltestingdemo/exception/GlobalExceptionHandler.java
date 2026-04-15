package com.example.tooltestingdemo.exception;

import com.example.tooltestingdemo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理认证异常
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleAuthenticationException(Exception e) {
        log.warn("认证异常: {}", e.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", "认证失败");
        response.put("message", "用户名或密码错误");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", "权限不足");
        response.put("message", "您没有权限执行此操作");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 处理模板校验异常
     */
    /**
     * 处理模板校验异常（模板模块统一使用）
     */
    @ExceptionHandler(TemplateValidationException.class)
    public Result<Object> handleTemplateValidationException(TemplateValidationException e) {
        log.warn("模板校验失败: {} - {}", e.getErrorType().getCode(), e.getMessage());
        return Result.error(e.getErrorType().getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);

        Map<String, String> response = new HashMap<>();
        response.put("error", "服务器内部错误");
        response.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);

        Map<String, String> response = new HashMap<>();
        response.put("error", "系统错误");
        response.put("message", "服务器内部错误，请稍后重试");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}