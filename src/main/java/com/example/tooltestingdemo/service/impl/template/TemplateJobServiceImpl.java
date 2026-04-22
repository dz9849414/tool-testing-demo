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
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import com.example.tooltestingdemo.service.template.TemplateExecuteService;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.TemplateJobService;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import com.example.tooltestingdemo.util.TraceIdContext;
import com.example.tooltestingdemo.vo.TemplateJobListVO;
import com.example.tooltestingdemo.vo.TemplateJobLogItemVO;
import com.example.tooltestingdemo.vo.TemplateJobLogVO;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
import com.example.tooltestingdemo.utils.SecurityUtils;

/**
 * 模板定时任务 Service 实现
 * 模板定时任务 Service 实现
 * 模板定时任务 Service 实现
 * 模板定时任务 Service 实现
 * 模板定时任务 Service 实现
 * 模板定时任务 Service 实现
 * 模板定时任务 Service 实现
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
    private final InterfaceTemplateService interfaceTemplateService;
    private final TemplateEnvironmentService templateEnvironmentService;
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

    @Resource
    @Lazy
    private TemplateJobService jobServiceProxy;

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

        // 一次性查询所有最新日志
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
            vo.setCreateName(job.getCreateName());
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
            job.setCreateId(getCurrentUserIdOrDefault());
            job.setCreateName(getCurrentUsernameOrDefault());
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
        boolean result = removeById(id);
        if (result) {
            jobItemMapper.deleteByJobId(id);
        }
        return result;
    }

    @Override
    public Map<String, Object> triggerJob(Long id) {
        TemplateJob job = Optional.ofNullable(getById(id))
            .orElseThrow(() -> new TemplateValidationException(TemplateValidationException.ErrorType.NOT_FOUND, "\u4efb\u52a1\u4e0d\u5b58\u5728"));

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
       │     ├── batchTriggerJobs(ids)  // 可能阻塞很久
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
        wrapper.orderByDesc(TemplateJobLog::getExecuteAt);

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
            vo.setCreateTime(logEntity.getExecuteAt());

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
            vo.setResultSummary(successCount + "\u4e2a\u6210\u529f" + (failCount > 0 ? ", " + failCount + "\u4e2a\u5931\u8d25" : ""));

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

    @Override
    public String exportJobs(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return JSON.toJSONString(Map.of("jobs", Collections.emptyList()));
        }

        List<Map<String, Object>> jobs = Arrays.stream(ids)
            .map(this::getJobDetail)
            .filter(Objects::nonNull)
            .map(this::convertJobForExport)
            .toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("version", "1.0");
        payload.put("exportTime", LocalDateTime.now());
        payload.put("jobs", jobs);
        return JSON.toJSONString(payload);
    }

    @Override
    public Map<String, Object> importJobs(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "导入文件不能为空"
            );
        }

        List<TemplateJob> importedJobs = parseImportedJobs(readFileContent(file));
        if (importedJobs.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                "导入文件中未找到任务配置"
            );
        }

        List<Map<String, Object>> successDetails = new ArrayList<>();
        List<Map<String, Object>> failDetails = new ArrayList<>();
        ImportValidationContext validationContext = buildImportValidationContext(importedJobs);

        int index = 0;
        for (TemplateJob importedJob : importedJobs) {
            index++;
            try {
                TemplateJob normalized = normalizeImportedJob(importedJob);
                validateImportedJob(index, normalized, validationContext);
                TemplateJob created = jobServiceProxy.createJob(normalized);
                successDetails.add(Map.of(
                    "index", index,
                    "jobId", created.getId(),
                    "jobName", created.getJobName(),
                    "status", "CREATED"
                ));
            } catch (Exception e) {
                log.error("导入定时任务失败: index={}, jobName={}", index, importedJob == null ? null : importedJob.getJobName(), e);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("index", index);
                item.put("jobName", importedJob == null ? null : importedJob.getJobName());
                item.put("message", e.getMessage());
                failDetails.add(item);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", importedJobs.size());
        result.put("successCount", successDetails.size());
        result.put("failCount", failDetails.size());
        result.put("successItems", successDetails);
        result.put("failItems", failDetails);
        return result;
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
        boolean traceCreatedHere = !StringUtils.hasText(TraceIdContext.get());
        String traceId = TraceIdContext.getOrCreate();

        ReentrantLock lock = JOB_LOCKS.computeIfAbsent(jobId, id -> new ReentrantLock());
        boolean locked = lock.tryLock();

        if (!locked) {
            log.info("\u4efb\u52a1\u5df2\u5728\u6267\u884c\uff0c\u8df3\u8fc7\u672c\u6b21\u8bf7\u6c42 jobId={}, traceId={}", jobId, traceId);
            if (traceCreatedHere) {
                TraceIdContext.clear();
            }
            return Map.of("success", false, "message", "\u4efb\u52a1\u6b63\u5728\u6267\u884c", "traceId", traceId);
        }
        try {
            log.info("\u5f00\u59cb\u6267\u884c\u4efb\u52a1 jobId={}, jobName={}, traceId={}", jobId, jobName, traceId);
            TemplateJob job = getById(jobId);
            if (job == null || Integer.valueOf(1).equals(job.getIsDeleted())) {
                return Map.of("success", false, "message", "\u4efb\u52a1\u4e0d\u5b58\u5728", "traceId", traceId);
            }

            if (Integer.valueOf(0).equals(job.getStatus())) {
                return Map.of("success", false, "message", "\u4efb\u52a1\u5df2\u505c\u7528", "traceId", traceId);
            }

            List<TemplateJobItem> items = jobItemMapper.selectByJobId(jobId);
            if (items == null || items.isEmpty()) {
                throw new TemplateValidationException(
                    TemplateValidationException.ErrorType.BUSINESS_RULE_VIOLATION,
                    "\u4efb\u52a1\u672a\u914d\u7f6e\u4efb\u52a1\u9879"
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
            jobLog.setTraceId(traceId);

            jobLogMapper.insert(jobLog);

            job.setLastExecuteTime(LocalDateTime.now());
            updateById(job);

            return Map.of(
                "success", allSuccess,
                "results", results,
                "message", allSuccess ? "\u5168\u90e8\u6210\u529f" : errorMsg,
                "traceId", traceId
            );

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            if (traceCreatedHere) {
                TraceIdContext.clear();
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
                "templateId", item.getTemplateId(),
                "traceId", TraceIdContext.get()
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

    private Map<String, Object> convertJobForExport(TemplateJob job) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("jobName", job.getJobName());
        payload.put("cronExpression", job.getCronExpression());
        payload.put("status", job.getStatus());
        payload.put("description", job.getDescription());
        payload.put("items", Optional.ofNullable(job.getItems()).orElse(Collections.emptyList()).stream()
            .map(this::convertJobItemForExport)
            .toList());
        return payload;
    }

    private Map<String, Object> convertJobItemForExport(TemplateJobItem item) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("templateId", item.getTemplateId());
        payload.put("environmentId", item.getEnvironmentId());
        payload.put("variables", item.getVariables());
        payload.put("sortOrder", item.getSortOrder());
        payload.put("status", item.getStatus());
        return payload;
    }

    private String readFileContent(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        } catch (Exception e) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                "读取导入文件失败: " + e.getMessage(),
                e
            );
        }
        return content.toString();
    }

    private List<TemplateJob> parseImportedJobs(String content) {
        try {
            Map<String, Object> payload = JSON.parseObject(content, new TypeReference<Map<String, Object>>() {
            });
            Object jobs = payload.get("jobs");
            if (jobs instanceof Collection<?>) {
                return ((Collection<?>) jobs).stream()
                    .map(item -> JSON.parseObject(JSON.toJSONString(item), TemplateJob.class))
                    .toList();
            }
        } catch (Exception ignored) {
            // Fall back to array parsing for backward compatibility.
        }

        try {
            return JSON.parseObject(content, new TypeReference<List<TemplateJob>>() {
            });
        } catch (Exception e) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                "\u5bfc\u5165\u6587\u4ef6\u683c\u5f0f\u4e0d\u6b63\u786e"
            );
        }
    }

    private TemplateJob normalizeImportedJob(TemplateJob importedJob) {
        if (importedJob == null) {
            return null;
        }
        TemplateJob normalized = new TemplateJob();
        normalized.setJobName(importedJob.getJobName());
        normalized.setCronExpression(importedJob.getCronExpression());
        normalized.setStatus(importedJob.getStatus());
        normalized.setDescription(importedJob.getDescription());
        normalized.setCreateId(null);
        normalized.setCreateName(null);
        normalized.setIsDeleted(0);

        List<TemplateJobItem> importedItems = Optional.ofNullable(importedJob.getItems()).orElse(Collections.emptyList());
        List<TemplateJobItem> normalizedItems = new ArrayList<>();
        for (TemplateJobItem importedItem : importedItems) {
            TemplateJobItem item = new TemplateJobItem();
            item.setTemplateId(importedItem.getTemplateId());
            item.setEnvironmentId(importedItem.getEnvironmentId());
            item.setVariables(importedItem.getVariables());
            item.setSortOrder(importedItem.getSortOrder());
            item.setStatus(importedItem.getStatus());
            normalizedItems.add(item);
        }
        normalized.setItems(normalizedItems);
        return normalized;
    }

    private ImportValidationContext buildImportValidationContext(List<TemplateJob> importedJobs) {
        Set<String> duplicateJobNames = collectDuplicateJobNames(importedJobs);
        Set<String> importedJobNames = collectImportedJobNames(importedJobs);
        Set<Long> templateIds = collectTemplateIds(importedJobs);
        Set<Long> environmentIds = collectEnvironmentIds(importedJobs);

        Set<String> existingJobNames = importedJobNames.isEmpty()
            ? Collections.emptySet()
            : lambdaQuery()
              .in(TemplateJob::getJobName, importedJobNames)
              .eq(TemplateJob::getIsDeleted, 0)
              .list()
              .stream()
              .map(TemplateJob::getJobName)
              .filter(StringUtils::hasText)
              .map(String::trim)
              .collect(Collectors.toSet());

        Set<Long> existingTemplateIds = templateIds.isEmpty()
            ? Collections.emptySet()
            : interfaceTemplateService.listByIds(templateIds)
              .stream()
              .map(com.example.tooltestingdemo.entity.template.InterfaceTemplate::getId)
              .collect(Collectors.toSet());

        Set<Long> existingEnvironmentIds = environmentIds.isEmpty()
            ? Collections.emptySet()
            : templateEnvironmentService.listByIds(environmentIds)
              .stream()
              .map(TemplateEnvironment::getId)
              .collect(Collectors.toSet());

        return new ImportValidationContext(
            duplicateJobNames,
            existingJobNames,
            existingTemplateIds,
            existingEnvironmentIds
        );
    }

    private Set<String> collectDuplicateJobNames(List<TemplateJob> importedJobs) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (TemplateJob job : importedJobs) {
            if (job == null || !StringUtils.hasText(job.getJobName())) {
                continue;
            }
            String normalizedName = job.getJobName().trim();
            if (!seen.add(normalizedName)) {
                duplicates.add(normalizedName);
            }
        }
        return duplicates;
    }

    private Set<String> collectImportedJobNames(List<TemplateJob> importedJobs) {
        return importedJobs.stream()
            .filter(Objects::nonNull)
            .map(TemplateJob::getJobName)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .collect(Collectors.toSet());
    }

    private Set<Long> collectTemplateIds(List<TemplateJob> importedJobs) {
        return importedJobs.stream()
            .filter(Objects::nonNull)
            .map(TemplateJob::getItems)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .map(TemplateJobItem::getTemplateId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Set<Long> collectEnvironmentIds(List<TemplateJob> importedJobs) {
        return importedJobs.stream()
            .filter(Objects::nonNull)
            .map(TemplateJob::getItems)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .map(TemplateJobItem::getEnvironmentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private void validateImportedJob(int index, TemplateJob job, ImportValidationContext validationContext) {
        if (job == null) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                "\u7b2c" + index + "\u6761\u4efb\u52a1\u6570\u636e\u4e3a\u7a7a"
            );
        }

        if (!StringUtils.hasText(job.getJobName())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "\u7b2c" + index + "\u6761\u4efb\u52a1\u7f3a\u5c11\u4efb\u52a1\u540d\u79f0"
            );
        }

        String jobName = job.getJobName().trim();
        job.setJobName(jobName);

        if (validationContext.duplicateJobNames().contains(jobName)) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.ALREADY_EXISTS,
                "\u5bfc\u5165\u6587\u4ef6\u4e2d\u5b58\u5728\u91cd\u590d\u4efb\u52a1\u540d: " + jobName
            );
        }

        if (validationContext.existingJobNames().contains(jobName)) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.ALREADY_EXISTS,
                "\u4efb\u52a1\u540d\u79f0\u5df2\u5b58\u5728: " + jobName
            );
        }

        if (!StringUtils.hasText(job.getCronExpression())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "\u4efb\u52a1[" + jobName + "]\u7f3a\u5c11 Cron \u8868\u8fbe\u5f0f"
            );
        }

        if (!CronExpression.isValidExpression(job.getCronExpression().trim())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                "\u4efb\u52a1[" + jobName + "] Cron \u8868\u8fbe\u5f0f\u4e0d\u5408\u6cd5"
            );
        }
        job.setCronExpression(job.getCronExpression().trim());

        List<TemplateJobItem> items = job.getItems();
        if (items == null || items.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.BUSINESS_RULE_VIOLATION,
                "\u4efb\u52a1[" + jobName + "]\u672a\u914d\u7f6e\u4efb\u52a1\u9879"
            );
        }

        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            validateImportedJobItem(jobName, itemIndex + 1, items.get(itemIndex), validationContext);
        }
    }

    private void validateImportedJobItem(String jobName, int itemIndex, TemplateJobItem item,
                                         ImportValidationContext validationContext) {
        if (item == null) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                "\u4efb\u52a1[" + jobName + "]\u7b2c" + itemIndex + "\u4e2a\u5b50\u9879\u4e3a\u7a7a"
            );
        }

        if (item.getTemplateId() == null) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "\u4efb\u52a1[" + jobName + "]\u7b2c" + itemIndex + "\u4e2a\u5b50\u9879\u7f3a\u5c11 templateId"
            );
        }

        if (!validationContext.existingTemplateIds().contains(item.getTemplateId())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.NOT_FOUND,
                "\u4efb\u52a1[" + jobName + "]\u7b2c" + itemIndex + "\u4e2a\u5b50\u9879\u5f15\u7528\u7684\u6a21\u677f\u4e0d\u5b58\u5728: " + item.getTemplateId()
            );
        }

    }

    private record ImportValidationContext(
        Set<String> duplicateJobNames,
        Set<String> existingJobNames,
        Set<Long> existingTemplateIds,
        Set<Long> existingEnvironmentIds
    ) {
    }

    @Data
    @AllArgsConstructor
    static class Summary {
        int success;
        int fail;

        String toText() {
            return success + "\u4e2a\u6210\u529f" + (fail > 0 ? ", " + fail + "\u4e2a\u5931\u8d25" : "");
        }
    }

    private Long getCurrentUserIdOrDefault() {
        String userId = String.valueOf(SecurityUtils.getUserId());
        if (StringUtils.hasText(userId)) {
            try {
                return Long.valueOf(userId);
            } catch (NumberFormatException ignored) {
            }
        }
        return 1L;
    }

    private String getCurrentUsernameOrDefault() {
        String username = SecurityUtils.getUsername();
        return StringUtils.hasText(username) ? username : "\u7ba1\u7406\u5458";
    }
}
