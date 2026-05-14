package com.example.tooltestingdemo.service.impl.template;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.template.TemplateJobGenerateRequest;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.entity.template.TemplateJob;
import com.example.tooltestingdemo.entity.template.TemplateJobGenerationLog;
import com.example.tooltestingdemo.entity.template.TemplateJobItem;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.mapper.template.TemplateJobGenerationLogMapper;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.SysOperationLogService;
import com.example.tooltestingdemo.service.template.TemplateJobGenerationService;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import com.example.tooltestingdemo.service.template.TemplateJobService;
import com.example.tooltestingdemo.util.OperationLogNameUtils;
import com.example.tooltestingdemo.vo.TemplateJobGenerationLogVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模板任务批量生成 Service 实现
 */
@Service
@RequiredArgsConstructor
public class TemplateJobGenerationServiceImpl
    extends ServiceImpl<TemplateJobGenerationLogMapper, TemplateJobGenerationLog>
    implements TemplateJobGenerationService {

    private static final int DEFAULT_JOB_STATUS = TemplateEnums.JobStatus.DISABLED.getCode();
    private static final int MAX_GENERATE_COUNT = 10000;
    private static final List<String> JOB_NAME_PREFIX_POOL = List.of(
        "船舶PDM图纸同步任务",
        "船体BOM校验任务",
        "船舶物料主数据同步任务",
        "船舶设计变更巡检任务",
        "船舶工艺路线验证任务",
        "船舶设备台账同步任务",
        "船舶装配关系核对任务",
        "船舶技术文件归档任务",
        "船舶质量检验回填任务",
        "船舶生产数据对账任务",
        "船舶PDM接口联调任务",
        "船舶零部件编码校验任务"
    );
    private static final List<String> DESCRIPTION_POOL = List.of(
        "用于船舶PDM图纸与物料数据同步验证",
        "用于船体结构BOM一致性检查",
        "用于船舶设计变更流转验证",
        "用于船舶工艺数据定时核对",
        "用于船舶技术文件归档抽检",
        "用于船舶设备台账与主数据同步",
        "用于船舶零部件编码映射校验",
        "用于船舶质量检验结果回填测试",
        "用于船舶装配关系联动验证",
        "用于船舶PDM接口自动化巡检"
    );
    private static final List<String> CRON_POOL = List.of(
        "0 0/5 * * * ?",
        "0 0/10 * * * ?",
        "0 0/15 * * * ?",
        "0 0/20 * * * ?",
        "0 0/30 * * * ?",
        "0 0 0/1 * * ?",
        "0 0 1 * * ?",
        "0 0 2 * * ?",
        "0 15 2 * * ?",
        "0 30 3 * * ?"
    );
    private static final List<String> VARIABLE_POOL = List.of(
        "{\"source\":\"ship-pdm\",\"domain\":\"hull\",\"priority\":\"low\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"bom\",\"priority\":\"normal\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"drawing\",\"priority\":\"high\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"process\",\"scene\":\"mock\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"material\",\"scene\":\"auto\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"equipment\",\"stage\":\"sync\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"quality\",\"stage\":\"review\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"assembly\",\"stage\":\"verify\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"archive\",\"stage\":\"batch\"}",
        "{\"source\":\"ship-pdm\",\"domain\":\"change\",\"stage\":\"audit\"}"
    );

    private final TemplateJobService templateJobService;
    private final InterfaceTemplateService interfaceTemplateService;
    private final TemplateEnvironmentService templateEnvironmentService;
    private final SysOperationLogService operationLogService;
    private final SecurityService securityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateJobGenerationLogVO generate(TemplateJobGenerateRequest request) {
        validateRequest(request);
        List<InterfaceTemplate> templates = interfaceTemplateService.lambdaQuery()
            .eq(InterfaceTemplate::getIsDeleted, 0)
            .list();
        if (templates.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.NOT_FOUND,
                "没有可用的接口模板，无法生成任务"
            );
        }
        List<TemplateEnvironment> environments = templateEnvironmentService.lambdaQuery()
            .eq(TemplateEnvironment::getIsDeleted, 0)
            .list();
        List<Long> jobIds = new ArrayList<>(request.getCount());
        String prefix = randomOf(JOB_NAME_PREFIX_POOL);

        for (int i = 1; i <= request.getCount(); i++) {
            TemplateJob job = new TemplateJob();
            LocalDateTime createTime = randomTime(request.getStartTime(), request.getEndTime());
            job.setJobName(prefix + "-" + createTime.toLocalDate() + "-" + System.currentTimeMillis() + "-" + i);
            job.setCronExpression(randomOf(CRON_POOL));
            job.setStatus(randomStatus());
            job.setDescription(randomOf(DESCRIPTION_POOL));
            job.setCreateTime(createTime);
            job.setUpdateTime(createTime);
            job.setIsDeleted(0);
            job.setItems(randomItems(templates, environments));

            TemplateJob created = templateJobService.createJob(job);
            jobIds.add(created.getId());
            recordOperationLog("新增任务", "createJob", created.getId(), created.getJobName(), null);
        }

        TemplateJobGenerationLog log = new TemplateJobGenerationLog();
        log.setStartTime(request.getStartTime());
        log.setEndTime(request.getEndTime());
        log.setGenerateCount(request.getCount());
        log.setJobNamePrefix(prefix);
        log.setJobIds(JSON.toJSONString(jobIds));
        log.setStatus(1);
        log.setMessage("生成成功");
        log.setIsDeleted(0);
        save(log);
        return toVO(log);
    }

    @Override
    public IPage<TemplateJobGenerationLogVO> pageLogs(Page<TemplateJobGenerationLog> page, String keyword) {
        LambdaQueryWrapper<TemplateJobGenerationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateJobGenerationLog::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TemplateJobGenerationLog::getJobNamePrefix, keyword)
                .or()
                .like(TemplateJobGenerationLog::getMessage, keyword));
        }
        wrapper.orderByDesc(TemplateJobGenerationLog::getCreateTime);

        IPage<TemplateJobGenerationLog> entityPage = page(page, wrapper);
        IPage<TemplateJobGenerationLogVO> voPage =
            new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }
    

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteLogsAndJobs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "生成记录ID列表不能为空"
            );
        }

        List<Long> distinctIds = ids.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (distinctIds.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "生成记录ID列表不能为空"
            );
        }

        List<TemplateJobGenerationLog> logs = listByIds(distinctIds).stream()
            .filter(Objects::nonNull)
            .toList();
        for (TemplateJobGenerationLog log : logs) {
            for (Long jobId : parseJobIds(log.getJobIds())) {
                if (jobId != null) {
                    boolean deleted = templateJobService.deleteJob(jobId);
                    recordOperationLog(
                        "删除任务",
                        "deleteJob",
                        jobId,
                        "jobId=" + jobId,
                        deleted ? null : "删除失败"
                    );
                }
            }
        }
        if (logs.isEmpty()) {
            return 0;
        }
        removeByIds(logs.stream().map(TemplateJobGenerationLog::getId).toList());
        return logs.size();
    }

    private void validateRequest(TemplateJobGenerateRequest request) {
        if (request == null) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "生成参数不能为空"
            );
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "开始时间和结束时间不能为空"
            );
        }
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.INVALID_FORMAT,
                "开始时间不能晚于结束时间"
            );
        }
        if (request.getCount() == null || request.getCount() <= 0) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                "生成条数必须大于0"
            );
        }
        if (request.getCount() > MAX_GENERATE_COUNT) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.BUSINESS_RULE_VIOLATION,
                "单次最多生成" + MAX_GENERATE_COUNT + "条"
            );
        }
    }

    private LocalDateTime randomTime(LocalDateTime startTime, LocalDateTime endTime) {
        long seconds = Duration.between(startTime, endTime).getSeconds();
        if (seconds <= 0) {
            return startTime;
        }
        long offset = ThreadLocalRandom.current().nextLong(seconds + 1);
        return startTime.plusSeconds(offset);
    }

    private List<TemplateJobItem> randomItems(List<InterfaceTemplate> templates, List<TemplateEnvironment> environments) {
        int upperBound = Math.min(templates.size(), 3);
        int itemCount = ThreadLocalRandom.current().nextInt(1, upperBound + 1);
        List<InterfaceTemplate> shuffledTemplates = new ArrayList<>(templates);
        Collections.shuffle(shuffledTemplates);
        List<TemplateJobItem> items = new ArrayList<>(itemCount);

        for (int i = 0; i < itemCount; i++) {
            InterfaceTemplate template = shuffledTemplates.get(i);
            TemplateJobItem item = new TemplateJobItem();
            item.setTemplateId(template.getId());
            item.setEnvironmentId(randomEnvironmentId(template.getId(), environments));
            item.setVariables(randomOf(VARIABLE_POOL));
            item.setSortOrder(i + 1);
            item.setStatus(TemplateEnums.JobStatus.ENABLED.getCode());
            item.setIsDeleted(0);
            items.add(item);
        }
        return items;
    }

    private Long randomEnvironmentId(Long templateId, List<TemplateEnvironment> environments) {
        List<TemplateEnvironment> matched = environments.stream()
            .filter(env -> Objects.equals(env.getTemplateId(), templateId))
            .toList();
        if (matched.isEmpty()) {
            return null;
        }
        return matched.get(ThreadLocalRandom.current().nextInt(matched.size())).getId();
    }

    private int randomStatus() {
        return ThreadLocalRandom.current().nextInt(100) < 80
            ? DEFAULT_JOB_STATUS
            : TemplateEnums.JobStatus.ENABLED.getCode();
    }

    private String randomOf(List<String> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    private void recordOperationLog(String operation, String method, Long targetId, String requestParams, String errorMessage) {
        SysOperationLog operationLog = new SysOperationLog();
        Long userId = securityService.getCurrentUserId();
        String username = securityService.getCurrentUsername();
        String roleId = securityService.getCurrentUserRoleId();
        operationLog.setUserId(String.valueOf(userId == null ? 1L : userId));
        operationLog.setUsername(StringUtils.hasText(username) ? username : "anonymous");
        operationLog.setRoleId(StringUtils.hasText(roleId) ? roleId : "anonymous");
        operationLog.setModule(OperationLogNameUtils.getModuleDisplayName("TemplateJob"));
        operationLog.setOperation(operation);
        operationLog.setMethod(method);
        operationLog.setRequestUrl(resolveRequestUrl());
        operationLog.setRequestParams(requestParams);
        operationLog.setIpAddress(resolveClientIp());
        operationLog.setUserAgent(resolveUserAgent());
        operationLog.setStatus(errorMessage == null ? 1 : 0);
        operationLog.setErrorMessage(errorMessage);
        operationLog.setExecuteTime(0L);
        operationLogService.recordOperationLog(operationLog);
    }

    private String resolveRequestUrl() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request == null ? null : request.getRequestURI();
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request == null ? null : request.getRemoteAddr();
    }

    private String resolveUserAgent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request == null ? null : request.getHeader("User-Agent");
    }

    private TemplateJobGenerationLogVO toVO(TemplateJobGenerationLog log) {
        TemplateJobGenerationLogVO vo = new TemplateJobGenerationLogVO();
        vo.setId(log.getId());
        vo.setStartTime(log.getStartTime());
        vo.setEndTime(log.getEndTime());
        vo.setGenerateCount(log.getGenerateCount());
        vo.setJobNamePrefix(log.getJobNamePrefix());
        vo.setJobIds(parseJobIds(log.getJobIds()));
        vo.setStatus(log.getStatus());
        vo.setMessage(log.getMessage());
        vo.setCreateName(log.getCreateName());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }

    private List<Long> parseJobIds(String jobIds) {
        if (!StringUtils.hasText(jobIds)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseObject(jobIds, new TypeReference<List<Long>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
