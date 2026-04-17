package com.example.tooltestingdemo.service.impl.template;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.config.JobDispatcher;
import com.example.tooltestingdemo.entity.template.TemplateJob;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.entity.template.TemplateJobItem;
import com.example.tooltestingdemo.mapper.template.TemplateJobItemMapper;
import com.example.tooltestingdemo.mapper.template.TemplateJobLogMapper;
import com.example.tooltestingdemo.mapper.template.TemplateJobMapper;
import com.example.tooltestingdemo.config.DynamicJobScheduler;
import com.example.tooltestingdemo.service.template.TemplateExecuteService;
import com.example.tooltestingdemo.service.template.TemplateJobService;
import com.example.tooltestingdemo.vo.TemplateJobListVO;
import com.example.tooltestingdemo.vo.TemplateJobLogItemVO;
import com.example.tooltestingdemo.vo.TemplateJobLogVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Value;
import com.example.tooltestingdemo.mapper.template.TemplateJobBatchMapper;
import com.example.tooltestingdemo.entity.template.TemplateJobBatch;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.enums.TemplateEnums.ApiResultKeys;

/**
 * 模板定时任务 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateJobServiceImpl extends ServiceImpl<TemplateJobMapper, TemplateJob>
        implements TemplateJobService {

    private final TemplateJobLogMapper jobLogMapper;
    private final TemplateJobItemMapper jobItemMapper;
    private final TemplateExecuteService executeService;
    private final DynamicJobScheduler jobScheduler;

    private static final ConcurrentHashMap<Long, java.util.concurrent.locks.ReentrantLock> JOB_LOCKS = new ConcurrentHashMap<>();
    private final Executor templateJobExecutor;
    private final TemplateJobBatchMapper batchMapper;

    @Value("${template.job.item.timeout-ms:120000}")
    private long itemTimeoutMs;

    @Value("${template.job.item.retry:1}")
    private int itemRetry;

    @Resource
    private JobDispatcher jobDispatcher;

    @Override
    public IPage<TemplateJob> pageJobs(Page<TemplateJob> page, String keyword, Integer status) {
        LambdaQueryWrapper<TemplateJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateJob::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(TemplateJob::getJobName, keyword);
        }
        if (status != null) {
            wrapper.eq(TemplateJob::getStatus, status);
        }
        wrapper.orderByDesc(TemplateJob::getCreateTime);
        return page(page, wrapper);
    }

    /**
     * 执行单个子项并支持重试策略
     */
    /*private Map<String, Object> executeItemWithRetry(TemplateJobItem item, Long jobId, String jobName) {
        long jobStartTime = System.currentTimeMillis();
        long jobTimeout = 60_000;
        Map<String, Object> variables = null;
        if (StringUtils.hasText(item.getVariables())) {
            try {
                variables = JSON.parseObject(item.getVariables(), new TypeReference<>() {
                });
            } catch (Exception e) {
                log.warn("解析子项变量失败: jobId={}, itemId={}", jobId, item.getId());
            }
        }

        Map<String, Object> lastResult = null;
        for (int attempt = 1; attempt <= Math.max(1, itemRetry); attempt++) {
            try {
                lastResult = executeService.executeTemplateForJob(jobId, jobName, item.getTemplateId(), item.getEnvironmentId(), variables);
                // 如果执行成功或没有再次重试的必要，返回结果
                if (Boolean.TRUE.equals(lastResult.get(ApiResultKeys.SUCCESS.getKey())) || attempt == itemRetry) {
                    return lastResult;
                }
                // 否则等待短暂退避后重试
                try {
                    Thread.sleep(200L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (Exception e) {
                log.error("子项执行尝试失败: jobId={}, itemId={}, attempt={}", jobId, item.getId(), attempt, e);
                lastResult = Map.of(ApiResultKeys.SUCCESS.getKey(), false, ApiResultKeys.MESSAGE.getKey(), e.getMessage(), ApiResultKeys.TEMPLATE_ID.getKey(), item.getTemplateId());
                if (attempt == itemRetry) {
                    return lastResult;
                }
                try {
                    Thread.sleep(200L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (System.currentTimeMillis() - jobStartTime > jobTimeout) {
            log.warn("任务整体执行超时 jobId={}", jobId);
        }
        return lastResult == null ? Map.of(ApiResultKeys.SUCCESS.getKey(), false, ApiResultKeys.MESSAGE.getKey(), "执行失败", ApiResultKeys.TEMPLATE_ID.getKey(), item.getTemplateId()) : lastResult;
    }*/

    @Override
    public IPage<TemplateJobListVO> pageJobsWithLastLog(Page<TemplateJob> page, String keyword, Integer status) {
        IPage<TemplateJob> entityPage = pageJobs(page, keyword, status);
        List<TemplateJobListVO> voList = new ArrayList<>();

        for (TemplateJob job : entityPage.getRecords()) {
            TemplateJobListVO vo = new TemplateJobListVO();
            vo.setId(job.getId());
            vo.setJobName(job.getJobName());
            vo.setCronExpression(job.getCronExpression());
            vo.setStatus(job.getStatus());
            vo.setDescription(job.getDescription());
            vo.setLastExecuteTime(job.getLastExecuteTime());
            vo.setCreateTime(job.getCreateTime());

            // 查询最近一次日志
            List<TemplateJobLog> recentLogs = jobLogMapper.selectRecentByJobId(job.getId(), 1);
            if (!recentLogs.isEmpty()) {
                TemplateJobLog lastLog = recentLogs.get(0);
                vo.setLastExecuteSuccess(lastLog.getSuccess());
                vo.setLastExecuteDurationMs(lastLog.getDurationMs());

                // 解析摘要
                int successCount = 0;
                int failCount = 0;
                if (StringUtils.hasText(lastLog.getExecuteResult())) {
                    try {
                        List<Map<String, Object>> resultList = JSON.parseObject(lastLog.getExecuteResult(), new TypeReference<List<Map<String, Object>>>() {
                        });
                        for (Map<String, Object> item : resultList) {
                            if (Boolean.TRUE.equals(item.get(ApiResultKeys.SUCCESS.getKey()))) {
                                successCount++;
                            } else {
                                failCount++;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析最近一次日志失败: jobId={}", job.getId());
                    }
                }
                vo.setLastExecuteSummary(successCount + "个成功" + (failCount > 0 ? ", " + failCount + "个失败" : ""));
            }

            voList.add(vo);
        }

        IPage<TemplateJobListVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @PostConstruct
    public void initScheduledJobs() {
        log.info("初始化加载启用的定时任务...");
        lambdaQuery().eq(TemplateJob::getStatus, TemplateEnums.JobStatus.ENABLED.getCode()).eq(TemplateJob::getIsDeleted, 0).list()
                .forEach(job -> jobScheduler.scheduleJob(job.getId(), job.getCronExpression(),
                        () -> this.doExecuteJob(job.getId(), job.getJobName())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateJob createJob(TemplateJob job) {
        job.setStatus(Optional.ofNullable(job.getStatus()).orElse(TemplateEnums.JobStatus.ENABLED.getCode()));
        job.setIsDeleted(0);
        if (job.getCreateId() == null) {
            job.setCreateId(1L);
            job.setCreateName("管理员");
        }
        save(job);
        saveJobItems(job);

        TemplateJob detail = getJobDetail(job.getId());
        if (job.getStatus() != null && job.getStatus() == TemplateEnums.JobStatus.ENABLED.getCode()) {
            jobScheduler.scheduleJob(job.getId(), job.getCronExpression(), () -> this.doExecuteJob(job.getId(), job.getJobName()));
        }

        log.info("创建模板任务成功: id={}, name={}, items={}", job.getId(), job.getJobName(),
                job.getItems() == null ? 0 : job.getItems().size());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateJob updateJob(TemplateJob job) {
        if (job.getId() == null) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY, "任务ID不能为空");
        }
        updateById(job);
        jobItemMapper.deleteByJobId(job.getId());
        saveJobItems(job);

        jobScheduler.cancelJob(job.getId());
        TemplateJob detail = getJobDetail(job.getId());
        if (detail.getStatus() != null && detail.getStatus() == TemplateEnums.JobStatus.ENABLED.getCode()) {
            jobScheduler.scheduleJob(job.getId(), detail.getCronExpression(), () -> this.doExecuteJob(job.getId(), detail.getJobName()));
        }

        log.info("更新模板任务成功: id={}", job.getId());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(Long id) {
        jobScheduler.cancelJob(id);
        TemplateJob job = new TemplateJob();
        job.setId(id);
        job.setIsDeleted(1);
        boolean result = updateById(job);
        if (result) {
            jobItemMapper.deleteByJobId(id);
        }
        return result;
    }

    @Override
    public Map<String, Object> triggerJob(Long id) {
        TemplateJob job = Optional.ofNullable(getById(id))
                .orElseThrow(() -> new TemplateValidationException(TemplateValidationException.ErrorType.NOT_FOUND, "任务不存在"));

        if (Integer.valueOf(TemplateEnums.JobStatus.DISABLED.getCode()).equals(job.getStatus())) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, "任务已停用，无法执行");
        }

        return doExecuteJob(id, job.getJobName());
    }

    @Override
    public Map<String, Object> batchTriggerJobs(Long[] ids) {
        List<Long> success = new ArrayList<>();
        List<Long> fail = new ArrayList<>();
        Map<Long, Object> details = new HashMap<>();
        for (Long id : ids) {
            try {
                Map<String, Object> res = triggerJob(id);
                Boolean ok = (Boolean) res.get(ApiResultKeys.SUCCESS.getKey());
                if (Boolean.TRUE.equals(ok)) {
                    success.add(id);
                } else {
                    fail.add(id);
                }
                details.put(id, res);
            } catch (Exception e) {
                log.error("批量触发执行失败: jobId={}", id, e);
                fail.add(id);
                details.put(id, e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successIds", success);
        result.put("failIds", fail);
        result.put("details", details);
        return result;
    }

    @Override
    public String submitBatchTriggerAsync(Long[] ids) {
        String batchId = java.util.UUID.randomUUID().toString();

        // Persist initial batch record using MyBatis-Plus mapper
        TemplateJobBatch batch = new TemplateJobBatch();
        batch.setId(batchId);
        batch.setStatus(TemplateEnums.JobBatchStatus.PENDING.getCode());
        batch.setResult(null);
        batch.setCreateTime(java.time.LocalDateTime.now());
        batch.setUpdateTime(java.time.LocalDateTime.now());
        batchMapper.insert(batch);

        // 使用CompletableFuture异步执行，并设置超时
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // update status to RUNNING
                TemplateJobBatch running = new TemplateJobBatch();
                running.setId(batchId);
                running.setStatus(TemplateEnums.JobBatchStatus.RUNNING.getCode());
                running.setUpdateTime(java.time.LocalDateTime.now());
                batchMapper.updateById(running);

                Map<String, Object> res = batchTriggerJobs(ids);

                // update to DONE with result
                TemplateJobBatch done = new TemplateJobBatch();
                done.setId(batchId);
                done.setStatus(TemplateEnums.JobBatchStatus.DONE.getCode());
                done.setResult(JSON.toJSONString(res));
                done.setUpdateTime(java.time.LocalDateTime.now());
                batchMapper.updateById(done);
            } catch (Exception e) {
                log.error("异步批量触发执行异常: batchId={}", batchId, e);
                TemplateJobBatch failed = new TemplateJobBatch();
                failed.setId(batchId);
                failed.setStatus(TemplateEnums.JobBatchStatus.FAILED.getCode());
                failed.setResult(e.getMessage());
                failed.setUpdateTime(java.time.LocalDateTime.now());
                batchMapper.updateById(failed);
            }
        }, templateJobExecutor);

        // 设置超时，如果超时则取消任务并更新状态
        future.orTimeout(300_000, TimeUnit.MILLISECONDS) // 5分钟超时
                .exceptionally(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        log.warn("异步批量触发超时: batchId={}", batchId);
                        TemplateJobBatch timeoutBatch = new TemplateJobBatch();
                        timeoutBatch.setId(batchId);
                        timeoutBatch.setStatus(TemplateEnums.JobBatchStatus.FAILED.getCode());
                        timeoutBatch.setResult("执行超时");
                        timeoutBatch.setUpdateTime(java.time.LocalDateTime.now());
                        batchMapper.updateById(timeoutBatch);
                    }
                    return null;
                });

        return batchId;
    }

    @Override
    public Map<String, Object> getBatchTriggerResult(String batchId) {
        Map<String, Object> resp = new java.util.HashMap<>();
        // Query DB for batch status/result via mapper
        TemplateJobBatch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            resp.put("status", "NOT_FOUND");
            return resp;
        }
        resp.put("status", batch.getStatus() == null ? "UNKNOWN" : batch.getStatus());
        if (batch.getResult() != null) {
            try {
                Object parsed = JSON.parse(batch.getResult());
                resp.put("details", parsed);
            } catch (Exception e) {
                resp.put("details", batch.getResult());
            }
        }
        return resp;
    }

    @Override
    public Map<String, Object> batchStopJobs(Long[] ids) {
        List<Long> success = new ArrayList<>();
        List<Long> fail = new ArrayList<>();
        Map<Long, Object> details = new java.util.HashMap<>();
        for (Long id : ids) {
            try {
                // 取消调度
                jobScheduler.cancelJob(id);
                // 将任务状态设为停用
                TemplateJob job = new TemplateJob();
                job.setId(id);
                job.setStatus(TemplateEnums.JobStatus.DISABLED.getCode());
                boolean updated = updateById(job);
                if (updated) {
                    success.add(id);
                    details.put(id, "stopped");
                } else {
                    fail.add(id);
                    details.put(id, "update_failed");
                }
            } catch (Exception e) {
                log.error("批量停止任务失败: jobId={}", id, e);
                fail.add(id);
                details.put(id, e.getMessage());
            }
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("successIds", success);
        result.put("failIds", fail);
        result.put("details", details);
        return result;
    }

    @Override
    public List<TemplateJobLog> getRecentLogs(Long jobId, Integer limit) {
        return jobLogMapper.selectRecentByJobId(jobId, Optional.ofNullable(limit).orElse(10));
    }

    @Override
    public IPage<TemplateJobLogVO> pageJobLogs(Page<TemplateJobLog> page, Long jobId, Integer success) {
        LambdaQueryWrapper<TemplateJobLog> wrapper = new LambdaQueryWrapper<>();
        if (jobId != null) {
            wrapper.eq(TemplateJobLog::getJobId, jobId);
        }
        if (success != null) {
            wrapper.eq(TemplateJobLog::getSuccess, success);
        }
        wrapper.orderByDesc(TemplateJobLog::getCreateTime);

        IPage<TemplateJobLog> entityPage = jobLogMapper.selectPage(page, wrapper);
        List<TemplateJobLogVO> voList = new java.util.ArrayList<>();

        for (TemplateJobLog logEntity : entityPage.getRecords()) {
            TemplateJobLogVO vo = new TemplateJobLogVO();
            vo.setId(logEntity.getId());
            vo.setJobId(logEntity.getJobId());
            vo.setTemplateId(logEntity.getTemplateId());
            vo.setSuccess(logEntity.getSuccess());
            vo.setDurationMs(logEntity.getDurationMs());
            vo.setErrorMsg(logEntity.getErrorMsg());
            vo.setCreateTime(logEntity.getCreateTime());

            // 解析 executeResult
            List<TemplateJobLogItemVO> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            if (StringUtils.hasText(logEntity.getExecuteResult())) {
                try {
                    List<Map<String, Object>> resultList = JSON.parseObject(logEntity.getExecuteResult(), new TypeReference<List<Map<String, Object>>>() {
                    });
                    for (Map<String, Object> item : resultList) {
                        TemplateJobLogItemVO itemVO = new TemplateJobLogItemVO();
                        itemVO.setTemplateId(getLongValue(item.get(ApiResultKeys.TEMPLATE_ID.getKey())));
                        itemVO.setTemplateName((String) item.get("templateName"));
                        itemVO.setSuccess(Boolean.TRUE.equals(item.get(ApiResultKeys.SUCCESS.getKey())));
                        itemVO.setStatusCode(getIntValue(item.get("statusCode")));
                        itemVO.setDurationMs(getLongValue(item.get("durationMs")));
                        itemVO.setMessage((String) item.get(ApiResultKeys.MESSAGE.getKey()));
                        itemVO.setRequest(getMapValue(item, "request"));
                        itemVO.setResponse(getMapValue(item, "response"));
                        itemVO.setAssertions(getListMapValue(item, "assertions"));
                        itemVO.setVariables(getMapValue(item, "variables"));
                        results.add(itemVO);

                        if (Boolean.TRUE.equals(item.get(ApiResultKeys.SUCCESS.getKey()))) {
                            successCount++;
                        } else {
                            failCount++;
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析任务日志结果失败: logId={}", logEntity.getId(), e);
                }
            }
            vo.setResults(results);
            vo.setResultSummary(successCount + "个成功" + (failCount > 0 ? ", " + failCount + "个失败" : ""));

            // 填充任务名称
            TemplateJob job = getById(logEntity.getJobId());
            if (job != null) {
                vo.setJobName(job.getJobName());
            }

            voList.add(vo);
        }

        IPage<TemplateJobLogVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private Long getLongValue(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.valueOf(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getIntValue(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.valueOf(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapValue(Map<String, Object> source, String key) {
        Object val = source.get(key);
        if (val instanceof Map) {
            return (Map<String, Object>) val;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getListMapValue(Map<String, Object> source, String key) {
        Object val = source.get(key);
        if (val instanceof List) {
            return (List<Map<String, Object>>) val;
        }
        return null;
    }

    /**
     * 获取任务详情（包含子项）
     */
    public TemplateJob getJobDetail(Long id) {
        TemplateJob job = getById(id);
        if (job != null) {
            job.setItems(jobItemMapper.selectByJobId(id));
        }
        return job;
    }

    private void saveJobItems(TemplateJob job) {
        List<TemplateJobItem> items = job.getItems();
        if (items == null || items.isEmpty()) return;
        int sort = 1;
        for (TemplateJobItem item : items) {
            item.setJobId(job.getId());
            item.setStatus(Optional.ofNullable(item.getStatus()).orElse(TemplateEnums.JobStatus.ENABLED.getCode()));
            item.setSortOrder(sort++);
            jobItemMapper.insert(item);
        }
    }

    /**
     * 执行模板任务核心逻辑（遍历所有子项）
     */
    public Map<String, Object> doExecuteJob(Long jobId, String jobName) {

        ReentrantLock lock = JOB_LOCKS.computeIfAbsent(jobId, id -> new ReentrantLock());
        boolean locked = lock.tryLock();

        if (!locked) {
            log.info("任务已在运行，跳过: jobId={}", jobId);
            return Map.of("success", false, "message", "任务正在执行");
        }
        try {
            TemplateJob job = getById(jobId);
            if (job == null || Integer.valueOf(1).equals(job.getIsDeleted())) {
                return Map.of("success", false, "message", "任务不存在");
            }

            if (Integer.valueOf(0).equals(job.getStatus())) {
                return Map.of("success", false, "message", "任务已停用");
            }

            List<TemplateJobItem> items = jobItemMapper.selectByJobId(jobId);
            if (items == null || items.isEmpty()) {
                throw new TemplateValidationException(
                        TemplateValidationException.ErrorType.BUSINESS_RULE_VIOLATION,
                        "任务未配置模板"
                );
            }

            long startTime = System.currentTimeMillis();

            // 🔥 并发执行
            List<Map<String, Object>> results = jobDispatcher.dispatch(
                    jobId,
                    items,
                    item -> executeSingleItem(jobId, jobName, item)
            );

            boolean allSuccess = results.stream()
                    .allMatch(r -> Boolean.TRUE.equals(r.get("success")));

            String errorMsg = results.stream()
                    .filter(r -> Boolean.FALSE.equals(r.get("success")))
                    .map(r -> String.valueOf(r.get("message")))
                    .reduce((a, b) -> a + "; " + b)
                    .orElse(null);

            // 写日志
            TemplateJobLog jobLog = new TemplateJobLog();
            jobLog.setJobId(jobId);
            jobLog.setSuccess(allSuccess ? 1 : 0);
            jobLog.setExecuteResult(JSON.toJSONString(results));
            jobLog.setErrorMsg(errorMsg);
            jobLog.setDurationMs(System.currentTimeMillis() - startTime);

            jobLogMapper.insert(jobLog);

            job.setLastExecuteTime(LocalDateTime.now());
            updateById(job);

            return Map.of(
                    "success", allSuccess,
                    "results", results,
                    "message", allSuccess ? "全部成功" : errorMsg
            );

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Map<String, Object> executeSingleItem(Long jobId,
                                                  String jobName,
                                                  TemplateJobItem item) {

        Map<String, Object> variables = null;

        if (StringUtils.hasText(item.getVariables())) {
            try {
                variables = JSON.parseObject(item.getVariables(), Map.class);
            } catch (Exception e) {
                log.warn("解析变量失败 itemId={}", item.getId());
            }
        }

        try {
            return executeService.executeTemplateForJob(
                    jobId,
                    jobName,
                    item.getTemplateId(),
                    item.getEnvironmentId(),
                    variables
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "templateId", item.getTemplateId()
            );
        }
    }
}
