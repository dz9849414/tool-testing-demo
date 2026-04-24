package com.example.tooltestingdemo.controller.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateJob;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.example.tooltestingdemo.service.template.TemplateJobService;
import com.example.tooltestingdemo.vo.TemplateJobListVO;
import com.example.tooltestingdemo.vo.TemplateJobLogVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模板定时任务 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/template/job")
@RequiredArgsConstructor
public class TemplateJobController {

    private final TemplateJobService jobService;

    /**
     * 分页查询任务列表（附带最近一次执行状态，推荐管理页面使用）
     */
    @GetMapping("/page-with-last-log")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<IPage<TemplateJobListVO>> pageJobsWithLastLog(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<TemplateJob> page = new Page<>(current, size);
        return Result.success(jobService.pageJobsWithLastLog(page, keyword, status));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<TemplateJob> getJobById(@PathVariable Long id) {
        TemplateJob job = jobService.getJobDetail(id);
        return job != null ? Result.success(job) : Result.error("任务不存在");
    }

    /**
     * 创建任务
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<TemplateJob> createJob(@RequestBody TemplateJob job) {
        return Result.success("创建成功", jobService.createJob(job));
    }

    /**
     * 导出任务配置为 JSON
     */
    @GetMapping("/export/json")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public void exportJobs(@RequestParam("jobIds") String[] rawJobIds, HttpServletResponse response) throws IOException {
        Long[] jobIds = parseJobIds(rawJobIds);
        String content = jobService.exportJobs(jobIds);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition",
                "attachment; filename=template_jobs_" + System.currentTimeMillis() + ".json");
        response.getWriter().write(content);
        response.getWriter().flush();
    }

    /**
     * 导入任务配置
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<Map<String, Object>> importJobs(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("导入文件不能为空");
        }
        Map<String, Object> result = jobService.importJobs(file);
        Integer failCount = getIntValue(result.get("failCount"));
        if (failCount != null && failCount > 0) {
            return Result.error(500, buildImportErrorMessage(result), result);
        }
        return Result.success("导入成功", result);
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<TemplateJob> updateJob(@PathVariable Long id, @RequestBody TemplateJob job) {
        job.setId(id);
        return Result.success("更新成功", jobService.updateJob(job));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<String> deleteJob(@PathVariable Long id) {
        return jobService.deleteJob(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量停止任务（停用并取消调度）
     */
    @PostMapping("/batch/stop")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<Map<String, Object>> batchStopJobs(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            return Result.error("任务ID列表不能为空");
        }
        return Result.success(jobService.batchStopJobs(ids));
    }

    /**
     * 异步批量触发（立即返回 batchId）
     */
    @PostMapping("/batch/trigger-async")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<String> submitBatchTriggerAsync(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            return Result.error("任务ID列表不能为空");
        }
        String batchId = jobService.submitBatchTriggerAsync(ids);
        return Result.success("提交成功", batchId);
    }

    /**
     * 查询异步批量触发状态
     */
    @GetMapping("/batch/{batchId}/status")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<Map<String, Object>> getBatchTriggerResult(@PathVariable String batchId) {
        return Result.success(jobService.getBatchTriggerResult(batchId));
    }

    /**
     * 手动触发执行（单个）
     */
    @PostMapping("/{id}/trigger")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<Map<String, Object>> triggerJob(@PathVariable Long id) {
        Map<String, Object> result = jobService.triggerJob(id);
        Boolean success = (Boolean) result.get("success");
        return Boolean.TRUE.equals(success)
                ? Result.success("执行成功", result)
                : Result.error(500, "执行失败", result);
    }

    /**
     * 分页查询任务执行日志（结构化，适合管理页面展示）
     */
    @GetMapping("/logs/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<IPage<TemplateJobLogVO>> pageJobLogs(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Integer success) {
        Page<TemplateJobLog> page = new Page<>(current, size);
        return Result.success(jobService.pageJobLogs(page, jobId, success));
    }

    /**
     * 查询任务最近日志
     */
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<List<TemplateJobLog>> getRecentLogs(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(jobService.getRecentLogs(id, limit));
    }

    private Long[] parseJobIds(String[] rawJobIds) {
        if (rawJobIds == null || rawJobIds.length == 0) {
            throw new IllegalArgumentException("jobIds 不能为空");
        }

        List<Long> parsed = new ArrayList<>();
        for (String rawJobId : rawJobIds) {
            if (rawJobId == null) {
                continue;
            }

            String normalized = rawJobId.trim();
            if (normalized.isEmpty()) {
                continue;
            }

            normalized = normalized.replace("[", "").replace("]", "");
            String[] parts = normalized.split(",");
            for (String part : parts) {
                String value = part.trim();
                if (!value.isEmpty()) {
                    parsed.add(Long.valueOf(value));
                }
            }
        }

        if (parsed.isEmpty()) {
            throw new IllegalArgumentException("jobIds 不能为空");
        }

        return parsed.toArray(new Long[0]);
    }

    @SuppressWarnings("unchecked")
    private String buildImportErrorMessage(Map<String, Object> result) {
        Object failItemsObj = result.get("failItems");
        if (failItemsObj instanceof List<?> failItems && !failItems.isEmpty()) {
            List<String> messages = new ArrayList<>();
            for (Object failItem : failItems) {
                if (failItem instanceof Map<?, ?> failMap) {
                    Object message = failMap.get("message");
                    if (message != null) {
                        String text = String.valueOf(message).trim();
                        if (!text.isEmpty() && !messages.contains(text)) {
                            messages.add(text);
                        }
                    }
                }
            }
            if (!messages.isEmpty()) {
                return String.join("; ", messages);
            }
        }
        Integer failCount = getIntValue(result.get("failCount"));
        return failCount == null ? "导入失败" : "导入失败，失败数量: " + failCount;
    }

    private Integer getIntValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}