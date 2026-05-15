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
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.mapper.template.TemplateJobGenerationLogMapper;
import com.example.tooltestingdemo.service.template.TemplateJobGenerationService;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import com.example.tooltestingdemo.service.template.TemplateJobService;
import com.example.tooltestingdemo.utils.SecurityUtils;
import com.example.tooltestingdemo.vo.TemplateJobGenerationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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

    public static final List<String> JOB_NAME_PREFIX_POOL = List.of(
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
            "船舶零部件编码校验任务",
            "船舶图纸版本一致性校验任务",
            "船舶PDM变更单审批同步任务",
            "船舶三维模型轻量化转换任务",
            "船舶设计BOM发布任务",
            "船舶制造BOM生成任务",
            "船舶PDM与ERP集成同步任务",
            "船舶图纸签名认证任务",
            "船舶PDM生命周期状态同步任务",
            "船舶技术通知单处理任务",
            "船舶设计数据备份任务",
            "船舶PDM权限同步任务",
            "船舶标准件库更新任务",
            "船舶设计任务分配同步任务",
            "船舶图纸打印归档任务",
            "船舶PDM修订版清理任务",
            "船舶外部协同设计数据接收任务",
            "船舶设计评审结论同步任务",
            "船舶PDM元数据校验任务",
            "船舶工艺规程关联校验任务",
            "船舶设计基线标记任务",
            "船舶PDM与CAM数据对接任务",
            "船舶材料定额同步任务",
            "船舶设计问题单闭环任务",
            "船舶PDM电子签名验证任务",
            "船舶技术状态统计任务",
            "船舶图纸转CAD格式任务",
            "船舶PDM用户账号同步任务",
            "船舶设计重用分析任务",
            "船舶变更影响范围计算任务",
            "船舶PDM历史版本迁移任务",
            "船舶设计交付物完整性检查任务",
            "船舶BOM成本计算同步任务",
            "船舶PDM与SCM集成任务",
            "船舶设计工时统计任务",
            "船舶图纸自动编号校验任务",
            "船舶PDM操作日志归档任务",
            "船舶外协件状态同步任务",
            "船舶设计数据脱敏导出任务"
    );

    private static final List<String> DESCRIPTION_POOL = List.of(
            "用于船舶PDM图纸与物料数据同步验证",
            "用于船体结构BOM一致性检查",
            "用于船舶物料主数据跨系统同步校验",
            "用于船舶设计变更流转验证",
            "用于船舶工艺数据定时核对",
            "用于船舶设备台账与主数据同步",
            "用于船舶装配关系联动验证",
            "用于船舶技术文件归档抽检",
            "用于船舶质量检验结果回填测试",
            "用于船舶生产数据与PDM对账",
            "用于船舶PDM接口自动化巡检",
            "用于船舶零部件编码映射校验",
            "检查图纸版本与PDM基线是否一致",
            "同步变更单审批状态到关联图纸",
            "将设计模型转换为轻量化视图格式",
            "设计BOM从PDM发布到下游系统",
            "基于设计BOM生成制造BOM结构",
            "同步PDM物料/BOM数据至ERP",
            "校验图纸电子签名及合规性",
            "同步图纸/文档生命周期状态",
            "解析并分发技术通知单至责任人",
            "定时备份PDM设计数据快照",
            "同步LDAP/域账号至PDM权限组",
            "更新PDM标准件库并校验引用",
            "同步设计任务分配结果到PDM",
            "将图纸打印版转换后归档存储",
            "清理过期修订版并记录日志",
            "接收外部厂家的设计数据并入库",
            "同步评审结论至图纸版本备注",
            "校验图纸元数据完整性及格式",
            "检查工艺规程与图纸关联关系",
            "对关键版本进行基线标记",
            "推送设计数据至CAM系统",
            "同步材料定额明细至PDM",
            "跟踪设计问题单处理状态至关闭",
            "验证电子签名的有效性和来源",
            "统计图纸/文档的技术状态分布",
            "将PDM图纸转换为CAD兼容格式",
            "同步HR系统用户至PDM账号",
            "分析零部件重用率并生成报告",
            "计算设计变更影响的BOM范围",
            "迁移历史版本至冷存储",
            "检查交付物清单是否齐全",
            "同步BOM成本计算结果至PDM",
            "同步PDM数据至供应链系统",
            "统计各设计任务的工时投入",
            "校验图纸编号规则与唯一性",
            "归档PDM操作日志并压缩",
            "同步外协件设计状态和版本",
            "脱敏导出设计数据供测试使用"
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

    private static final List<String> DEPARTMENT_POOL = List.of(
            "标准数据部",
            "测试验证部",
            "运维监控部",
            "研发设计部",
            "质量管理部",
            "工艺管理部"
    );

    private static final List<GenerationLogScenario> LOG_SCENARIO_POOL = List.of(
            new GenerationLogScenario("用户管理", "新增", "POST"),
            new GenerationLogScenario("用户管理", "修改", "PUT"),
            new GenerationLogScenario("用户管理", "删除", "DELETE"),
            new GenerationLogScenario("角色管理", "新增", "POST"),
            new GenerationLogScenario("角色管理", "修改", "PUT"),
            new GenerationLogScenario("角色管理", "删除", "DELETE"),
            new GenerationLogScenario("系统菜单", "新增", "POST"),
            new GenerationLogScenario("系统菜单", "修改", "PUT"),
            new GenerationLogScenario("操作日志", "导出", "GET"),
            new GenerationLogScenario("系统配置", "修改", "PUT"),
            new GenerationLogScenario("系统字典", "修改", "PUT"),
            new GenerationLogScenario("模板任务", "执行", "POST"),
            new GenerationLogScenario("模板任务", "导出", "GET"),
            new GenerationLogScenario("接口模板", "发布", "PUT"),
            new GenerationLogScenario("接口模板", "导入", "POST"),
            new GenerationLogScenario("模板执行", "执行", "POST"),
            new GenerationLogScenario("执行日志", "导出", "GET"),
            new GenerationLogScenario("模板目录", "新增", "POST"),
            new GenerationLogScenario("模板历史", "删除", "DELETE"),
            new GenerationLogScenario("模板导入", "导入", "POST"),
            new GenerationLogScenario("报表管理", "导出", "GET"),
            new GenerationLogScenario("报表模板", "预览", "GET"),
            new GenerationLogScenario("报表图表", "修改", "PUT"),
            new GenerationLogScenario("模板统计", "导出", "GET"),
            new GenerationLogScenario("协议配置", "修改", "PUT"),
            new GenerationLogScenario("协议配置", "导入", "POST"),
            new GenerationLogScenario("协议类型", "删除", "DELETE"),
            new GenerationLogScenario("协议测试记录", "执行", "POST"),
            new GenerationLogScenario("模拟数据", "导出", "GET")
    );

    private final TemplateJobService templateJobService;
    private final InterfaceTemplateService interfaceTemplateService;
    private final TemplateEnvironmentService templateEnvironmentService;

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

        List<TemplateJobGenerationLog> generationLogs = new ArrayList<>(request.getCount());
        String operatorName = getCurrentUsernameOrDefault();

        for (int i = 1; i <= request.getCount(); i++) {
            // 保证每个任务的 jobName 和 description 来自同一个索引
            int idx = randomIndex(JOB_NAME_PREFIX_POOL.size());
            String jobNamePrefix = JOB_NAME_PREFIX_POOL.get(idx);
            String description = DESCRIPTION_POOL.get(idx);

            TemplateJob job = new TemplateJob();
            LocalDateTime createTime = randomTime(request.getStartTime(), request.getEndTime());
            job.setJobName(jobNamePrefix + "-" + createTime.toLocalDate() + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4));
            job.setCronExpression(randomOf(CRON_POOL));
            job.setStatus(randomStatus());
            job.setDescription(description);
            job.setCreateTime(createTime);
            job.setUpdateTime(createTime);
            job.setIsDeleted(0);
            job.setItems(randomItems(templates, environments));

            TemplateJob created = templateJobService.createJob(job);
            generationLogs.add(buildGenerationLog(request, created.getId(), created.getJobName(), createTime, operatorName));
        }

        saveBatch(generationLogs);
        return toVO(generationLogs.get(0));
    }

    @Override
    public IPage<TemplateJobGenerationLogVO> pageLogs(Page<TemplateJobGenerationLog> page,
                                                      String keyword,
                                                      String systemModule,
                                                      String operationType,
                                                      String operatorName,
                                                      Integer status,
                                                      LocalDateTime startTime,
                                                      LocalDateTime endTime) {
        LambdaQueryWrapper<TemplateJobGenerationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateJobGenerationLog::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TemplateJobGenerationLog::getJobNamePrefix, keyword)
                    .or()
                    .like(TemplateJobGenerationLog::getMessage, keyword));
        }
        if (StringUtils.hasText(systemModule)) {
            wrapper.like(TemplateJobGenerationLog::getJobNamePrefix, systemModule);
        }
        if (StringUtils.hasText(operationType)) {
            wrapper.like(TemplateJobGenerationLog::getMessage, operationType);
        }
        if (StringUtils.hasText(operatorName)) {
            wrapper.and(w -> w.like(TemplateJobGenerationLog::getCreateName, operatorName)
                    .or()
                    .like(TemplateJobGenerationLog::getMessage, operatorName));
        }
        if (status != null) {
            wrapper.eq(TemplateJobGenerationLog::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(TemplateJobGenerationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(TemplateJobGenerationLog::getCreateTime, endTime);
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
                    templateJobService.deleteJob(jobId);
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

    private <T> T randomOf(List<T> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    private int randomIndex(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    private TemplateJobGenerationLog buildGenerationLog(TemplateJobGenerateRequest request,
                                                        Long jobId,
                                                        String jobName,
                                                        LocalDateTime operationTime,
                                                        String operatorName) {
        GenerationLogScenario scenario = selectScenario(request);
        String department = randomOf(DEPARTMENT_POOL);
        long durationMs = ThreadLocalRandom.current().nextLong(120, 3000);
        int status = ThreadLocalRandom.current().nextInt(100) < 92 ? 1 : 0;

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("systemModule", scenario.systemModule());
        detail.put("operationType", scenario.operationType());
        detail.put("operatorName", operatorName);
        detail.put("department", department);
        detail.put("requestMethod", scenario.requestMethod());
        detail.put("requestUrl", buildRequestUrl(scenario.systemModule(), scenario.operationType()));
        detail.put("logSource", "一键生成");
        detail.put("durationMs", durationMs);
        detail.put("operationTime", operationTime.toString());
        detail.put("businessName", jobName);

        TemplateJobGenerationLog log = new TemplateJobGenerationLog();
        log.setStartTime(request.getStartTime());
        log.setEndTime(request.getEndTime());
        log.setGenerateCount(1);
        log.setJobNamePrefix(scenario.systemModule());
        log.setJobIds(JSON.toJSONString(List.of(jobId)));
        log.setStatus(status);
        log.setMessage(JSON.toJSONString(detail));
        log.setCreateName(operatorName);
        log.setCreateTime(operationTime);
        log.setUpdateName(operatorName);
        log.setUpdateTime(operationTime);
        log.setIsDeleted(0);
        return log;
    }

    private GenerationLogScenario selectScenario(TemplateJobGenerateRequest request) {
        List<GenerationLogScenario> matched = LOG_SCENARIO_POOL.stream()
                .filter(item -> request.getSystemModules() == null
                        || request.getSystemModules().isEmpty()
                        || request.getSystemModules().contains(item.systemModule()))
                .filter(item -> request.getOperationTypes() == null
                        || request.getOperationTypes().isEmpty()
                        || request.getOperationTypes().contains(item.operationType()))
                .toList();
        if (!matched.isEmpty()) {
            return randomOf(matched);
        }
        String systemModule = request.getSystemModules() == null || request.getSystemModules().isEmpty()
                ? randomOf(LOG_SCENARIO_POOL).systemModule()
                : randomOf(request.getSystemModules());
        String operationType = request.getOperationTypes() == null || request.getOperationTypes().isEmpty()
                ? randomOf(LOG_SCENARIO_POOL).operationType()
                : randomOf(request.getOperationTypes());
        String requestMethod = switch (operationType) {
            case "修改", "发布" -> "PUT";
            case "删除" -> "DELETE";
            case "预览", "导出" -> "GET";
            default -> "POST";
        };
        return new GenerationLogScenario(systemModule, operationType, requestMethod);
    }

    private String buildRequestUrl(String systemModule, String operationType) {
        String modulePath = switch (systemModule) {
            case "用户管理" -> "/api/users";
            case "角色管理" -> "/api/roles";
            case "系统菜单" -> "/api/menus";
            case "操作日志" -> "/api/operation-logs";
            case "系统配置" -> "/system/config";
            case "系统字典" -> "/system/dict/data";
            case "模拟数据" -> "/api/mock-pdm-json-data";
            case "协议配置" -> "/api/protocol/protocolConfig";
            case "协议测试记录" -> "/api/protocol/protocolTestRecord";
            case "协议类型" -> "/api/protocol/protocolType";
            case "报表图表" -> "/api/report/charts";
            case "报表管理" -> "/api/reports";
            case "报表模板" -> "/api/report/templates";
            case "模板统计" -> "/api/report/template-statistics";
            case "接口模板" -> "/api/template";
            case "模板环境" -> "/api/template/environment";
            case "模板执行" -> "/api/template/execute";
            case "执行日志" -> "/api/template/execute-log";
            case "模板收藏" -> "/api/template/favorite";
            case "模板目录" -> "/api/template/folder";
            case "模板历史" -> "/api/template/history";
            case "模板导入" -> "/api/template/import-export";
            case "模板任务" -> "/api/template/job";
            default -> "/api/business/" + sanitizePathSegment(systemModule);
        };
        String actionPath = switch (operationType) {
            case "新增" -> "/add";
            case "修改" -> "/edit";
            case "删除" -> "/remove";
            case "导入" -> "/import";
            case "导出" -> "/export";
            case "发布" -> "/publish";
            case "执行" -> "/trigger";
            case "预览" -> "/preview";
            default -> "/" + sanitizePathSegment(operationType);
        };
        return modulePath + actionPath;
    }

    private String sanitizePathSegment(String value) {
        if (!StringUtils.hasText(value)) {
            return "unknown";
        }
        return value.replaceAll("[^A-Za-z0-9\\u4e00-\\u9fa5_-]", "-");
    }

    private String getCurrentUsernameOrDefault() {
        String username = SecurityUtils.getUsername();
        return StringUtils.hasText(username) ? username : "系统";
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
        fillGenerationLogDetail(vo, log);
        return vo;
    }

    private void fillGenerationLogDetail(TemplateJobGenerationLogVO vo, TemplateJobGenerationLog log) {
        vo.setSystemModule(log.getJobNamePrefix());
        vo.setOperatorName(log.getCreateName());
        vo.setOperationTime(log.getCreateTime());
        if (!StringUtils.hasText(log.getMessage())) {
            return;
        }
        try {
            Map<String, Object> detail = JSON.parseObject(log.getMessage(), new TypeReference<Map<String, Object>>() {});
            vo.setSystemModule(stringValue(detail.get("systemModule"), vo.getSystemModule()));
            vo.setOperationType(stringValue(detail.get("operationType"), null));
            vo.setOperatorName(stringValue(detail.get("operatorName"), vo.getOperatorName()));
            vo.setDepartment(stringValue(detail.get("department"), null));
            vo.setRequestMethod(stringValue(detail.get("requestMethod"), null));
            vo.setRequestUrl(stringValue(detail.get("requestUrl"), null));
            vo.setLogSource(stringValue(detail.get("logSource"), null));
            vo.setDurationMs(longValue(detail.get("durationMs")));
            vo.setOperationTime(parseLocalDateTime(stringValue(detail.get("operationTime"), null), vo.getOperationTime()));
        } catch (Exception ignored) {
            // 老数据 message 是普通文本时，保留基础字段展示。
        }
    }

    private String stringValue(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(String value, LocalDateTime fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private List<Long> parseJobIds(String jobIds) {
        if (!StringUtils.hasText(jobIds)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseObject(jobIds, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private record GenerationLogScenario(String systemModule, String operationType, String requestMethod) {
    }
}
