package com.example.tooltestingdemo.service.template.engine.interceptor;

import com.example.tooltestingdemo.constants.TemplateConstants;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.example.tooltestingdemo.vo.TemplatePreProcessorVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 前置处理器拦截器
 * 
 * 在执行请求前处理前置处理器，如设置变量、生成随机数、加密等
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@Order(200)
@RequiredArgsConstructor
public class PreProcessorInterceptor implements ExecutionInterceptor {

    private final ObjectMapper objectMapper;
    private final InterfaceTemplateService templateService;

    @Override
    public void beforeExecute(TemplateContext context) {
        log.debug("执行前置处理器拦截器");
        
        if (context.getTemplate() == null) {
            return;
        }
        
        // 获取请求配置，确认是否执行前置处理器
        if (context.getRequest() != null && !context.getRequest().isExecutePreProcessors()) {
            log.debug("跳过前置处理器执行");
            return;
        }
        
        // 加载模板的前置处理器
        InterfaceTemplateVO templateVO = templateService.getTemplateDetail(context.getTemplate().getId());
        if (templateVO == null || CollectionUtils.isEmpty(templateVO.getPreProcessors())) {
            return;
        }
        
        // 按顺序执行前置处理器
        List<TemplatePreProcessorVO> processors = templateVO.getPreProcessors().stream()
                .filter(p -> Integer.valueOf(1).equals(p.getIsEnabled()))
                .sorted(Comparator.comparing(TemplatePreProcessorVO::getSortOrder, 
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        
        for (TemplatePreProcessorVO processor : processors) {
            try {
                executePreProcessor(processor, context);
            } catch (Exception e) {
                log.error("前置处理器执行失败: processorId={}, name={}", 
                        processor.getId(), processor.getProcessorName(), e);
            }
        }
    }

    @Override
    public void afterExecute(TemplateContext context, ExecutionResult result) {
        // 前置处理器在执行后不需要操作
    }

    @Override
    public int getOrder() {
        return 200;
    }

    /**
     * 执行单个前置处理器
     */
    private void executePreProcessor(TemplatePreProcessorVO processor, TemplateContext context) {
        String processorType = processor.getProcessorType();
        String targetVariable = processor.getTargetVariable();
        
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
                result = UUID.randomUUID().toString().replace("-", "");
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
            context.setTemplateVariable(targetVariable, result);
            log.debug("前置处理器结果: {} = {}", targetVariable, result);
        }
    }

    // ==================== 处理器实现 ====================

    private Object executeSetVariable(String config) {
        if (!StringUtils.hasText(config)) {
            return null;
        }
        try {
            Map<String, Object> configMap = objectMapper.readValue(config,
                    new TypeReference<>() {
                    });
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
                Map<String, Object> configMap = objectMapper.readValue(config, 
                        new TypeReference<Map<String, Object>>() {});
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
                Map<String, Object> configMap = objectMapper.readValue(config, 
                        new TypeReference<Map<String, Object>>() {});
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

    private String base64Encode(String config, TemplateContext context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Decode(String config, TemplateContext context) {
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

    private String urlEncode(String config, TemplateContext context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String urlDecode(String config, TemplateContext context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String md5(String config, TemplateContext context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return hash(value, "MD5");
    }

    private String sha1(String config, TemplateContext context) {
        String value = extractValueFromConfig(config, context);
        if (value == null) {
            return null;
        }
        return hash(value, "SHA-1");
    }

    private String sha256(String config, TemplateContext context) {
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

    private Object executeJsScript(String script, TemplateContext context) {
        // TODO: 使用脚本引擎（如 GraalJS）执行 JavaScript
        log.warn("JavaScript脚本执行暂未实现");
        return null;
    }

    /**
     * 从配置中提取值
     */
    private String extractValueFromConfig(String config, TemplateContext context) {
        if (!StringUtils.hasText(config)) {
            return null;
        }
        
        try {
            Map<String, Object> configMap = objectMapper.readValue(config, 
                    new TypeReference<Map<String, Object>>() {});
            
            // 直接值
            Object value = configMap.get("value");
            if (value != null) {
                return value.toString();
            }
            
            // 从变量读取
            Object variableName = configMap.get("variable");
            if (variableName != null) {
                Object varValue = context.getVariable(variableName.toString());
                return varValue != null ? varValue.toString() : null;
            }
        } catch (Exception e) {
            // 如果解析失败，直接返回 config 作为值
            return config;
        }
        
        return config;
    }
}
