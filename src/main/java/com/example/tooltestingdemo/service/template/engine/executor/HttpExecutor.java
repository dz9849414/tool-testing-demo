package com.example.tooltestingdemo.service.template.engine.executor;

import com.alibaba.fastjson2.JSON;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpExecutor implements TemplateExecutor {

    private static final Set<String> METHODS_WITHOUT_BODY =
        Set.of("GET", "DELETE", "HEAD", "OPTIONS");

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
        RequestBuilder requestBuilder = new RequestBuilder(context);
        ExecutionResult.RequestInfo requestInfo = requestBuilder.build();
        ResponseData responseData = sendHttpRequest(requestBuilder, requestInfo);

        ExecutionResult.ResponseInfo responseInfo = ExecutionResult.ResponseInfo.builder()
            .statusCode(responseData.getStatusCode())
            .statusText(responseData.getStatusText())
            .headers(responseData.getHeaders())
            .body(parseResponseBody(responseData.getBody()))
            .size(responseData.getBody() != null ? (long) responseData.getBody().length() : 0L)
            .responseTime(responseData.getResponseTime())
            .build();

        boolean success = responseData.getStatusCode() != null
            && responseData.getStatusCode() == 200
            && responseData.getBody() != null
            && JSON.parseObject(responseData.getBody()).getInteger("code") == 200;
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
            return ValidationResult.failure("模板信息不能为空");
        }
        if (!StringUtils.hasText(context.getTemplate().getMethod())) {
            return ValidationResult.failure("请求方法不能为空");
        }
        String fullUrl = buildFullUrl(context.getTemplate(), context.getTemplateVariables());
        if (!StringUtils.hasText(fullUrl)) {
            return ValidationResult.failure("请求URL不能为空");
        }
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
     * 发送请求并获取响应数据
     *
     * @param requestBuilder
     * @param requestInfo
     * @return
     */
    private ResponseData sendHttpRequest(RequestBuilder requestBuilder,
                                         ExecutionResult.RequestInfo requestInfo) {
        ResponseData responseData = new ResponseData();
        try {
            HttpHeaders headers = requestBuilder.buildHttpHeaders(requestInfo);
            HttpEntity<?> entity = requestBuilder.buildRequestEntity(headers);
            HttpMethod httpMethod = HttpMethod.valueOf(requestInfo.getMethod().toUpperCase(Locale.ROOT));

            logRequest(requestInfo, headers, entity.getBody());

            long start = System.currentTimeMillis();
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                requestInfo.getUrl(), httpMethod, entity, String.class);

            responseData.setStatusCode(responseEntity.getStatusCode().value());
            responseData.setStatusText(HttpStatus.valueOf(
                responseEntity.getStatusCode().value()).getReasonPhrase());
            responseData.setHeaders(convertHeaders(responseEntity.getHeaders()));
            responseData.setBody(responseEntity.getBody());
            responseData.setResponseTime(System.currentTimeMillis() - start);
        } catch (HttpStatusCodeException e) {
            log.error("HTTP 请求返回错误状态: method={}, url={}, status={}",
                requestInfo.getMethod(), requestInfo.getUrl(), e.getStatusCode().value(), e);
            responseData.setStatusCode(e.getStatusCode().value());
            responseData.setStatusText(e.getStatusText());
            responseData.setHeaders(convertHeaders(e.getResponseHeaders()));
            responseData.setBody(e.getResponseBodyAsString());
            responseData.setResponseTime(0L);
        } catch (Exception e) {
            log.error("HTTP 请求执行失败: method={}, url={}",
                requestInfo.getMethod(), requestInfo.getUrl(), e);
            responseData.setStatusCode(0);
            responseData.setStatusText("Error");
            responseData.setBody(e.getMessage());
            responseData.setResponseTime(0L);
        }
        return responseData;
    }

    private Object parseResponseBody(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        try {
            String trimBody = body.trim();
            if (trimBody.startsWith("{")) {
                return objectMapper.readValue(trimBody, new TypeReference<Map<String, Object>>() {
                });
            }
            if (trimBody.startsWith("[")) {
                return objectMapper.readValue(trimBody, new TypeReference<List<Object>>() {
                });
            }
        } catch (Exception e) {
            log.debug("响应体不是 JSON，按原始字符串返回");
        }
        return body;
    }

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

    private void logRequest(ExecutionResult.RequestInfo requestInfo, HttpHeaders headers, Object body) {
        String bodyPreview;
        try {
            bodyPreview = body == null ? null : objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            bodyPreview = String.valueOf(body);
        }
        if (bodyPreview != null && bodyPreview.length() > 2000) {
            bodyPreview = bodyPreview.substring(0, 2000) + "...";
        }
        log.info("发送 HTTP 请求: method={}, url={}, headers={}, body={}",
            requestInfo.getMethod(), requestInfo.getUrl(), headers, bodyPreview);
    }

    private String buildFullUrl(InterfaceTemplate template, Map<String, Object> variables) {
        String overrideFullUrl = resolveFullUrl(variables);
        if (StringUtils.hasText(overrideFullUrl)) {
            return overrideFullUrl;
        }
        if (StringUtils.hasText(template.getFullUrl())) {
            return template.getFullUrl();
        }

        StringBuilder url = new StringBuilder();
        String baseUrl = template.getBaseUrl();
        if (StringUtils.hasText(baseUrl)) {
            if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
                url.append(baseUrl);
            } else {
                String protocol = StringUtils.hasText(template.getProtocolType())
                    ? template.getProtocolType().toLowerCase(Locale.ROOT) : "http";
                url.append(protocol).append("://").append(baseUrl);
            }
        } else {
            String protocol = StringUtils.hasText(template.getProtocolType())
                ? template.getProtocolType().toLowerCase(Locale.ROOT) : "http";
            url.append(protocol).append("://");
        }

        String resolvedPath = resolvePath(template, variables);
        if (StringUtils.hasText(resolvedPath)) {
            char lastChar = url.charAt(url.length() - 1);
            if (lastChar != '/' && !resolvedPath.startsWith("/")) {
                url.append("/");
            }
            url.append(resolvedPath);
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

    private class RequestBuilder {
        private final Map<String, Object> templateVariables;
        private final Map<String, Object> variables;
        private final InterfaceTemplate template;
        private InterfaceTemplateVO fullTemplate;

        RequestBuilder(TemplateContext context) {
            this.templateVariables = context.getTemplateVariables();
            this.variables = context.getAllVariables();
            this.template = context.getTemplate();
        }

        ExecutionResult.RequestInfo build() {
            InterfaceTemplateVO currentTemplate = getFullTemplate();
            String url = replaceVariables(buildFullUrl(template, templateVariables));

            Map<String, String> headers = new HashMap<>();
            if (currentTemplate != null && !CollectionUtils.isEmpty(currentTemplate.getHeaders())) {
                currentTemplate.getHeaders().stream()
                    .filter(h -> Integer.valueOf(1).equals(h.getIsEnabled()))
                    .filter(h -> StringUtils.hasText(h.getHeaderName()))
                    .filter(h -> StringUtils.hasText(h.getHeaderValue()))
                    .forEach(h -> headers.put(h.getHeaderName(), replaceVariables(h.getHeaderValue())));
            }

            String body = resolvePreviewBody(currentTemplate);
            Map<String, String> parameters = new HashMap<>();
            if (currentTemplate != null && !CollectionUtils.isEmpty(currentTemplate.getParameters())) {
                for (var parameter : currentTemplate.getParameters()) {
                    if (!Integer.valueOf(1).equals(parameter.getIsEnabled())) {
                        continue;
                    }
                    String paramValue = resolveParameterValue(parameter.getParamName(), parameter.getParamValue());
                    if ("PATH".equalsIgnoreCase(parameter.getParamType())) {
                        url = replacePathParameter(url, parameter.getParamName(), paramValue);
                    } else if ("QUERY".equalsIgnoreCase(parameter.getParamType())) {
                        parameters.put(parameter.getParamName(), paramValue);
                    }
                }
            }
            applyAuthQueryParameters(parameters);
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

        HttpHeaders buildHttpHeaders(ExecutionResult.RequestInfo requestInfo) {
            HttpHeaders headers = new HttpHeaders();
            if (requestInfo.getHeaders() != null) {
                requestInfo.getHeaders().forEach(headers::set);
            }
            MediaType contentType = resolveContentType();
            if (shouldSetContentType(requestInfo) && contentType != null
                && !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.setContentType(contentType);
            }
            if (!headers.containsKey(HttpHeaders.ACCEPT)) {
                headers.setAccept(List.of(MediaType.ALL));
            }
            applyForwardedAuthHeaders(headers);
            applyAuthHeaders(headers);
            return headers;
        }

        private boolean shouldSetContentType(ExecutionResult.RequestInfo requestInfo) {
            if (requestInfo == null || !StringUtils.hasText(requestInfo.getMethod())) {
                return false;
            }
            return !METHODS_WITHOUT_BODY.contains(requestInfo.getMethod().trim().toUpperCase(Locale.ROOT));
        }

        private void applyForwardedAuthHeaders(HttpHeaders headers) {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest currentRequest = attributes.getRequest();
            if (currentRequest == null) {
                return;
            }

            copyHeaderIfAbsent(currentRequest, headers, HttpHeaders.AUTHORIZATION);
            copyHeaderIfAbsent(currentRequest, headers, HttpHeaders.COOKIE);
        }

        private void copyHeaderIfAbsent(HttpServletRequest request, HttpHeaders headers, String headerName) {
            if (headers.containsKey(headerName)) {
                return;
            }
            String headerValue = request.getHeader(headerName);
            if (StringUtils.hasText(headerValue)) {
                headers.set(headerName, headerValue);
            }
        }

        HttpEntity<?> buildRequestEntity(HttpHeaders headers) {
            String method = template != null ? template.getMethod() : null;
            if (!StringUtils.hasText(method)
                || METHODS_WITHOUT_BODY.contains(method.trim().toUpperCase(Locale.ROOT))) {
                return new HttpEntity<>(headers);
            }

            String bodyType = resolveBodyType();
            if (TemplateEnums.BodyType.FORM_DATA.getCode().equalsIgnoreCase(bodyType)) {
                return new HttpEntity<>(buildFormDataBody(), headers);
            }
            if (TemplateEnums.BodyType.X_WWW_FORM_URLENCODED.getCode().equalsIgnoreCase(bodyType)) {
                return new HttpEntity<>(buildUrlEncodedBody(), headers);
            }
            return new HttpEntity<>(resolveRawBody(getFullTemplate()), headers);
        }

        private InterfaceTemplateVO getFullTemplate() {
            if (fullTemplate == null && template != null && template.getId() != null) {
                fullTemplate = templateService.getTemplateDetail(template.getId());
            }
            return fullTemplate;
        }

        private String resolvePreviewBody(InterfaceTemplateVO currentTemplate) {
            String bodyType = resolveBodyType();
            if (TemplateEnums.BodyType.FORM_DATA.getCode().equalsIgnoreCase(bodyType)
                || TemplateEnums.BodyType.X_WWW_FORM_URLENCODED.getCode().equalsIgnoreCase(bodyType)) {
                return buildFormPreview(currentTemplate);
            }
            return resolveRawBody(currentTemplate);
        }

        private String resolveRawBody(InterfaceTemplateVO currentTemplate) {
            String bodyFromVariables = resolveBodyFromTemplateVariables();
            if (StringUtils.hasText(bodyFromVariables)) {
                return bodyFromVariables;
            }
            if (template != null && StringUtils.hasText(template.getBodyContent())) {
                return replaceVariables(template.getBodyContent());
            }
            if (currentTemplate == null || CollectionUtils.isEmpty(currentTemplate.getParameters())) {
                return null;
            }
            for (var parameter : currentTemplate.getParameters()) {
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

        private String buildFormPreview(InterfaceTemplateVO currentTemplate) {
            if (currentTemplate == null || CollectionUtils.isEmpty(currentTemplate.getFormDataList())) {
                return null;
            }

            Map<String, Object> preview = new HashMap<>();
            currentTemplate.getFormDataList().stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsEnabled()))
                .filter(item -> StringUtils.hasText(item.getFieldName()))
                .forEach(item -> {
                    String fieldType = defaultString(item.getFieldType()).toUpperCase(Locale.ROOT);
                    if ("FILE".equals(fieldType) && StringUtils.hasText(item.getFilePath())) {
                        preview.put(item.getFieldName().trim(), replaceVariables(item.getFilePath()));
                    } else {
                        preview.put(item.getFieldName().trim(),
                            replaceVariables(defaultString(item.getFieldValue())));
                    }
                });
            try {
                return objectMapper.writeValueAsString(preview);
            } catch (Exception e) {
                return preview.toString();
            }
        }

        private String resolveBodyType() {
            if (template != null && StringUtils.hasText(template.getBodyType())) {
                return template.getBodyType().trim();
            }
            InterfaceTemplateVO currentTemplate = getFullTemplate();
            if (currentTemplate != null && StringUtils.hasText(currentTemplate.getBodyType())) {
                return currentTemplate.getBodyType().trim();
            }
            return TemplateEnums.BodyType.RAW.getCode();
        }

        private MediaType resolveContentType() {
            String configuredContentType = resolveConfiguredContentType();
            if (StringUtils.hasText(configuredContentType)) {
                try {
                    return MediaType.parseMediaType(configuredContentType);
                } catch (Exception e) {
                    log.warn("无效的 Content-Type 配置: {}", configuredContentType, e);
                }
            }

            String bodyType = resolveBodyType().toUpperCase(Locale.ROOT);
            return switch (bodyType) {
                case "FORM_DATA" -> MediaType.MULTIPART_FORM_DATA;
                case "X_WWW_FORM_URLENCODED" -> MediaType.APPLICATION_FORM_URLENCODED;
                case "XML" -> MediaType.APPLICATION_XML;
                case "HTML" -> MediaType.TEXT_HTML;
                case "TEXT" -> MediaType.TEXT_PLAIN;
                case "JAVASCRIPT" -> MediaType.parseMediaType("application/javascript");
                case "BINARY" -> MediaType.APPLICATION_OCTET_STREAM;
                default -> MediaType.APPLICATION_JSON;
            };
        }

        private String resolveConfiguredContentType() {
            if (template != null && StringUtils.hasText(template.getContentType())) {
                return replaceVariables(template.getContentType());
            }
            InterfaceTemplateVO currentTemplate = getFullTemplate();
            if (currentTemplate != null && StringUtils.hasText(currentTemplate.getContentType())) {
                return replaceVariables(currentTemplate.getContentType());
            }

            String bodyType = resolveBodyType();
            String bodyRawType = template != null ? template.getBodyRawType() : null;
            if (!StringUtils.hasText(bodyRawType) && currentTemplate != null) {
                bodyRawType = currentTemplate.getBodyRawType();
            }
            if (!TemplateEnums.BodyType.RAW.getCode().equalsIgnoreCase(bodyType)
                && !TemplateEnums.BodyType.GRAPHQL.getCode().equalsIgnoreCase(bodyType)) {
                return null;
            }
            if (!StringUtils.hasText(bodyRawType)) {
                return MediaType.APPLICATION_JSON_VALUE;
            }
            return switch (bodyRawType.trim().toUpperCase(Locale.ROOT)) {
                case "XML" -> MediaType.APPLICATION_XML_VALUE;
                case "HTML" -> MediaType.TEXT_HTML_VALUE;
                case "TEXT" -> MediaType.TEXT_PLAIN_VALUE;
                case "JAVASCRIPT" -> "application/javascript";
                case "JSON" -> MediaType.APPLICATION_JSON_VALUE;
                default -> MediaType.APPLICATION_JSON_VALUE;
            };
        }

        private void applyAuthHeaders(HttpHeaders headers) {
            Map<String, Object> authConfig = parseAuthConfig();
            String authType = resolveAuthType();
            if (!StringUtils.hasText(authType) || CollectionUtils.isEmpty(authConfig)) {
                return;
            }

            switch (authType.trim().toUpperCase(Locale.ROOT)) {
                case "BASIC" -> applyBasicAuth(headers, authConfig);
                case "BEARER", "JWT", "OAUTH2" -> applyBearerAuth(headers, authConfig);
                case "APIKEY" -> applyApiKeyHeader(headers, authConfig);
                default -> {
                }
            }
        }

        private void applyBasicAuth(HttpHeaders headers, Map<String, Object> authConfig) {
            String username = getConfigValue(authConfig, "username", "userName");
            String password = getConfigValue(authConfig, "password", "passWord");
            if (!StringUtils.hasText(username)) {
                return;
            }
            String rawValue = username + ":" + defaultString(password);
            String encodedValue = Base64.getEncoder()
                .encodeToString(rawValue.getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedValue);
        }

        private void applyBearerAuth(HttpHeaders headers, Map<String, Object> authConfig) {
            String token = getConfigValue(authConfig, "token", "accessToken", "bearerToken", "jwt");
            if (StringUtils.hasText(token)) {
                headers.setBearerAuth(token.trim());
            }
        }

        private void applyApiKeyHeader(HttpHeaders headers, Map<String, Object> authConfig) {
            String key = getConfigValue(authConfig, "key", "headerName", "name");
            String value = getConfigValue(authConfig, "value", "apiKey", "token");
            String location = getConfigValue(authConfig, "in", "location");
            if (!StringUtils.hasText(key) || !StringUtils.hasText(value)) {
                return;
            }
            if (!"QUERY".equalsIgnoreCase(defaultString(location, "HEADER"))) {
                headers.set(key.trim(), replaceVariables(value));
            }
        }

        private void applyAuthQueryParameters(Map<String, String> parameters) {
            Map<String, Object> authConfig = parseAuthConfig();
            String authType = resolveAuthType();
            if (!"APIKEY".equalsIgnoreCase(authType) || CollectionUtils.isEmpty(authConfig)) {
                return;
            }
            String location = getConfigValue(authConfig, "in", "location");
            if (!"QUERY".equalsIgnoreCase(location)) {
                return;
            }

            String key = getConfigValue(authConfig, "key", "paramName", "name");
            String value = getConfigValue(authConfig, "value", "apiKey", "token");
            if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                parameters.put(key.trim(), replaceVariables(value));
            }
        }

        private String resolveAuthType() {
            if (template != null && StringUtils.hasText(template.getAuthType())) {
                return template.getAuthType();
            }
            InterfaceTemplateVO currentTemplate = getFullTemplate();
            if (currentTemplate != null && StringUtils.hasText(currentTemplate.getAuthType())) {
                return currentTemplate.getAuthType();
            }
            return null;
        }

        private Map<String, Object> parseAuthConfig() {
            String authConfig = null;
            if (template != null && StringUtils.hasText(template.getAuthConfig())) {
                authConfig = replaceVariables(template.getAuthConfig());
            } else if (getFullTemplate() != null && StringUtils.hasText(getFullTemplate().getAuthConfig())) {
                authConfig = replaceVariables(getFullTemplate().getAuthConfig());
            }
            if (!StringUtils.hasText(authConfig)) {
                return Collections.emptyMap();
            }
            try {
                return objectMapper.readValue(authConfig, new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception e) {
                log.warn("解析认证配置失败: {}", authConfig, e);
                return Collections.emptyMap();
            }
        }

        private MultiValueMap<String, Object> buildFormDataBody() {
            LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            InterfaceTemplateVO currentTemplate = getFullTemplate();
            if (currentTemplate == null || CollectionUtils.isEmpty(currentTemplate.getFormDataList())) {
                return formData;
            }

            currentTemplate.getFormDataList().stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsEnabled()))
                .filter(item -> StringUtils.hasText(item.getFieldName()))
                .forEach(item -> {
                    String fieldName = item.getFieldName().trim();
                    String fieldType = defaultString(item.getFieldType()).toUpperCase(Locale.ROOT);
                    if ("FILE".equals(fieldType) && StringUtils.hasText(item.getFilePath())) {
                        File file = new File(replaceVariables(item.getFilePath()));
                        if (file.exists() && file.isFile()) {
                            formData.add(fieldName, new FileSystemResource(file));
                            return;
                        }
                    }
                    formData.add(fieldName, replaceVariables(defaultString(item.getFieldValue())));
                });
            return formData;
        }

        private MultiValueMap<String, String> buildUrlEncodedBody() {
            LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            InterfaceTemplateVO currentTemplate = getFullTemplate();
            if (currentTemplate == null || CollectionUtils.isEmpty(currentTemplate.getFormDataList())) {
                return formData;
            }

            currentTemplate.getFormDataList().stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsEnabled()))
                .filter(item -> StringUtils.hasText(item.getFieldName()))
                .forEach(item -> formData.add(item.getFieldName().trim(),
                    replaceVariables(defaultString(item.getFieldValue()))));
            return formData;
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
                if (value != null) {
                    try {
                        return objectMapper.writeValueAsString(value);
                    } catch (Exception e) {
                        log.warn("Failed to build request body from variable: {}", key, e);
                    }
                }
            }
            Object dataJson = templateVariables.get("dataJson");
            if (dataJson != null) {
                try {
                    Map<String, Object> requestBody = new LinkedHashMap<>();
                    requestBody.put("dataJson", dataJson);
                    Object remark = templateVariables.get("remark");
                    if (remark != null) {
                        requestBody.put("remark", remark);
                    }
                    return objectMapper.writeValueAsString(requestBody);
                } catch (Exception e) {
                    log.warn("Failed to build request body from dataJson", e);
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

        private String resolveParameterValue(String paramName, String configuredValue) {
            if (StringUtils.hasText(paramName) && variables != null && variables.containsKey(paramName.trim())) {
                Object variableValue = variables.get(paramName.trim());
                return variableValue == null ? "" : String.valueOf(variableValue);
            }
            return replaceVariables(configuredValue);
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

        private String getConfigValue(Map<String, Object> config, String... keys) {
            for (String key : keys) {
                Object value = config.get(key);
                if (value != null && StringUtils.hasText(value.toString())) {
                    return value.toString().trim();
                }
            }
            return null;
        }

        private String defaultString(String value) {
            return value == null ? "" : value;
        }

        private String defaultString(String value, String defaultValue) {
            return StringUtils.hasText(value) ? value : defaultValue;
        }
    }

    private static class ResponseData {
        private Integer statusCode;
        private String statusText;
        private Map<String, String> headers;
        private String body;
        private Long responseTime;

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
