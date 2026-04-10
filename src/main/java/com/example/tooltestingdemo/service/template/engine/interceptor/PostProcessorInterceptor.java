package com.example.tooltestingdemo.service.template.engine.interceptor;

import com.example.tooltestingdemo.constants.TemplateConstants;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.example.tooltestingdemo.vo.TemplatePostProcessorVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 后置处理器拦截器
 * 
 * 在请求执行后处理响应数据，如 JSON 提取、正则提取等
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@Order(400)
@RequiredArgsConstructor
public class PostProcessorInterceptor implements ExecutionInterceptor {

    private final ObjectMapper objectMapper;
    private final InterfaceTemplateService templateService;

    @Override
    public void beforeExecute(TemplateContext context) {
        // 后置处理器在执行前不需要操作
    }

    @Override
    public void afterExecute(TemplateContext context, ExecutionResult result) {
        log.debug("执行后置处理器拦截器");
        
        if (context.getTemplate() == null || result == null) {
            return;
        }
        
        // 获取请求配置，确认是否执行后置处理器
        if (context.getRequest() != null && !context.getRequest().isExecutePostProcessors()) {
            log.debug("跳过后置处理器执行");
            return;
        }
        
        // 加载模板的后置处理器
        InterfaceTemplateVO templateVO = templateService.getTemplateDetail(context.getTemplate().getId());
        if (templateVO == null || CollectionUtils.isEmpty(templateVO.getPostProcessors())) {
            return;
        }
        
        // 按顺序执行后置处理器
        List<TemplatePostProcessorVO> processors = templateVO.getPostProcessors().stream()
                .filter(p -> Integer.valueOf(1).equals(p.getIsEnabled()))
                .sorted(Comparator.comparing(TemplatePostProcessorVO::getSortOrder, 
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        
        // 构建响应数据 Map
        Map<String, Object> responseData = buildResponseData(result);
        
        for (TemplatePostProcessorVO processor : processors) {
            try {
                executePostProcessor(processor, context, responseData);
            } catch (Exception e) {
                log.error("后置处理器执行失败: processorId={}, name={}", 
                        processor.getId(), processor.getProcessorName(), e);
            }
        }
        
        // 将提取的变量同步到结果中
        result.setVariables(context.getAllVariables());
    }

    @Override
    public int getOrder() {
        return 400;
    }

    /**
     * 构建响应数据 Map
     */
    private Map<String, Object> buildResponseData(ExecutionResult result) {
        Map<String, Object> responseData = new HashMap<>();
        
        if (result.getResponse() != null) {
            responseData.put("statusCode", result.getResponse().getStatusCode());
            responseData.put("statusText", result.getResponse().getStatusText());
            responseData.put("headers", result.getResponse().getHeaders());
            responseData.put("body", result.getResponse().getBody());
            responseData.put("responseTime", result.getResponse().getResponseTime());
            responseData.put("size", result.getResponse().getSize());
        }
        
        return responseData;
    }

    /**
     * 执行单个后置处理器
     */
    private void executePostProcessor(TemplatePostProcessorVO processor, 
                                       TemplateContext context,
                                       Map<String, Object> responseData) {
        String processorType = processor.getProcessorType();
        String targetVariable = processor.getTargetVariable();
        String defaultValue = processor.getDefaultValue();
        
        log.debug("执行后置处理器: type={}, name={}", processorType, processor.getProcessorName());
        
        Object result = null;
        
        switch (processorType) {
            case TemplateConstants.POST_PROCESSOR_JSON_EXTRACT:
                result = extractFromJson(responseData, processor.getExtractExpression());
                break;
            case TemplateConstants.POST_PROCESSOR_XML_EXTRACT:
                result = extractFromXml(responseData, processor.getExtractExpression());
                break;
            case TemplateConstants.POST_PROCESSOR_REGEX_EXTRACT:
                result = extractByRegex(responseData, processor.getExtractExpression(), 
                        processor.getExtractMatchNo());
                break;
            case TemplateConstants.POST_PROCESSOR_HEADER_EXTRACT:
                result = extractFromHeader(responseData, processor.getExtractExpression());
                break;
            case TemplateConstants.POST_PROCESSOR_COOKIE_EXTRACT:
                result = extractFromCookie(responseData, processor.getExtractExpression());
                break;
            default:
                log.warn("未知的后置处理器类型: {}", processorType);
                break;
        }
        
        // 如果提取失败，使用默认值
        if (result == null && StringUtils.hasText(defaultValue)) {
            result = defaultValue;
        }
        
        // 将结果存入上下文
        if (StringUtils.hasText(targetVariable) && result != null) {
            String variableScope = processor.getVariableScope();
            if ("GLOBAL".equalsIgnoreCase(variableScope)) {
                context.setGlobalVariable(targetVariable, result);
            } else if ("LOCAL".equalsIgnoreCase(variableScope)) {
                context.setLocalVariable(targetVariable, result);
            } else {
                context.setTemplateVariable(targetVariable, result);
            }
            log.debug("后置处理器结果: {} = {}", targetVariable, result);
        }
    }

    // ==================== 提取方法 ====================

    /**
     * 从 JSON 响应中提取数据
     * 
     * 支持 JSONPath 语法简化版：data.user.name
     */
    @SuppressWarnings("unchecked")
    private Object extractFromJson(Map<String, Object> responseData, String expression) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        
        Object body = responseData.get("body");
        if (body == null) {
            return null;
        }

        try {
            // body 可能是 Map（已解析的JSON）或 String（原始JSON）
            Map<String, Object> jsonMap;
            if (body instanceof Map) {
                jsonMap = (Map<String, Object>) body;
            } else {
                // 解析 JSON 字符串
                String jsonStr = body.toString();
                jsonMap = objectMapper.readValue(jsonStr, 
                        new TypeReference<Map<String, Object>>() {});
            }
            
            // 使用点号分隔的路径提取，例如：data.user.name
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
            log.error("JSON提取失败: expression={}, body={}", expression, body, e);
            return null;
        }
    }

    /**
     * 从 XML 响应中提取数据（使用 XPath）
     * 
     * TODO: 需要引入 XPath 解析库
     */
    private Object extractFromXml(Map<String, Object> responseData, String expression) {
        // TODO: 使用 javax.xml.xpath 或第三方库解析 XML
        log.warn("XML提取暂未实现");
        return null;
    }

    /**
     * 使用正则表达式提取数据
     */
    private Object extractByRegex(Map<String, Object> responseData, String expression, Integer matchNo) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        
        Object body = responseData.get("body");
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

    /**
     * 从响应头中提取数据
     */
    @SuppressWarnings("unchecked")
    private Object extractFromHeader(Map<String, Object> responseData, String headerName) {
        if (!StringUtils.hasText(headerName)) {
            return null;
        }
        
        Object headers = responseData.get("headers");
        if (headers instanceof Map) {
            return ((Map<?, ?>) headers).get(headerName);
        }
        return null;
    }

    /**
     * 从 Cookie 中提取数据
     */
    private Object extractFromCookie(Map<String, Object> responseData, String cookieName) {
        if (!StringUtils.hasText(cookieName)) {
            return null;
        }
        
        // 从 Set-Cookie 或 Cookie 头中提取
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) responseData.get("headers");
        if (headers == null) {
            return null;
        }
        
        String setCookie = headers.get("Set-Cookie");
        if (setCookie == null) {
            setCookie = headers.get("set-cookie");
        }
        
        if (setCookie == null) {
            return null;
        }
        
        // 解析 Cookie 字符串
        String[] cookies = setCookie.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && parts[0].trim().equals(cookieName)) {
                return parts[1].trim();
            }
        }
        
        return null;
    }
}
