package com.example.tooltestingdemo.service.template.engine.interceptor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志记录拦截器
 * 
 * 记录模板执行的详细日志，用于调试和审计
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@Order(600)
public class LoggingInterceptor implements ExecutionInterceptor {

    private static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void beforeExecute(TemplateContext context) {
        if (!shouldLog(context)) {
            return;
        }
        
        if (context.getTemplate() != null) {
            log.info("[执行开始] 模板ID: {}, 名称: {}, 时间: {}",
                    context.getTemplate().getId(),
                    context.getTemplate().getName(),
                    context.getStartTime().format(TIME_FORMATTER));
            
            if (log.isDebugEnabled()) {
                log.debug("[执行变量] {}", context.getAllVariables());
                if (context.getEnvironment() != null) {
                    log.debug("[执行环境] {}: {}", 
                            context.getEnvironment().getEnvName(),
                            context.getEnvironment().getEnvCode());
                }
            }
        }
    }

    @Override
    public void afterExecute(TemplateContext context, ExecutionResult result) {
        if (!shouldLog(context)) {
            return;
        }
        
        context.setEndTime(LocalDateTime.now());
        
        if (result != null && context.getTemplate() != null) {
            if (result.isSuccess()) {
                log.info("[执行成功] 模板ID: {}, 名称: {}, 状态码: {}, 耗时: {}ms",
                        context.getTemplate().getId(),
                        context.getTemplate().getName(),
                        result.getStatusCode(),
                        result.getDurationMs());
            } else {
                log.warn("[执行失败] 模板ID: {}, 名称: {}, 状态码: {}, 耗时: {}ms, 原因: {}",
                        context.getTemplate().getId(),
                        context.getTemplate().getName(),
                        result.getStatusCode(),
                        result.getDurationMs(),
                        result.getMessage());
            }
            
            if (log.isDebugEnabled() && result.getResponse() != null) {
                log.debug("[响应信息] 状态码: {}, 响应时间: {}ms, 响应大小: {} bytes",
                        result.getResponse().getStatusCode(),
                        result.getResponse().getResponseTime(),
                        result.getResponse().getSize());
            }
            
            // 记录断言结果
            if (result.getAssertions() != null && !result.getAssertions().isEmpty()) {
                long passedCount = result.getAssertions().stream()
                        .filter(ExecutionResult.AssertionResult::isPassed)
                        .count();
                long failedCount = result.getAssertions().size() - passedCount;
                
                if (failedCount > 0) {
                    log.warn("[断言结果] 通过: {}, 失败: {}", passedCount, failedCount);
                    result.getAssertions().stream()
                            .filter(a -> !a.isPassed())
                            .forEach(a -> log.warn("[断言失败] {}: {}", 
                                    a.getName(), a.getErrorMessage()));
                } else {
                    log.debug("[断言结果] 全部通过: {}", passedCount);
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return 600;
    }

    @Override
    public boolean isEnabled(TemplateContext context) {
        // 检查是否启用日志
        if (context.getRequest() != null) {
            return context.getRequest().isEnableLogging();
        }
        return true;
    }

    /**
     * 判断是否记录日志
     */
    private boolean shouldLog(TemplateContext context) {
        if (context.getRequest() == null) {
            return true;
        }
        return context.getRequest().isEnableLogging();
    }
}
