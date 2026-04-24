package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.TemplateImportDTO;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.service.template.TemplateImportService;
import com.example.tooltestingdemo.vo.TemplateImportResultVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;

/**
 * 模板导入导出控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/template/import-export")
@RequiredArgsConstructor
public class TemplateImportController {

    private final TemplateImportService importService;

    /**
     * 导入模板
     *
     * @param file      导入文件
     * @param format    文件格式：JSON/YAML/POSTMAN/OPENAPI（可选，默认根据文件扩展名识别）
     * @param folderId  目标文件夹ID（可选）
     * @param strategy  导入策略：SKIP/OVERWRITE/RENAME（默认SKIP）
     * @return 导入结果
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:create')")
    public Result<TemplateImportResultVO> importTemplates(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "format", required = false) String format,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "strategy", required = false, defaultValue = TemplateEnums.ImportStrategy.SKIP_CODE) String strategy) {
        
        log.info("导入模板请求: file={}, format={}, folderId={}, strategy={}", 
                file.getOriginalFilename(), format, folderId, strategy);
        
        // 验证文件
        if (file.isEmpty()) {
            return Result.error("导入文件不能为空");
        }
        
        // 构建DTO
        TemplateImportDTO dto = new TemplateImportDTO();
        dto.setFile(file);
        dto.setFormat(format);
        dto.setFolderId(folderId);
        dto.setStrategy(strategy);
        
        try {
            TemplateImportResultVO result = importService.importTemplates(dto);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入模板失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 验证导入文件
     *
     * @param file   导入文件
     * @param format 文件格式（可选）
     * @return 验证结果
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:create')")
    public Result<TemplateImportResultVO> validateImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "format", required = false) String format) {
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        TemplateImportDTO dto = new TemplateImportDTO();
        dto.setFile(file);
        dto.setFormat(format);
        
        try {
            TemplateImportResultVO result = importService.validateImport(dto);
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证导入文件失败", e);
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 导出模板为JSON
     *
     * @param templateIds 模板ID数组
     * @param response    HTTP响应
     */
    @GetMapping("/export/json")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:search')")
    public void exportToJson(@RequestParam("templateIds") Long[] templateIds, 
                             HttpServletResponse response) throws IOException {
        
        String content = importService.exportToJson(templateIds);
        
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=templates_" + 
                System.currentTimeMillis() + ".json");

        response.getWriter().write(content);
        response.getWriter().flush();
    }

    /**
     * 导出为Postman Collection
     *
     * @param templateIds 模板ID数组
     * @param response    HTTP响应
     */
    @GetMapping("/export/postman")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:search')")
    public void exportToPostman(@RequestParam("templateIds") Long[] templateIds,
                                HttpServletResponse response) throws IOException {
        
        String content = importService.exportToPostman(templateIds);
        
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=collection_" + 
                System.currentTimeMillis() + ".json");
        
        response.getWriter().write(content);
        response.getWriter().flush();
    }
}