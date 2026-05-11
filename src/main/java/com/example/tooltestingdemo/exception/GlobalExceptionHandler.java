package com.example.tooltestingdemo.exception;

import com.example.tooltestingdemo.common.ErrorStatus;
import com.example.tooltestingdemo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 客户端断开连接：浏览器刷新、切页、取消请求或代理超时后，服务端继续写响应会触发。
     * 这类异常不代表查询业务失败，不再按 500 打印完整堆栈。
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.debug("客户端已断开连接，停止写响应: {}", e.getMessage());
    }

    /**
     * 处理响应写出时的客户端断连。
     */
    @ExceptionHandler(IOException.class)
    public Object handleIOException(IOException e) {
        if (isClientAbort(e)) {
            log.debug("客户端连接已中止: {}", e.getMessage());
            return null;
        }
        log.error("IO异常: {}", e.getMessage(), e);

        Map<String, String> response = new HashMap<>();
        response.put("error", "服务器内部错误");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

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
    public Result<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ErrorStatus.FORBIDDEN.getCode(), "您没有权限执行此操作");
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
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {} - {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (isClientAbort(e)) {
            log.debug("客户端连接已中止: {}", e.getMessage());
            return ResponseEntity.status(499).build();
        }
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
        if (isClientAbort(e)) {
            log.debug("客户端连接已中止: {}", e.getMessage());
            return ResponseEntity.status(499).build();
        }
        log.error("系统异常: {}", e.getMessage(), e);

        Map<String, String> response = new HashMap<>();
        response.put("error", "系统错误");
        response.put("message", "服务器内部错误，请稍后重试");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private boolean isClientAbort(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName();
            String message = current.getMessage();
            if (className.contains("ClientAbortException")
                || className.contains("AsyncRequestNotUsableException")
                || containsClientAbortMessage(message)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean containsClientAbortMessage(String message) {
        if (message == null) {
            return false;
        }
        return message.contains("你的主机中的软件中止了一个已建立的连接")
            || message.contains("An established connection was aborted")
            || message.contains("Connection reset by peer")
            || message.contains("Broken pipe")
            || message.contains("ServletOutputStream failed to write");
    }
}
