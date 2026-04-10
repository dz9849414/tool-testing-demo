package com.example.tooltestingdemo.template;

import com.example.tooltestingdemo.entity.template.*;
import com.example.tooltestingdemo.mapper.template.*;
import com.example.tooltestingdemo.service.template.TemplateExecuteService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模板执行引擎测试类
 * 
 * 使用 JUnit 5 测试模板执行引擎的完整功能
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback(false) // 测试后不回滚，方便查看数据
public class TemplateExecuteEngineTest {

    @Autowired
    private InterfaceTemplateMapper templateMapper;
    
    @Autowired
    private TemplatePreProcessorMapper preProcessorMapper;
    
    @Autowired
    private TemplateAssertionMapper assertionMapper;
    
    @Autowired
    private TemplatePostProcessorMapper postProcessorMapper;
    
    @Autowired
    private TemplateHeaderMapper headerMapper;
    
    @Autowired
    private TemplateExecuteService executeService;

    private static Long testTemplateId;

    // ==================== 测试：创建模板 ====================

    @Test
    @Order(1)
    @DisplayName("步骤1：创建POST请求模板")
    void testCreateTemplate() {
        log.info("========================================");
        log.info("【步骤1】创建POST请求模板");
        log.info("========================================");
        
        InterfaceTemplate template = new InterfaceTemplate();
        template.setFolderId(1L);
        template.setName("用户登录接口-测试-" + System.currentTimeMillis());
        template.setDescription("JUnit测试模板：包含前置处理、断言和后置处理");
        template.setProtocolType("HTTP");
        template.setMethod("POST");
        template.setBaseUrl("https://httpbin.org");
        template.setPath("/post");
        template.setFullUrl("https://httpbin.org/post");
        template.setContentType("application/json");
        template.setBodyType("RAW");
        template.setBodyRawType("JSON");
        
        // 请求体示例（使用变量）
        String bodyContent = """
            {
              "username": "testuser",
              "password": "123456",
              "timestamp": "${timestamp}",
              "requestId": "${requestId}",
              "deviceId": "${deviceId}",
              "randomCode": ${randomCode}
            }
            """;
        template.setBodyContent(bodyContent);
        
        template.setConnectTimeout(30000);
        template.setReadTimeout(30000);
        template.setStatus(1);
        template.setVisibility(1);
        template.setIsLatest(1);
        template.setIsDeleted(0);
        
        templateMapper.insert(template);
        testTemplateId = template.getId();
        
        log.info("✓ 模板创建成功，ID: {}", testTemplateId);
        assertNotNull(testTemplateId, "模板ID不应为空");
        assertTrue(testTemplateId > 0, "模板ID应大于0");
    }

    @Test
    @Order(2)
    @DisplayName("步骤2：创建请求头")
    void testCreateHeaders() {
        log.info("========================================");
        log.info("【步骤2】创建请求头");
        log.info("========================================");
        
        List<TemplateHeader> headers = new ArrayList<>();
        
        // Content-Type
        TemplateHeader header1 = new TemplateHeader();
        header1.setTemplateId(testTemplateId);
        header1.setHeaderName("Content-Type");
        header1.setHeaderValue("application/json");
        header1.setIsEnabled(1);
        header1.setSortOrder(1);
        headers.add(header1);
        
        // Accept
        TemplateHeader header2 = new TemplateHeader();
        header2.setTemplateId(testTemplateId);
        header2.setHeaderName("Accept");
        header2.setHeaderValue("application/json");
        header2.setIsEnabled(1);
        header2.setSortOrder(2);
        headers.add(header2);
        
        // X-Request-ID（使用变量）
        TemplateHeader header3 = new TemplateHeader();
        header3.setTemplateId(testTemplateId);
        header3.setHeaderName("X-Request-ID");
        header3.setHeaderValue("${requestId}");
        header3.setIsEnabled(1);
        header3.setSortOrder(3);
        headers.add(header3);
        
        int count = headerMapper.batchInsert(headers);
        
        log.info("✓ 请求头创建成功，数量: {}", count);
        assertEquals(3, count, "应创建3个请求头");
    }

