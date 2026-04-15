package com.example.tooltestingdemo.service.impl.template;

import com.example.tooltestingdemo.service.template.TemplateExecuteService;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionEngine;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionRequest;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模板执行 Service 实现类（基于执行引擎）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateExecuteServiceImpl implements TemplateExecuteService {

    private final ExecutionEngine executionEngine;

    @Override
    public Map<String, Object> executeTemplate(Long templateId, Long environmentId, Map<String, Object> variables) {
        log.info("执行模板请求: 模板id={}, 环境标识={}", templateId, environmentId);
        
        ExecutionRequest request = ExecutionRequest.builder()
            .templateId(templateId)
            .environmentId(environmentId)
            .variables(variables)
            .executeAssertions(true)
            .executePreProcessors(true)
            .executePostProcessors(true)
            .build();
        
        return convertToLegacyFormat(executionEngine.execute(request));
    }

    @Override
    public Map<String, Object> executeTemplate(Long templateId, Map<String, Object> variables) {
        return executeTemplate(templateId, null, variables);
    }

    @Override
    public Map<String, Object> validateTemplate(Long templateId) {
        log.info("验证模板配置: templateId={}", templateId);
        
        var validation = executionEngine.validate(templateId);
        Map<String, Object> result = new HashMap<>();
        result.put("valid", validation.isValid());
        result.put("errors", validation.isValid() ? Collections.emptyList() : List.of(validation.getMessage()));
        result.put("warnings", Collections.emptyList());
        
        return result;
    }

    @Override
    public Map<String, Object> previewRequest(Long templateId, Long environmentId, Map<String, Object> variables) {
        log.info("预览模板请求: templateId={}, environmentId={}", templateId, environmentId);
        
        ExecutionRequest request = ExecutionRequest.builder()
            .templateId(templateId)
            .environmentId(environmentId)
            .variables(variables)
            .build();
        
        var preview = executionEngine.preview(request);
        Map<String, Object> result = new HashMap<>();
        result.put("url", preview.getUrl());
        result.put("method", preview.getMethod());
        result.put("headers", preview.getHeaders());
        result.put("body", preview.getBody());
        result.put("parameters", preview.getParameters());
        
        return result;
    }

    private Map<String, Object> convertToLegacyFormat(ExecutionResult result) {
        Map<String, Object> legacy = new HashMap<>();
        
        legacy.put("templateId", result.getTemplateId());
        legacy.put("templateName", result.getTemplateName());
        legacy.put("success", result.isSuccess());
        legacy.put("statusCode", result.getStatusCode());
        legacy.put("message", result.getMessage());
        legacy.put("durationMs", result.getDurationMs());
        
        Optional.ofNullable(result.getRequest()).ifPresent(req -> {
            Map<String, Object> reqInfo = new HashMap<>();
            reqInfo.put("url", req.getUrl());
            reqInfo.put("method", req.getMethod());
            reqInfo.put("headers", req.getHeaders());
            reqInfo.put("body", req.getBody());
            reqInfo.put("parameters", req.getParameters());
            legacy.put("request", reqInfo);
        });
        
        Optional.ofNullable(result.getResponse()).ifPresent(resp -> {
            Map<String, Object> respInfo = new HashMap<>();
            respInfo.put("statusCode", resp.getStatusCode());
            respInfo.put("statusText", resp.getStatusText());
            respInfo.put("headers", resp.getHeaders());
            respInfo.put("body", resp.getBody());
            respInfo.put("responseTime", resp.getResponseTime());
            respInfo.put("size", resp.getSize());
            legacy.put("response", respInfo);
        });
        
        legacy.put("variables", result.getVariables());
        
        Optional.ofNullable(result.getAssertions()).ifPresent(assertions -> 
            legacy.put("assertions", assertions.stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", a.getName());
                map.put("passed", a.isPassed());
                map.put("type", a.getType());
                map.put("actualValue", a.getActualValue());
                map.put("expectedValue", a.getExpectedValue());
                map.put("errorMessage", a.getErrorMessage());
                return map;
            }).collect(Collectors.toList()))
        );
        
        return legacy;
    }
}
