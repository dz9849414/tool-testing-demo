package com.example.tooltestingdemo.service.template.engine.executor;

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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * 基于 {@link TemplateContext} 构建可直接执行的 HTTP 请求数据。
 *
 * <p>这个工厂把 HTTP 请求组装相关的职责统一收口，让 {@link HttpExecutor}
 * 只负责流程编排。这里主要负责：
 * <ul>
 *   <li>根据模板配置和运行时变量解析最终 URL</li>
 *   <li>构建请求头，并在需要时透传当前请求的认证信息</li>
 *   <li>应用模板中配置的认证方式</li>
 *   <li>生成 raw、form-data、urlencoded 等不同类型的请求体</li>
 *   <li>在 URL、请求头、请求体中统一执行变量替换</li>
 * </ul>
 */
public class HttpRequestFactory {

    /**
     * 不应默认携带请求体的方法集合。
     */
    private static final Set<String> METHODS_WITHOUT_BODY =
        Set.of("GET", "DELETE", "HEAD", "OPTIONS");

    /**
     * JSON 序列化与反序列化工具。
     */
    private final ObjectMapper objectMapper;

    /**
     * 模板详情查询服务，用于按需加载完整模板配置。
     */
    private final InterfaceTemplateService templateService;

    /**
     * 构建最终用于执行 HTTP 请求的规格对象。
     */
    public HttpRequestSpec build(TemplateContext context) {
        BuilderState state = new BuilderState(context);
        ExecutionResult.RequestInfo requestInfo = buildRequestInfo(state);
        HttpHeaders headers = buildHttpHeaders(state, requestInfo);
        HttpEntity<?> entity = buildRequestEntity(state, headers);
        return new HttpRequestSpec(requestInfo, entity);
    }

    /**
     * 解析最终请求 URL，供校验和预览场景复用。
     */
    public String resolveRequestUrl(TemplateContext context) {
        BuilderState state = new BuilderState(context);
        String url = replaceVariables(state, buildFullUrl(state.template(), state.templateVariables()));

        InterfaceTemplateVO currentTemplate = state.fullTemplate();
        if (currentTemplate == null || CollectionUtils.isEmpty(currentTemplate.getParameters())) {
            return replacePathVariablesFromTemplateVariables(state, url);
        }

        Map<String, String> parameters = new HashMap<>();
        for (var parameter : currentTemplate.getParameters()) {
            if (!Integer.valueOf(1).equals(parameter.getIsEnabled())) {
                continue;
            }
            String paramValue = resolveParameterValue(state, parameter.getParamName(), parameter.getParamValue());
            if ("PATH".equalsIgnoreCase(parameter.getParamType())) {
                url = replacePathParameter(url, parameter.getParamName(), paramValue);
            } else if ("QUERY".equalsIgnoreCase(parameter.getParamType())) {
                parameters.put(parameter.getParamName(), paramValue);
            }
        }
        applyAuthQueryParameters(state, parameters);
        url = replacePathVariablesFromTemplateVariables(state, url);
        return appendQueryParameters(url, parameters);
    }

    /**
     * 构建预览和结果展示使用的轻量请求信息。
     */
    private ExecutionResult.RequestInfo buildRequestInfo(BuilderState state) {
        InterfaceTemplateVO currentTemplate = state.fullTemplate();
        String url = replaceVariables(state, buildFullUrl(state.template(), state.templateVariables()));

        Map<String, String> headers = new HashMap<>();
        if (currentTemplate != null && !CollectionUtils.isEmpty(currentTemplate.getHeaders())) {
            currentTemplate.getHeaders().stream()
                .filter(h -> Integer.valueOf(1).equals(h.getIsEnabled()))
                .filter(h -> StringUtils.hasText(h.getHeaderName()))
                .filter(h -> StringUtils.hasText(h.getHeaderValue()))
                .forEach(h -> headers.put(h.getHeaderName(), replaceVariables(state, h.getHeaderValue())));
        }

        String body = resolvePreviewBody(state, currentTemplate);
        Map<String, String> parameters = new HashMap<>();
        if (currentTemplate != null && !CollectionUtils.isEmpty(currentTemplate.getParameters())) {
            for (var parameter : currentTemplate.getParameters()) {
                if (!Integer.valueOf(1).equals(parameter.getIsEnabled())) {
                    continue;
                }
                String paramValue = resolveParameterValue(state, parameter.getParamName(), parameter.getParamValue());
                // PATH 参数直接替换进 URL 模板。
                if ("PATH".equalsIgnoreCase(parameter.getParamType())) {
                    url = replacePathParameter(url, parameter.getParamName(), paramValue);
                    // QUERY 参数先收集起来，后面再与鉴权参数统一合并。
                } else if ("QUERY".equalsIgnoreCase(parameter.getParamType())) {
                    parameters.put(parameter.getParamName(), paramValue);
                }
            }
        }
        applyAuthQueryParameters(state, parameters);
        url = replacePathVariablesFromTemplateVariables(state, url);
        url = appendQueryParameters(url, parameters);

        return ExecutionResult.RequestInfo.builder()
            .url(url)
            .method(state.template() != null ? state.template().getMethod() : null)
            .headers(headers)
            .body(body)
            .parameters(parameters)
            .build();
    }