    @Test
    @Order(3)
    @DisplayName("步骤3：创建前置处理器")
    void testCreatePreProcessors() {
        log.info("========================================");
        log.info("【步骤3】创建前置处理器");
        log.info("========================================");
        
        List<TemplatePreProcessor> processors = new ArrayList<>();
        
        // 1. 生成时间戳
        TemplatePreProcessor p1 = new TemplatePreProcessor();
        p1.setTemplateId(testTemplateId);
        p1.setProcessorName("生成时间戳");
        p1.setProcessorType("TIMESTAMP");
        p1.setTargetVariable("timestamp");
        p1.setVariableScope("TEMPLATE");
        p1.setIsEnabled(1);
        p1.setSortOrder(1);
        processors.add(p1);
        log.info("  - 添加处理器: TIMESTAMP → timestamp");
        
        // 2. 生成 UUID
        TemplatePreProcessor p2 = new TemplatePreProcessor();
        p2.setTemplateId(testTemplateId);
        p2.setProcessorName("生成UUID");
        p2.setProcessorType("RANDOM_UUID");
        p2.setTargetVariable("requestId");
        p2.setVariableScope("TEMPLATE");
        p2.setIsEnabled(1);
        p2.setSortOrder(2);
        processors.add(p2);
        log.info("  - 添加处理器: RANDOM_UUID → requestId");
        
        // 3. 生成设备ID
        TemplatePreProcessor p3 = new TemplatePreProcessor();
        p3.setTemplateId(testTemplateId);
        p3.setProcessorName("生成设备ID");
        p3.setProcessorType("RANDOM_STRING");
        p3.setConfig("{\"length\": 16}");
        p3.setTargetVariable("deviceId");
        p3.setVariableScope("TEMPLATE");
        p3.setIsEnabled(1);
        p3.setSortOrder(3);
        processors.add(p3);
        log.info("  - 添加处理器: RANDOM_STRING(16) → deviceId");
        
        // 4. 生成随机数
        TemplatePreProcessor p4 = new TemplatePreProcessor();
        p4.setTemplateId(testTemplateId);
        p4.setProcessorName("生成随机数");
        p4.setProcessorType("RANDOM_NUMBER");
        p4.setConfig("{\"min\": 1000, \"max\": 9999}");
        p4.setTargetVariable("randomCode");
        p4.setVariableScope("TEMPLATE");
        p4.setIsEnabled(1);
        p4.setSortOrder(4);
        processors.add(p4);
        log.info("  - 添加处理器: RANDOM_NUMBER(1000-9999) → randomCode");
        
        int count = preProcessorMapper.batchInsert(processors);
        
        log.info("✓ 前置处理器创建成功，数量: {}", count);
        assertEquals(4, count, "应创建4个前置处理器");
    }

    @Test
    @Order(4)
    @DisplayName("步骤4：创建断言")
    void testCreateAssertions() {
        log.info("========================================");
        log.info("【步骤4】创建断言");
        log.info("========================================");
        
        List<TemplateAssertion> assertions = new ArrayList<>();
        
        // 1. 验证状态码为 200
        TemplateAssertion a1 = new TemplateAssertion();
        a1.setTemplateId(testTemplateId);
        a1.setAssertName("状态码为200");
        a1.setAssertType("STATUS_CODE");
        a1.setExpectedValue("200");
        a1.setOperator("EQUALS");
        a1.setIsEnabled(1);
        a1.setSortOrder(1);
        assertions.add(a1);
        log.info("  - 添加断言: STATUS_CODE == 200");
        
        // 2. 验证响应时间小于 5000ms
        TemplateAssertion a2 = new TemplateAssertion();
        a2.setTemplateId(testTemplateId);
        a2.setAssertName("响应时间小于5秒");
        a2.setAssertType("RESPONSE_TIME");
        a2.setExpectedValue("5000");
        a2.setOperator("LESS_THAN");
        a2.setIsEnabled(1);
        a2.setSortOrder(2);
        assertions.add(a2);
        log.info("  - 添加断言: RESPONSE_TIME < 5000ms");
        
        // 3. 验证响应体包含 url 字段
        TemplateAssertion a3 = new TemplateAssertion();
        a3.setTemplateId(testTemplateId);
        a3.setAssertName("响应包含url字段");
        a3.setAssertType("CONTAINS");
        a3.setExpectedValue("url");
        a3.setOperator("CONTAINS");
        a3.setIsEnabled(1);
        a3.setSortOrder(3);
        assertions.add(a3);
        log.info("  - 添加断言: body CONTAINS 'url'");
        
        int count = assertionMapper.batchInsert(assertions);
        
        log.info("✓ 断言创建成功，数量: {}", count);
        assertEquals(3, count, "应创建3个断言");
    }

