package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.service.template.TemplateExecuteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 模板执行 Controller
 *
 * <p>基于执行引擎的模板执行控制器，负责接收请求并转发给 Service 层处理。</p>
 * <p>所有实际执行逻辑已下沉到 {@link TemplateExecuteService} 和执行引擎。</p>
 *
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/template/execute")
@RequiredArgsConstructor
public class TemplateExecuteController {

    private final TemplateExecuteService executeService;

    /**
     * 执行模板请求
     * <p>
     * 接口地址：POST /api/template/execute/{templateId}
     *
     * @param templateId 模板ID
     * @param request    执行请求（可选，包含environmentId和variables）
     * @return 执行结果
     */
    @PostMapping("/{templateId}")
    public Result<Map<String, Object>> executeTemplate(
            @PathVariable Long templateId,
            @RequestBody(required = false) ExecuteRequest request) {

        log.info("收到执行模板请求: templateId={}", templateId);

        try {
            Long environmentId = request != null ? request.getEnvironmentId() : null;
            Map<String, Object> variables = request != null ? request.getVariables() : null;

            Map<String, Object> result = executeService.executeTemplate(templateId, environmentId, variables);

            Boolean success = (Boolean) result.get("success");
            return Boolean.TRUE.equals(success) ? Result.success("执行成功", result)
                    : Result.error("执行失败");

        } catch (Exception e) {
            log.error("执行模板失败: templateId={}", templateId, e);
            return Result.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 验证模板配置
     * <p>
     * 接口地址：GET /api/template/execute/{templateId}/validate
     *
     * @param templateId 模板ID
     * @return 验证结果
     */
    @GetMapping("/{templateId}/validate")
    public Result<Map<String, Object>> validateTemplate(@PathVariable Long templateId) {
        log.info("收到验证模板请求: templateId={}", templateId);

        try {
            Map<String, Object> result = executeService.validateTemplate(templateId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证模板失败: templateId={}", templateId, e);
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 预览请求（生成最终的请求内容，但不发送）
     * <p>
     * 接口地址：POST /api/template/execute/{templateId}/preview
     *
     * @param templateId 模板ID
     * @param request    预览请求（可选，包含environmentId和variables）
     * @return 请求预览
     */
    @PostMapping("/{templateId}/preview")
    public Result<Map<String, Object>> previewRequest(
            @PathVariable Long templateId,
            @RequestBody(required = false) ExecuteRequest request) {

        log.info("收到预览请求: templateId={}", templateId);

        try {
            Long environmentId = request != null ? request.getEnvironmentId() : null;
            Map<String, Object> variables = request != null ? request.getVariables() : null;

            Map<String, Object> result = executeService.previewRequest(templateId, environmentId, variables);
            return Result.success(result);

        } catch (Exception e) {
            log.error("预览请求失败: templateId={}", templateId, e);
            return Result.error("预览失败: " + e.getMessage());
        }
    }

    /**
     * 执行请求体
     */
    @lombok.Data
    public static class ExecuteRequest {
        private Long environmentId;
        private Map<String, Object> variables;
    }
}
