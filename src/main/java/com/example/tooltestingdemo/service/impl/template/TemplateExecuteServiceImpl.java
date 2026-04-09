package com.example.tooltestingdemo.service.impl.template;

import com.example.tooltestingdemo.entity.template.*;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.ProcessorExecuteService;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import com.example.tooltestingdemo.service.template.TemplateExecuteService;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.example.tooltestingdemo.vo.TemplateEnvironmentVO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 模板执行 Service 实现类
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/impl/template/TemplateExecuteServiceImpl.java
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateExecuteServiceImpl implements TemplateExecuteService {

    private final InterfaceTemplateService templateService;
    private final TemplateEnvironmentService environmentService;
    private final ProcessorExecuteService processorExecuteService;
    private final RestTemplate restTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public Map<String, Object> executeTemplate(Long templateId, Long environmentId, Map<String, Object> variables) {
        // 获取模板详情
        InterfaceTemplateVO template = templateService.getTemplateDetail(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        log.info("开始执行模板: templateId={}, name={}", templateId, template.getName());

        // 初始化执行上下文
        Map<String, Object> context = new HashMap<>();
        if (variables != null) {
            context.putAll(variables);
        }

        // 加载环境变量
        if (environmentId != null) {
            TemplateEnvironmentVO environment = environmentService.getEnvironmentById(environmentId);
            if (environment != null && StringUtils.hasText(environment.getVariables())) {
                // 解析环境变量JSON
                loadEnvironmentVariables(context, environment.getVariables());
            }
        }

        // 执行前置处理器
        if (!CollectionUtils.isEmpty(template.getPreProcessors())) {
            List<TemplatePreProcessor> preProcessors = convertToPreProcessors(template.getPreProcessors());
            context = processorExecuteService.executePreProcessors(preProcessors, context);
        }

        // 构建并发送请求
        Map<String, Object> requestInfo = buildRequest(template, context);
        Map<String, Object> responseInfo = sendRequest(template, requestInfo);

        // 执行后置处理器
        if (!CollectionUtils.isEmpty(template.getPostProcessors())) {
            List<TemplatePostProcessor> postProcessors = convertToPostProcessors(template.getPostProcessors());
            context = processorExecuteService.executePostProcessors(postProcessors, context, responseInfo);
        }

        // 组装执行结果
        Map<String, Object> result = new HashMap<>();
        result.put("templateId", templateId);
        result.put("templateName", template.getName());
        result.put("request", requestInfo);
        result.put("response", responseInfo);
        result.put("variables", context);

        log.info("模板执行完成: templateId={}", templateId);
        return result;
    }

    @Override
    public Map<String, Object> executeTemplate(Long templateId, Map<String, Object> variables) {
        // 获取默认环境
        TemplateEnvironmentVO defaultEnv = environmentService.getDefaultEnvironment(templateId);
        Long environmentId = defaultEnv != null ? defaultEnv.getId() : null;
        return executeTemplate(templateId, environmentId, variables);
    }

    @Override
    public Map<String, Object> validateTemplate(Long templateId) {
        InterfaceTemplateVO template = templateService.getTemplateDetail(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 验证必填字段
        if (!StringUtils.hasText(template.getName())) {
            errors.add("模板名称不能为空");
        }
        if (!StringUtils.hasText(template.getProtocolType())) {
            errors.add("协议类型不能为空");
        }
        if (!StringUtils.hasText(template.getMethod())) {
            errors.add("请求方法不能为空");
        }
        if (!StringUtils.hasText(template.getPath())) {
            errors.add("请求路径不能为空");
        }

        // 验证URL
        String fullUrl = buildFullUrl(template);
        if (!StringUtils.hasText(fullUrl)) {
            errors.add("请求URL不能为空");
        }

        // 验证请求头
        if (!CollectionUtils.isEmpty(template.getHeaders())) {
            for (var header : template.getHeaders()) {
                if (!StringUtils.hasText(header.getHeaderName())) {
                    warnings.add("存在请求头名称未设置");
                }
            }
        }

        // 验证断言
        if (!CollectionUtils.isEmpty(template.getAssertions())) {
            for (var assertion : template.getAssertions()) {
                if (!StringUtils.hasText(assertion.getAssertType())) {
                    warnings.add("存在断言类型未设置");
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("warnings", warnings);

        return result;
    }

    @Override
    public Map<String, Object> previewRequest(Long templateId, Long environmentId, Map<String, Object> variables) {
        InterfaceTemplateVO template = templateService.getTemplateDetail(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        Map<String, Object> context = new HashMap<>();
        if (variables != null) {
            context.putAll(variables);
        }

        // 加载环境变量
        if (environmentId != null) {
            TemplateEnvironmentVO environment = environmentService.getEnvironmentById(environmentId);
            if (environment != null && StringUtils.hasText(environment.getVariables())) {
                loadEnvironmentVariables(context, environment.getVariables());
            }
        }

        // 执行前置处理器
        if (!CollectionUtils.isEmpty(template.getPreProcessors())) {
            List<TemplatePreProcessor> preProcessors = convertToPreProcessors(template.getPreProcessors());
            context = processorExecuteService.executePreProcessors(preProcessors, context);
        }

        return buildRequest(template, context);
    }

    // ==================== 私有方法 ====================

    private Map<String, Object> buildRequest(InterfaceTemplateVO template, Map<String, Object> context) {
        Map<String, Object> request = new HashMap<>();

        // URL
        String url = buildFullUrl(template);
        url = replaceVariables(url, context);
        request.put("url", url);

        // Method
        request.put("method", template.getMethod());

        // Headers
        Map<String, String> headers = new HashMap<>();
        if (!CollectionUtils.isEmpty(template.getHeaders())) {
            template.getHeaders().forEach(h -> {
                if (Integer.valueOf(1).equals(h.getIsEnabled())) {
                    String value = replaceVariables(h.getHeaderValue(), context);
                    headers.put(h.getHeaderName(), value);
                }
            });
        }
        request.put("headers", headers);

        // Body
        if (StringUtils.hasText(template.getBodyContent())) {
            String body = replaceVariables(template.getBodyContent(), context);
            request.put("body", body);
        }

        // Parameters
        if (!CollectionUtils.isEmpty(template.getParameters())) {
            Map<String, String> params = new HashMap<>();
            template.getParameters().forEach(p -> {
                if (Integer.valueOf(1).equals(p.getIsEnabled())) {
                    String value = replaceVariables(p.getParamValue(), context);
                    params.put(p.getParamName(), value);
                }
            });
            request.put("parameters", params);
        }

        return request;
    }

    private Map<String, Object> sendRequest(InterfaceTemplateVO template, Map<String, Object> requestInfo) {
        Map<String, Object> response = new HashMap<>();

        String url = (String) requestInfo.get("url");
        String method = (String) requestInfo.get("method");
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) requestInfo.get("headers");
        String body = (String) requestInfo.get("body");

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, entity, String.class);
            long responseTime = System.currentTimeMillis() - startTime;

            response.put("statusCode", responseEntity.getStatusCode().value());
            response.put("statusText", org.springframework.http.HttpStatus.valueOf(responseEntity.getStatusCode().value()).getReasonPhrase());
            response.put("headers", responseEntity.getHeaders());
            
            // 尝试解析body为JSON，使其更易读
            String responseBody = responseEntity.getBody();
            Object parsedBody = parseResponseBody(responseBody);
            response.put("body", parsedBody);
            response.put("responseTime", responseTime);

        } catch (Exception e) {
            log.error("请求执行失败", e);
            response.put("error", e.getMessage());
            response.put("statusCode", 0);
        }

        return response;
    }

    /**
     * 尝试解析响应body为Map/List，使其更易读
     */
    private Object parseResponseBody(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        
        try {
            // 尝试解析为JSON
            body = body.trim();
            if (body.startsWith("{")) {
                // JSON Object
                return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            } else if (body.startsWith("[")) {
                // JSON Array
                return objectMapper.readValue(body, new TypeReference<List<Object>>() {});
            }
        } catch (Exception e) {
            log.debug("响应body不是JSON格式，返回原始字符串");
        }
        
        // 不是JSON或解析失败，返回原始字符串
        return body;
    }

    private String buildFullUrl(InterfaceTemplateVO template) {
        if (StringUtils.hasText(template.getFullUrl())) {
            return template.getFullUrl();
        }
        
        StringBuilder url = new StringBuilder();
        
        // Base URL (可能已包含协议头)
        String baseUrl = template.getBaseUrl();
        if (StringUtils.hasText(baseUrl)) {
            // 如果baseUrl已包含协议头，直接使用
            if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
                url.append(baseUrl);
            } else {
                // 添加协议头
                String protocol = template.getProtocolType();
                if (!StringUtils.hasText(protocol)) {
                    protocol = "HTTP";
                }
                url.append(protocol.toLowerCase()).append("://");
                url.append(baseUrl);
            }
        } else {
            // 没有baseUrl，只添加协议头
            String protocol = template.getProtocolType();
            if (!StringUtils.hasText(protocol)) {
                protocol = "HTTP";
            }
            url.append(protocol.toLowerCase()).append("://");
        }
        
        // Path
        if (StringUtils.hasText(template.getPath())) {
            String path = template.getPath();
            // 避免重复的斜杠
            char lastChar = url.charAt(url.length() - 1);
            if (lastChar != '/' && !path.startsWith("/")) {
                url.append("/");
            }
            url.append(path);
        }
        
        return url.toString();
    }

    private String replaceVariables(String content, Map<String, Object> context) {
        if (!StringUtils.hasText(content) || context == null) {
            return content;
        }

        String result = content;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    private void loadEnvironmentVariables(Map<String, Object> context, String variablesJson) {
        try {
            // 简单解析JSON字符串，格式：{"key": "value"}
            // 实际项目中可以使用更完善的JSON解析
            if (variablesJson.startsWith("{")) {
                variablesJson = variablesJson.substring(1);
            }
            if (variablesJson.endsWith("}")) {
                variablesJson = variablesJson.substring(0, variablesJson.length() - 1);
            }

            String[] pairs = variablesJson.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "");
                    String value = keyValue[1].trim().replace("\"", "");
                    context.put(key, value);
                }
            }
        } catch (Exception e) {
            log.error("解析环境变量失败", e);
        }
    }

    private List<TemplatePreProcessor> convertToPreProcessors(List<?> voList) {
        List<TemplatePreProcessor> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(voList)) {
            return list;
        }

        for (Object vo : voList) {
            TemplatePreProcessor processor = new TemplatePreProcessor();
            // 使用反射复制属性
            try {
                org.springframework.beans.BeanUtils.copyProperties(vo, processor);
                list.add(processor);
            } catch (Exception e) {
                log.error("转换前置处理器失败", e);
            }
        }

        return list;
    }

    private List<TemplatePostProcessor> convertToPostProcessors(List<?> voList) {
        List<TemplatePostProcessor> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(voList)) {
            return list;
        }

        for (Object vo : voList) {
            TemplatePostProcessor processor = new TemplatePostProcessor();
            try {
                org.springframework.beans.BeanUtils.copyProperties(vo, processor);
                list.add(processor);
            } catch (Exception e) {
                log.error("转换后置处理器失败", e);
            }
        }

        return list;
    }
}