    /**
     * 构建真正发送请求时使用的 HTTP 请求头。
     */
    private HttpHeaders buildHttpHeaders(BuilderState state, ExecutionResult.RequestInfo requestInfo) {
        HttpHeaders headers = new HttpHeaders();
        if (requestInfo.getHeaders() != null) {
            requestInfo.getHeaders().forEach(headers::set);
        }

        MediaType contentType = resolveContentType(state);
        if (shouldSetContentType(requestInfo) && contentType != null
            && !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(contentType);
        }
        if (!headers.containsKey(HttpHeaders.ACCEPT)) {
            headers.setAccept(List.of(MediaType.ALL));
        }

        // 如果模板没有鉴权头，传当前调用方的认证信息。
        applyForwardedAuthHeaders(headers);
        applyAuthHeaders(state, headers);
        return headers;
    }

    /**
     * GET、DELETE 这类通常无请求体的方法，不默认补 Content-Type。
     */
    private boolean shouldSetContentType(ExecutionResult.RequestInfo requestInfo) {
        if (requestInfo == null || !StringUtils.hasText(requestInfo.getMethod())) {
            return false;
        }
        return !METHODS_WITHOUT_BODY.contains(requestInfo.getMethod().trim().toUpperCase(Locale.ROOT));
    }

    /**
     * 构建 RestTemplate 实际发送时使用的请求实体。
     */
    private HttpEntity<?> buildRequestEntity(BuilderState state, HttpHeaders headers) {
        String method = state.template() != null ? state.template().getMethod() : null;
        if (!StringUtils.hasText(method)
            || METHODS_WITHOUT_BODY.contains(method.trim().toUpperCase(Locale.ROOT))) {
            return new HttpEntity<>(headers);
        }

        String bodyType = resolveBodyType(state);
        if (TemplateEnums.BodyType.FORM_DATA.getCode().equalsIgnoreCase(bodyType)) {
            return new HttpEntity<>(buildFormDataBody(state), headers);
        }
        if (TemplateEnums.BodyType.X_WWW_FORM_URLENCODED.getCode().equalsIgnoreCase(bodyType)) {
            return new HttpEntity<>(buildUrlEncodedBody(state), headers);
        }
        return new HttpEntity<>(resolveRawBody(state, state.fullTemplate()), headers);
    }

