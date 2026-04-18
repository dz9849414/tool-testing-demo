package com.example.tooltestingdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 使用spring原生调用任务
 */
@Slf4j
@Configuration
@EnableScheduling
public class DynamicJobScheduler {
    //存放执行的任务
    private final Map<Long, ScheduledFuture<?>> futureMap = new ConcurrentHashMap<>();

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(20);
        scheduler.setThreadNamePrefix("template-job-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }

    /**F
     * 注册一个 Cron 任务
     */
    public void scheduleJob(Long jobId, String cronExpression, Runnable task) {
        if (cronExpression == null || cronExpression.isEmpty()) {
            log.warn("任务Cron为空，跳过注册: jobId={}", jobId);
            return;
        }
        cancelJob(jobId);
        ScheduledFuture<?> future = taskScheduler().schedule(task, new CronTrigger(cronExpression));
        futureMap.put(jobId, future);
        log.info("任务已注册: jobId={}, cron={}", jobId, cronExpression);
    }

    /**
     * 取消任务
     */
    public void cancelJob(Long jobId) {
        ScheduledFuture<?> future = futureMap.remove(jobId);
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            log.info("任务已取消: jobId={}", jobId);
        }
    }

    /**
     * 取消所有任务（用于启动前清理，防止重复注册）
     */
    public void cancelAllJobs() {
        futureMap.forEach((jobId, future) -> {
            if (future != null && !future.isCancelled()) {
                future.cancel(true);
                log.info("任务已取消: jobId={}", jobId);
            }
        });
        futureMap.clear();
        log.info("已清空所有动态任务");
    }
}
