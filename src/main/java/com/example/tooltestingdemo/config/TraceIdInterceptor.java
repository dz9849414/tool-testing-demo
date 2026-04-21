package com.example.tooltestingdemo.config;

import com.example.tooltestingdemo.util.TraceIdContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TraceId 拦截器。
 *
 * <p>用于在 HTTP 请求进入系统时读取或生成当前链路的 traceId，
 * 并写入 {@link TraceIdContext}，方便后续日志、异常处理和业务执行统一透传。
 * 请求完成后会清理线程上下文，避免线程复用导致链路信息串用。</p>
 */
@Component
@RequiredArgsConstructor
public class TraceIdInterceptor implements HandlerInterceptor {

    private final TraceIdProperties traceIdProperties;

    /**
     * 在请求处理前初始化当前请求的 traceId。
     *
     * <p>优先按配置从请求头中读取 traceId；如果没有传入有效值，则自动生成新的 traceId。
     * 生成后的值会写入线程上下文，并按配置回写到响应头。</p>
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (!traceIdProperties.isEnabled()) {
            return true;
        }

        String traceId = null;
        if (traceIdProperties.isAllowRequestOverride()) {
            String requestTraceId = request.getHeader(traceIdProperties.getRequestHeader());
            if (StringUtils.hasText(requestTraceId)) {
                traceId = requestTraceId.trim();
            }
        }

        if (!StringUtils.hasText(traceId)) {
            traceId = TraceIdContext.generate();
        }

        TraceIdContext.set(traceId);
        request.setAttribute(TraceIdContext.TRACE_ID_KEY, traceId);

        if (traceIdProperties.isIncludeInResponseHeader()) {
            response.setHeader(traceIdProperties.getResponseHeader(), traceId);
        }

        return true;
    }

    /**
     * 请求完成后清理线程上下文中的 traceId，避免污染后续请求。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceIdContext.clear();
    }
}