    /**
     * 当模板请求没有显式定义认证头时，从当前入站请求中透传相关认证头。
     */
    private void applyForwardedAuthHeaders(HttpHeaders headers) {
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest)
            .ifPresent(request -> {
                copyHeaderIfAbsent(request, headers, HttpHeaders.AUTHORIZATION);
                copyHeaderIfAbsent(request, headers, HttpHeaders.COOKIE);
            });
    }


    /**
     * 请求头替换（如果没有）
     *
     * @param request
     * @param headers
     * @param headerName
     */
    private void copyHeaderIfAbsent(HttpServletRequest request, HttpHeaders headers, String headerName) {
        if (headers.containsKey(headerName)) {
            return;
        }
        String headerValue = request.getHeader(headerName);
        if (StringUtils.hasText(headerValue)) {
            headers.set(headerName, headerValue);
        }
    }

    /**
     * 基于模板配置和运行时变量解析完整 URL。
     */
    private String buildFullUrl(InterfaceTemplate template, Map<String, Object> variables) {
        String overrideFullUrl = resolveFullUrl(variables);
        if (StringUtils.hasText(overrideFullUrl)) {
            return overrideFullUrl;
        }
        if (template != null && StringUtils.hasText(template.getFullUrl())) {
            return template.getFullUrl();
        }

        StringBuilder url = new StringBuilder();
        String baseUrl = template != null ? template.getBaseUrl() : null;
        if (StringUtils.hasText(baseUrl)) {
            if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
                url.append(baseUrl);
            } else {
                String protocol = template != null && StringUtils.hasText(template.getProtocolType())
                    ? template.getProtocolType().toLowerCase(Locale.ROOT) : "http";
                url.append(protocol).append("://").append(baseUrl);
            }
        } else {
            String protocol = template != null && StringUtils.hasText(template.getProtocolType())
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

    /**
     * 允许运行时变量直接覆盖完整请求地址。
     */
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

    /**
     * 当调用方只提供路径覆盖项时，解析路径部分。
     */
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
     * 生成预览用请求体，尽量贴近真实发送结构。
     */
    private String resolvePreviewBody(BuilderState state, InterfaceTemplateVO currentTemplate) {
        String bodyType = resolveBodyType(state);
        if (TemplateEnums.BodyType.FORM_DATA.getCode().equalsIgnoreCase(bodyType)
            || TemplateEnums.BodyType.X_WWW_FORM_URLENCODED.getCode().equalsIgnoreCase(bodyType)) {
            return buildFormPreview(state, currentTemplate);
        }
        return resolveRawBody(state, currentTemplate);
    }

    private String resolveRawBody(BuilderState state, InterfaceTemplateVO currentTemplate) {
        String bodyFromVariables = resolveBodyFromTemplateVariables(state);
        if (StringUtils.hasText(bodyFromVariables)) {
            return bodyFromVariables;
        }
        if (state.template() != null && StringUtils.hasText(state.template().getBodyContent())) {
            return replaceVariables(state, state.template().getBodyContent());
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
            return replaceVariables(state, parameter.getParamValue());
        }
        return null;
    }

    /**
     * 把表单字段转换成便于 UI 展示的预览内容。
     */
    private String buildFormPreview(BuilderState state, InterfaceTemplateVO currentTemplate) {
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
                    preview.put(item.getFieldName().trim(), replaceVariables(state, item.getFilePath()));
                } else {
                    preview.put(item.getFieldName().trim(),
                        replaceVariables(state, defaultString(item.getFieldValue())));
                }
            });
        try {
            return objectMapper.writeValueAsString(preview);
        } catch (Exception e) {
            return preview.toString();
        }
    }

    /**
     * 解析最终生效的请求体类型，优先使用运行时拿到的模板详情。
     */
    private String resolveBodyType(BuilderState state) {
        if (state.template() != null && StringUtils.hasText(state.template().getBodyType())) {
            return state.template().getBodyType().trim();
        }
        InterfaceTemplateVO currentTemplate = state.fullTemplate();
        if (currentTemplate != null && StringUtils.hasText(currentTemplate.getBodyType())) {
            return currentTemplate.getBodyType().trim();
        }
        return TemplateEnums.BodyType.RAW.getCode();
    }

    /**
     * 解析最终发送时的 Content-Type，显式配置优先于推断结果。
     */
    private MediaType resolveContentType(BuilderState state) {
        String configuredContentType = resolveConfiguredContentType(state);
        if (StringUtils.hasText(configuredContentType)) {
            try {
                return MediaType.parseMediaType(configuredContentType);
            } catch (Exception e) {
                log.warn("无效的 Content-Type 配置: {}", configuredContentType, e);
            }
        }

        String bodyType = resolveBodyType(state).toUpperCase(Locale.ROOT);
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

    /**
     * 当模板未显式配置 Content-Type 时，根据 raw 子类型推断默认值。
     */
    private String resolveConfiguredContentType(BuilderState state) {
        if (state.template() != null && StringUtils.hasText(state.template().getContentType())) {
            return replaceVariables(state, state.template().getContentType());
        }
        InterfaceTemplateVO currentTemplate = state.fullTemplate();
        if (currentTemplate != null && StringUtils.hasText(currentTemplate.getContentType())) {
            return replaceVariables(state, currentTemplate.getContentType());
        }

        String bodyType = resolveBodyType(state);
        String bodyRawType = state.template() != null ? state.template().getBodyRawType() : null;
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

    /**
     * 把模板中配置的认证策略应用到请求头。
     */
    private void applyAuthHeaders(BuilderState state, HttpHeaders headers) {
        Map<String, Object> authConfig = parseAuthConfig(state);
        String authType = resolveAuthType(state);
        if (!StringUtils.hasText(authType) || CollectionUtils.isEmpty(authConfig)) {
            return;
        }

        switch (authType.trim().toUpperCase(Locale.ROOT)) {
            case "BASIC" -> applyBasicAuth(headers, authConfig);
            case "BEARER", "JWT", "OAUTH2" -> applyBearerAuth(headers, authConfig);
            case "APIKEY" -> applyApiKeyHeader(state, headers, authConfig);
            default -> {
            }
        }
    }

    /**
     * BASIC 认证会把用户名和密码编码后写入 Authorization。
     */
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

    /**
     * Bearer 类认证最终都转成 Authorization: Bearer {token}。
     */
    private void applyBearerAuth(HttpHeaders headers, Map<String, Object> authConfig) {
        String token = getConfigValue(authConfig, "token", "accessToken", "bearerToken", "jwt");
        if (StringUtils.hasText(token)) {
            headers.setBearerAuth(token.trim());
        }
    }

    /**
     * API Key 认证既可能放请求头，也可能放查询参数。
     */
    private void applyApiKeyHeader(BuilderState state, HttpHeaders headers, Map<String, Object> authConfig) {
        String key = getConfigValue(authConfig, "key", "headerName", "name");
        String value = getConfigValue(authConfig, "value", "apiKey", "token");
        String location = getConfigValue(authConfig, "in", "location");
        if (!StringUtils.hasText(key) || !StringUtils.hasText(value)) {
            return;
        }
        if (!"QUERY".equalsIgnoreCase(defaultString(location, "HEADER"))) {
            headers.set(key.trim(), replaceVariables(state, value));
        }
    }

    /**
     * 当 API Key 指定放在 QUERY 中时，把它追加到查询参数里。
     */
    private void applyAuthQueryParameters(BuilderState state, Map<String, String> parameters) {
        Map<String, Object> authConfig = parseAuthConfig(state);
        String authType = resolveAuthType(state);
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
            parameters.put(key.trim(), replaceVariables(state, value));
        }
    }

    private String resolveAuthType(BuilderState state) {
        if (state.template() != null && StringUtils.hasText(state.template().getAuthType())) {
            return state.template().getAuthType();
        }
        InterfaceTemplateVO currentTemplate = state.fullTemplate();
        if (currentTemplate != null && StringUtils.hasText(currentTemplate.getAuthType())) {
            return currentTemplate.getAuthType();
        }
        return null;
    }

    private Map<String, Object> parseAuthConfig(BuilderState state) {
        String authConfig = null;
        if (state.template() != null && StringUtils.hasText(state.template().getAuthConfig())) {
            authConfig = replaceVariables(state, state.template().getAuthConfig());
        } else if (state.fullTemplate() != null && StringUtils.hasText(state.fullTemplate().getAuthConfig())) {
            authConfig = replaceVariables(state, state.fullTemplate().getAuthConfig());
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

    private MultiValueMap<String, Object> buildFormDataBody(BuilderState state) {
        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        InterfaceTemplateVO currentTemplate = state.fullTemplate();
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
                    File file = new File(replaceVariables(state, item.getFilePath()));
                    // 只有文件路径真实存在时，才按文件资源上传。
                    if (file.exists() && file.isFile()) {
                        formData.add(fieldName, new FileSystemResource(file));
                        return;
                    }
                }
                formData.add(fieldName, replaceVariables(state, defaultString(item.getFieldValue())));
            });
        return formData;
    }

    private MultiValueMap<String, String> buildUrlEncodedBody(BuilderState state) {
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        InterfaceTemplateVO currentTemplate = state.fullTemplate();
        if (currentTemplate == null || CollectionUtils.isEmpty(currentTemplate.getFormDataList())) {
            return formData;
        }

        currentTemplate.getFormDataList().stream()
            .filter(item -> Integer.valueOf(1).equals(item.getIsEnabled()))
            .filter(item -> StringUtils.hasText(item.getFieldName()))
            .forEach(item -> formData.add(item.getFieldName().trim(),
                replaceVariables(state, defaultString(item.getFieldValue()))));
        return formData;
    }

    private String resolveBodyFromTemplateVariables(BuilderState state) {
        if (CollectionUtils.isEmpty(state.templateVariables())) {
            return null;
        }
        for (String key : List.of("body", "bodyContent", "requestBody")) {
            Object value = state.templateVariables().get(key);
            if (value instanceof String text && StringUtils.hasText(text)) {
                return replaceVariables(state, text);
            }
            if (value != null) {
                try {
                    // 非字符串变量会序列化为 JSON，便于直接传结构化对象。
                    return objectMapper.writeValueAsString(value);
                } catch (Exception e) {
                    log.warn("Failed to build request body from variable: {}", key, e);
                }
            }
        }
        Object dataJson = state.templateVariables().get("dataJson");
        if (dataJson != null) {
            try {
                Map<String, Object> requestBody = new LinkedHashMap<>();
                requestBody.put("dataJson", dataJson);
                Object remark = state.templateVariables().get("remark");
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

    private String replaceVariables(BuilderState state, String content) {
        if (!StringUtils.hasText(content) || state.variables() == null) {
            return content;
        }

        String result = content;
        for (Map.Entry<String, Object> entry : state.variables().entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String shorthandPlaceholder = "$" + entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            // 同时兼容 ${name} 和 $name 两种历史占位符写法。
            result = result.replace(placeholder, value);
            result = result.replace(shorthandPlaceholder, value);
        }
        return result;
    }

    private String resolveParameterValue(BuilderState state, String paramName, String configuredValue) {
        if (StringUtils.hasText(paramName) && state.variables() != null && state.variables().containsKey(paramName.trim())) {
            Object variableValue = state.variables().get(paramName.trim());
            return variableValue == null ? "" : String.valueOf(variableValue);
        }
        return replaceVariables(state, configuredValue);
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

    private String replacePathVariablesFromTemplateVariables(BuilderState state, String url) {
        if (!StringUtils.hasText(url) || CollectionUtils.isEmpty(state.templateVariables())) {
            return url;
        }
        String resolvedUrl = url;
        for (Map.Entry<String, Object> entry : state.templateVariables().entrySet()) {
            if (!StringUtils.hasText(entry.getKey())) {
                continue;
            }
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            resolvedUrl = replacePathParameter(resolvedUrl, entry.getKey(), value);
        }
        return resolvedUrl;
    }

    /**
     * 使用 UriComponentsBuilder 追加查询参数，避免手写字符串拼接。
     */
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

    /**
     * 内部状态载体，避免在各个辅助方法之间重复传递相同上下文。
     */
    private final class BuilderState {
        /**
         * 模板级变量，通常来自当前模板的运行时入参。
         */
        private final Map<String, Object> templateVariables;

        /**
         * 合并后的全量变量集合，包含全局、模板、局部变量。
         */
        private final Map<String, Object> variables;

        /**
         * 当前执行的模板基础信息。
         */
        private final InterfaceTemplate template;

        /**
         * 延迟加载的模板详情对象，包含请求头、参数、表单等完整配置。
         */
        private InterfaceTemplateVO fullTemplate;

        private BuilderState(TemplateContext context) {
            this.templateVariables = context.getTemplateVariables();
            this.variables = context.getAllVariables();
            this.template = context.getTemplate();
        }

        private Map<String, Object> templateVariables() {
            return templateVariables;
        }

        private Map<String, Object> variables() {
            return variables;
        }

        private InterfaceTemplate template() {
            return template;
        }

        private InterfaceTemplateVO fullTemplate() {
            if (fullTemplate == null && template != null && template.getId() != null) {
                // 模板详情按需懒加载，避免预览、校验时无意义查询完整模板结构。
                fullTemplate = templateService.getTemplateDetail(template.getId());
            }
            return fullTemplate;
        }
    }
}
