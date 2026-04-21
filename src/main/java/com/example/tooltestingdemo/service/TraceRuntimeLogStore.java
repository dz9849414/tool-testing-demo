package com.example.tooltestingdemo.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.example.tooltestingdemo.config.TraceLogQueryProperties;
import com.example.tooltestingdemo.vo.TraceLogEntryVO;
import com.example.tooltestingdemo.vo.TraceRuntimeLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行时链路日志内存存储。
 *
 * <p>用于接收 {@code TraceMemoryAppender} 采集到的日志事件，
 * 按 traceId 在内存中分组缓存，供按链路查询运行时日志的接口读取。</p>
 *
 * <p>该实现仅适用于开发调试或轻量排查场景，不作为持久化日志存储。
 * 日志数据会随应用重启丢失，并且会受最大链路数和单链路最大日志条数限制。</p>
 */
@Component
@RequiredArgsConstructor
public class TraceRuntimeLogStore {

    private final TraceLogQueryProperties properties;

    private final Map<String, Deque<TraceLogEntryVO>> traceLogs = new LinkedHashMap<>();

    /**
     * 追加一条日志事件到对应的 traceId 缓存中。
     *
     * <p>只有满足以下条件的日志才会被采集：</p>
     * <p>1. 已启用运行时日志采集。</p>
     * <p>2. 日志事件中存在 traceId。</p>
     * <p>3. logger 名称命中允许采集的前缀配置。</p>
     *
     * <p>写入后会执行两层裁剪：</p>
     * <p>1. 单个 traceId 超过最大条数时，移除最早的日志。</p>
     * <p>2. 总链路数量超过上限时，移除最早进入缓存的 traceId。</p>
     */
    public synchronized void append(ILoggingEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        if (mdcPropertyMap == null) {
            return;
        }

        String traceId = mdcPropertyMap.get("traceId");
        if (!StringUtils.hasText(traceId)) {
            return;
        }
        if (!shouldCapture(event.getLoggerName())) {
            return;
        }

        Deque<TraceLogEntryVO> entries = traceLogs.computeIfAbsent(traceId, key -> new ArrayDeque<>());
        entries.addLast(toEntry(event));

        while (entries.size() > properties.getMaxEntriesPerTrace()) {
            entries.removeFirst();
        }

        while (traceLogs.size() > properties.getMaxTraceCount()) {
            String oldestKey = traceLogs.keySet().iterator().next();
            traceLogs.remove(oldestKey);
        }
    }

    /**
     * 根据 traceId 查询当前内存中缓存的运行时日志。
     *
     * @param traceId 链路追踪 ID
     * @return 链路运行时日志；如果不存在则返回 {@code null}
     */
    public synchronized TraceRuntimeLogVO getByTraceId(String traceId) {
        Deque<TraceLogEntryVO> entries = traceLogs.get(traceId);
        if (entries == null) {
            return null;
        }

        TraceRuntimeLogVO result = new TraceRuntimeLogVO();
        result.setTraceId(traceId);
        result.setEntries(new ArrayList<>(entries));
        result.setEntryCount(entries.size());
        return result;
    }

    /**
     * 判断当前 logger 是否允许被采集。
     *
     * <p>当未配置前缀时默认全部放行；配置后仅采集命中前缀的业务日志。</p>
     */
    private boolean shouldCapture(String loggerName) {
        List<String> includeLoggerPrefixes = properties.getIncludeLoggerPrefixes();
        if (includeLoggerPrefixes == null || includeLoggerPrefixes.isEmpty()) {
            return true;
        }
        if (!StringUtils.hasText(loggerName)) {
            return false;
        }
        for (String prefix : includeLoggerPrefixes) {
            if (StringUtils.hasText(prefix) && loggerName.startsWith(prefix.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将 Logback 日志事件转换为接口可返回的日志条目对象。
     */
    private TraceLogEntryVO toEntry(ILoggingEvent event) {
        TraceLogEntryVO entry = new TraceLogEntryVO();
        entry.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), ZoneId.systemDefault()));
        entry.setLevel(event.getLevel() == null ? null : event.getLevel().toString());
        entry.setLogger(event.getLoggerName());
        entry.setThread(event.getThreadName());
        entry.setMessage(event.getFormattedMessage());
        entry.setThrowable(buildThrowable(event.getThrowableProxy()));
        return entry;
    }

    /**
     * 提取异常摘要信息和部分堆栈，避免单条日志占用过多内存。
     */
    private String buildThrowable(IThrowableProxy throwableProxy) {
        if (throwableProxy == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(throwableProxy.getClassName()).append(": ").append(throwableProxy.getMessage());
        StackTraceElementProxy[] stackTrace = throwableProxy.getStackTraceElementProxyArray();
        if (stackTrace != null) {
            int limit = Math.min(stackTrace.length, 20);
            for (int i = 0; i < limit; i++) {
                builder.append(System.lineSeparator()).append("\tat ").append(stackTrace[i]);
            }
        }
        return builder.toString();
    }
}
