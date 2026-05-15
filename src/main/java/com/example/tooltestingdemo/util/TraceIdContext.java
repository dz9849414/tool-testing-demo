package com.example.tooltestingdemo.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceIdContext {

    public static final String TRACE_ID_KEY = "traceId";

    private TraceIdContext() {
    }

    public static String get() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static String set(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
        return traceId;
    }

    public static String getOrCreate() {
        String traceId = get();
        if (traceId == null || traceId.isBlank()) {
            traceId = generate();
            set(traceId);
        }
        return traceId;
    }

    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