    @Test
    @Order(5)
    @DisplayName("步骤5：创建后置处理器")
    void testCreatePostProcessors() {
        log.info("========================================");
        log.info("【步骤5】创建后置处理器");
        log.info("========================================");
        
        List<TemplatePostProcessor> processors = new ArrayList<>();
        
        // 1. 提取 origin IP
        TemplatePostProcessor p1 = new TemplatePostProcessor();
        p1.setTemplateId(testTemplateId);
        p1.setProcessorName("提取请求IP");
        p1.setProcessorType("JSON_EXTRACT");
        p1.setExtractExpression("origin");
        p1.setTargetVariable("clientIp");
        p1.setVariableScope("TEMPLATE");
        p1.setDefaultValue("unknown");
        p1.setIsEnabled(1);
        p1.setSortOrder(1);
        processors.add(p1);
        log.info("  - 添加后置处理器: origin → clientIp");
        
        // 2. 提取 URL
        TemplatePostProcessor p2 = new TemplatePostProcessor();
        p2.setTemplateId(testTemplateId);
        p2.setProcessorName("提取请求URL");
        p2.setProcessorType("JSON_EXTRACT");
        p2.setExtractExpression("url");
        p2.setTargetVariable("requestUrl");
        p2.setVariableScope("TEMPLATE");
        p2.setIsEnabled(1);
        p2.setSortOrder(2);
        processors.add(p2);
        log.info("  - 添加后置处理器: url → requestUrl");
        
        // 3. 提取 deviceId
        TemplatePostProcessor p3 = new TemplatePostProcessor();
        p3.setTemplateId(testTemplateId);
        p3.setProcessorName("提取deviceId");
        p3.setProcessorType("JSON_EXTRACT");
        p3.setExtractExpression("json.deviceId");
        p3.setTargetVariable("responseDeviceId");
        p3.setVariableScope("TEMPLATE");
        p3.setIsEnabled(1);
        p3.setSortOrder(3);
        processors.add(p3);
        log.info("  - 添加后置处理器: json.deviceId → responseDeviceId");
        
        int count = postProcessorMapper.batchInsert(processors);
        
        log.info("✓ 后置处理器创建成功，数量: {}", count);
        assertEquals(3, count, "应创建3个后置处理器");
    }

    // ==================== 测试：执行模板 ====================

    @Test
    @Order(6)
    @DisplayName("步骤6：执行模板并验证结果")
    void testExecuteTemplate() {
        log.info("========================================");
        log.info("【步骤6】执行模板并验证结果");
        log.info("========================================");
        log.info("模板ID: {}", testTemplateId);
        
        // 准备变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("customVar", "customValue");
        
        // 执行模板
        log.info("开始执行模板...");
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = executeService.executeTemplate(testTemplateId, variables);
        long duration = System.currentTimeMillis() - startTime;
        
        // 打印结果
        printExecutionResult(result);
        
        // 验证结果
        assertNotNull(result, "执行结果不应为空");
        
        // 验证成功状态
        Boolean success = (Boolean) result.get("success");
        assertTrue(success, "执行应该成功");
        
        // 验证状态码
        String statusCode = (String) result.get("statusCode");
        assertEquals("200", statusCode, "状态码应为200");
        
        // 验证响应时间
        Long durationMs = (Long) result.get("durationMs");
        assertNotNull(durationMs, "响应时间不应为空");
        assertTrue(durationMs < 10000, "响应时间应小于10秒");
        
        // 验证请求信息
        @SuppressWarnings("unchecked")
        Map<String, Object> request = (Map<String, Object>) result.get("request");
        assertNotNull(request, "请求信息不应为空");
        assertEquals("https://httpbin.org/post", request.get("url"), "URL应正确");
        assertEquals("POST", request.get("method"), "方法应为POST");
        
        // 验证响应信息
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) result.get("response");
        assertNotNull(response, "响应信息不应为空");
        assertEquals(200, response.get("statusCode"), "响应状态码应为200");
        
