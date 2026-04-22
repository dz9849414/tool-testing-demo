package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.example.tooltestingdemo.mapper.template.TemplateExecuteLogMapper;
import com.example.tooltestingdemo.mapper.template.TemplateJobLogMapper;
import com.example.tooltestingdemo.service.template.TemplateExecuteLogService;
import com.example.tooltestingdemo.vo.TraceChainDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 模板执行统一日志 Service 实现。
 */
@Service
@RequiredArgsConstructor
public class TemplateExecuteLogServiceImpl extends ServiceImpl<TemplateExecuteLogMapper, TemplateExecuteLog>
        implements TemplateExecuteLogService {

    private final TemplateJobLogMapper templateJobLogMapper;

    @Override
    public IPage<TemplateExecuteLog> pageLogs(Page<TemplateExecuteLog> page,
                                              Long templateId,
                                              Long jobId,
                                              String executeType,
                                              Integer success,
                                              String keyword,
                                              LocalDateTime startTime,
                                              LocalDateTime endTime) {
        LambdaQueryWrapper<TemplateExecuteLog> wrapper = new LambdaQueryWrapper<>();

        if (templateId != null) {
            wrapper.eq(TemplateExecuteLog::getTemplateId, templateId);
        }
        if (jobId != null) {
            wrapper.eq(TemplateExecuteLog::getJobId, jobId);
        }
        if (StringUtils.hasText(executeType)) {
            wrapper.eq(TemplateExecuteLog::getExecuteType, executeType);
        }
        if (success != null) {
            wrapper.eq(TemplateExecuteLog::getSuccess, success);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TemplateExecuteLog::getTemplateName, keyword)
                    .or()
                    .like(TemplateExecuteLog::getJobName, keyword)
                    .or()
                    .like(TemplateExecuteLog::getExecuteUserName, keyword));
        }
        if (startTime != null) {
            wrapper.ge(TemplateExecuteLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(TemplateExecuteLog::getCreateTime, endTime);
        }

        wrapper.orderByDesc(TemplateExecuteLog::getExecuteAt);
        wrapper.orderByDesc(TemplateExecuteLog::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public TraceChainDetailVO getTraceChainDetail(String traceId) {
        LambdaQueryWrapper<TemplateExecuteLog> executeWrapper = new LambdaQueryWrapper<>();
        executeWrapper.eq(TemplateExecuteLog::getTraceId, traceId)
                 .orderByAsc(TemplateExecuteLog::getExecuteAt)
                .orderByAsc(TemplateExecuteLog::getCreateTime)
                .orderByAsc(TemplateExecuteLog::getId);
        List<TemplateExecuteLog> executeLogs = list(executeWrapper);

        LambdaQueryWrapper<TemplateJobLog> jobWrapper = new LambdaQueryWrapper<>();
        jobWrapper.eq(TemplateJobLog::getTraceId, traceId)
                 .orderByAsc(TemplateJobLog::getExecuteAt)
                .orderByAsc(TemplateJobLog::getCreateTime)
                .orderByAsc(TemplateJobLog::getId);
        List<TemplateJobLog> jobLogs = templateJobLogMapper.selectList(jobWrapper);

        TraceChainDetailVO detail = new TraceChainDetailVO();
        detail.setTraceId(traceId);
        detail.setJobLogs(jobLogs);
        detail.setExecuteLogs(executeLogs);
        detail.setJobLogCount(jobLogs.size());
        detail.setExecuteLogCount(executeLogs.size());

        int successCount = 0;
        int failCount = 0;
        for (TemplateJobLog jobLog : jobLogs) {
            if (Integer.valueOf(1).equals(jobLog.getSuccess())) {
                successCount++;
            } else {
                failCount++;
            }
        }
        for (TemplateExecuteLog executeLog : executeLogs) {
            if (Integer.valueOf(1).equals(executeLog.getSuccess())) {
                successCount++;
            } else {
                failCount++;
            }
        }
        detail.setSuccessCount(successCount);
        detail.setFailCount(failCount);
        detail.setSuccess(failCount == 0 && (!jobLogs.isEmpty() || !executeLogs.isEmpty()));

        List<LocalDateTime> timestamps = new ArrayList<>();
        for (TemplateJobLog jobLog : jobLogs) {
            if (jobLog.getExecuteAt() != null) {
                timestamps.add(jobLog.getExecuteAt());
            }
        }
        for (TemplateExecuteLog executeLog : executeLogs) {
            if (executeLog.getExecuteAt() != null) {
                timestamps.add(executeLog.getExecuteAt());
            }
        }
        if (!timestamps.isEmpty()) {
            timestamps.sort(Comparator.naturalOrder());
            LocalDateTime startTime = timestamps.get(0);
            LocalDateTime endTime = timestamps.get(timestamps.size() - 1);
            detail.setStartTime(startTime);
            detail.setEndTime(endTime);
            detail.setTotalDurationMs(ChronoUnit.MILLIS.between(startTime, endTime));
        }

        return detail;
}
}
