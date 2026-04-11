package com.example.tooltestingdemo.aspect;

import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.service.SysOperationLogService;
import com.example.tooltestingdemo.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面
 * 用于自动记录用户操作日志
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogService operationLogService;
    private final SecurityService securityService;

    // 定义切点，拦截所有Controller中的方法
    @Pointcut("execution(* com.example.tooltestingdemo.controller.*Controller.*(..))")
    public void operationLogPointcut() {
    }

    // 环绕通知，记录操作日志
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 开始时间
        long startTime = System.currentTimeMillis();

        // 执行方法
        Object result = joinPoint.proceed();

        // 结束时间
        long endTime = System.currentTimeMillis();

        // 记录操作日志
        recordOperationLog(joinPoint, endTime - startTime, null);

        return result;
    }

    // 异常通知，记录错误日志
    @AfterThrowing(pointcut = "operationLogPointcut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        recordOperationLog(joinPoint, 0, e.getMessage());
    }

    // 记录操作日志
    private void recordOperationLog(JoinPoint joinPoint, long executeTime, String errorMessage) {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 获取当前用户
        String userId = securityService.getCurrentUserId();
        String username = securityService.getCurrentUsername();

        if (userId == null) {
            userId = "anonymous";
        }

        if (username == null) {
            username = "anonymous";
        }

        // 构建操作日志
        SysOperationLog operationLog = new SysOperationLog();
        operationLog.setUserId(userId);
        operationLog.setUsername(username);
        operationLog.setModule(getModuleName(joinPoint));
        operationLog.setOperation(getOperationName(joinPoint));
        operationLog.setMethod(joinPoint.getSignature().getName());
        operationLog.setRequestUrl(request.getRequestURI());
        operationLog.setRequestParams(Arrays.toString(joinPoint.getArgs()));
        operationLog.setIpAddress(request.getRemoteAddr());
        operationLog.setUserAgent(request.getHeader("User-Agent"));
        operationLog.setStatus(errorMessage == null ? 1 : 0);
        operationLog.setErrorMessage(errorMessage);
        operationLog.setExecuteTime(executeTime);
        operationLog.setCreateTime(LocalDateTime.now());

        // 保存操作日志
        operationLogService.recordOperationLog(operationLog);
    }

    // 获取模块名称
    private String getModuleName(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getName();
        // 从类名中提取模块名称
        String[] parts = className.split("\\.");
        if (parts.length > 0) {
            String controllerName = parts[parts.length - 1];
            if (controllerName.endsWith("Controller")) {
                return controllerName.substring(0, controllerName.length() - 10);
            }
        }
        return "Unknown";
    }

    // 获取操作名称
    private String getOperationName(JoinPoint joinPoint) {
        // 这里可以根据方法名或者注解来获取操作名称
        // 暂时返回方法名
        return joinPoint.getSignature().getName();
    }
}