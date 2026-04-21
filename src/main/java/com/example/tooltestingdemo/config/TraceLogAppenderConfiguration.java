package com.example.tooltestingdemo.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.example.tooltestingdemo.logging.TraceMemoryAppender;
import com.example.tooltestingdemo.service.TraceRuntimeLogStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 TraceId 内存日志采集器。
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TraceLogQueryProperties.class)
public class TraceLogAppenderConfiguration {

    private static final String APPENDER_NAME = "TRACE_MEMORY_APPENDER";

    private final TraceLogQueryProperties properties;
    private final TraceRuntimeLogStore traceRuntimeLogStore;

    @PostConstruct
    public void registerAppender() {
        if (!properties.isEnabled()) {
            return;
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        if (rootLogger.getAppender(APPENDER_NAME) != null) {
            return;
        }

        TraceMemoryAppender appender = new TraceMemoryAppender(traceRuntimeLogStore);
        appender.setName(APPENDER_NAME);
        appender.setContext(context);
        appender.start();
        rootLogger.addAppender(appender);
    }
}
