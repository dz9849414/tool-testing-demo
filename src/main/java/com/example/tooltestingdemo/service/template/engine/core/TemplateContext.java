package com.example.tooltestingdemo.service.template.engine.core;

import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 模板执行上下文
 * 
 * 贯穿整个执行流程的上下文对象，承载执行状态和数据
 */
@Data
public class TemplateContext {

    /**
     * 执行请求
     */
    private ExecutionRequest request;

    /**
     * 模板信息
     */
    private InterfaceTemplate template;

    /**
     * 环境配置
     */
    private TemplateEnvironment environment;

    /**
     * 变量存储（全局作用域）
     */
    private Map<String, Object> globalVariables;

    /**
     * 变量存储（模板作用域）
     */
    private Map<String, Object> templateVariables;

    /**
     * 变量存储（局部作用域）
     */
    private Map<String, Object> localVariables;

    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;

    /**
     * 执行结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行结果（执行完成后设置）
     */
    private ExecutionResult result;

    /**
     * 扩展属性（用于各阶段传递数据）
     */
    private Map<String, Object> attributes;

    public TemplateContext() {
        this.globalVariables = new HashMap<>();
        this.templateVariables = new HashMap<>();
        this.localVariables = new HashMap<>();
        this.attributes = new HashMap<>();
        this.startTime = LocalDateTime.now();
    }

    /**
     * 获取变量（按优先级：local > template > global）
     */
    public Object getVariable(String name) {
        if (localVariables.containsKey(name)) {
            return localVariables.get(name);
        }
        if (templateVariables.containsKey(name)) {
            return templateVariables.get(name);
        }
        return globalVariables.get(name);
    }

    /**
     * 设置模板变量
     */
    public void setTemplateVariable(String name, Object value) {
        templateVariables.put(name, value);
    }

    /**
     * 设置局部变量
     */
    public void setLocalVariable(String name, Object value) {
        localVariables.put(name, value);
    }

    /**
     * 设置全局变量
     */
    public void setGlobalVariable(String name, Object value) {
        globalVariables.put(name, value);
    }

    /**
     * 合并所有变量到一个Map
     */
    public Map<String, Object> getAllVariables() {
        Map<String, Object> all = new HashMap<>();
        all.putAll(globalVariables);
        all.putAll(templateVariables);
        all.putAll(localVariables);
        return all;
    }

    /**
     * 获取协议类型
     */
    public String getProtocolType() {
        return template != null ? template.getProtocolType() : null;
    }

    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 计算执行耗时
     */
    public long getDurationMs() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMillis();
    }
}
