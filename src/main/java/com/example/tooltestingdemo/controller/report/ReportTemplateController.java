package com.example.tooltestingdemo.controller.report;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.report.ReportTemplateDTO;
import com.example.tooltestingdemo.entity.report.TemplateXml;
import com.example.tooltestingdemo.service.report.IReportTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            // 参数校验
            if (templateDTO.getName() == null || templateDTO.getName().trim().isEmpty()) {
                return Result.error("模板名称不能为空");
            }
            if (templateDTO.getTemplateType() == null || templateDTO.getTemplateType().trim().isEmpty()) {
                return Result.error("模板类型不能为空");
            }
            
            // 处理chapterStructure，确保是有效的JSON格式
            if (templateDTO.getChapterStructure() != null && !templateDTO.getChapterStructure().trim().isEmpty()) {
                String chapterStructure = templateDTO.getChapterStructure().trim();
                if (!chapterStructure.startsWith("[") && !chapterStructure.startsWith("{")) {
                    return Result.error("章节结构必须是有效的JSON格式");
                }
                
                // 验证JSON格式
                try {
                    com.alibaba.fastjson2.JSON.parse(chapterStructure);
                } catch (Exception e) {
                    return Result.error("章节结构JSON格式不正确");
                }
            } else {
                // 如果没有提供chapterStructure，设置为空数组
                templateDTO.setChapterStructure("[]");
            }
            
            // 处理templateStructure，确保是有效的JSON格式
            if (templateDTO.getTemplateStructure() != null && !templateDTO.getTemplateStructure().trim().isEmpty()) {
                String templateStructure = templateDTO.getTemplateStructure().trim();
                if (!templateStructure.startsWith("[") && !templateStructure.startsWith("{")) {
                    return Result.error("模板结构必须是有效的JSON格式");
                }
                
                // 验证JSON格式
                try {
                    com.alibaba.fastjson2.JSON.parse(templateStructure);
                } catch (Exception e) {
                    return Result.error("模板结构JSON格式不正确");
                }
            } else {
                // 如果没有提供templateStructure，设置为空对象
                templateDTO.setTemplateStructure("{}");
            }
            
            // 处理styleConfig，确保是有效的JSON格式
            if (templateDTO.getStyleConfig() != null && !templateDTO.getStyleConfig().trim().isEmpty()) {
                String styleConfig = templateDTO.getStyleConfig().trim();
                if (!styleConfig.startsWith("[") && !styleConfig.startsWith("{")) {
                    return Result.error("样式配置必须是有效的JSON格式");
                }
                
                // 验证JSON格式
                try {
                    com.alibaba.fastjson2.JSON.parse(styleConfig);
                } catch (Exception e) {
                    return Result.error("样式配置JSON格式不正确");
                }
            } else {
                // 如果没有提供styleConfig，设置为空对象
                templateDTO.setStyleConfig("{}");
            }
            
            Long templateId = reportTemplateService.createTemplate(templateDTO);
            return Result.success("模板创建成功", templateId);
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
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) String name) {
        try {
            List<ReportTemplateDTO> templates = reportTemplateService.getTemplateList(templateType, isPublic, name);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("获取报告模板列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取报告模板列表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:view')")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReportTemplateDTO>> getTemplateListPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) String name) {
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReportTemplateDTO> pageResult = 
                    reportTemplateService.getTemplateListPage(page, size, templateType, isPublic, name);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("分页获取报告模板列表失败：" + e.getMessage());
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

    @PostMapping("/import")
    @Operation(summary = "导入报告模板（XML文件上传）")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:import')")
    public Result<Long> importTemplate(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "newName", required = false) String newName) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的XML文件");
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
                return Result.error("只支持XML格式的文件");
            }
            
            String xmlContent = new String(file.getBytes(), "UTF-8");
            Long templateId = reportTemplateService.importTemplate(xmlContent, newName);
            return Result.success("模板导入成功", templateId);
        } catch (Exception e) {
            return Result.error("导入报告模板失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "导出报告模板（XML文件下载）")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:export')")
    public ResponseEntity<byte[]> exportTemplate(@PathVariable Long id) {
        try {
            String xmlContent = reportTemplateService.exportTemplate(id);
            if (xmlContent == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 设置响应头
            String filename = "template_" + id + ".xml";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(xmlContent.getBytes("UTF-8"), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/xml-preview")
    @Operation(summary = "预览XML模板内容")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:template:view')")
    public Result<TemplateXml> previewTemplateXml(@RequestBody String xmlContent) {
        try {
            TemplateXml templateXml = reportTemplateService.previewTemplateXml(xmlContent);
            return Result.success(templateXml);
        } catch (Exception e) {
            return Result.error("XML预览失败：" + e.getMessage());
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