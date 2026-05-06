package com.example.tooltestingdemo.aspect;

import com.example.tooltestingdemo.util.MethodCallChainTracker;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Service方法调用AOP
 * 用于记录Service层的方法调用，构建方法调用链
 */
@Aspect
@Component
@Slf4j
public class ServiceMethodCallAspect {

    @Around("execution(* com.example.tooltestingdemo.service..*(..)) || " +
            "execution(* com.example.tooltestingdemo.service.impl..*(..))")
    public Object recordServiceMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        
        // 排除AOP切面、拦截器、工具类等非业务Service
        if (shouldExclude(className, methodName)) {
            return joinPoint.proceed();
        }
        
        // 记录方法调用
        MethodCallChainTracker.recordMethodCall(className, methodName);
        
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw e;
        }
    }
    
    /**
     * 判断是否应该排除该Service方法调用
     */
    private boolean shouldExclude(String className, String methodName) {
        // 排除AOP切面类
        if (className.contains(".aspect.")) {
            return true;
        }
        
        // 排除配置类
        if (className.contains(".config.")) {
            return true;
        }
        
        // 排除拦截器类
        if (className.contains("Interceptor")) {
            return true;
        }
        
        // 排除工具类
        if (className.contains(".util.") || className.contains(".utils.")) {
            return true;
        }
        
        // 排除日志相关Service
        if (className.contains("OperationLog") || className.contains("TraceRuntimeLogStore")) {
            return true;
        }
        
        // 排除安全相关Service
        if (className.contains("SecurityService") || className.contains("SecurityServiceImpl")) {
            return true;
        }
        
        // 排除getter/setter方法
        if (methodName.startsWith("get") || methodName.startsWith("set") || 
            methodName.startsWith("is") || methodName.startsWith("has")) {
            return true;
        }
        
        // 排除查询方法
        if (methodName.startsWith("select") || methodName.startsWith("find") || 
            methodName.startsWith("list") || methodName.startsWith("query") ||
            methodName.startsWith("search") || methodName.startsWith("count") ||
            methodName.startsWith("exists")) {
            return true;
        }
        
        return false;
    }
}