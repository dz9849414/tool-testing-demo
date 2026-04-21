package com.example.tooltestingdemo.aspect;

import com.alibaba.fastjson2.JSON;
import com.example.tooltestingdemo.config.MethodTraceProperties;
import com.example.tooltestingdemo.util.TraceIdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MethodTraceAspect {

    private final MethodTraceProperties methodTraceProperties;

    @Around("execution(* com.example.tooltestingdemo.service.impl..*(..))")
    public Object traceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!methodTraceProperties.isEnabled()) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getSignature().getDeclaringTypeName();
        if (!shouldTrace(className)) {
            return joinPoint.proceed();
        }

        boolean traceCreatedHere = !StringUtils.hasText(TraceIdContext.get());
        String traceId = TraceIdContext.getOrCreate();
        String method = className + "#" + joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();

        try {
            if (methodTraceProperties.isLogArgs()) {
                log.info("method-trace start: method={}, traceId={}, args={}",
                        method, traceId, summarize(joinPoint.getArgs()));
            } else {
                log.info("method-trace start: method={}, traceId={}", method, traceId);
            }

            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;
            if (methodTraceProperties.isLogResult()) {
                log.info("method-trace end: method={}, traceId={}, durationMs={}, result={}",
                        method, traceId, duration, summarize(result));
            } else {
                log.info("method-trace end: method={}, traceId={}, durationMs={}",
                        method, traceId, duration);
            }
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("method-trace error: method={}, traceId={}, durationMs={}, error={}",
                    method, traceId, duration, ex.getMessage(), ex);
            throw ex;
        } finally {
            if (traceCreatedHere) {
                TraceIdContext.clear();
            }
        }
    }

    private boolean shouldTrace(String className) {
        List<String> includePackages = methodTraceProperties.getIncludePackages();
        if (CollectionUtils.isEmpty(includePackages)) {
            return false;
        }
        return includePackages.stream().anyMatch(className::startsWith);
    }

    private String summarize(Object value) {
        if (value == null) {
            return "null";
        }

        String text;
        try {
            if (value instanceof Object[] array) {
                text = JSON.toJSONString(Arrays.asList(array));
            } else {
                text = JSON.toJSONString(value);
            }
        } catch (Exception e) {
            text = String.valueOf(value);
        }

        int maxLength = Math.max(50, methodTraceProperties.getMaxLength());
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
