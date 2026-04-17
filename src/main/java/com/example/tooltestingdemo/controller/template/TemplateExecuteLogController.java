package com.example.tooltestingdemo.controller.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;
import com.example.tooltestingdemo.service.template.TemplateExecuteLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 模板执行统一日志 Controller（手动执行 + 定时任务执行）
 */
@Slf4j
@RestController
@RequestMapping("/api/template/execute-log")
@RequiredArgsConstructor
public class TemplateExecuteLogController {

    private final TemplateExecuteLogService executeLogService;

    /**
     * 分页查询模板执行日志
     */
    @GetMapping("/page")
    public Result<IPage<TemplateExecuteLog>> pageLogs(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String executeType,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) String keyword) {
        Page<TemplateExecuteLog> page = new Page<>(current, size);
        return Result.success(executeLogService.pageLogs(page, templateId, jobId, executeType, success, keyword));
    }

    /**
     * 获取单条执行日志详情
     */
    @GetMapping("/{id}")
    public Result<TemplateExecuteLog> getLogById(@PathVariable Long id) {
        TemplateExecuteLog log = executeLogService.getById(id);
        return log != null ? Result.success(log) : Result.error("日志不存在");
    }
}
