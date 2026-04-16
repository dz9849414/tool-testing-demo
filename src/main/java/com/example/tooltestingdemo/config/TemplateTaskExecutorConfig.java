package com.example.tooltestingdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class TemplateTaskExecutorConfig {

    @Bean("templateJobExecutor")
    public Executor templateJobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("template-job-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }
}

