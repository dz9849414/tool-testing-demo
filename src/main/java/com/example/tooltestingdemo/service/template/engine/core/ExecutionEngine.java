package com.example.tooltestingdemo.service.template.engine.core;

import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import com.example.tooltestingdemo.service.template.engine.executor.ExecutorFactory;
import com.example.tooltestingdemo.service.template.engine.executor.TemplateExecutor;
import com.example.tooltestingdemo.service.template.engine.interceptor.ExecutionInterceptor;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.example.tooltestingdemo.vo.TemplateEnvironmentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板执行引擎
 * 
 * 核心调度器，负责：
 * 1. 构建执行上下文
 * 2. 调用对应的执行器
 * 3. 执行拦截器链
 * 4. 返回执行结果
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionEngine {

    private final ExecutorFactory executorFactory;
    private final InterfaceTemplateService templateService;
    private final TemplateEnvironmentService environmentService;
    private final List<ExecutionInterceptor> interceptors;

    /**
     * 执行模板
     * 
     * <p>标准执行流程：</p>
     * <ol>
     *   <li>构建执行上下文</li>
     *   <li>获取对应的执行器</li>
     *   <li>执行前拦截器链</li>
     *   <li>执行器执行</li>
     *   <li>执行后拦截器链</li>
     *   <li>返回结果</li>
     * </ol>
     *
     * @param request 执行请求
     * @return 执行结果
     * @throws RuntimeException 执行失败时抛出
     */
    public ExecutionResult execute(ExecutionRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("开始执行模板: templateId={}", request.getTemplateId());

        try {
            // 1. 构建执行上下文
            TemplateContext context = buildContext(request);
            
            // 2. 获取执行器（根据协议类型获取不同的执行器）
            TemplateExecutor executor = executorFactory.getExecutor(context.getProtocolType());
            
            // 3. 验证模板
            TemplateExecutor.ValidationResult validation = executor.validate(context);
            if (!validation.isValid()) {
                log.warn("模板验证失败: {}", validation.getMessage());
                return buildErrorResult(request.getTemplateId(), 
                        context.getTemplate() != null ? context.getTemplate().getName() : null,
                        validation.getMessage(), startTime);
            }
            
            // 4. 执行前拦截器
            executeBeforeInterceptors(context);
            
            // 5. 执行
            ExecutionResult result = executor.execute(context);
            
            // 6. 执行后拦截器
            executeAfterInterceptors(context, result);
            
            // 7. 记录结束时间
            result.setEndTime(LocalDateTime.now());
            result.setDurationMs(System.currentTimeMillis() - startTime);
            
            log.info("模板执行完成: templateId={}, duration={}ms, success={}", 
                    request.getTemplateId(), result.getDurationMs(), result.isSuccess());
            
            return result;
            
        } catch (Exception e) {
            log.error("模板执行异常: templateId={}", request.getTemplateId(), e);
            return buildErrorResult(request.getTemplateId(), null, e.getMessage(), startTime);
        }
    }

    /**
     * 预览请求（不实际执行）
     *
     * @param request 执行请求
     * @return 预览结果
     */
    public TemplateExecutor.PreviewResult preview(ExecutionRequest request) {
        log.info("预览模板请求: templateId={}", request.getTemplateId());
        
        // 1. 构建上下文
        TemplateContext context = buildContext(request);
        
        // 2. 获取执行器
        TemplateExecutor executor = executorFactory.getExecutor(context.getProtocolType());
        
        // 3. 调用预览
        return executor.preview(context);
    }

    /**
     * 验证模板配置
     *
     * @param templateId 模板ID
     * @return 验证结果
     */
    public TemplateExecutor.ValidationResult validate(Long templateId) {
        log.info("验证模板配置: templateId={}", templateId);
        
        // 1. 加载模板
        InterfaceTemplateVO templateVO = templateService.getTemplateDetail(templateId);
        if (templateVO == null) {
            return TemplateExecutor.ValidationResult.failure("模板不存在");
        }
        
        // 2. 构建最小上下文
        TemplateContext context = new TemplateContext();
        InterfaceTemplate template = new InterfaceTemplate();
        template.setId(templateVO.getId());
        template.setName(templateVO.getName());
        template.setProtocolType(templateVO.getProtocolType());
        template.setMethod(templateVO.getMethod());
        template.setBaseUrl(templateVO.getBaseUrl());
        template.setPath(templateVO.getPath());
        context.setTemplate(template);
        
        // 3. 获取执行器并验证
        TemplateExecutor executor = executorFactory.getExecutor(context.getProtocolType());
        return executor.validate(context);
    }

    /**
     * 构建执行上下文
     */
    private TemplateContext buildContext(ExecutionRequest request) {
        TemplateContext context = new TemplateContext();
        context.setRequest(request);
        
        // 1. 加载模板
        InterfaceTemplateVO templateVO = templateService.getTemplateDetail(request.getTemplateId());
        if (templateVO == null) {
            throw new RuntimeException("模板不存在: " + request.getTemplateId());
        }
        
        // 2. 设置模板信息
        InterfaceTemplate template = convertToEntity(templateVO);
        context.setTemplate(template);
        
        // 3. 加载环境
        if (request.getEnvironmentId() != null) {
            TemplateEnvironmentVO envVO = environmentService.getEnvironmentById(request.getEnvironmentId());
            if (envVO != null) {
                TemplateEnvironment env = convertToEntity(envVO);
                context.setEnvironment(env);
                // 加载环境变量
                loadEnvironmentVariables(context, env);
            }
        } else {
            // 使用默认环境
            TemplateEnvironmentVO defaultEnv = environmentService.getDefaultEnvironment(request.getTemplateId());
            if (defaultEnv != null) {
                context.setEnvironment(convertToEntity(defaultEnv));
                loadEnvironmentVariables(context, convertToEntity(defaultEnv));
            }
        }
        
        // 4. 加载传入的变量
        if (!CollectionUtils.isEmpty(request.getVariables())) {
            context.getTemplateVariables().putAll(request.getVariables());
        }
        
        return context;
    }

    /**
     * 执行前拦截器链
     */
    private void executeBeforeInterceptors(TemplateContext context) {
        if (CollectionUtils.isEmpty(interceptors)) {
            return;
        }
        
        List<ExecutionInterceptor> sortedInterceptors = interceptors.stream()
                .filter(i -> i.isEnabled(context))
                .sorted(Comparator.comparingInt(ExecutionInterceptor::getOrder))
                .collect(Collectors.toList());
        
        for (ExecutionInterceptor interceptor : sortedInterceptors) {
            try {
                interceptor.beforeExecute(context);
            } catch (Exception e) {
                log.warn("执行前拦截器异常: {}", interceptor.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 执行后拦截器链
     */
    private void executeAfterInterceptors(TemplateContext context, ExecutionResult result) {
        if (CollectionUtils.isEmpty(interceptors)) {
            return;
        }
        
        List<ExecutionInterceptor> sortedInterceptors = interceptors.stream()
                .filter(i -> i.isEnabled(context))
                .sorted(Comparator.comparingInt(ExecutionInterceptor::getOrder))
                .collect(Collectors.toList());
        
        for (ExecutionInterceptor interceptor : sortedInterceptors) {
            try {
                interceptor.afterExecute(context, result);
            } catch (Exception e) {
                log.warn("执行后拦截器异常: {}", interceptor.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 加载环境变量
     */
    private void loadEnvironmentVariables(TemplateContext context, TemplateEnvironment env) {
        if (env == null || !StringUtils.hasText(env.getVariables())) {
            return;
        }
        
        try {
            String vars = env.getVariables().trim();
            if (vars.startsWith("{") && vars.endsWith("}")) {
                // 简单 JSON 解析
                vars = vars.substring(1, vars.length() - 1);
                String[] pairs = vars.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim().replace("\"", "").replace("'", "");
                        String value = kv[1].trim().replace("\"", "").replace("'", "");
                        context.setTemplateVariable(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析环境变量失败", e);
        }
    }

    /**
     * VO 转 Entity
     */
    private InterfaceTemplate convertToEntity(InterfaceTemplateVO vo) {
        InterfaceTemplate entity = new InterfaceTemplate();
        entity.setId(vo.getId());
        entity.setName(vo.getName());
        entity.setDescription(vo.getDescription());
        entity.setProtocolType(vo.getProtocolType());
        entity.setMethod(vo.getMethod());
        entity.setBaseUrl(vo.getBaseUrl());
        entity.setPath(vo.getPath());
        entity.setFullUrl(vo.getFullUrl());
        entity.setAuthType(vo.getAuthType());
        entity.setAuthConfig(vo.getAuthConfig());
        entity.setContentType(vo.getContentType());
        entity.setBodyType(vo.getBodyType());
        entity.setBodyContent(vo.getBodyContent());
        entity.setBodyRawType(vo.getBodyRawType());
        entity.setConnectTimeout(vo.getConnectTimeout());
        entity.setReadTimeout(vo.getReadTimeout());
        entity.setRetryCount(vo.getRetryCount());
        entity.setRetryInterval(vo.getRetryInterval());
        entity.setCharset(vo.getCharset());
        return entity;
    }

    /**
     * Environment VO 转 Entity
     */
    private TemplateEnvironment convertToEntity(TemplateEnvironmentVO vo) {
        TemplateEnvironment entity = new TemplateEnvironment();
        entity.setId(vo.getId());
        entity.setTemplateId(vo.getTemplateId());
        entity.setEnvName(vo.getEnvName());
        entity.setEnvCode(vo.getEnvCode());
        entity.setBaseUrl(vo.getBaseUrl());
        entity.setHeaders(vo.getHeaders());
        entity.setVariables(vo.getVariables());
        entity.setAuthType(vo.getAuthType());
        entity.setAuthConfig(vo.getAuthConfig());
        return entity;
    }

    /**
     * 构建错误结果
     */
    private ExecutionResult buildErrorResult(Long templateId, String templateName, 
                                              String message, long startTime) {
        return ExecutionResult.builder()
                .success(false)
                .statusCode("ERROR")
                .message(message)
                .templateId(templateId)
                .templateName(templateName)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .durationMs(System.currentTimeMillis() - startTime)
                .build();
    }
}