        // 验证变量
        @SuppressWarnings("unchecked")
        Map<String, Object> resultVars = (Map<String, Object>) result.get("variables");
        assertNotNull(resultVars, "变量不应为空");
        assertNotNull(resultVars.get("timestamp"), "timestamp变量应存在");
        assertNotNull(resultVars.get("requestId"), "requestId变量应存在");
        assertNotNull(resultVars.get("deviceId"), "deviceId变量应存在");
        assertNotNull(resultVars.get("randomCode"), "randomCode变量应存在");
        // 后置处理器提取的变量
        assertNotNull(resultVars.get("clientIp"), "clientIp变量应存在（后置处理器提取）");
        assertNotNull(resultVars.get("requestUrl"), "requestUrl变量应存在（后置处理器提取）");
        assertNotNull(resultVars.get("responseDeviceId"), "responseDeviceId变量应存在（后置处理器提取）");
        
        // 验证断言结果
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assertions = (List<Map<String, Object>>) result.get("assertions");
        assertNotNull(assertions, "断言结果不应为空");
        assertEquals(3, assertions.size(), "应有3个断言");
        
        // 验证所有断言都通过
        for (Map<String, Object> assertion : assertions) {
            Boolean passed = (Boolean) assertion.get("passed");
            String name = (String) assertion.get("name");
            assertTrue(passed, "断言应通过: " + name);
        }
        
