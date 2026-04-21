package com.example.tooltestingdemo.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.example.tooltestingdemo.service.TraceRuntimeLogStore;

/**
 * 将带 traceId 的日志事件写入内存，供链路查询接口读取。
 */
public class TraceMemoryAppender extends AppenderBase<ILoggingEvent> {

    private final TraceRuntimeLogStore traceRuntimeLogStore;

    public TraceMemoryAppender(TraceRuntimeLogStore traceRuntimeLogStore) {
        this.traceRuntimeLogStore = traceRuntimeLogStore;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        traceRuntimeLogStore.append(eventObject);
    }
}
