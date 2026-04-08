package com.example.tooltestingdemo.service.impl.template;

import com.example.tooltestingdemo.constants.TemplateConstants;
import com.example.tooltestingdemo.entity.template.TemplatePostProcessor;
import com.example.tooltestingdemo.entity.template.TemplatePreProcessor;
import com.example.tooltestingdemo.service.template.ProcessorExecuteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 前置/后置处理器执行 Service 实现类
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/impl/template/ProcessorExecuteServiceImpl.java
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessorExecuteServiceImpl implements ProcessorExecuteService {

    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> executePreProcessors(List<TemplatePreProcessor> processors, Map<String, Object> context) {
        if (CollectionUtils.isEmpty(processors) || context == null) {
            return context;
        }

        // 按执行顺序排序
        processors.stream()
                .filter(p -> Integer.valueOf(1).equals(p.getIsEnabled()))
                .sorted(Comparator.comparing(TemplatePreProcessor::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(processor -> {
                    try {
                        executePreProcessor(processor, context);
                    } catch (Exception e) {
                        log.error("执行前置处理器失败: processorId={}, name={}", processor.getId(), processor.getProcessorName(), e);
                    }
                });

        return context;
    }

    @Override
    public Map<String, Object> executePostProcessors(List<TemplatePostProcessor> processors, 
                                                      Map<String, Object> context,
                                                      Map<String, Object> response) {
        if (CollectionUtils.isEmpty(processors) || context == null) {
            return context;
        }

        // 按执行顺序排序
        processors.stream()
                .filter(p -> Integer.valueOf(1).equals(p.getIsEnabled()))
                .sorted(Comparator.comparing(TemplatePostProcessor::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(processor -> {
                    try {
                        executePostProcessor(processor, context, response);
                    } catch (Exception e) {
                        log.error("执行后置处理器失败: processorId={}, name={}", processor.getId(), processor.getProcessorName(), e);
                    }
                });

        return context;
    }

    @Override
    public Map<String, Object> executePreProcessor(TemplatePreProcessor processor, Map<String, Object> context) {
        if (processor == null || Integer.valueOf(0).equals(processor.getIsEnabled())) {
            return context;
        }

        String processorType = processor.getProcessorType();
        String targetVariable = processor.getTargetVariable();
        String variableScope = processor.getVariableScope();

        log.debug("执行前置处理器: type={}, name={}", processorType, processor.getProcessorName());

        Object result = null;

        switch (processorType) {
            case TemplateConstants.PRE_PROCESSOR_SET_VAR:
                result = executeSetVariable(processor.getConfig());
                break;
            case TemplateConstants.PRE_PROCESSOR_TIMESTAMP:
                result = System.currentTimeMillis();
                break;
            case TemplateConstants.PRE_PROCESSOR_RANDOM_STR:
                result = generateRandomString(processor.getConfig());
                break;
            case TemplateConstants.PRE_PROCESSOR_RANDOM_NUM:
                result = generateRandomNumber(processor.getConfig());
                break;
            case TemplateConstants.PRE_PROCESSOR_UUID:
                result = UUID.randomUUID().toString();
                break;
            case TemplateConstants.PRE_PROCESSOR_BASE64_ENC:
                result = base64Encode(processor.getConfig(), context);
                break;
            case TemplateConstants.PRE_PROCESSOR_BASE64_DEC:
                result = base64Decode(processor.getConfig(), context);
                break;
            case TemplateConstants.PRE_PROCESSOR_URL_ENC:
                result = urlEncode(processor.getConfig(), context);
                break;
            case TemplateConstants.PRE_PROCESSOR_URL_DEC:
                result = urlDecode(processor.getConfig(), context);
                break;
            case TemplateConstants.PRE_PROCESSOR_MD5:
                result = md5(processor.getConfig(), context);
                break;
            case TemplateConstants.PRE_PROCESSOR_SHA1:
                result = sha1(processor.getConfig(), context);
                break;
            case TemplateConstants.PRE_PROCESSOR_SHA256:
                result = sha256(processor.getConfig(), context);
                break;
            case TemplateConstants.PRE_PROCESSOR_JS_SCRIPT:
                result = executeJsScript(processor.getScriptContent(), context);
                break;
            default:
                log.warn("未知的前置处理器类型: {}", processorType);
                break;
        }

        // 将结果存入上下文
        if (StringUtils.hasText(targetVariable) && result != null) {
            String variableKey = buildVariableKey(targetVariable, variableScope);
            context.put(variableKey, result);
            log.debug("前置处理器结果: {} = {}", variableKey, result);
        }

        return context;
    }

    @Override
    public Map<String, Object> executePostProcessor(TemplatePostProcessor processor, 
                                                     Map<String, Object> context,
                                                     Map<String, Object> response) {
        if (processor == null || Integer.valueOf(0).equals(processor.getIsEnabled())) {
            return context;
        }

        String processorType = processor.getProcessorType();
        String targetVariable = processor.getTargetVariable();
        String variableScope = processor.getVariableScope();

        log.debug("执行后置处理器: type={}, name={}", processorType, processor.getProcessorName());

        Object result = null;

        switch (processorType) {
            case TemplateConstants.POST_PROCESSOR_JSON_EXTRACT:
                result = extractFromJson(response, processor.getExtractExpression());
                break;
            case TemplateConstants.POST_PROCESSOR_XML_EXTRACT:
                result = extractFromXml(response, processor.getExtractExpression());
                break;
            case TemplateConstants.POST_PROCESSOR_REGEX_EXTRACT:
                result = extractByRegex(response, processor.getExtractExpression(), processor.getExtractMatchNo());
                break;
            case TemplateConstants.POST_PROCESSOR_HEADER_EXTRACT:
                result = extractFromHeader(response, processor.getExtractExpression());
                break;
            case TemplateConstants.POST_PROCESSOR_COOKIE_EXTRACT:
                result = extractFromCookie(response, processor.getExtractExpression());
                break;
            default:
                log.warn("未知的后置处理器类型: {}", processorType);
                break;
        }

        // 如果提取失败，使用默认值
        if (result == null && StringUtils.hasText(processor.getDefaultValue())) {
            result = processor.getDefaultValue();
        }

        // 将结果存入上下文
        if (StringUtils.hasText(targetVariable) && result != null) {
            String variableKey = buildVariableKey(targetVariable, variableScope);
            context.put(variableKey, result);
            log.debug("后置处理器结果: {} = {}", variableKey, result);
        }

        return context;
    }

    // ==================== 前置处理器方法 ====================

    private Object executeSetVariable(String config) {
        if (!StringUtils.hasText(config)) {
            return null;
        }
        try {
            Map<String, Object> configMap = objectMapper.readValue(config, new TypeReference<Map<String, Object>>() {});
            return configMap.get("value");
        } catch (JsonProcessingException e) {
            log.error("解析设置变量配置失败", e);
            return config;
        }
    }

    private String generateRandomString(String config) {
        int length = 8;
        if (StringUtils.hasText(config)) {
            try {
                Map<String, Object> configMap = objectMapper.readValue(config, new TypeReference<Map<String, Object>>() {});
                if (configMap.get("length") != null) {
                    length = Integer.parseInt(configMap.get("length").toString());
                }
            } catch (Exception e) {
                log.error("解析随机字符串配置失败", e);
            }
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private Long generateRandomNumber(String config) {
        int min = 0;
        int max = 999999;
        
        if (StringUtils.hasText(config)) {
            try {
                Map<String, Object> configMap = objectMapper.readValue(config, new TypeReference<Map<String, Object>>() {});
                if (configMap.get("min") != null) {
                    min = Integer.parseInt(configMap.get("min").toString());
                }
                if (configMap.get("max") != null) {
                    max = Integer.parseInt(configMap.get("max").toString());
                }
            } catch (Exception e) {
                log.error("解析随机数配置失败", e);
            }
        }
        
        return (long) (min + Math.random() * (max - min + 1));
    }

    private String base64Encode(String config, Map<String, Object> context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Decode(String config, Map<String, Object> context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Base64解码失败", e);
            return null;
        }
    }

    private String urlEncode(String config, Map<String, Object> context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String urlDecode(String config, Map<String, Object> context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String md5(String config, Map<String, Object> context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return hash(value, "MD5");
    }

    private String sha1(String config, Map<String, Object> context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return hash(value, "SHA-1");
    }

    private String sha256(String config, Map<String, Object> context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return hash(value, "SHA-256");
    }

    private String hash(String value, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("不支持算法: {}", algorithm, e);
            return null;
        }
    }

    private Object executeJsScript(String script, Map<String, Object> context) {
        // TODO: 使用脚本引擎（如Nashorn、GraalJS）执行JavaScript
        log.warn("JavaScript脚本执行暂未实现");
        return null;
    }

    // ==================== 后置处理器方法 ====================

    private Object extractFromJson(Map<String, Object> response, String expression) {
        if (response == null || !StringUtils.hasText(expression)) {
            return null;
        }
        
        Object body = response.get("body");
        if (body == null) {
            return null;
        }

        try {
            // 简单实现：使用点号分隔的路径提取
            // 例如：data.user.name
            String jsonStr = body.toString();
            Map<String, Object> jsonMap = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            
            String[] parts = expression.split("\\.");
            Object current = jsonMap;
            
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else {
                    return null;
                }
            }
            
            return current;
        } catch (Exception e) {
            log.error("JSON提取失败", e);
            return null;
        }
    }

    private Object extractFromXml(Map<String, Object> response, String expression) {
        // TODO: 使用XPath解析XML
        log.warn("XML提取暂未实现");
        return null;
    }

    private Object extractByRegex(Map<String, Object> response, String expression, Integer matchNo) {
        if (response == null || !StringUtils.hasText(expression)) {
            return null;
        }
        
        Object body = response.get("body");
        if (body == null) {
            return null;
        }

        try {
            Pattern pattern = Pattern.compile(expression);
            Matcher matcher = pattern.matcher(body.toString());
            
            int matchIndex = matchNo != null && matchNo > 0 ? matchNo : 1;
            int currentIndex = 0;
            
            while (matcher.find()) {
                currentIndex++;
                if (currentIndex == matchIndex) {
                    if (matcher.groupCount() > 0) {
                        return matcher.group(1);
                    }
                    return matcher.group();
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("正则提取失败", e);
            return null;
        }
    }

    private Object extractFromHeader(Map<String, Object> response, String headerName) {
        if (response == null || !StringUtils.hasText(headerName)) {
            return null;
        }
        
        Object headers = response.get("headers");
        if (headers instanceof Map) {
            return ((Map<?, ?>) headers).get(headerName);
        }
        return null;
    }

    private Object extractFromCookie(Map<String, Object> response, String cookieName) {
        if (response == null || !StringUtils.hasText(cookieName)) {
            return null;
        }
        
        Object cookies = response.get("cookies");
        if (cookies instanceof Map) {
            return ((Map<?, ?>) cookies).get(cookieName);
        }
        return null;
    }

    // ==================== 工具方法 ====================

    private String extractValueFromConfig(String config, Map<String, Object> context) {
        if (!StringUtils.hasText(config)) {
            return null;
        }
        
        try {
            Map<String, Object> configMap = objectMapper.readValue(config, new TypeReference<Map<String, Object>>() {});
            Object value = configMap.get("value");
            if (value != null) {
                return value.toString();
            }
            
            // 支持从上下文变量读取
            Object variableName = configMap.get("variable");
            if (variableName != null) {
                Object varValue = context.get(variableName.toString());
                return varValue != null ? varValue.toString() : null;
            }
        } catch (Exception e) {
            // 如果解析失败，直接返回config作为值
            return config;
        }
        
        return config;
    }

    private String buildVariableKey(String variableName, String scope) {
        if (!StringUtils.hasText(scope)) {
            scope = TemplateConstants.SCOPE_TEMPLATE;
        }
        return scope + "." + variableName;
    }
}
