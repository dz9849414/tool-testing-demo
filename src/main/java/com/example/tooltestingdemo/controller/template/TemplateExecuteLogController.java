package com.example.tooltestingdemo.controller.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;
import com.example.tooltestingdemo.service.template.TemplateExecuteLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

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

    /**
     * 分页查询模板执行日志。
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:search')")
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
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:search')")
    public Result<TemplateExecuteLog> getLogById(@PathVariable Long id) {
        TemplateExecuteLog log = executeLogService.getById(id);
        return log != null ? Result.success(log) : Result.error("日志不存在");
    }

}
