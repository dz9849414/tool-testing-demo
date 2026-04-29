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
    private static final ConcurrentHashMap<String, BatchRuntimeContext> BATCH_RUNTIME_CONTEXTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, AtomicInteger> JOB_FAIL_COUNTS = new ConcurrentHashMap<>();
    //达到次数停止
    private static final int AUTO_DISABLE_THRESHOLD = 3;
    private static final String LOG_INIT_SCHEDULED_JOBS = "初始化加载启用的定时任务...";
    private static final String LOG_CREATE_JOB_SUCCESS = "创建模板任务成功: id={}, name={}, items={}";
    private static final String LOG_UPDATE_JOB_SUCCESS = "更新模板任务成功: id={}";
    private static final String LOG_BATCH_TRIGGER_FAILED = "批量触发执行失败: jobId={}";
    private static final String LOG_DUPLICATE_ASYNC_BATCH = "异步批量任务重复提交, 返回已有batchId={}, key={}";
    private static final String MSG_DUPLICATE_ASYNC_BATCH = "异步批量任务重复提交, 返回已有batchId=%s";
    private static final String LOG_ASYNC_BATCH_EXECUTE_ERROR = "异步批量触发执行异常: batchId={}";
    private static final String LOG_ASYNC_BATCH_TIMEOUT = "异步批量触发超时: batchId={}";
    private static final String MSG_EXECUTION_TIMEOUT = "执行超时";
    private static final String LOG_BATCH_STOP_FAILED = "批量停止任务失败: jobId={}";
    private static final String LOG_PARSE_JOB_LOG_RESULT_FAILED = "解析任务日志结果失败: logId={}";
    private static final String MSG_SUMMARY_SUCCESS_SUFFIX = "个成功";
    private static final String MSG_SUMMARY_FAIL_TEMPLATE = ", %d个失败";
    private static final String MSG_JOB_ID_REQUIRED = "任务ID不能为空";
    private static final String MSG_JOB_NOT_FOUND = "任务不存在";
    private static final String MSG_JOB_DISABLED_CANNOT_EXECUTE = "任务已停用，无法执行";
    private static final String MSG_IMPORT_FILE_EMPTY = "导入文件不能为空";
    private static final String MSG_IMPORT_JOB_CONFIG_NOT_FOUND = "导入文件中未找到任务配置";
    private static final String LOG_IMPORT_JOB_FAILED = "导入定时任务失败: index={}, jobName={}";
    private static final String LOG_BATCH_EXECUTE_START = "批量执行开始: batchId={}, totalCount={}";
    private static final String LOG_BATCH_EXECUTE_ITEM = "批量执行任务: batchId={}, index={}, totalCount={}, jobId={}";
    private static final String LOG_BATCH_EXECUTE_FINISH = "批量执行结束: batchId={}, status={}, completed={}, total={}";
    private static final String LOG_BATCH_EXECUTE_UNCAUGHT = "批量执行异常: batchId={}";
    private static final String MSG_BATCH_NOT_FOUND = "批次不存在";
    private static final String MSG_BATCH_ALREADY_RUNNING = "批次正在执行中";
    private static final String MSG_BATCH_NOT_RUNNING = "批次未在执行中";
    private static final String MSG_BATCH_NOT_PAUSED = "当前批次不是暂停状态";
    private static final String MSG_BATCH_NO_FAILED_ITEMS = "当前批次没有可重试的失败项";
    private static final String MSG_BATCH_IDS_EMPTY = "任务ID列表不能为空";
    private static final String MSG_BATCH_PAUSE_REQUESTED = "暂停请求已记录，将在当前任务完成后暂停";
    private static final String MSG_BATCH_RESUME_SUBMITTED = "批次恢复执行已提交";
    private static final String MSG_BATCH_RETRY_SUBMITTED = "失败项重试批次已提交";
    private static final String MSG_BATCH_ALREADY_PAUSED = "批次已暂停";
    private static final String MSG_BATCH_CANCEL_REQUESTED = "取消请求已记录，将在当前任务完成后取消";
    private static final String MSG_BATCH_STATUS_UNKNOWN = "UNKNOWN";
    private static final String MSG_BATCH_STATUS_NOT_FOUND = "NOT_FOUND";
    private static final String MSG_BATCH_PAUSED = "批次已暂停";
    private static final String MSG_BATCH_CANCELED = "批次已取消";
    private static final String MSG_BATCH_DONE = "批次执行完成";
    private static final String MSG_BATCH_RUNNING = "批次执行中";
    private static final String MSG_JOB_ITEM_LIST_EMPTY = "任务模板列表不能为空";
    private static final String LOG_JOB_ALREADY_RUNNING = "任务已在执行，跳过本次请求 jobId={}, traceId={}";
    private static final String MSG_JOB_RUNNING = "任务正在执行";
    private static final String LOG_START_EXECUTE_JOB = "开始执行任务 jobId={}, jobName={}, traceId={}";
    private static final String MSG_JOB_DISABLED = "任务已停用";
    private static final String MSG_JOB_ITEM_NOT_CONFIGURED = "任务未配置任务项";
    private static final String MSG_ALL_SUCCESS = "全部成功";
    private static final String LOG_DEMO_PAUSE_START = "演示暂停开始 jobId={}, jobName={}, sleepMs={}, traceId={}";
    private static final String MSG_DEMO_PAUSE_INTERRUPTED = "任务演示暂停被中断";
    private static final String LOG_DEMO_PAUSE_END = "演示暂停结束 jobId={}, jobName={}, traceId={}";
    private static final String LOG_SCHEDULE_EXECUTE_ERROR = "定时任务执行异常 jobId={}";
    private static final String LOG_SCHEDULE_EXECUTE_FAILED = "定时任务执行失败 jobId={}, 连续失败次数={}/{}";
    private static final String LOG_SCHEDULE_AUTO_DISABLED = "定时任务连续失败{}次，自动停用: jobId={}";
    private static final String LOG_PARSE_VARIABLES_FAILED = "解析变量失败 itemId={}";
    private static final String LOG_PARSE_EXECUTE_RESULT_FAILED = "解析执行结果失败";
    private static final String MSG_READ_IMPORT_FILE_FAILED = "读取导入文件失败: ";
    private static final String MSG_IMPORT_FILE_FORMAT_INVALID = "导入文件格式不正确";
    private static final String MSG_IMPORT_JOB_DATA_EMPTY_TEMPLATE = "第%d条任务数据为空";
    private static final String MSG_IMPORT_JOB_NAME_REQUIRED_TEMPLATE = "第%d条任务缺少任务名称";
    private static final String MSG_IMPORT_DUPLICATE_JOB_NAME_TEMPLATE = "导入文件中存在重复任务名: %s";
    private static final String MSG_JOB_NAME_ALREADY_EXISTS_TEMPLATE = "任务名称已存在: %s";
    private static final String MSG_JOB_CRON_REQUIRED_TEMPLATE = "任务[%s]缺少 Cron 表达式";
    private static final String MSG_JOB_CRON_INVALID_TEMPLATE = "任务[%s] Cron 表达式不合法";
    private static final String MSG_JOB_ITEMS_NOT_CONFIGURED_TEMPLATE = "任务[%s]未配置任务项";
    private static final String MSG_JOB_ITEM_EMPTY_TEMPLATE = "任务[%s]第%d个子项为空";
    private static final String MSG_JOB_ITEM_TEMPLATE_ID_REQUIRED_TEMPLATE = "任务[%s]第%d个子项缺少 templateId";
    private static final String MSG_JOB_ITEM_TEMPLATE_NOT_FOUND_TEMPLATE = "任务[%s]第%d个子项引用的模板不存在: %s";
    private static final String MSG_ADMIN_DEFAULT_NAME = "管理员";
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
        if (entityPage.getRecords().isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        IPage<TemplateJobListVO> voPage =
            new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(buildJobListVOs(entityPage.getRecords()));
        return voPage;
    }

    @Override
    public List<TemplateJobListVO> listJobsByTemplateId(Long templateId) {
        if (templateId == null) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "模板ID不能为空"
            );
        }

        List<Long> jobIds = jobItemMapper.selectByTemplateId(templateId).stream()
            .map(TemplateJobItem::getJobId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (jobIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<TemplateJob> jobs = lambdaQuery()
            .eq(TemplateJob::getIsDeleted, 0)
            .in(TemplateJob::getId, jobIds)
            .orderByDesc(TemplateJob::getCreateTime)
            .list();
        return buildJobListVOs(jobs);
    }

    @PostConstruct
    public void initScheduledJobs() {
        log.info(LOG_INIT_SCHEDULED_JOBS);
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

        log.info(LOG_CREATE_JOB_SUCCESS, job.getId(), job.getJobName(),
            job.getItems() == null ? 0 : job.getItems().size());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateJob updateJob(TemplateJob job) {
        if (null == job.getId()) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY, MSG_JOB_ID_REQUIRED);
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

        log.info(LOG_UPDATE_JOB_SUCCESS, job.getId());
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
            .orElseThrow(() -> new TemplateValidationException(TemplateValidationException.ErrorType.NOT_FOUND, MSG_JOB_NOT_FOUND));

        if (Integer.valueOf(TemplateEnums.JobStatus.DISABLED.getCode()).equals(job.getStatus())) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, MSG_JOB_DISABLED_CANNOT_EXECUTE);
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
                log.error(LOG_BATCH_TRIGGER_FAILED, id, e);
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
        validateBatchIds(ids);
        String batchId = UUID.randomUUID().toString();
        BatchExecutionSnapshot snapshot = createInitialBatchSnapshot(batchId, null, Arrays.asList(ids));
        acquireBatchKey(snapshot.getJobIds(), batchId);
        insertBatchRecord(batchId, TemplateEnums.JobBatchStatus.PENDING.getCode(), snapshot);
        startBatchExecution(snapshot, batchId, false);
        return batchId;
    }

    @Override
    public Map<String, Object> getBatchTriggerResult(String batchId) {
        return getBatchTriggerProgress(batchId);
    }

    @Override
    public Map<String, Object> getBatchTriggerProgress(String batchId) {
        TemplateJobBatch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            return Map.of(TemplateEnums.ApiResultKeys.STATUS.getKey(), MSG_BATCH_STATUS_NOT_FOUND);
        }
        BatchExecutionSnapshot snapshot = parseBatchSnapshot(batch);
        return toBatchProgressResponse(batch, snapshot);
    }

    @Override
    public Map<String, Object> pauseBatchTrigger(String batchId) {
        TemplateJobBatch batch = getBatchOrThrow(batchId);
        if (TemplateEnums.JobBatchStatus.PAUSED.getCode().equalsIgnoreCase(batch.getStatus())) {
            return Map.of(
                "batchId", batchId,
                TemplateEnums.ApiResultKeys.STATUS.getKey(), batch.getStatus(),
                TemplateEnums.ApiResultKeys.MESSAGE.getKey(), MSG_BATCH_ALREADY_PAUSED
            );
        }
        if (!TemplateEnums.JobBatchStatus.RUNNING.getCode().equalsIgnoreCase(batch.getStatus())) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, MSG_BATCH_NOT_RUNNING);
        }
        BatchRuntimeContext context = BATCH_RUNTIME_CONTEXTS.get(batchId);
        if (context == null) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, MSG_BATCH_NOT_RUNNING);
        }
        context.pauseRequested.set(true);
        BatchExecutionSnapshot snapshot = parseBatchSnapshot(batch);
        snapshot.setMessage(MSG_BATCH_PAUSE_REQUESTED);
        updateBatchRecord(batchId, batch.getStatus(), snapshot);
        return Map.of(
            "batchId", batchId,
            TemplateEnums.ApiResultKeys.STATUS.getKey(), batch.getStatus(),
            TemplateEnums.ApiResultKeys.MESSAGE.getKey(), MSG_BATCH_PAUSE_REQUESTED
        );
    }

    @Override
    public Map<String, Object> cancelBatchTrigger(String batchId) {
        TemplateJobBatch batch = getBatchOrThrow(batchId);
        if (TemplateEnums.JobBatchStatus.CANCELED.getCode().equalsIgnoreCase(batch.getStatus())) {
            return Map.of(
                "batchId", batchId,
                TemplateEnums.ApiResultKeys.STATUS.getKey(), batch.getStatus(),
                TemplateEnums.ApiResultKeys.MESSAGE.getKey(), MSG_BATCH_CANCELED
            );
        }
        if (!(TemplateEnums.JobBatchStatus.RUNNING.getCode().equalsIgnoreCase(batch.getStatus())
            || TemplateEnums.JobBatchStatus.PAUSED.getCode().equalsIgnoreCase(batch.getStatus())
            || TemplateEnums.JobBatchStatus.PENDING.getCode().equalsIgnoreCase(batch.getStatus()))) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, MSG_BATCH_NOT_RUNNING);
        }
        BatchRuntimeContext context = BATCH_RUNTIME_CONTEXTS.get(batchId);
        BatchExecutionSnapshot snapshot = parseBatchSnapshot(batch);
        if (context == null || TemplateEnums.JobBatchStatus.PAUSED.getCode().equalsIgnoreCase(batch.getStatus())) {
            snapshot.setMessage(MSG_BATCH_CANCELED);
            snapshot.setCurrentJobId(null);
            snapshot.setCurrentJobName(null);
            updateBatchRecord(batchId, TemplateEnums.JobBatchStatus.CANCELED.getCode(), snapshot);
        } else {
            context.cancelRequested.set(true);
            snapshot.setMessage(MSG_BATCH_CANCEL_REQUESTED);
            updateBatchRecord(batchId, batch.getStatus(), snapshot);
        }
        return Map.of(
            "batchId", batchId,
            TemplateEnums.ApiResultKeys.STATUS.getKey(),
                context == null || TemplateEnums.JobBatchStatus.PAUSED.getCode().equalsIgnoreCase(batch.getStatus())
                    ? TemplateEnums.JobBatchStatus.CANCELED.getCode()
                    : batch.getStatus(),
            TemplateEnums.ApiResultKeys.MESSAGE.getKey(),
                context == null || TemplateEnums.JobBatchStatus.PAUSED.getCode().equalsIgnoreCase(batch.getStatus())
                    ? MSG_BATCH_CANCELED
                    : MSG_BATCH_CANCEL_REQUESTED
        );
    }

    @Override
    public Map<String, Object> resumeBatchTrigger(String batchId) {
        TemplateJobBatch batch = getBatchOrThrow(batchId);
        if (!TemplateEnums.JobBatchStatus.PAUSED.getCode().equalsIgnoreCase(batch.getStatus())) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, MSG_BATCH_NOT_PAUSED);
        }
        BatchExecutionSnapshot snapshot = parseBatchSnapshot(batch);
        acquireBatchKey(snapshot.getPendingJobIds(), batchId);
        startBatchExecution(snapshot, batchId, true);
        return Map.of(
            "batchId", batchId,
            TemplateEnums.ApiResultKeys.STATUS.getKey(), TemplateEnums.JobBatchStatus.RUNNING.getCode(),
            TemplateEnums.ApiResultKeys.MESSAGE.getKey(), MSG_BATCH_RESUME_SUBMITTED
        );
    }

    @Override
    public Map<String, Object> retryFailedBatchTrigger(String batchId) {
        TemplateJobBatch batch = getBatchOrThrow(batchId);
        BatchExecutionSnapshot snapshot = parseBatchSnapshot(batch);
        List<Long> retryIds = snapshot.getRetryableJobIds();
        if (retryIds.isEmpty()) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, MSG_BATCH_NO_FAILED_ITEMS);
        }
        String retryBatchId = UUID.randomUUID().toString();
        BatchExecutionSnapshot retrySnapshot = createInitialBatchSnapshot(retryBatchId, batchId, retryIds);
        acquireBatchKey(retrySnapshot.getJobIds(), retryBatchId);
        insertBatchRecord(retryBatchId, TemplateEnums.JobBatchStatus.PENDING.getCode(), retrySnapshot);
        startBatchExecution(retrySnapshot, retryBatchId, false);
        return Map.of(
            "batchId", retryBatchId,
            "sourceBatchId", batchId,
            TemplateEnums.ApiResultKeys.STATUS.getKey(), TemplateEnums.JobBatchStatus.PENDING.getCode(),
            TemplateEnums.ApiResultKeys.MESSAGE.getKey(), MSG_BATCH_RETRY_SUBMITTED
        );
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
                log.error(LOG_BATCH_STOP_FAILED, id, e);
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

    private void validateBatchIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY, MSG_BATCH_IDS_EMPTY);
        }
    }

    private BatchExecutionSnapshot createInitialBatchSnapshot(String batchId, String sourceBatchId, List<Long> jobIds) {
        BatchExecutionSnapshot snapshot = new BatchExecutionSnapshot();
        snapshot.setBatchId(batchId);
        snapshot.setSourceBatchId(sourceBatchId);
        snapshot.setJobIds(new ArrayList<>(jobIds));
        snapshot.setTotalCount(jobIds.size());
        snapshot.setCompletedCount(0);
        snapshot.setSuccessCount(0);
        snapshot.setFailCount(0);
        snapshot.setNextIndex(0);
        snapshot.setProgressPercent(0);
        snapshot.setSuccessIds(new ArrayList<>());
        snapshot.setFailIds(new ArrayList<>());
        snapshot.setDetails(new LinkedHashMap<>());
        snapshot.setMessage(null);
        return snapshot;
    }

    private void acquireBatchKey(List<Long> jobIds, String batchId) {
        String batchKey = buildBatchKey(jobIds);
        String existBatchId = BATCH_ASYNC_LOCKS.putIfAbsent(batchKey, batchId);
        if (existBatchId != null && !existBatchId.equals(batchId)) {
            log.warn(LOG_DUPLICATE_ASYNC_BATCH, existBatchId, batchKey);
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED,
                String.format(MSG_DUPLICATE_ASYNC_BATCH, existBatchId)
            );
        }
    }

    private String buildBatchKey(List<Long> jobIds) {
        return jobIds.stream()
            .filter(Objects::nonNull)
            .sorted()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }

    private void insertBatchRecord(String batchId, String status, BatchExecutionSnapshot snapshot) {
        TemplateJobBatch batch = new TemplateJobBatch();
        batch.setId(batchId);
        batch.setStatus(status);
        batch.setResult(JSON.toJSONString(snapshot));
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(LocalDateTime.now());
        batchMapper.insert(batch);
    }

    private void updateBatchRecord(String batchId, String status, BatchExecutionSnapshot snapshot) {
        snapshot.setProgressPercent(calculateProgress(snapshot));
        TemplateJobBatch update = new TemplateJobBatch();
        update.setId(batchId);
        update.setStatus(status);
        update.setResult(JSON.toJSONString(snapshot));
        update.setUpdateTime(LocalDateTime.now());
        batchMapper.updateById(update);
    }

    private int calculateProgress(BatchExecutionSnapshot snapshot) {
        if (snapshot.getTotalCount() <= 0) {
            return 0;
        }
        return Math.min(100, (int) ((snapshot.getCompletedCount() * 100.0d) / snapshot.getTotalCount()));
    }

    private void startBatchExecution(BatchExecutionSnapshot snapshot, String batchId, boolean resume) {
        BatchRuntimeContext context = new BatchRuntimeContext(buildBatchKey(snapshot.getPendingJobIds()));
        BatchRuntimeContext existing = BATCH_RUNTIME_CONTEXTS.putIfAbsent(batchId, context);
        if (existing != null) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, MSG_BATCH_ALREADY_RUNNING);
        }

        if (resume) {
            snapshot.setMessage(MSG_BATCH_RESUME_SUBMITTED);
        }

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> executeBatch(snapshot, batchId, context), templateJobExecutor);
        BATCH_ASYNC_FUTURES.put(batchId, future);
        future.whenComplete((v, t) -> {
            BATCH_ASYNC_FUTURES.remove(batchId);
            BATCH_RUNTIME_CONTEXTS.remove(batchId);
            BATCH_ASYNC_LOCKS.remove(context.batchKey, batchId);
            if (t != null) {
                log.error(LOG_BATCH_EXECUTE_UNCAUGHT, batchId, t);
            }
        });
    }

    private void executeBatch(BatchExecutionSnapshot snapshot, String batchId, BatchRuntimeContext context) {
        try {
            log.info(LOG_BATCH_EXECUTE_START, batchId, snapshot.getTotalCount());
            updateBatchRecord(batchId, TemplateEnums.JobBatchStatus.RUNNING.getCode(), snapshot);

            for (int index = snapshot.getNextIndex(); index < snapshot.getJobIds().size(); index++) {
                if (context.cancelRequested.get()) {
                    snapshot.setMessage(MSG_BATCH_CANCELED);
                    snapshot.setCurrentJobId(null);
                    snapshot.setCurrentJobName(null);
                    updateBatchRecord(batchId, TemplateEnums.JobBatchStatus.CANCELED.getCode(), snapshot);
                    log.info(LOG_BATCH_EXECUTE_FINISH, batchId, TemplateEnums.JobBatchStatus.CANCELED.getCode(),
                        snapshot.getCompletedCount(), snapshot.getTotalCount());
                    return;
                }
                if (context.pauseRequested.get()) {
                    snapshot.setMessage(MSG_BATCH_PAUSED);
                    snapshot.setCurrentJobId(null);
                    snapshot.setCurrentJobName(null);
                    updateBatchRecord(batchId, TemplateEnums.JobBatchStatus.PAUSED.getCode(), snapshot);
                    log.info(LOG_BATCH_EXECUTE_FINISH, batchId, TemplateEnums.JobBatchStatus.PAUSED.getCode(),
                        snapshot.getCompletedCount(), snapshot.getTotalCount());
                    return;
                }

                Long jobId = snapshot.getJobIds().get(index);
                TemplateJob job = getById(jobId);
                snapshot.setCurrentJobId(jobId);
                snapshot.setCurrentJobName(job == null ? null : job.getJobName());
                snapshot.setMessage(MSG_BATCH_RUNNING);
                updateBatchRecord(batchId, TemplateEnums.JobBatchStatus.RUNNING.getCode(), snapshot);
                log.info(LOG_BATCH_EXECUTE_ITEM, batchId, index + 1, snapshot.getTotalCount(), jobId);

                Map<String, Object> result;
                try {
                    result = triggerJob(jobId);
                } catch (Exception e) {
                    log.error(LOG_BATCH_TRIGGER_FAILED, jobId, e);
                    result = Map.of(
                        ApiResultKeys.SUCCESS.getKey(), false,
                        ApiResultKeys.MESSAGE.getKey(), Objects.toString(e.getMessage(), e.getClass().getSimpleName()),
                        "jobId", jobId
                    );
                }

                boolean success = Boolean.TRUE.equals(result.get(ApiResultKeys.SUCCESS.getKey()));
                snapshot.getDetails().put(jobId, result);
                if (success) {
                    snapshot.getSuccessIds().add(jobId);
                    snapshot.setSuccessCount(snapshot.getSuccessCount() + 1);
                } else {
                    snapshot.getFailIds().add(jobId);
                    snapshot.setFailCount(snapshot.getFailCount() + 1);
                }
                snapshot.setCompletedCount(snapshot.getCompletedCount() + 1);
                snapshot.setNextIndex(index + 1);
                snapshot.setCurrentJobId(null);
                snapshot.setCurrentJobName(null);
                updateBatchRecord(batchId, TemplateEnums.JobBatchStatus.RUNNING.getCode(), snapshot);
            }

            snapshot.setMessage(MSG_BATCH_DONE);
            updateBatchRecord(batchId, TemplateEnums.JobBatchStatus.DONE.getCode(), snapshot);
            log.info(LOG_BATCH_EXECUTE_FINISH, batchId, TemplateEnums.JobBatchStatus.DONE.getCode(),
                snapshot.getCompletedCount(), snapshot.getTotalCount());
        } catch (Exception e) {
            snapshot.setMessage(e.getMessage());
            snapshot.setCurrentJobId(null);
            snapshot.setCurrentJobName(null);
            updateBatchRecord(batchId, TemplateEnums.JobBatchStatus.FAILED.getCode(), snapshot);
            log.error(LOG_BATCH_EXECUTE_UNCAUGHT, batchId, e);
        }
    }

    private TemplateJobBatch getBatchOrThrow(String batchId) {
        TemplateJobBatch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.NOT_FOUND, MSG_BATCH_NOT_FOUND);
        }
        return batch;
    }

    private BatchExecutionSnapshot parseBatchSnapshot(TemplateJobBatch batch) {
        if (!StringUtils.hasText(batch.getResult())) {
            return createInitialBatchSnapshot(batch.getId(), null, Collections.emptyList());
        }
        try {
            BatchExecutionSnapshot snapshot = JSON.parseObject(batch.getResult(), BatchExecutionSnapshot.class);
            if (snapshot.getJobIds() == null) {
                snapshot.setJobIds(new ArrayList<>());
            }
            if (snapshot.getSuccessIds() == null) {
                snapshot.setSuccessIds(new ArrayList<>());
            }
            if (snapshot.getFailIds() == null) {
                snapshot.setFailIds(new ArrayList<>());
            }
            if (snapshot.getDetails() == null) {
                snapshot.setDetails(new LinkedHashMap<>());
            }
            return snapshot;
        } catch (Exception e) {
            BatchExecutionSnapshot fallback = createInitialBatchSnapshot(batch.getId(), null, Collections.emptyList());
            fallback.setMessage(batch.getResult());
            return fallback;
        }
    }

    private Map<String, Object> toBatchProgressResponse(TemplateJobBatch batch, BatchExecutionSnapshot snapshot) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("batchId", batch.getId());
        resp.put("sourceBatchId", snapshot.getSourceBatchId());
        resp.put(TemplateEnums.ApiResultKeys.STATUS.getKey(),
            StringUtils.hasText(batch.getStatus()) ? batch.getStatus() : MSG_BATCH_STATUS_UNKNOWN);
        resp.put("totalCount", snapshot.getTotalCount());
        resp.put("completedCount", snapshot.getCompletedCount());
        resp.put("successCount", snapshot.getSuccessCount());
        resp.put("failCount", snapshot.getFailCount());
        resp.put("pendingCount", Math.max(0, snapshot.getTotalCount() - snapshot.getCompletedCount()));
        resp.put("progressPercent", calculateProgress(snapshot));
        resp.put("nextIndex", snapshot.getNextIndex());
        resp.put("currentJobId", snapshot.getCurrentJobId());
        resp.put("currentJobName", snapshot.getCurrentJobName());
        resp.put("jobIds", snapshot.getJobIds());
        resp.put("successIds", snapshot.getSuccessIds());
        resp.put("failIds", snapshot.getFailIds());
        resp.put(TemplateEnums.ApiResultKeys.DETAILS.getKey(), snapshot.getDetails());
        resp.put(TemplateEnums.ApiResultKeys.MESSAGE.getKey(), snapshot.getMessage());
        resp.put("canPause", TemplateEnums.JobBatchStatus.RUNNING.getCode().equalsIgnoreCase(batch.getStatus()));
        resp.put("canResume", TemplateEnums.JobBatchStatus.PAUSED.getCode().equalsIgnoreCase(batch.getStatus()));
        resp.put("canCancel",
            TemplateEnums.JobBatchStatus.RUNNING.getCode().equalsIgnoreCase(batch.getStatus())
                || TemplateEnums.JobBatchStatus.PAUSED.getCode().equalsIgnoreCase(batch.getStatus())
                || TemplateEnums.JobBatchStatus.PENDING.getCode().equalsIgnoreCase(batch.getStatus()));
        resp.put("canRetryFailed", !snapshot.getFailIds().isEmpty());
        return resp;
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
                    log.warn(LOG_PARSE_JOB_LOG_RESULT_FAILED, logEntity.getId(), e);
                }
            }
            vo.setResults(results);
            vo.setResultSummary(buildSummaryText(successCount, failCount));

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
                MSG_IMPORT_FILE_EMPTY
            );
        }

        List<TemplateJob> importedJobs = parseImportedJobs(readFileContent(file));
        if (importedJobs.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                MSG_IMPORT_JOB_CONFIG_NOT_FOUND
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
                log.error(LOG_IMPORT_JOB_FAILED, index, importedJob == null ? null : importedJob.getJobName(), e);
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
     /*   if (items == null || items.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.BUSINESS_RULE_VIOLATION,
                MSG_JOB_ITEM_LIST_EMPTY
            );
        }*/
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
            log.info(LOG_JOB_ALREADY_RUNNING, jobId, traceId);
            if (traceCreatedHere) {
                TraceIdContext.clear();
            }
            return Map.of("success", false, "message", MSG_JOB_RUNNING, "traceId", traceId);
        }
        try {
            log.info(LOG_START_EXECUTE_JOB, jobId, jobName, traceId);
            randomPauseForDemo(jobId, jobName, traceId);
            TemplateJob job = getById(jobId);
            if (job == null || Integer.valueOf(1).equals(job.getIsDeleted())) {
                return Map.of("success", false, "message", MSG_JOB_NOT_FOUND, "traceId", traceId);
            }

            if (Integer.valueOf(0).equals(job.getStatus())) {
                return Map.of("success", false, "message", MSG_JOB_DISABLED, "traceId", traceId);
            }

            List<TemplateJobItem> items = jobItemMapper.selectByJobId(jobId);
            if (items == null || items.isEmpty()) {
                throw new TemplateValidationException(
                    TemplateValidationException.ErrorType.BUSINESS_RULE_VIOLATION,
                    MSG_JOB_ITEM_NOT_CONFIGURED
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
                "message", allSuccess ? MSG_ALL_SUCCESS : errorMsg,
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

    private void randomPauseForDemo(Long jobId, String jobName, String traceId) {
        long sleepMillis = ThreadLocalRandom.current().nextLong(3000, 5001);
        log.info(LOG_DEMO_PAUSE_START, jobId, jobName, sleepMillis, traceId);
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED,
                MSG_DEMO_PAUSE_INTERRUPTED
            );
        }
        log.info(LOG_DEMO_PAUSE_END, jobId, jobName, traceId);
    }

    /**
     * 供自动调度使用的执行入口（带连续失败自动停用保护）
     */
    private void executeJobForSchedule(Long jobId, String jobName) {
        Map<String, Object> result;
        try {
            result = doExecuteJob(jobId, jobName);
        } catch (Exception e) {
            log.error(LOG_SCHEDULE_EXECUTE_ERROR, jobId, e);
            result = Map.of("success", false, "message", e.getMessage());
        }

        AtomicInteger counter = JOB_FAIL_COUNTS.computeIfAbsent(jobId, k -> new AtomicInteger(0));
        if (Boolean.FALSE.equals(result.get("success"))) {
            int failCount = counter.incrementAndGet();
            log.warn(LOG_SCHEDULE_EXECUTE_FAILED, jobId, failCount, AUTO_DISABLE_THRESHOLD);
            if (failCount >= AUTO_DISABLE_THRESHOLD) {
                log.error(LOG_SCHEDULE_AUTO_DISABLED, AUTO_DISABLE_THRESHOLD, jobId);
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
                log.warn(LOG_PARSE_VARIABLES_FAILED, item.getId());
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
            log.warn(LOG_PARSE_EXECUTE_RESULT_FAILED, e);
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
                MSG_READ_IMPORT_FILE_FAILED + e.getMessage(),
                e
            );
        }
        return content.toString();
    }

    private List<TemplateJobListVO> buildJobListVOs(List<TemplateJob> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> jobIds = jobs.stream().map(TemplateJob::getId).toList();
        List<TemplateJobLog> lastLogs = jobLogMapper.selectLastLogsByJobIds(jobIds);
        Map<Long, TemplateJobLog> logMap = lastLogs.stream()
            .collect(Collectors.toMap(
                TemplateJobLog::getJobId,
                Function.identity(),
                (a, b) -> a));

        return jobs.stream().map(job -> {
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

            ReentrantLock lock = JOB_LOCKS.get(job.getId());
            vo.setExecuting(lock != null && lock.isLocked());
            return vo;
        }).toList();
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
                MSG_IMPORT_FILE_FORMAT_INVALID
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
                String.format(MSG_IMPORT_JOB_DATA_EMPTY_TEMPLATE, index)
            );
        }

        if (!StringUtils.hasText(job.getJobName())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                String.format(MSG_IMPORT_JOB_NAME_REQUIRED_TEMPLATE, index)
            );
        }

        String jobName = job.getJobName().trim();
        job.setJobName(jobName);

        if (validationContext.duplicateJobNames().contains(jobName)) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.ALREADY_EXISTS,
                String.format(MSG_IMPORT_DUPLICATE_JOB_NAME_TEMPLATE, jobName)
            );
        }

        if (validationContext.existingJobNames().contains(jobName)) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.ALREADY_EXISTS,
                String.format(MSG_JOB_NAME_ALREADY_EXISTS_TEMPLATE, jobName)
            );
        }

        if (!StringUtils.hasText(job.getCronExpression())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                String.format(MSG_JOB_CRON_REQUIRED_TEMPLATE, jobName)
            );
        }

        if (!CronExpression.isValidExpression(job.getCronExpression().trim())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                String.format(MSG_JOB_CRON_INVALID_TEMPLATE, jobName)
            );
        }
        job.setCronExpression(job.getCronExpression().trim());

        List<TemplateJobItem> items = job.getItems();
        if (items == null || items.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.BUSINESS_RULE_VIOLATION,
                String.format(MSG_JOB_ITEMS_NOT_CONFIGURED_TEMPLATE, jobName)
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
                String.format(MSG_JOB_ITEM_EMPTY_TEMPLATE, jobName, itemIndex)
            );
        }

        if (item.getTemplateId() == null) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                String.format(MSG_JOB_ITEM_TEMPLATE_ID_REQUIRED_TEMPLATE, jobName, itemIndex)
            );
        }

        if (!validationContext.existingTemplateIds().contains(item.getTemplateId())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.NOT_FOUND,
                String.format(MSG_JOB_ITEM_TEMPLATE_NOT_FOUND_TEMPLATE, jobName, itemIndex, item.getTemplateId())
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
    private static class BatchExecutionSnapshot {
        private String batchId;
        private String sourceBatchId;
        private List<Long> jobIds;
        private int totalCount;
        private int completedCount;
        private int successCount;
        private int failCount;
        private int nextIndex;
        private int progressPercent;
        private Long currentJobId;
        private String currentJobName;
        private List<Long> successIds;
        private List<Long> failIds;
        private Map<Long, Object> details;
        private String message;

        List<Long> getPendingJobIds() {
            if (jobIds == null || jobIds.isEmpty() || nextIndex >= jobIds.size()) {
                return Collections.emptyList();
            }
            return new ArrayList<>(jobIds.subList(nextIndex, jobIds.size()));
        }

        List<Long> getRetryableJobIds() {
            return failIds == null ? Collections.emptyList() : new ArrayList<>(failIds);
        }
    }

    private static class BatchRuntimeContext {
        private final String batchKey;
        private final AtomicBoolean pauseRequested = new AtomicBoolean(false);
        private final AtomicBoolean cancelRequested = new AtomicBoolean(false);

        private BatchRuntimeContext(String batchKey) {
            this.batchKey = batchKey;
        }
    }

    @Data
    @AllArgsConstructor
    static class Summary {
        int success;
        int fail;

        String toText() {
            return buildSummaryText(success, fail);
        }
    }

    private static String buildSummaryText(int success, int fail) {
        return success + MSG_SUMMARY_SUCCESS_SUFFIX
            + (fail > 0 ? String.format(MSG_SUMMARY_FAIL_TEMPLATE, fail) : "");
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
        return StringUtils.hasText(username) ? username : MSG_ADMIN_DEFAULT_NAME;
    }
}
