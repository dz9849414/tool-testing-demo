package com.example.tooltestingdemo.controller.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;
import com.example.tooltestingdemo.service.TraceRuntimeLogStore;
import com.example.tooltestingdemo.service.template.TemplateExecuteLogService;
import com.example.tooltestingdemo.vo.TraceChainDetailVO;
import com.example.tooltestingdemo.vo.TraceRuntimeLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 模板执行统一日志 Controller。
 */
@Slf4j
@RestController
@RequestMapping("/api/template/execute-log")
@RequiredArgsConstructor
public class TemplateExecuteLogController {

    private final TemplateExecuteLogService executeLogService;
    private final TraceRuntimeLogStore traceRuntimeLogStore;

    /**
     * 分页查询模板执行日志。
     */
    @GetMapping("/page")
    public Result<IPage<TemplateExecuteLog>> pageLogs(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String executeType,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<TemplateExecuteLog> page = new Page<>(current, size);
        return Result.success(executeLogService.pageLogs(
                page, templateId, jobId, executeType, success, keyword, startTime, endTime));
    }

    /**
     * 获取单条执行日志详情。
     */
    @GetMapping("/{id}")
    public Result<TemplateExecuteLog> getLogById(@PathVariable Long id) {
        TemplateExecuteLog log = executeLogService.getById(id);
        return log != null ? Result.success(log) : Result.error("日志不存在");
    }

    /**
     * 根据 traceId 查询完整链路信息。
     */
    @GetMapping("/trace/{traceId}")
    public Result<TraceChainDetailVO> getTraceChainDetail(@PathVariable String traceId) {
        if (!StringUtils.hasText(traceId)) {
            return Result.error("traceId 不能为空");
        }

        TraceChainDetailVO detail = executeLogService.getTraceChainDetail(traceId.trim());
        if ((detail.getJobLogs() == null || detail.getJobLogs().isEmpty())
                && (detail.getExecuteLogs() == null || detail.getExecuteLogs().isEmpty())) {
            return Result.error("未查询到对应链路信息");
        }

        return Result.success(detail);
    }

    /**
     * 根据 traceId 查询当前进程内采集到的运行时日志。
     */
    @GetMapping("/trace/{traceId}/runtime-logs")
    public Result<TraceRuntimeLogVO> getTraceRuntimeLogs(@PathVariable String traceId) {
        if (!StringUtils.hasText(traceId)) {
            return Result.error("traceId 不能为空");
        }

        TraceRuntimeLogVO runtimeLogs = traceRuntimeLogStore.getByTraceId(traceId.trim());
        if (runtimeLogs == null || runtimeLogs.getEntryCount() == null || runtimeLogs.getEntryCount() == 0) {
            return Result.error("未查询到对应链路日志");
        }

        return Result.success(runtimeLogs);
    }
}
