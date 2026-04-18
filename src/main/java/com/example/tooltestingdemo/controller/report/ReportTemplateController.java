package com.example.tooltestingdemo.controller.report;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.report.ReportTemplateDTO;
import com.example.tooltestingdemo.service.report.IReportTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报告模板控制器
 */
@RestController
@RequestMapping("/api/report/templates")
@RequiredArgsConstructor
@Tag(name = "报告模板管理")
public class ReportTemplateController {

    private final IReportTemplateService reportTemplateService;

    @PostMapping
    @Operation(summary = "创建报告模板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:create')")
    public Result<Long> createTemplate(@RequestBody ReportTemplateDTO templateDTO) {
        try {
            Long templateId = reportTemplateService.createTemplate(templateDTO);
            return Result.success(templateId);
        } catch (Exception e) {
            return Result.error("创建报告模板失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新报告模板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:update')")
    public Result<Boolean> updateTemplate(@PathVariable Long id, @RequestBody ReportTemplateDTO templateDTO) {
        try {
            Boolean result = reportTemplateService.updateTemplate(id, templateDTO);
            return result ? Result.success(true) : Result.error("报告模板不存在");
        } catch (Exception e) {
            return Result.error("更新报告模板失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报告模板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:delete')")
    public Result<Boolean> deleteTemplate(@PathVariable Long id) {
        try {
            Boolean result = reportTemplateService.deleteTemplate(id);
            return result ? Result.success(true) : Result.error("报告模板不存在");
        } catch (Exception e) {
            return Result.error("删除报告模板失败：" + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "获取报告模板列表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:view')")
    public Result<List<ReportTemplateDTO>> getTemplateList(
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) Boolean isPublic) {
        try {
            List<ReportTemplateDTO> templates = reportTemplateService.getTemplateList(templateType, isPublic);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("获取报告模板列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取报告模板详情")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:view')")
    public Result<ReportTemplateDTO> getTemplateDetail(@PathVariable Long id) {
        try {
            ReportTemplateDTO template = reportTemplateService.getTemplateDetail(id);
            return template != null ? Result.success(template) : Result.error("报告模板不存在");
        } catch (Exception e) {
            return Result.error("获取报告模板详情失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/import")
    @Operation(summary = "导入报告模板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:import')")
    public Result<Boolean> importTemplate(@PathVariable Long id, @RequestBody String xmlContent) {
        try {
            Boolean result = reportTemplateService.importTemplate(xmlContent);
            return result ? Result.success(true) : Result.error("导入报告模板失败");
        } catch (Exception e) {
            return Result.error("导入报告模板失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "导出报告模板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:export')")
    public Result<String> exportTemplate(@PathVariable Long id) {
        try {
            String xmlContent = reportTemplateService.exportTemplate(id);
            return xmlContent != null ? Result.success(xmlContent) : Result.error("报告模板不存在");
        } catch (Exception e) {
            return Result.error("导出报告模板失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/relate")
    @Operation(summary = "关联业务对象")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:relate')")
    public Result<Boolean> relateBusinessType(@PathVariable Long id, @RequestParam String businessType) {
        try {
            Boolean result = reportTemplateService.relateBusinessType(id, businessType);
            return result ? Result.success(true) : Result.error("报告模板不存在");
        } catch (Exception e) {
            return Result.error("关联业务对象失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "获取模板使用记录")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:view')")
    public Result<List<Object>> getTemplateUsageRecords(@PathVariable Long id) {
        try {
            List<Object> records = reportTemplateService.getTemplateUsageRecords(id);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("获取模板使用记录失败：" + e.getMessage());
        }
    }
}