package com.example.tooltestingdemo.controller.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateJob;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.example.tooltestingdemo.service.template.TemplateJobService;
import com.example.tooltestingdemo.vo.TemplateJobListVO;
import com.example.tooltestingdemo.vo.TemplateJobLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
     * 分页查询任务列表（基础版）
     */
    @GetMapping("/page")
    public Result<IPage<TemplateJob>> pageJobs(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<TemplateJob> page = new Page<>(current, size);
        return Result.success(jobService.pageJobs(page, keyword, status));
    }

    /**
     * 分页查询任务列表（附带最近一次执行状态，推荐管理页面使用）
     */
    @GetMapping("/page-with-last-log")
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
    public Result<TemplateJob> getJobById(@PathVariable Long id) {
        TemplateJob job = jobService.getJobDetail(id);
        return job != null ? Result.success(job) : Result.error("任务不存在");
    }

    /**
     * 创建任务
     */
    @PostMapping
    public Result<TemplateJob> createJob(@RequestBody TemplateJob job) {
        return Result.success("创建成功", jobService.createJob(job));
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public Result<TemplateJob> updateJob(@PathVariable Long id, @RequestBody TemplateJob job) {
        job.setId(id);
        return Result.success("更新成功", jobService.updateJob(job));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteJob(@PathVariable Long id) {
        return jobService.deleteJob(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量触发执行任务
     */
    @PostMapping("/batch/trigger")
    public Result<Map<String, Object>> batchTriggerJobs(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) return Result.error("任务ID列表不能为空");
        return Result.success(jobService.batchTriggerJobs(ids));
    }

    /**
     * 批量停止任务（停用并取消调度）
     */
    @PostMapping("/batch/stop")
    public Result<Map<String, Object>> batchStopJobs(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) return Result.error("任务ID列表不能为空");
        return Result.success(jobService.batchStopJobs(ids));
    }

    /**
     * 异步批量触发（立即返回 batchId）
     */
    @PostMapping("/batch/trigger-async")
    public Result<String> submitBatchTriggerAsync(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) return Result.error("任务ID列表不能为空");
        String batchId = jobService.submitBatchTriggerAsync(ids);
        return Result.success("提交成功", batchId);
    }

    /**
     * 查询异步批量触发状态
     */
    @GetMapping("/batch/{batchId}/status")
    public Result<Map<String, Object>> getBatchTriggerResult(@PathVariable String batchId) {
        return Result.success(jobService.getBatchTriggerResult(batchId));
    }

    /**
     * 手动触发执行
     */
    @PostMapping("/{id}/trigger")
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
    public Result<List<TemplateJobLog>> getRecentLogs(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(jobService.getRecentLogs(id, limit));
    }
}
