package com.example.tooltestingdemo.util;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方法调用链跟踪器
 * 用于记录控制器方法调用了哪些服务类和方法
 */
@Slf4j
public class MethodCallChainTracker {

    private static final ThreadLocal<List<MethodCallInfo>> callChain = ThreadLocal.withInitial(ArrayList::new);
    private static final ConcurrentHashMap<String, List<MethodCallInfo>> traceIdToCallChain = new ConcurrentHashMap<>();

    /**
     * 开始跟踪方法调用链
     */
    public static void startTracking(String traceId) {
        callChain.set(new ArrayList<>());
        traceIdToCallChain.put(traceId, callChain.get());
    }

    /**
     * 记录方法调用
     */
    public static void recordMethodCall(String className, String methodName) {
        List<MethodCallInfo> chain = callChain.get();
        if (chain != null) {
            chain.add(new MethodCallInfo(className, methodName));
        }
    }

    /**
     * 获取方法调用链JSON（通过traceId）
     */
    public static String getCallChainJson(String traceId) {
        List<MethodCallInfo> chain = traceIdToCallChain.get(traceId);
        if (chain != null && !chain.isEmpty()) {
            return JSON.toJSONString(chain);
        }
        return null;
    }
    
    /**
     * 获取方法调用链JSON（直接从ThreadLocal获取）
     */
    public static String getMethodJson() {
        List<MethodCallInfo> chain = callChain.get();
        if (chain != null && !chain.isEmpty()) {
            log.info("获取到方法调用链，共{}个方法调用", chain.size());
            String json = JSON.toJSONString(chain);
            log.info("方法调用链JSON: {}", json);
            return json;
        }
        log.warn("ThreadLocal中的方法调用链为空");
        return null;
    }

    /**
     * 清理跟踪数据（通过traceId）
     */
    public static void clearTracking(String traceId) {
        traceIdToCallChain.remove(traceId);
        callChain.remove();
    }
    
    /**
     * 清理跟踪数据（直接清理ThreadLocal）
     */
    public static void clear() {
        callChain.remove();
        log.info("MethodCallChainTracker已清理");
    }

    /**
     * 方法调用信息
     */
    public static class MethodCallInfo {
        private String className;
        private String methodName;
        private long timestamp;

        public MethodCallInfo(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
            this.timestamp = System.currentTimeMillis();
        }

        // getters and setters
        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}