package com.example.tooltestingdemo.controller.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.template.TemplateJobGenerateRequest;
import com.example.tooltestingdemo.entity.template.TemplateJobGenerationLog;
import com.example.tooltestingdemo.service.template.TemplateJobGenerationService;
import com.example.tooltestingdemo.vo.TemplateJobGenerationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 模板任务批量生成 Controller
 */
@RestController
@RequestMapping("/api/template/job-generation")
@RequiredArgsConstructor
public class TemplateJobGenerationController {

    private final TemplateJobGenerationService generationService;

    /**
     * 按时间范围和条数批量生成任务
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<TemplateJobGenerationLogVO> generate(@RequestBody TemplateJobGenerateRequest request) {
        return Result.success("生成成功", generationService.generate(request));
    }

    /**
     * 分页查询生成记录
     */
    @GetMapping("/logs/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<IPage<TemplateJobGenerationLogVO>> pageLogs(
        @RequestParam(defaultValue = "1") Long current,
        @RequestParam(defaultValue = "10") Long size,
        @RequestParam(required = false) String keyword) {
        Page<TemplateJobGenerationLog> page = new Page<>(current, size);
        return Result.success(generationService.pageLogs(page, keyword));
    }

    /**
     * 批量删除生成记录，并删除这些记录对应生成的任务
     */
    @PostMapping("/logs/batch-delete")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:relateTask')")
    public Result<Map<String, Object>> batchDeleteLogs(@RequestBody List<Long> ids) {
        int deletedCount = generationService.batchDeleteLogsAndJobs(ids);
        return Result.success("删除成功", Map.of("deletedCount", deletedCount));
    }
}
