package com.example.tooltestingdemo.service.impl.template;

import com.example.tooltestingdemo.service.template.TemplateExecuteService;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionEngine;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionRequest;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.executor.TemplateExecutor;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板执行 Service 实现类（基于执行引擎）
 * 
 * <p>此实现通过调用 {@link ExecutionEngine} 来完成模板执行，</p>
 * <p>将具体的执行逻辑下沉到引擎层，实现策略模式和拦截器链。</p>
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateExecuteServiceImpl implements TemplateExecuteService {

    private final ExecutionEngine executionEngine;

    /**
     * 执行模板请求
     * 
     * <p>通过 ExecutionEngine 执行模板，支持拦截器链扩展</p>
     *
     * @param templateId    模板ID
     * @param environmentId 环境ID（可选）
     * @param variables     执行变量
     * @return 执行结果（兼容旧格式）
     */
    @Override
    public Map<String, Object> executeTemplate(Long templateId, Long environmentId, Map<String, Object> variables) {
        log.info("执行模板请求: templateId={}, environmentId={}", templateId, environmentId);
        // 构建执行请求
        ExecutionRequest request = ExecutionRequest.builder()
                .templateId(templateId)
                .environmentId(environmentId)
                .variables(variables)
                .executeAssertions(true)
                .executePreProcessors(true)
                .executePostProcessors(true)
                .build();
        
        // 调用执行引擎
        ExecutionResult result = executionEngine.execute(request);
        
        // 转换为兼容旧格式的结果
        return convertToLegacyFormat(result);
    }

    /**
     * 执行模板请求（使用默认环境）
     *
     * @param templateId 模板ID
     * @param variables  执行变量
     * @return 执行结果
     */
    @Override
    public Map<String, Object> executeTemplate(Long templateId, Map<String, Object> variables) {
        return executeTemplate(templateId, null, variables);
    }

    /**
     * 验证模板配置
     *
     * @param templateId 模板ID
     * @return 验证结果
     */
    @Override
    public Map<String, Object> validateTemplate(Long templateId) {
        log.info("验证模板配置: templateId={}", templateId);
        
        TemplateExecutor.ValidationResult validation = executionEngine.validate(templateId);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (!validation.isValid()) {
            errors.add(validation.getMessage());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", validation.isValid());
        result.put("errors", errors);
        result.put("warnings", warnings);
        
        return result;
    }

    /**
     * 预览请求（生成最终请求内容，但不发送）
     *
     * @param templateId    模板ID
     * @param environmentId 环境ID（可选）
     * @param variables     执行变量
     * @return 请求预览
     */
    @Override
    public Map<String, Object> previewRequest(Long templateId, Long environmentId, Map<String, Object> variables) {
        log.info("预览模板请求: templateId={}, environmentId={}", templateId, environmentId);
        
        // 构建执行请求
        ExecutionRequest request = ExecutionRequest.builder()
                .templateId(templateId)
                .environmentId(environmentId)
                .variables(variables)
                .build();
        
        // 调用预览
        TemplateExecutor.PreviewResult preview = executionEngine.preview(request);
        
        // 转换为兼容格式
        Map<String, Object> result = new HashMap<>();
        result.put("url", preview.getUrl());
        result.put("method", preview.getMethod());
        result.put("headers", preview.getHeaders());
        result.put("body", preview.getBody());
        result.put("parameters", preview.getParameters());
        
        return result;
    }

    /**
     * 将 ExecutionResult 转换为兼容旧格式的 Map
     */
    private Map<String, Object> convertToLegacyFormat(ExecutionResult result) {
        Map<String, Object> legacyResult = new HashMap<>();
        
        legacyResult.put("templateId", result.getTemplateId());
        legacyResult.put("templateName", result.getTemplateName());
        legacyResult.put("success", result.isSuccess());
        legacyResult.put("statusCode", result.getStatusCode());
        legacyResult.put("message", result.getMessage());
        legacyResult.put("durationMs", result.getDurationMs());
        
        // 请求信息
        if (result.getRequest() != null) {
            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("url", result.getRequest().getUrl());
            requestInfo.put("method", result.getRequest().getMethod());
            requestInfo.put("headers", result.getRequest().getHeaders());
            requestInfo.put("body", result.getRequest().getBody());
            requestInfo.put("parameters", result.getRequest().getParameters());
            legacyResult.put("request", requestInfo);
        }
        
        // 响应信息
        if (result.getResponse() != null) {
            Map<String, Object> responseInfo = new HashMap<>();
            responseInfo.put("statusCode", result.getResponse().getStatusCode());
            responseInfo.put("statusText", result.getResponse().getStatusText());
            responseInfo.put("headers", result.getResponse().getHeaders());
            responseInfo.put("body", result.getResponse().getBody());
            responseInfo.put("responseTime", result.getResponse().getResponseTime());
            responseInfo.put("size", result.getResponse().getSize());
            legacyResult.put("response", responseInfo);
        }
        
        // 变量
        legacyResult.put("variables", result.getVariables());
        
        // 断言结果
        if (result.getAssertions() != null) {
            List<Map<String, Object>> assertions = result.getAssertions().stream()
                    .map(a -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", a.getName());
                        map.put("passed", a.isPassed());
                        map.put("type", a.getType());
                        map.put("actualValue", a.getActualValue());
                        map.put("expectedValue", a.getExpectedValue());
                        map.put("errorMessage", a.getErrorMessage());
                        return map;
                    })
                    .collect(Collectors.toList());
            legacyResult.put("assertions", assertions);
        }
        
        return legacyResult;
    }
}
