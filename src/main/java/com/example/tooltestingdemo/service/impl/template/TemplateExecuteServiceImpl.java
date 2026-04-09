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

    /**
     * 执行模板请求
     * 
     * <p>完整执行流程：</p>
     * <ol>
     *   <li>获取模板详情，验证模板存在性</li>
     *   <li>初始化执行上下文，合并传入变量</li>
     *   <li>加载环境变量（如果指定了环境ID）</li>
     *   <li>执行前置处理器（脚本、变量提取等）</li>
     *   <li>构建HTTP请求（URL、Header、Body等）</li>
     *   <li>发送HTTP请求并获取响应</li>
     *   <li>执行后置处理器（响应提取、变量赋值等）</li>
     *   <li>组装并返回执行结果</li>
     * </ol>
     *
     * @param templateId    模板ID，必填
     * @param environmentId 环境ID，可选，用于加载环境变量
     * @param variables     执行变量，可选，支持 ${variableName} 占位符替换
     * @return 执行结果Map，包含：
     *         - templateId: 模板ID
     *         - templateName: 模板名称
     *         - request: 请求信息（url、method、headers、body等）
     *         - response: 响应信息（statusCode、headers、body、responseTime等）
     *         - variables: 执行后的变量上下文
     * @throws RuntimeException 当模板不存在时抛出
     */
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

    /**
     * 执行模板请求（使用默认环境）
     * 
     * <p>自动获取该模板关联的默认环境，然后调用带环境ID的executeTemplate方法</p>
     *
     * @param templateId 模板ID，必填
     * @param variables  执行变量，可选
     * @return 执行结果Map，结构同 {@link #executeTemplate(Long, Long, Map)}
     * @throws RuntimeException 当模板不存在时抛出
     */
    @Override
    public Map<String, Object> executeTemplate(Long templateId, Map<String, Object> variables) {
        // 获取默认环境
        TemplateEnvironmentVO defaultEnv = environmentService.getDefaultEnvironment(templateId);
        Long environmentId = defaultEnv != null ? defaultEnv.getId() : null;
        return executeTemplate(templateId, environmentId, variables);
    }

    /**
     * 验证模板配置是否正确
     * 
     * <p>验证内容包括：</p>
     * <ul>
     *   <li>必填字段：模板名称、协议类型、请求方法、请求路径</li>
     *   <li>URL可构建性</li>
     *   <li>请求头配置（警告级别）</li>
     *   <li>断言配置（警告级别）</li>
     * </ul>
     *
     * @param templateId 模板ID，必填
     * @return 验证结果Map，包含：
     *         - valid: Boolean，是否验证通过（无errors时为true）
     *         - errors: List<String>，错误信息列表
     *         - warnings: List<String>，警告信息列表
     * @throws RuntimeException 当模板不存在时抛出
     */
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

    /**
     * 预览请求（生成最终请求内容，但不发送）
     * 
     * <p>用于在执行前查看变量替换后的最终请求内容，便于调试</p>
     * 
     * <p>处理流程：</p>
     * <ol>
     *   <li>获取模板详情</li>
     *   <li>加载环境变量（如果指定了环境ID）</li>
     *   <li>执行前置处理器（修改变量上下文）</li>
     *   <li>构建请求（变量替换后的最终内容）</li>
     * </ol>
     *
     * @param templateId    模板ID，必填
     * @param environmentId 环境ID，可选
     * @param variables     执行变量，可选
     * @return 请求预览Map，包含：
     *         - url: 完整的请求URL（变量已替换）
     *         - method: HTTP方法
     *         - headers: 请求头Map
     *         - body: 请求体（变量已替换）
     *         - parameters: URL参数Map
     * @throws RuntimeException 当模板不存在时抛出
     */
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

    /**
     * 构建HTTP请求
     * 
     * <p>根据模板配置和执行上下文构建完整的HTTP请求信息，包括：</p>
     * <ul>
     *   <li>URL：替换变量后的完整URL</li>
     *   <li>Method：HTTP方法（GET/POST/PUT/DELETE等）</li>
     *   <li>Headers：启用的请求头，值中的变量会被替换</li>
     *   <li>Body：请求体，变量会被替换</li>
     *   <li>Parameters：启用的URL参数，值中的变量会被替换</li>
     * </ul>
     *
     * @param template 模板VO对象
     * @param context  执行上下文（变量Map）
     * @return 请求信息Map
     */
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

    /**
     * 发送HTTP请求
     * 
     * <p>使用RestTemplate发送实际HTTP请求，记录响应信息</p>
     *
     * @param template   模板VO对象（用于日志记录）
     * @param requestInfo 请求信息Map（包含url、method、headers、body）
     * @return 响应信息Map，包含：
     *         - statusCode: HTTP状态码
     *         - statusText: 状态描述
     *         - headers: 响应头
     *         - body: 响应体（尝试解析为JSON，失败则返回原始字符串）
     *         - responseTime: 响应时间（毫秒）
     *         - error: 错误信息（请求失败时）
     */
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

    /**
     * 构建完整的请求URL
     * 
     * <p>拼接规则：</p>
     * <ul>
     *   <li>优先使用模板中存储的fullUrl</li>
     *   <li>否则拼接：protocol + baseUrl + path</li>
     *   <li>自动处理协议头（http:// 或 https://）</li>
     *   <li>自动处理斜杠重复问题</li>
     * </ul>
     *
     * @param template 模板VO对象
     * @return 完整的请求URL
     */
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

    /**
     * 替换字符串中的变量占位符
     * 
     * <p>支持 ${variableName} 格式的占位符替换</p>
     *
     * @param content 原始字符串
     * @param context 变量上下文Map
     * @return 替换后的字符串
     */
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

    /**
     * 从JSON字符串加载环境变量到上下文
     * 
     * <p>解析简单的JSON对象格式：{"key1": "value1", "key2": "value2"}</p>
     *
     * @param context       执行上下文Map（会被修改）
     * @param variablesJson 环境变量JSON字符串
     */
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

    /**
     * 将VO列表转换为前置处理器实体列表
     *
     * @param voList 前置处理器VO列表
     * @return 前置处理器实体列表
     */
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

    /**
     * 将VO列表转换为后置处理器实体列表
     *
     * @param voList 后置处理器VO列表
     * @return 后置处理器实体列表
     */
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
