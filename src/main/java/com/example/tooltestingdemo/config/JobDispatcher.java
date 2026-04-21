package com.example.tooltestingdemo.config;

import com.example.tooltestingdemo.entity.template.TemplateJobItem;
import com.example.tooltestingdemo.util.TraceIdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobDispatcher {

    @Value("${template.job.item.timeout-ms:120000}")
    private long itemTimeoutMs;

    @Value("${template.job.item.retry:1}")
    private int itemRetry;

    /**
     * 创建一个执行任务的线程池
     */
    private final ExecutorService executor = new ThreadPoolExecutor(
            10,
            20,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 并发执行任务（超时 + 重试）
     */
    public List<Map<String, Object>> dispatch(Long jobId, List<TemplateJobItem> items,
                                              Function<TemplateJobItem, Map<String, Object>> executorFunc
    ) {
        int retry = Optional.of(itemRetry).orElse(0);
        long timeout = Optional.of(itemTimeoutMs).orElse(120000L);
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        String traceId = TraceIdContext.get();

        // 过滤出符合状态的可执行任务 -> 异步调用
        List<CompletableFuture<Map<String, Object>>> futures = items.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getStatus()))
                .map(item -> CompletableFuture.supplyAsync(
                        () -> executeWithMdc(contextMap, () -> executeWithRetry(jobId, item, executorFunc, retry)),
                        executor
                )
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .exceptionally(e -> {
                    log.warn("任务异常/超时 jobId={}, itemId={}", jobId, item.getId());
                    return Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "templateId", item.getTemplateId(),
                            "traceId", traceId
                    );
                }))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 单任务重试（同步执行，不额外嵌套线程池）
     */
    private Map<String, Object> executeWithRetry(
            Long jobId,
            TemplateJobItem item,
            Function<TemplateJobItem, Map<String, Object>> executorFunc,
            int retry
    ) {
        for (int i = 0; i <= retry; i++) {
            try {
                return executorFunc.apply(item);
            } catch (Exception e) {
                log.error("任务执行异常 jobId={}, itemId={}, attempt={}", jobId, item.getId(), i + 1, e);
            }
        }

        return Map.of("success", false, "message", "执行失败（重试后）");
    }

    private Map<String, Object> executeWithMdc(Map<String, String> contextMap,
                                               Supplier<Map<String, Object>> supplier) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        try {
            if (contextMap == null || contextMap.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(contextMap);
            }
            return supplier.get();
        } finally {
            if (previous == null || previous.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(previous);
            }
        }
    }
}
