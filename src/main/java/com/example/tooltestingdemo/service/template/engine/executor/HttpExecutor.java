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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Locale;

/**
 * HTTP/HTTPS 执行器
 * <p>
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

        String fullUrl = buildFullUrl(context.getTemplate(), context.getTemplateVariables());
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
            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
            if (!headers.containsKey(HttpHeaders.ACCEPT)) {
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
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
                return objectMapper.readValue(trimBody, new TypeReference<Map<String, Object>>() {
                });
            } else if (trimBody.startsWith("[")) {
                return objectMapper.readValue(trimBody, new TypeReference<List<Object>>() {
                });
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
    private String buildFullUrl(InterfaceTemplate template,
                                Map<String, Object> variables) {
        String overrideFullUrl = resolveFullUrl(variables);
        if (StringUtils.hasText(overrideFullUrl)) {
            return overrideFullUrl;
        }
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
        String resolvedPath = resolvePath(template, variables);
        if (StringUtils.hasText(resolvedPath)) {
            String path = resolvedPath;
            char lastChar = url.charAt(url.length() - 1);
            if (lastChar != '/' && !path.startsWith("/")) {
                url.append("/");
            }
            url.append(path);
        }

        return url.toString();
    }

    private String resolveFullUrl(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return null;
        }
        for (String key : List.of("fullUrl", "url", "requestUrl")) {
            Object value = variables.get(key);
            if (value instanceof String text && StringUtils.hasText(text)) {
                String candidate = text.trim();
                if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private String resolvePath(InterfaceTemplate template, Map<String, Object> variables) {
        if (variables != null && !variables.isEmpty()) {
            for (String key : List.of("path", "urlPath", "fullPath")) {
                Object value = variables.get(key);
                if (value instanceof String text && StringUtils.hasText(text)) {
                    String candidate = text.trim();
                    if (candidate.startsWith("/")) {
                        return candidate;
                    }
                }
            }
        }
        if (template != null && StringUtils.hasText(template.getPath())) {
            return template.getPath();
        }
        return null;
    }

    /**
     * 请求构建器内部类
     */
    private class RequestBuilder {
        private final TemplateContext context;
        private final Map<String, Object> templateVariables;
        private final Map<String, Object> variables;

        RequestBuilder(TemplateContext context) {
            this.context = context;
            this.templateVariables = context.getTemplateVariables();
            this.variables = context.getAllVariables();
        }

        ExecutionResult.RequestInfo build() {
            InterfaceTemplate template = context.getTemplate();

            // 获取完整模板详情（包含 headers、parameters 等）
            InterfaceTemplateVO fullTemplate = templateService.getTemplateDetail(template.getId());

            // URL（使用模板中的，已包含环境 baseUrl）
            String url = buildFullUrl(template, templateVariables);
            url = replaceVariables(url);

            // Headers
            Map<String, String> headers = new HashMap<>();
            if (fullTemplate != null && !CollectionUtils.isEmpty(fullTemplate.getHeaders())) {
                fullTemplate.getHeaders().stream()
                    .filter(h -> Integer.valueOf(1).equals(h.getIsEnabled())
                        && StringUtils.hasText(h.getHeaderName())
                        && StringUtils.hasText(h.getHeaderValue()))
                    .forEach(h -> headers.put(h.getHeaderName(), replaceVariables(h.getHeaderValue())));
            }

            // Body
            String body = resolveRequestBody(template, fullTemplate);

            // Parameters
            Map<String, String> parameters = new HashMap<>();
            if (fullTemplate != null && !CollectionUtils.isEmpty(fullTemplate.getParameters())) {
                for (var parameter : fullTemplate.getParameters()) {
                    if (!Integer.valueOf(1).equals(parameter.getIsEnabled())) {
                        continue;
                    }
                    String paramValue = replaceVariables(parameter.getParamValue());
                    if ("PATH".equalsIgnoreCase(parameter.getParamType())) {
                        url = replacePathParameter(url, parameter.getParamName(), paramValue);
                    } else if ("QUERY".equalsIgnoreCase(parameter.getParamType())) {
                        parameters.put(parameter.getParamName(), paramValue);
                    }
                }
            }
            url = replacePathVariablesFromTemplateVariables(url);
            url = appendQueryParameters(url, parameters);

            return ExecutionResult.RequestInfo.builder()
                .url(url)
                .method(template.getMethod())
                .headers(headers)
                .body(body)
                .parameters(parameters)
                .build();
        }

        private String resolveRequestBody(InterfaceTemplate template, InterfaceTemplateVO fullTemplate) {
            String bodyFromVariables = resolveBodyFromTemplateVariables();
            if (StringUtils.hasText(bodyFromVariables)) {
                return bodyFromVariables;
            }
            if (StringUtils.hasText(template.getBodyContent())) {
                return replaceVariables(template.getBodyContent());
            }
            if (fullTemplate == null || CollectionUtils.isEmpty(fullTemplate.getParameters())) {
                return null;
            }
            for (var parameter : fullTemplate.getParameters()) {
                if (!Integer.valueOf(1).equals(parameter.getIsEnabled())) {
                    continue;
                }
                if (!isBodyParameter(parameter.getParamType(), parameter.getParamName())) {
                    continue;
                }
                return replaceVariables(parameter.getParamValue());
            }
            return null;
        }

        private String resolveBodyFromTemplateVariables() {
            if (CollectionUtils.isEmpty(templateVariables)) {
                return null;
            }
            for (String key : List.of("body", "bodyContent", "requestBody")) {
                Object value = templateVariables.get(key);
                if (value instanceof String text && StringUtils.hasText(text)) {
                    return replaceVariables(text);
                }
            }
            Object dataJson = templateVariables.get("dataJson");
            if (dataJson != null) {
                try {
                    return objectMapper.writeValueAsString(Collections.singletonMap("dataJson", dataJson));
                } catch (Exception e) {
                    log.warn("构建任务请求体失败: dataJson", e);
                }
            }
            return null;
        }

        private boolean isBodyParameter(String paramType, String paramName) {
            if (StringUtils.hasText(paramType)) {
                String normalizedType = paramType.trim().toUpperCase(Locale.ROOT);
                if ("BODY".equals(normalizedType) || "RAW".equals(normalizedType)) {
                    return true;
                }
            }
            if (!StringUtils.hasText(paramName)) {
                return false;
            }
            return "body".equalsIgnoreCase(paramName.trim())
                || "bodyContent".equalsIgnoreCase(paramName.trim());
        }

        private String replaceVariables(String content) {
            if (!StringUtils.hasText(content) || variables == null) {
                return content;
            }

            String result = content;
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String shorthandPlaceholder = "$" + entry.getKey();
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
                result = result.replace(shorthandPlaceholder, value);
            }
            return result;
        }

        private String replacePathParameter(String url, String paramName, String paramValue) {
            if (!StringUtils.hasText(url) || !StringUtils.hasText(paramName)) {
                return url;
            }

            String normalizedParamName = paramName.trim();
            String safeValue = paramValue != null ? paramValue : "";
            return url.replace("{" + normalizedParamName + "}", safeValue)
                .replace("${" + normalizedParamName + "}", safeValue)
                .replace("$" + normalizedParamName, safeValue);
        }

        private String replacePathVariablesFromTemplateVariables(String url) {
            if (!StringUtils.hasText(url) || CollectionUtils.isEmpty(templateVariables)) {
                return url;
            }

            String resolvedUrl = url;
            for (Map.Entry<String, Object> entry : templateVariables.entrySet()) {
                if (!StringUtils.hasText(entry.getKey())) {
                    continue;
                }
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                resolvedUrl = replacePathParameter(resolvedUrl, entry.getKey(), value);
            }
            return resolvedUrl;
        }

        private String appendQueryParameters(String url, Map<String, String> parameters) {
            if (!StringUtils.hasText(url) || parameters == null || parameters.isEmpty()) {
                return url;
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            parameters.forEach(builder::queryParam);
            return builder.build(true).toUriString();
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
        public Integer getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public Long getResponseTime() {
            return responseTime;
        }

        public void setResponseTime(Long responseTime) {
            this.responseTime = responseTime;
        }
    }
}
