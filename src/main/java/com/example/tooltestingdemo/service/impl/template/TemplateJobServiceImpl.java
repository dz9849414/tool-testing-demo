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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final ConcurrentHashMap<Long, ReentrantLock> JOB_LOCKS = new ConcurrentHashMap<>();

    // 批量异步任务幂等控制：key -> batchId
    private static final ConcurrentHashMap<String, String> BATCH_ASYNC_LOCKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CompletableFuture<Void>> BATCH_ASYNC_FUTURES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, AtomicInteger> JOB_FAIL_COUNTS = new ConcurrentHashMap<>();
    //达到次数停止
    private static final int AUTO_DISABLE_THRESHOLD = 3;
    private final Executor templateJobExecutor;
    private final TemplateJobBatchMapper batchMapper;

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

    @Override
    public IPage<TemplateJobListVO> pageJobsWithLastLog(Page<TemplateJob> page, String keyword, Integer status) {
        IPage<TemplateJob> entityPage = pageJobs(page, keyword, status);
        List<TemplateJob> jobs = entityPage.getRecords();

        if (jobs.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        List<Long> jobIds = jobs.stream().map(TemplateJob::getId).toList();

        // 🔥  一次性查所有“最新日志”
        List<TemplateJobLog> lastLogs = jobLogMapper.selectLastLogsByJobIds(jobIds);

        Map<Long, TemplateJobLog> logMap = lastLogs.stream()
                .collect(Collectors.toMap(
                        TemplateJobLog::getJobId,
                        Function.identity(),
                        (a, b) -> a));

        // 🔥组装VO
        List<TemplateJobListVO> voList = jobs.stream().map(job -> {
            TemplateJobListVO vo = new TemplateJobListVO();
            vo.setId(job.getId());
            vo.setJobName(job.getJobName());
            vo.setCronExpression(job.getCronExpression());
            vo.setStatus(job.getStatus());
            vo.setDescription(job.getDescription());
            vo.setLastExecuteTime(job.getLastExecuteTime());
            vo.setCreateTime(job.getCreateTime());

            TemplateJobLog lastLog = logMap.get(job.getId());
            if (lastLog != null) {
                vo.setLastExecuteSuccess(lastLog.getSuccess());
                vo.setLastExecuteDurationMs(lastLog.getDurationMs());
                Summary summary = parseSummary(lastLog.getExecuteResult());
                vo.setLastExecuteSummary(summary.toText());
            }

            // 判断是否正在执行中（通过 ReentrantLock）
            ReentrantLock lock = JOB_LOCKS.get(job.getId());
            vo.setExecuting(lock != null && lock.isLocked());

            return vo;
        }).toList();

        IPage<TemplateJobListVO> voPage =
                new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());

        voPage.setRecords(voList);
        return voPage;
    }

    @PostConstruct
    public void initScheduledJobs() {
        log.info("初始化加载启用的定时任务...");
        jobScheduler.cancelAllJobs();
        lambdaQuery().eq(TemplateJob::getStatus, TemplateEnums.JobStatus.ENABLED.getCode()).eq(TemplateJob::getIsDeleted, 0).list()
                .forEach(job -> jobScheduler.scheduleJob(job.getId(), job.getCronExpression(),
                        () -> this.executeJobForSchedule(job.getId(), job.getJobName())));
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
            jobScheduler.scheduleJob(job.getId(), job.getCronExpression(), () -> this.executeJobForSchedule(job.getId(), job.getJobName()));
        }

        log.info("创建模板任务成功: id={}, name={}, items={}", job.getId(), job.getJobName(),
                job.getItems() == null ? 0 : job.getItems().size());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateJob updateJob(TemplateJob job) {
        if (null == job.getId()) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY, "任务ID不能为空");
        }

        TemplateJob detail = getJobDetail(job.getId());
        if (job.isUpdateStatus()) {
            job.setStatus(Optional.ofNullable(job.getStatus()).orElse(TemplateEnums.JobStatus.ENABLED.getCode()));
        } else {
            jobItemMapper.deleteByJobId(job.getId());
            saveJobItems(job);
        }
        jobScheduler.cancelJob(job.getId());
        updateById(job);
        if (detail.getStatus() != null && detail.getStatus() == TemplateEnums.JobStatus.ENABLED.getCode()) {
            jobScheduler.scheduleJob(job.getId(), detail.getCronExpression(), () -> this.executeJobForSchedule(job.getId(), detail.getJobName()));
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

    /* submitBatchTriggerAsync(ids)
       ├── 生成 batchKey + batchId
       ├── BATCH_ASYNC_LOCKS.putIfAbsent 幂等锁
       ├── insert PENDING
       ├── AtomicBoolean dbUpdated = false
       ├── runAsync
       │     ├── updateBatch(RUNNING)
       │     ├── batchTriggerJobs(ids)  ← 可能阻塞很久
       │     └── updateBatch(DONE)
       └── orTimeout(5分钟)
             └── whenComplete
                   ├── 清理 BATCH_ASYNC_FUTURES / BATCH_ASYNC_LOCKS
                   ├── 若超时：cancel(true) + updateBatch(FAILED, "执行超时")
                   └── 若其他异常且未更新：updateBatch(FAILED, 异常信息)*/
    @Override
    public String submitBatchTriggerAsync(Long[] ids) {
        String batchKey = Arrays.stream(ids).sorted().map(String::valueOf).collect(Collectors.joining(","));

        String batchId = UUID.randomUUID().toString();
        String existBatchId = BATCH_ASYNC_LOCKS.putIfAbsent(batchKey, batchId);
        if (existBatchId != null) {
            log.warn("异步批量任务重复提交, 返回已有batchId={}, key={}", existBatchId, batchKey);
            throw new TemplateValidationException(
                    TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED,
                    String.format("异步批量任务重复提交, 返回已有batchId=%s", existBatchId));
        }

        TemplateJobBatch batch = new TemplateJobBatch();
        batch.setId(batchId);
        batch.setStatus(TemplateEnums.JobBatchStatus.PENDING.getCode());
        batch.setResult(null);
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(LocalDateTime.now());
        batchMapper.insert(batch);

        AtomicBoolean dbUpdated = new AtomicBoolean(false);

        BiConsumer<String, String> updateBatch = (status, result) -> {
            if (dbUpdated.compareAndSet(false, true)) {
                TemplateJobBatch update = new TemplateJobBatch();
                update.setId(batchId);
                update.setStatus(status);
                update.setResult(result);
                update.setUpdateTime(LocalDateTime.now());
                batchMapper.updateById(update);
            }
        };

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                updateBatch.accept(TemplateEnums.JobBatchStatus.RUNNING.getCode(), null);
                Map<String, Object> res = batchTriggerJobs(ids);
                updateBatch.accept(TemplateEnums.JobBatchStatus.DONE.getCode(), JSON.toJSONString(res));
            } catch (Exception e) {
                log.error("异步批量触发执行异常: batchId={}", batchId, e);
                updateBatch.accept(TemplateEnums.JobBatchStatus.FAILED.getCode(), e.getMessage());
            }
        }, templateJobExecutor);

        BATCH_ASYNC_FUTURES.put(batchKey, future);

        future.orTimeout(300_000, TimeUnit.MILLISECONDS) // 5分钟超时
                .whenComplete((v, t) -> {
                    BATCH_ASYNC_FUTURES.remove(batchKey);
                    BATCH_ASYNC_LOCKS.remove(batchKey, batchId);

                    if (t instanceof TimeoutException) {
                        log.warn("异步批量触发超时: batchId={}", batchId);
                        future.cancel(true);
                        updateBatch.accept(TemplateEnums.JobBatchStatus.FAILED.getCode(), "执行超时");
                    } else if (t != null && !dbUpdated.get()) {
                        // 其他未被 catch 的异常兜底
                        updateBatch.accept(TemplateEnums.JobBatchStatus.FAILED.getCode(), t.getMessage());
                    }
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
        if (items == null || items.isEmpty()) {
            throw new TemplateValidationException(
                    TemplateValidationException.ErrorType.BUSINESS_RULE_VIOLATION,
                    "任务模板列表不能为空"
            );
        }
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

    /**
     * 供自动调度使用的执行入口（带连续失败自动停用保护）
     */
    private void executeJobForSchedule(Long jobId, String jobName) {
        Map<String, Object> result;
        try {
            result = doExecuteJob(jobId, jobName);
        } catch (Exception e) {
            log.error("定时任务执行异常 jobId={}", jobId, e);
            result = Map.of("success", false, "message", e.getMessage());
        }

        AtomicInteger counter = JOB_FAIL_COUNTS.computeIfAbsent(jobId, k -> new AtomicInteger(0));
        if (Boolean.FALSE.equals(result.get("success"))) {
            int failCount = counter.incrementAndGet();
            log.warn("定时任务执行失败 jobId={}, 连续失败次数={}/{}", jobId, failCount, AUTO_DISABLE_THRESHOLD);
            if (failCount >= AUTO_DISABLE_THRESHOLD) {
                log.error("定时任务连续失败{}次，自动停用: jobId={}", AUTO_DISABLE_THRESHOLD, jobId);
                jobScheduler.cancelJob(jobId);
                TemplateJob stopJob = new TemplateJob();
                stopJob.setId(jobId);
                stopJob.setStatus(TemplateEnums.JobStatus.DISABLED.getCode());
                updateById(stopJob);
                JOB_FAIL_COUNTS.remove(jobId);
            }
        } else {
            counter.set(0);
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

    private Summary parseSummary(String json) {
        if (!StringUtils.hasText(json)) {
            return new Summary(0, 0);
        }

        int success = 0;
        int fail = 0;

        try {
            List<Map<String, Object>> resultList =
                    JSON.parseObject(json, new TypeReference<>() {
                    });
            for (Map<String, Object> item : resultList) {
                if (Boolean.TRUE.equals(item.get(ApiResultKeys.SUCCESS.getKey()))) {
                    success++;
                } else {
                    fail++;
                }
            }
        } catch (Exception e) {
            log.warn("解析执行结果失败", e);
        }

        return new Summary(success, fail);
    }

    @Data
    @AllArgsConstructor
    static class Summary {
        int success;
        int fail;

        String toText() {
            return success + "个成功" + (fail > 0 ? ", " + fail + "个失败" : "");
        }
    }
}
