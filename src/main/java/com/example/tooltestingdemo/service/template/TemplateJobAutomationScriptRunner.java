package com.example.tooltestingdemo.service.template;

import com.example.tooltestingdemo.dto.template.TemplateJobAutomationConfigDTO;
import com.example.tooltestingdemo.entity.template.TemplateJobItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 任务级自动化脚本执行器
 */
@Slf4j
@Component
public class TemplateJobAutomationScriptRunner {

    private final ScriptEngineManager engineManager = new ScriptEngineManager();
    private final ExecutorService scriptExecutor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("template-job-automation-script");
        thread.setDaemon(true);
        return thread;
    });

    public ScriptRunResult runBefore(Long jobId,
                                     String jobName,
                                     List<TemplateJobItem> items,
                                     TemplateJobAutomationConfigDTO.ScriptConfig config,
                                     Map<String, Object> variables) {
        return run(jobId, jobName, items, null, config, variables, "before", config == null ? null : config.getBeforeScript());
    }

    public ScriptRunResult runAfter(Long jobId,
                                    String jobName,
                                    List<TemplateJobItem> items,
                                    List<Map<String, Object>> results,
                                    TemplateJobAutomationConfigDTO.ScriptConfig config,
                                    Map<String, Object> variables) {
        return run(jobId, jobName, items, results, config, variables, "after", config == null ? null : config.getAfterScript());
    }

    private ScriptRunResult run(Long jobId,
                                String jobName,
                                List<TemplateJobItem> items,
                                List<Map<String, Object>> results,
                                TemplateJobAutomationConfigDTO.ScriptConfig config,
                                Map<String, Object> variables,
                                String phase,
                                String script) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled()) || !StringUtils.hasText(script)) {
            return ScriptRunResult.success("脚本未启用或内容为空", variables, null);
        }

        ScriptEngine engine = engineManager.getEngineByName(normalizeLanguage(config.getLanguage()));
        if (engine == null) {
            return ScriptRunResult.failure("不支持的脚本语言或脚本引擎未找到: " + config.getLanguage(), variables, null);
        }

        Map<String, Object> scriptVariables = variables == null ? new HashMap<>() : variables;
        List<String> output = new ArrayList<>();
        Callable<ScriptRunResult> task = () -> {
            Bindings bindings = engine.createBindings();
            bindings.put("jobId", jobId);
            bindings.put("jobName", jobName);
            bindings.put("items", items);
            bindings.put("results", results);
            bindings.put("variables", scriptVariables);
            bindings.put("phase", phase);
            bindings.put("console", new ScriptConsole(output));
            Object value = engine.eval(script, bindings);
            if (Boolean.FALSE.equals(value)) {
                return ScriptRunResult.failure("脚本返回 false", scriptVariables, output);
            }
            return ScriptRunResult.success("脚本执行成功", scriptVariables, output);
        };

        Future<ScriptRunResult> future = scriptExecutor.submit(task);
        try {
            long timeoutMs = Math.min(Math.max(config.getTimeoutMs() == null ? 30000L : config.getTimeoutMs(), 1000L), 300000L);
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return ScriptRunResult.failure("脚本执行超时", scriptVariables, output);
        } catch (Exception e) {
            log.warn("任务自动化脚本执行失败 jobId={}, phase={}", jobId, phase, e);
            return ScriptRunResult.failure("脚本执行失败: " + e.getMessage(), scriptVariables, output);
        }
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return "javascript";
        }
        String lower = language.toLowerCase();
        return switch (lower) {
            case "js", "javascript", "ecmascript" -> "javascript";
            case "groovy" -> "groovy";
            default -> lower;
        };
    }

    public static class ScriptConsole {
        private final List<String> output;

        public ScriptConsole(List<String> output) {
            this.output = output;
        }

        public void log(Object message) {
            output.add(String.valueOf(message));
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
    }

    @Data
    @AllArgsConstructor
    public static class ScriptRunResult {
        private boolean success;
        private String message;
        private Map<String, Object> variables;
        private List<String> output;

        public static ScriptRunResult success(String message, Map<String, Object> variables, List<String> output) {
            return new ScriptRunResult(true, message, variables, output);
        }

        public static ScriptRunResult failure(String message, Map<String, Object> variables, List<String> output) {
            return new ScriptRunResult(false, message, variables, output);
        }
    }
}
