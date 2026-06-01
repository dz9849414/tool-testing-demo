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
     * method_json 最大长度限制（LONGBLOB支持4GB，这里设置4MB作为合理上限,数据库8.0云库默认max_allowed_packet4M）
     */
    private static final int MAX_METHOD_JSON_SIZE = 4 * 1024 * 1024; // 4MB

    /**
     * 获取方法调用链JSON（直接从ThreadLocal获取）
     */
    public static String getMethodJson() {
        List<MethodCallInfo> chain = callChain.get();
        if (chain != null && !chain.isEmpty()) {
            log.info("获取到方法调用链，共{}个方法调用", chain.size());
            String json = JSON.toJSONString(chain);
            
            // 检查JSON大小是否超过限制
            if (json.length() > MAX_METHOD_JSON_SIZE) {
                // 记录超过限制的原因和具体大小
                log.warn("method_json超过最大长度限制: {} -> {}", json.length(), MAX_METHOD_JSON_SIZE);
                // 截断到最大长度，保留有效JSON格式（移除末尾不完整部分）
                json = json.substring(0, MAX_METHOD_JSON_SIZE - 3) + "...";
                log.warn("已截断method_json，截断后长度: {}", json.length());
            }
            
            log.debug("方法调用链JSON: {}", json.length() > 500 ? json.substring(0, 500) + "..." : json);
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