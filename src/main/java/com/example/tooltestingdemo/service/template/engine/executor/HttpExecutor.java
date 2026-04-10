package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * HTTP/HTTPS 执行器
 * 
 * 执行 HTTP 协议的模板请求
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpExecutor implements TemplateExecutor {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final InterfaceTemplateService templateService;

    @Override
    public String getType() {
        return "HTTP";
    }

    @Override
    public ExecutionResult execute(TemplateContext context) {
        log.debug("执行 HTTP 请求: templateId={}", 
                context.getTemplate() != null ? context.getTemplate().getId() : null);
        
        LocalDateTime startTime = LocalDateTime.now();
        long startMs = System.currentTimeMillis();
        
        // 1. 构建请求
        RequestBuilder requestBuilder = new RequestBuilder(context);
        ExecutionResult.RequestInfo requestInfo = requestBuilder.build();
        
        // 2. 发送请求
        ResponseData responseData = sendHttpRequest(requestInfo);
        
        // 3. 构建结果
        ExecutionResult.ResponseInfo responseInfo = ExecutionResult.ResponseInfo.builder()
                .statusCode(responseData.getStatusCode())
                .statusText(responseData.getStatusText())
                .headers(responseData.getHeaders())
                .body(parseResponseBody(responseData.getBody()))
                .size(responseData.getBody() != null ? (long) responseData.getBody().length() : 0L)
                .responseTime(System.currentTimeMillis() - startMs)
                .build();
        
        boolean success = responseData.getStatusCode() != null && 
                responseData.getStatusCode() >= 200 && responseData.getStatusCode() < 300;
        
        return ExecutionResult.builder()
                .success(success)
                .statusCode(String.valueOf(responseData.getStatusCode()))
                .message(success ? "执行成功" : "执行失败")
                .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                .startTime(startTime)
                .request(requestInfo)
                .response(responseInfo)
                .variables(context.getAllVariables())
                .build();
    }

    @Override
    public ValidationResult validate(TemplateContext context) {
        if (context.getTemplate() == null) {
            return ValidationResult.failure("模板信息为空");
        }
        
        // 验证必填字段
        if (!StringUtils.hasText(context.getTemplate().getMethod())) {
            return ValidationResult.failure("请求方法不能为空");
        }
        
        String fullUrl = buildFullUrl(context.getTemplate());
        if (!StringUtils.hasText(fullUrl)) {
            return ValidationResult.failure("请求URL不能为空");
        }
        
        // 验证URL格式
        try {
            new java.net.URL(fullUrl);
        } catch (Exception e) {
            return ValidationResult.failure("URL格式不正确: " + fullUrl);
        }
        
        return ValidationResult.success();
    }

    @Override
    public PreviewResult preview(TemplateContext context) {
        RequestBuilder builder = new RequestBuilder(context);
        ExecutionResult.RequestInfo requestInfo = builder.build();
        
        return new PreviewResult(
                requestInfo.getUrl(),
                requestInfo.getMethod(),
                requestInfo.getHeaders(),
                requestInfo.getBody(),
                requestInfo.getParameters()
        );
    }

    /**
     * 发送 HTTP 请求
     */
    private ResponseData sendHttpRequest(ExecutionResult.RequestInfo requestInfo) {
        ResponseData responseData = new ResponseData();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            if (requestInfo.getHeaders() != null) {
                requestInfo.getHeaders().forEach(headers::set);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(requestInfo.getBody(), headers);
            HttpMethod httpMethod = HttpMethod.valueOf(requestInfo.getMethod().toUpperCase());
            
            long start = System.currentTimeMillis();
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    requestInfo.getUrl(), httpMethod, entity, String.class);
            long responseTime = System.currentTimeMillis() - start;
            
            responseData.setStatusCode(responseEntity.getStatusCode().value());
            responseData.setStatusText(HttpStatus.valueOf(
                    responseEntity.getStatusCode().value()).getReasonPhrase());
            responseData.setHeaders(convertHeaders(responseEntity.getHeaders()));
            responseData.setBody(responseEntity.getBody());
            responseData.setResponseTime(responseTime);
            
        } catch (Exception e) {
            log.error("HTTP 请求执行失败", e);
            responseData.setStatusCode(0);
            responseData.setStatusText("Error");
            responseData.setBody(e.getMessage());
        }
        
        return responseData;
    }

    /**
     * 解析响应体
     */
    private Object parseResponseBody(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        
        try {
            String trimBody = body.trim();
            if (trimBody.startsWith("{")) {
                return objectMapper.readValue(trimBody, new TypeReference<Map<String, Object>>() {});
            } else if (trimBody.startsWith("[")) {
                return objectMapper.readValue(trimBody, new TypeReference<List<Object>>() {});
            }
        } catch (Exception e) {
            log.debug("响应体不是JSON格式，返回原始字符串");
        }
        
        return body;
    }

    /**
     * 转换响应头
     */
    private Map<String, String> convertHeaders(HttpHeaders headers) {
        Map<String, String> result = new HashMap<>();
        if (headers != null) {
            headers.forEach((key, values) -> {
                if (!values.isEmpty()) {
                    result.put(key, String.join(", ", values));
                }
            });
        }
        return result;
    }

    /**
     * 构建完整 URL
     */
    private String buildFullUrl(com.example.tooltestingdemo.entity.template.InterfaceTemplate template) {
        if (StringUtils.hasText(template.getFullUrl())) {
            return template.getFullUrl();
        }
        
        StringBuilder url = new StringBuilder();
        
        // Base URL
        String baseUrl = template.getBaseUrl();
        if (StringUtils.hasText(baseUrl)) {
            if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
                url.append(baseUrl);
            } else {
                String protocol = StringUtils.hasText(template.getProtocolType()) 
                        ? template.getProtocolType().toLowerCase() : "http";
                url.append(protocol).append("://").append(baseUrl);
            }
        } else {
            String protocol = StringUtils.hasText(template.getProtocolType()) 
                    ? template.getProtocolType().toLowerCase() : "http";
            url.append(protocol).append("://");
        }
        
        // Path
        if (StringUtils.hasText(template.getPath())) {
            String path = template.getPath();
            char lastChar = url.charAt(url.length() - 1);
            if (lastChar != '/' && !path.startsWith("/")) {
                url.append("/");
            }
            url.append(path);
        }
        
        return url.toString();
    }

    /**
     * 请求构建器内部类
     */
    private class RequestBuilder {
        private final TemplateContext context;
        private final Map<String, Object> variables;
        
        RequestBuilder(TemplateContext context) {
            this.context = context;
            this.variables = context.getAllVariables();
        }
        
        ExecutionResult.RequestInfo build() {
            InterfaceTemplate template = context.getTemplate();
            
            // 获取完整模板详情（包含 headers、parameters 等）
            InterfaceTemplateVO fullTemplate = templateService.getTemplateDetail(template.getId());
            
            // URL（使用模板中的，已包含环境 baseUrl）
            String url = buildFullUrl(template);
            url = replaceVariables(url);
            
            // Headers
            Map<String, String> headers = new HashMap<>();
            if (fullTemplate != null && !CollectionUtils.isEmpty(fullTemplate.getHeaders())) {
                fullTemplate.getHeaders().stream()
                        .filter(h -> Integer.valueOf(1).equals(h.getIsEnabled()))
                        .forEach(h -> headers.put(h.getHeaderName(), replaceVariables(h.getHeaderValue())));
            }
            
            // Body
            String body = null;
            if (StringUtils.hasText(template.getBodyContent())) {
                body = replaceVariables(template.getBodyContent());
            }
            
            // Parameters
            Map<String, String> parameters = new HashMap<>();
            if (fullTemplate != null && !CollectionUtils.isEmpty(fullTemplate.getParameters())) {
                fullTemplate.getParameters().stream()
                        .filter(p -> Integer.valueOf(1).equals(p.getIsEnabled()))
                        .forEach(p -> parameters.put(p.getParamName(), replaceVariables(p.getParamValue())));
            }
            
            return ExecutionResult.RequestInfo.builder()
                    .url(url)
                    .method(template.getMethod())
                    .headers(headers)
                    .body(body)
                    .parameters(parameters)
                    .build();
        }
        
        private String replaceVariables(String content) {
            if (!StringUtils.hasText(content) || variables == null) {
                return content;
            }
            
            String result = content;
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
            return result;
        }
    }

    /**
     * 响应数据内部类
     */
    private static class ResponseData {
        private Integer statusCode;
        private String statusText;
        private Map<String, String> headers;
        private String body;
        private Long responseTime;
        
        // Getters and Setters
        public Integer getStatusCode() { return statusCode; }
        public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
        public String getStatusText() { return statusText; }
        public void setStatusText(String statusText) { this.statusText = statusText; }
        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public Long getResponseTime() { return responseTime; }
        public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
    }
}
