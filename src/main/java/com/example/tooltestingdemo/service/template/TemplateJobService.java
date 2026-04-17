package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.template.TemplateJob;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;

import java.util.List;
import java.util.Map;

/**
 * 模板定时任务 Service
 */
public interface TemplateJobService extends IService<TemplateJob> {

    /**
     * 分页查询任务列表
     */
    IPage<TemplateJob> pageJobs(Page<TemplateJob> page, String keyword, Integer status);

    /**
     * 分页查询任务列表（附带最近一次执行状态，适合管理页面）
     */
    IPage<com.example.tooltestingdemo.vo.TemplateJobListVO> pageJobsWithLastLog(
            Page<TemplateJob> page, String keyword, Integer status);

    /**
     * 创建任务
     */
    TemplateJob createJob(TemplateJob job);

    /**
     * 更新任务
     */
    TemplateJob updateJob(TemplateJob job);

    /**
     * 删除任务
     */
    boolean deleteJob(Long id);

    /**
     * 手动触发执行任务
     */
    Map<String, Object> triggerJob(Long id);

    /**
     * 批量触发执行任务
     * @param ids 任务ID数组
     * @return 结果包含 successIds 与 failIds
     */
    Map<String, Object> batchTriggerJobs(Long[] ids);

    /**
     * 批量停止任务（停用并取消调度）
     * @param ids 任务ID数组
     * @return 结果包含 successIds 与 failIds
     */
    Map<String, Object> batchStopJobs(Long[] ids);

    /**
     * 异步提交批量触发任务，立即返回 batchId，可通过 getBatchTriggerResult 查询结果
     * @param ids 任务ID数组
     * @return batchId
     */
    String submitBatchTriggerAsync(Long[] ids);

    /**
     * 查询异步批量触发结果/状态
     * @param batchId 提交返回的 batchId
     * @return 包含 status 和 details（若已完成）
     */
    Map<String, Object> getBatchTriggerResult(String batchId);

    /**
     * 查询任务最近日志
     */
    List<TemplateJobLog> getRecentLogs(Long jobId, Integer limit);

    /**
     * 分页查询任务执行日志（结构化）
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.example.tooltestingdemo.vo.TemplateJobLogVO> pageJobLogs(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<TemplateJobLog> page,
            Long jobId,
            Integer success);

    /**
     * 获取任务详情（包含子项）
     */
    TemplateJob getJobDetail(Long id);
}
