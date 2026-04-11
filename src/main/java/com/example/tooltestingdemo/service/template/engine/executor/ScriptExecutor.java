package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.script.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 脚本执行器
 * 
 * 执行 JavaScript/Groovy/Python 等脚本的模板执行器
 * 
 * <p>支持的协议类型：</p>
 * <ul>
 *   <li>SCRIPT - 通用脚本</li>
 *   <li>JS - JavaScript 脚本</li>
 *   <li>GROOVY - Groovy 脚本</li>
 *   <li>PYTHON - Python 脚本（需要 Jython）</li>
 * </ul>
 * 
 * <p>脚本上下文变量：</p>
 * <ul>
 *   <li>variables - 所有变量 Map</li>
 *   <li>template - 模板信息</li>
 *   <li>console - 打印输出</li>
 * </ul>
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScriptExecutor implements TemplateExecutor {

    private final ObjectMapper objectMapper;
    
    // 脚本引擎管理器
    private final ScriptEngineManager engineManager = new ScriptEngineManager();

    @Override
    public String getType() {
        return "SCRIPT";
    }
    
    /**
     * 支持多种脚本语言类型
     */
    @Override
    public boolean supports(String protocolType) {
        if (protocolType == null) {
            return false;
        }
        String upper = protocolType.toUpperCase();
        return upper.equals("SCRIPT") || 
               upper.equals("JS") || 
               upper.equals("JAVASCRIPT") ||
               upper.equals("GROOVY") ||
               upper.equals("PYTHON");
    }

    @Override
    public ExecutionResult execute(TemplateContext context) {
        log.debug("执行脚本: templateId={}", 
                context.getTemplate() != null ? context.getTemplate().getId() : null);
        
        LocalDateTime startTime = LocalDateTime.now();
        long startMs = System.currentTimeMillis();
        
        ScriptConfig config = parseConfig(context);
        if (config == null) {
            return buildErrorResult(context, "脚本配置解析失败", startMs);
        }
        
        String script = config.getScript();
        if (!StringUtils.hasText(script)) {
            return buildErrorResult(context, "脚本内容为空", startMs);
        }
        
        // 替换变量
        script = replaceVariables(script, context.getAllVariables());
        
        try {
            // 获取脚本引擎
            ScriptEngine engine = getScriptEngine(config.getLanguage());
            if (engine == null) {
                return buildErrorResult(context, 
                        "不支持的脚本语言: " + config.getLanguage() + 
                        "，请确保相关依赖已添加", startMs);
            }
            
            // 创建脚本上下文
            ScriptContext scriptContext = new SimpleScriptContext();
            Bindings bindings = engine.createBindings();
            
            // 注入变量
            bindings.put("variables", context.getAllVariables());
            bindings.put("globalVars", context.getGlobalVariables());
            bindings.put("templateVars", context.getTemplateVariables());
            bindings.put("localVars", context.getLocalVariables());
            
            // 注入模板信息
            if (context.getTemplate() != null) {
                Map<String, Object> templateInfo = new HashMap<>();
                templateInfo.put("id", context.getTemplate().getId());
                templateInfo.put("name", context.getTemplate().getName());
                bindings.put("template", templateInfo);
            }
            
            // 注入工具对象
            bindings.put("console", new ScriptConsole());
            bindings.put("log", new ScriptLogger());
            
            // 设置变量操作 API
            bindings.put("setVariable", new SetVariableFunction(context));
            bindings.put("getVariable", new GetVariableFunction(context));
            bindings.put("setGlobalVariable", new SetGlobalVariableFunction(context));
            
            scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            
            // 执行脚本
            Object scriptResult = engine.eval(script, scriptContext);
            
            // 处理输出
            List<String> output = new ArrayList<>();
            if (bindings.get("_output") instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> scriptOutput = (List<String>) bindings.get("_output");
                output = scriptOutput;
            }
            
            // 构建结果
            long durationMs = System.currentTimeMillis() - startMs;
            
            Map<String, Object> resultBody = new HashMap<>();
            resultBody.put("scriptResult", scriptResult);
            resultBody.put("output", output);
            resultBody.put("language", config.getLanguage());
            
            ExecutionResult.ResponseInfo responseInfo = ExecutionResult.ResponseInfo.builder()
                    .statusCode(200)
                    .statusText("OK")
                    .body(resultBody)
                    .responseTime(durationMs)
                    .build();
            
            ExecutionResult.RequestInfo requestInfo = ExecutionResult.RequestInfo.builder()
                    .url("script://" + config.getLanguage())
                    .method("EXECUTE")
                    .body(script.length() > 1000 ? script.substring(0, 1000) + "..." : script)
                    .build();
            
            return ExecutionResult.builder()
                    .success(true)
                    .statusCode("200")
                    .message("脚本执行成功")
                    .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                    .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                    .startTime(startTime)
                    .request(requestInfo)
                    .response(responseInfo)
                    .variables(context.getAllVariables())
                    .build();
            
        } catch (ScriptException e) {
            log.error("脚本执行失败", e);
            return buildErrorResult(context, "脚本执行错误: " + e.getMessage(), startMs);
        } catch (Exception e) {
            log.error("脚本执行异常", e);
            return buildErrorResult(context, "脚本执行异常: " + e.getMessage(), startMs);
        }
    }

    @Override
    public ValidationResult validate(TemplateContext context) {
        if (context.getTemplate() == null) {
            return ValidationResult.failure("模板信息为空");
        }
        
        ScriptConfig config = parseConfig(context);
        if (config == null) {
            return ValidationResult.failure("脚本配置解析失败");
        }
        
        if (!StringUtils.hasText(config.getScript())) {
            return ValidationResult.failure("脚本内容不能为空");
        }
        
        // 验证脚本引擎是否可用
        ScriptEngine engine = getScriptEngine(config.getLanguage());
        if (engine == null) {
            return ValidationResult.failure("不支持的脚本语言或引擎未找到: " + config.getLanguage());
        }
        
        return ValidationResult.success();
    }

    @Override
    public PreviewResult preview(TemplateContext context) {
        ScriptConfig config = parseConfig(context);
        if (config == null) {
            return new PreviewResult(null, "SCRIPT", null, "配置解析失败", null);
        }
        
        String script = replaceVariables(config.getScript(), context.getAllVariables());
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Script-Language", config.getLanguage());
        headers.put("Script-Length", String.valueOf(script.length()));
        
        return new PreviewResult(
                "script://" + config.getLanguage(),
                "EXECUTE",
                headers,
                script.length() > 500 ? script.substring(0, 500) + "..." : script,
                null
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 解析脚本配置
     */
    private ScriptConfig parseConfig(TemplateContext context) {
        try {
            String bodyContent = context.getTemplate().getBodyContent();
            if (!StringUtils.hasText(bodyContent)) {
                return null;
            }
            
            return objectMapper.readValue(bodyContent, ScriptConfig.class);
        } catch (Exception e) {
            log.error("解析脚本配置失败", e);
            return null;
        }
    }

    /**
     * 获取脚本引擎
     */
    private ScriptEngine getScriptEngine(String language) {
        if (!StringUtils.hasText(language)) {
            language = "javascript";
        }
        
        String engineName = normalizeLanguageName(language);
        return engineManager.getEngineByName(engineName);
    }

    /**
     * 标准化语言名称
     */
    private String normalizeLanguageName(String language) {
        String lower = language.toLowerCase();
        switch (lower) {
            case "js":
            case "javascript":
            case "ecmascript":
                return "javascript";
            case "groovy":
                return "groovy";
            case "python":
            case "py":
                return "python";
            case "ruby":
                return "ruby";
            default:
                return lower;
        }
    }

    /**
     * 替换脚本中的变量
     */
    private String replaceVariables(String script, Map<String, Object> variables) {
        if (!StringUtils.hasText(script) || variables == null) {
            return script;
        }
        
        String result = script;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            // 对值进行转义，避免破坏脚本语法
            value = escapeForScript(value);
            result = result.replace(placeholder, value);
        }
        
        return result;
    }

    /**
     * 对值进行转义，避免破坏脚本
     */
    private String escapeForScript(String value) {
        if (value == null) {
            return "";
        }
        // 简单的转义处理
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * 构建错误结果
     */
    private ExecutionResult buildErrorResult(TemplateContext context, String message, long startMs) {
        return ExecutionResult.builder()
                .success(false)
                .statusCode("ERROR")
                .message(message)
                .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .durationMs(System.currentTimeMillis() - startMs)
                .build();
    }

    // ==================== 脚本辅助类 ====================

    /**
     * 脚本控制台
     */
    public static class ScriptConsole {
        private final List<String> output = new ArrayList<>();
        
        public void log(Object message) {
            String msg = String.valueOf(message);
            output.add("[LOG] " + msg);
            System.out.println("[Script] " + msg);
        }
        
        public void info(Object message) {
            log("[INFO] " + message);
        }
        
        public void warn(Object message) {
            log("[WARN] " + message);
        }
        
        public void error(Object message) {
            log("[ERROR] " + message);
        }
        
        public List<String> getOutput() {
            return output;
        }
    }

    /**
     * 脚本日志
     */
    public static class ScriptLogger {
        public void info(Object message) {
            log.info("[Script] {}", message);
        }
        
        public void debug(Object message) {
            log.debug("[Script] {}", message);
        }
        
        public void warn(Object message) {
            log.warn("[Script] {}", message);
        }
        
        public void error(Object message) {
            log.error("[Script] {}", message);
        }
    }

    /**
     * 设置变量函数
     */
    public static class SetVariableFunction {
        private final TemplateContext context;
        
        public SetVariableFunction(TemplateContext context) {
            this.context = context;
        }
        
        public void call(String name, Object value) {
            context.setTemplateVariable(name, value);
        }
    }

    /**
     * 获取变量函数
     */
    public static class GetVariableFunction {
        private final TemplateContext context;
        
        public GetVariableFunction(TemplateContext context) {
            this.context = context;
        }
        
        public Object call(String name) {
            return context.getVariable(name);
        }
    }

    /**
     * 设置全局变量函数
     */
    public static class SetGlobalVariableFunction {
        private final TemplateContext context;
        
        public SetGlobalVariableFunction(TemplateContext context) {
            this.context = context;
        }
        
        public void call(String name, Object value) {
            context.setGlobalVariable(name, value);
        }
    }

    // ==================== 配置内部类 ====================

    /**
     * 脚本配置
     */
    private static class ScriptConfig {
        private String language = "javascript";
        private String script;
        private Integer timeout = 30;

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getScript() { return script; }
        public void setScript(String script) { this.script = script; }
        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
    }
}