        log.info("✓ 所有验证通过！");
        log.info("========================================");
        log.info("总耗时: {} ms", duration);
        log.info("========================================");
    }

    @Test
    @Order(7)
    @DisplayName("步骤7：验证前置处理器生成的变量")
    void testPreProcessorVariables() {
        log.info("========================================");
        log.info("【步骤7】验证前置处理器生成的变量");
        log.info("========================================");
        
        Map<String, Object> result = executeService.executeTemplate(testTemplateId, null);
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) result.get("variables");
        
        // 验证 timestamp
        String timestamp = (String) variables.get("timestamp");
        assertNotNull(timestamp, "timestamp 不应为空");
        assertTrue(timestamp.matches("\\d+"), "timestamp 应为数字");
        long ts = Long.parseLong(timestamp);
        long now = System.currentTimeMillis();
        assertTrue(Math.abs(now - ts) < 60000, "timestamp 应在1分钟内");
        log.info("✓ timestamp: {} (有效)", timestamp);
        
        // 验证 requestId (UUID)
        String requestId = (String) variables.get("requestId");
        assertNotNull(requestId, "requestId 不应为空");
        assertEquals(32, requestId.length(), "UUID 应为32位（去掉-）");
        log.info("✓ requestId: {} (有效UUID)", requestId);
        
        // 验证 deviceId
        String deviceId = (String) variables.get("deviceId");
        assertNotNull(deviceId, "deviceId 不应为空");
        assertEquals(16, deviceId.length(), "deviceId 应为16位");
        log.info("✓ deviceId: {} (16位随机字符串)", deviceId);
        
        // 验证 randomCode
        String randomCode = String.valueOf(variables.get("randomCode"));
        assertNotNull(randomCode, "randomCode 不应为空");
        int code = Integer.parseInt(randomCode);
        assertTrue(code >= 1000 && code <= 9999, "randomCode 应在1000-9999之间");
        log.info("✓ randomCode: {} (1000-9999之间)", randomCode);
    }

    @Test
    @Order(8)
    @DisplayName("步骤8：验证后置处理器提取的数据")
    void testPostProcessorExtraction() {
        log.info("========================================");
        log.info("【步骤8】验证后置处理器提取的数据");
        log.info("========================================");
        
        Map<String, Object> result = executeService.executeTemplate(testTemplateId, null);
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) result.get("variables");
        
        // 验证 clientIp
        String clientIp = (String) variables.get("clientIp");
        assertNotNull(clientIp, "clientIp 不应为空");
        log.info("✓ clientIp 提取成功: {}", clientIp);
        
        // 验证 requestUrl
        String requestUrl = (String) variables.get("requestUrl");
        assertNotNull(requestUrl, "requestUrl 不应为空");
        assertTrue(requestUrl.contains("httpbin.org"), "requestUrl 应包含 httpbin.org");
        log.info("✓ requestUrl 提取成功: {}", requestUrl);
        
        // 验证 responseDeviceId（从响应体中提取）
        String responseDeviceId = (String) variables.get("responseDeviceId");
        assertNotNull(responseDeviceId, "responseDeviceId 不应为空");
        String originalDeviceId = (String) variables.get("deviceId");
        assertEquals(originalDeviceId, responseDeviceId, 
                "responseDeviceId 应与请求中的 deviceId 一致");
        log.info("✓ responseDeviceId 提取成功: {}", responseDeviceId);
        log.info("  请求deviceId: {}", originalDeviceId);
        log.info("  响应deviceId: {}", responseDeviceId);
    }

    @Test
    @Order(9)
    @DisplayName("步骤9：验证断言结果")
    void testAssertions() {
        log.info("========================================");
        log.info("【步骤9】验证断言结果");
        log.info("========================================");
        
        Map<String, Object> result = executeService.executeTemplate(testTemplateId, null);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assertions = (List<Map<String, Object>>) result.get("assertions");
        
        assertNotNull(assertions, "断言列表不应为空");
        assertEquals(3, assertions.size(), "应有3个断言");
        
        // 验证每个断言
        for (int i = 0; i < assertions.size(); i++) {
            Map<String, Object> assertion = assertions.get(i);
            String name = (String) assertion.get("name");
            String type = (String) assertion.get("type");
            Boolean passed = (Boolean) assertion.get("passed");
            Object expected = assertion.get("expectedValue");
            Object actual = assertion.get("actualValue");
            
            log.info("\n断言[{}]: {}", i + 1, name);
            log.info("  类型: {}", type);
            log.info("  期望值: {}", expected);
            log.info("  实际值: {}", actual);
            log.info("  结果: {}", passed ? "✓ 通过" : "✗ 失败");
            
            assertTrue(passed, "断言应通过: " + name);
        }
        
        log.info("\n✓ 所有 {} 个断言均通过！", assertions.size());
    }

    // ==================== 辅助方法 ====================

    private void printExecutionResult(Map<String, Object> result) {
        log.info("\n----------------------------------------");
        log.info("执行结果详情:");
        log.info("----------------------------------------");
        
        // 基本信息
        log.info("成功: {}", result.get("success"));
        log.info("状态码: {}", result.get("statusCode"));
        log.info("消息: {}", result.get("message"));
        log.info("耗时: {} ms", result.get("durationMs"));
        
        // 请求信息
        @SuppressWarnings("unchecked")
        Map<String, Object> request = (Map<String, Object>) result.get("request");
        if (request != null) {
            log.info("\n请求信息:");
            log.info("  URL: {}", request.get("url"));
            log.info("  Method: {}", request.get("method"));
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) request.get("headers");
            if (headers != null) {
                log.info("  Headers:");
                headers.forEach((k, v) -> log.info("    {}: {}", k, v));
            }
            String body = (String) request.get("body");
            if (body != null) {
                log.info("  Body: {}", body.length() > 200 ? body.substring(0, 200) + "..." : body);
            }
        }
        
        // 响应信息
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) result.get("response");
        if (response != null) {
            log.info("\n响应信息:");
            log.info("  状态码: {}", response.get("statusCode"));
            log.info("  状态文本: {}", response.get("statusText"));
            log.info("  响应时间: {} ms", response.get("responseTime"));
            log.info("  响应大小: {} bytes", response.get("size"));
        }
        
        // 变量
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) result.get("variables");
        if (variables != null) {
            log.info("\n执行后变量:");
            variables.forEach((k, v) -> log.info("  {} = {}", k, v));
        }
        
        // 断言结果
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assertions = (List<Map<String, Object>>) result.get("assertions");
        if (assertions != null) {
            log.info("\n断言结果:");
            for (Map<String, Object> assertion : assertions) {
                String name = (String) assertion.get("name");
                Boolean passed = (Boolean) assertion.get("passed");
                if (Boolean.TRUE.equals(passed)) {
                    log.info("  ✓ {} - 通过", name);
                } else {
                    String errorMsg = (String) assertion.get("errorMessage");
                    log.info("  ✗ {} - 失败: {}", name, errorMsg);
                }
            }
        }
        
        log.info("----------------------------------------\n");
    }
}
