package com.example.tooltestingdemo.service.template.engine.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行器工厂
 * 
 * 管理所有 TemplateExecutor 实例，根据协议类型获取对应的执行器
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
public class ExecutorFactory {

    private final Map<String, TemplateExecutor> executorMap = new HashMap<>();

    /**
     * 构造函数
     * 
     * Spring 会自动注入所有 TemplateExecutor 实现类
     *
     * @param executors 执行器列表
     */
    public ExecutorFactory(List<TemplateExecutor> executors) {
        if (executors != null) {
            for (TemplateExecutor executor : executors) {
                String type = executor.getType().toUpperCase();
                executorMap.put(type, executor);
                log.info("注册模板执行器: {}", type);
            }
        }
        log.info("共注册 {} 个模板执行器", executorMap.size());
    }

    /**
     * 获取执行器
     *
     * @param type 协议类型，如 "HTTP", "HTTPS", "SQL"
     * @return 对应的执行器
     * @throws UnsupportedOperationException 当不支持该协议类型时抛出
     */
    public TemplateExecutor getExecutor(String type) {
        if (type == null) {
            throw new IllegalArgumentException("协议类型不能为空");
        }
        
        String upperType = type.toUpperCase();
        TemplateExecutor executor = executorMap.get(upperType);
        
        if (executor == null) {
            // 尝试兼容 HTTP/HTTPS
            if ("HTTPS".equals(upperType)) {
                executor = executorMap.get("HTTP");
            }
        }
        
        if (executor == null) {
            throw new UnsupportedOperationException("不支持的协议类型: " + type + 
                    "，支持的类型: " + executorMap.keySet());
        }
        
        return executor;
    }

    /**
     * 判断是否支持该协议类型
     *
     * @param type 协议类型
     * @return 是否支持
     */
    public boolean supports(String type) {
        if (type == null) {
            return false;
        }
        String upperType = type.toUpperCase();
        return executorMap.containsKey(upperType) || 
               ("HTTPS".equals(upperType) && executorMap.containsKey("HTTP"));
    }

    /**
     * 获取所有支持的协议类型
     *
     * @return 协议类型列表
     */
    public java.util.Set<String> getSupportedTypes() {
        return executorMap.keySet();
    }
}
