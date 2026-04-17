package com.example.tooltestingdemo.config;

import com.example.tooltestingdemo.entity.template.TemplateJobItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

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
     * 并发执行任务
     */
    public List<Map<String, Object>> dispatch(Long jobId, List<TemplateJobItem> items,
                                              Function<TemplateJobItem, Map<String, Object>> executorFunc
    ) {
        //过滤出符合状态的可执行任务->异步调用
        List<CompletableFuture<Map<String, Object>>> futures = items.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getStatus()))
                .map(item -> CompletableFuture.supplyAsync(
                        () -> executeWithControl(jobId, item, executorFunc),
                        executor
                ))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 单任务控制（超时 + 重试）
     */
    private Map<String, Object> executeWithControl(
            Long jobId,
            TemplateJobItem item,
            Function<TemplateJobItem, Map<String, Object>> executorFunc
    ) {

        int retry = Optional.of(itemRetry).orElse(0);
        int timeout = Math.toIntExact(Optional.of(itemTimeoutMs).orElse(10L));

        for (int i = 0; i <= retry; i++) {
            try {

                return CompletableFuture
                        .supplyAsync(() -> executorFunc.apply(item), executor)
                        .orTimeout(timeout, TimeUnit.SECONDS)
                        .exceptionally(e -> {
                            log.warn("任务异常/超时 jobId={}, itemId={}", jobId, item.getId());
                            return Map.of(
                                    "success", false,
                                    "message", e.getMessage(),
                                    "templateId", item.getTemplateId()
                            );
                        })
                        .join();

            } catch (Exception e) {
                log.error("任务执行异常 jobId={}, itemId={}", jobId, item.getId(), e);
            }
        }

        return Map.of("success", false, "message", "执行失败（重试后）");
    }
}