package com.example.tooltestingdemo.service.template.engine.interceptor;

import com.example.tooltestingdemo.constants.TemplateConstants;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.example.tooltestingdemo.vo.TemplateAssertionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 断言校验拦截器
 * 
 * 在请求执行后进行断言校验，验证响应是否符合预期
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@Order(500)
@RequiredArgsConstructor
public class AssertionInterceptor implements ExecutionInterceptor {

    private final InterfaceTemplateService templateService;
    private final ObjectMapper objectMapper;

    @Override
    public void beforeExecute(TemplateContext context) {
        // 断言校验在执行后处理
    }

    @Override
    public void afterExecute(TemplateContext context, ExecutionResult result) {
        log.debug("执行断言校验拦截器");
        
        if (context.getTemplate() == null || result == null) {
            return;
        }
        
        // 获取请求配置，确认是否执行断言
        if (context.getRequest() != null && !context.getRequest().isExecuteAssertions()) {
            log.debug("跳过断言执行");
            return;
        }
        
        // 加载模板的断言
        InterfaceTemplateVO templateVO = templateService.getTemplateDetail(context.getTemplate().getId());
        if (templateVO == null || CollectionUtils.isEmpty(templateVO.getAssertions())) {
            return;
        }
        
        // 获取启用的断言
        List<TemplateAssertionVO> assertions = templateVO.getAssertions().stream()
                .filter(a -> Integer.valueOf(1).equals(a.getIsEnabled()))
                .sorted(java.util.Comparator.comparing(TemplateAssertionVO::getSortOrder, 
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .toList();
        
        if (assertions.isEmpty()) {
            return;
        }
        
        // 构建响应数据
        Map<String, Object> responseData = buildResponseData(result);
        
        // 执行断言校验
        List<ExecutionResult.AssertionResult> assertionResults = new ArrayList<>();
        boolean allPassed = true;
        
        for (TemplateAssertionVO assertion : assertions) {
            ExecutionResult.AssertionResult assertionResult = executeAssertion(assertion, responseData);
            assertionResults.add(assertionResult);
            
            if (!assertionResult.isPassed()) {
                allPassed = false;
            }
        }
        
        // 设置断言结果到执行结果
        result.setAssertions(assertionResults);
        
        // 更新整体执行状态（如果断言失败）
        if (!allPassed) {
            result.setSuccess(false);
            result.setMessage("断言校验失败");
            log.warn("模板 {} 断言校验失败，共 {} 个断言未通过", 
                    context.getTemplate().getName(), 
                    assertionResults.stream().filter(a -> !a.isPassed()).count());
        } else {
            log.debug("所有断言校验通过");
        }
    }

    @Override
    public int getOrder() {
        return 500;
    }

    /**
     * 构建响应数据
     */
    private Map<String, Object> buildResponseData(ExecutionResult result) {
        Map<String, Object> responseData = new java.util.HashMap<>();
        
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
     * 执行单个断言
     */
    private ExecutionResult.AssertionResult executeAssertion(TemplateAssertionVO assertion, 
                                                               Map<String, Object> responseData) {
        String assertType = assertion.getAssertType();
        String expectedValue = assertion.getExpectedValue();
        String operator = assertion.getOperator();
        String extractPath = assertion.getExtractPath();
        
        log.debug("执行断言: type={}, name={}", assertType, assertion.getAssertName());
        
        Object actualValue = null;
        boolean passed = false;
        String errorMessage = null;
        
        try {
            // 提取实际值
            actualValue = extractActualValue(assertType, extractPath, responseData);
            
            // 执行比较
            passed = compareValues(actualValue, expectedValue, operator);
            
            if (!passed) {
                errorMessage = String.format("期望值: %s, 实际值: %s", expectedValue, actualValue);
            }
            
        } catch (Exception e) {
            passed = false;
            errorMessage = "断言执行异常: " + e.getMessage();
            log.error("断言执行异常: {}", assertion.getAssertName(), e);
        }
        
        return ExecutionResult.AssertionResult.builder()
                .name(assertion.getAssertName())
                .passed(passed)
                .type(assertType)
                .actualValue(actualValue)
                .expectedValue(expectedValue)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 提取实际值
     */
    @SuppressWarnings("unchecked")
    private Object extractActualValue(String assertType, String extractPath, 
                                       Map<String, Object> responseData) {
        switch (assertType) {
            case TemplateConstants.ASSERT_STATUS_CODE:
                return responseData.get("statusCode");
                
            case TemplateConstants.ASSERT_STATUS_MESSAGE:
                return responseData.get("statusText");
                
            case TemplateConstants.ASSERT_RESPONSE_HEADER:
                if (!StringUtils.hasText(extractPath)) {
                    return null;
                }
                Map<String, String> headers = (Map<String, String>) responseData.get("headers");
                return headers != null ? headers.get(extractPath) : null;
                
            case TemplateConstants.ASSERT_RESPONSE_BODY:
                return responseData.get("body");
                
            case TemplateConstants.ASSERT_RESPONSE_TIME:
                return responseData.get("responseTime");
                
            case TemplateConstants.ASSERT_RESPONSE_SIZE:
                return responseData.get("size");
                
            case TemplateConstants.ASSERT_JSON_PATH:
                return extractJsonPath(responseData.get("body"), extractPath);
                
            case TemplateConstants.ASSERT_REGEX:
                return responseData.get("body");
                
            case TemplateConstants.ASSERT_CONTAINS:
                Object body = responseData.get("body");
                return body != null ? body.toString() : null;
                
            default:
                return null;
        }
    }

    /**
     * 从 JSON 中提取路径值
     */
    @SuppressWarnings("unchecked")
    private Object extractJsonPath(Object body, String path) {
        if (!StringUtils.hasText(path) || body == null) {
            return null;
        }
        
        try {
            Map<String, Object> jsonMap;
            if (body instanceof Map) {
                jsonMap = (Map<String, Object>) body;
            } else {
                jsonMap = objectMapper.readValue(body.toString(), 
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            }
            
            String[] parts = path.split("\\.");
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
            log.error("JSON路径提取失败: path={}", path, e);
            return null;
        }
    }

    /**
     * 比较值
     */
    private boolean compareValues(Object actual, Object expected, String operator) {
        if (operator == null) {
            operator = TemplateConstants.ASSERT_EQUALS;
        }
        
        switch (operator.toUpperCase()) {
            case "EQUALS":
            case "=":
            case "==":
                return Objects.equals(actual != null ? actual.toString() : null,
                        expected != null ? expected.toString() : null);
                
            case "NOT_EQUALS":
            case "!=":
            case "<>":
                return !Objects.equals(actual != null ? actual.toString() : null,
                        expected != null ? expected.toString() : null);
                
            case "CONTAINS":
                if (actual == null || expected == null) {
                    return false;
                }
                return actual.toString().contains(expected.toString());
                
            case "NOT_CONTAINS":
                if (actual == null || expected == null) {
                    return false;
                }
                return !actual.toString().contains(expected.toString());
                
            case "STARTS_WITH":
                if (actual == null || expected == null) {
                    return false;
                }
                return actual.toString().startsWith(expected.toString());
                
            case "ENDS_WITH":
                if (actual == null || expected == null) {
                    return false;
                }
                return actual.toString().endsWith(expected.toString());
                
            case "MATCHES":
            case "REGEX":
                if (actual == null || expected == null) {
                    return false;
                }
                return Pattern.matches(expected.toString(), actual.toString());
                
            case "GREATER_THAN":
            case ">":
                return compareNumbers(actual, expected) > 0;
                
            case "GREATER_THAN_OR_EQUALS":
            case ">=":
                return compareNumbers(actual, expected) >= 0;
                
            case "LESS_THAN":
            case "<":
                return compareNumbers(actual, expected) < 0;
                
            case "LESS_THAN_OR_EQUALS":
            case "<=":
                return compareNumbers(actual, expected) <= 0;
                
            case "IS_NULL":
                return actual == null;
                
            case "IS_NOT_NULL":
                return actual != null;
                
            case "IS_EMPTY":
                return actual == null || actual.toString().isEmpty();
                
            case "IS_NOT_EMPTY":
                return actual != null && !actual.toString().isEmpty();
                
            default:
                return Objects.equals(actual, expected);
        }
    }

    /**
     * 比较数字
     * @return 1: actual > expected, 0: equal, -1: actual < expected
     */
    private int compareNumbers(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return actual == expected ? 0 : (actual == null ? -1 : 1);
        }
        
        try {
            double actualNum = Double.parseDouble(actual.toString());
            double expectedNum = Double.parseDouble(expected.toString());
            return Double.compare(actualNum, expectedNum);
        } catch (NumberFormatException e) {
            // 不是数字，按字符串比较
            return actual.toString().compareTo(expected.toString());
        }
    }
}
