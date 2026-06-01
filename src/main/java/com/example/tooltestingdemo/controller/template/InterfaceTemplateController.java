package com.example.tooltestingdemo.controller.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.dto.TemplateParamConfigDTO;
import com.example.tooltestingdemo.dto.TemplatePageQueryDTO;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.entity.template.TemplateFile;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.TemplateFileService;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;

/**
 * 接口模板 Controller
 *
 * 文件位置：src/main/java/com/example/tooltestingdemo/controller/template/InterfaceTemplateController.java
 */
@Slf4j
@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
public class InterfaceTemplateController {

    private final InterfaceTemplateService templateService;
    private final TemplateFileService fileService;

    /**
     * 分页查询模板列表
     *
     * 接口地址：GET /api/template/page
     *
     * @param current 当前页
     * @param size 每页条数
     * @param folderId 文件夹ID
     * @param keyword 关键词
     * @param protocolId 协议ID
     * @param protocolType 协议类型
     * @param statuses 状态，支持多个
     * @param extNum1 扩展数字字段1
     * @return 分页结果VO
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:search')")
    public Result<IPage<InterfaceTemplateVO>> pageTemplates(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String extField2,
            @RequestParam(required = false) String extField3,
            @RequestParam(required = false) String pdmSystemType,
            @RequestParam(required = false) Long protocolId,
            @RequestParam(required = false) String protocolType,
            @RequestParam(required = false, name = "status") List<Integer> statuses,
            @RequestParam(required = false) Long extNum1) {

        Page<InterfaceTemplate> page = new Page<>(current, size);
        IPage<InterfaceTemplateVO> result = templateService.pageTemplates(
            page, folderId, keyword, name, extField2, extField3, protocolId, protocolType, statuses, extNum1, pdmSystemType);
        return Result.success(result);
    }

    /**
     * 分页查询模板列表（POST 请求体版本）。
     *
     * 接口地址：POST /api/template/page
     */
    @PostMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:search')")
    public Result<IPage<InterfaceTemplateVO>> pageTemplatesByPost(@RequestBody(required = false) TemplatePageQueryDTO query) {
        TemplatePageQueryDTO condition = query == null ? new TemplatePageQueryDTO() : query;
        Long current = condition.getCurrent() == null ? 1L : condition.getCurrent();
        Long size = condition.getSize() == null ? 10L : condition.getSize();
        Page<InterfaceTemplate> page = new Page<>(current, size);
        IPage<InterfaceTemplateVO> result = templateService.pageTemplates(
            page,
            condition.getFolderId(),
            condition.getKeyword(),
            condition.getName(),
            condition.getExtField2(),
            condition.getExtField3(),
            condition.getProtocolId(),
            condition.getProtocolType(),
            condition.getStatus(),
            condition.getExtNum1(),
            condition.getPdmSystemType()
        );
        return Result.success(result);
    }

    /**
     * 获取模板详情
     *
     * 接口地址：GET /api/template/{id}
     *
     * @param id 模板ID
     * @return 模板详情VO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:search')")
    public Result<InterfaceTemplateVO> getTemplateById(@PathVariable Long id) {
        InterfaceTemplateVO vo = templateService.getTemplateDetail(id);
        if (vo != null) {
            return Result.success(vo);
        }
        return Result.error("模板不存在");
    }

    /**
     * 获取模板参数配置页数据。
     *
     * 接口地址：GET /api/template/{id}/param-config
     */
    @GetMapping("/{id}/param-config")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:search')")
    public Result<TemplateParamConfigDTO> getParamConfig(@PathVariable Long id) {
        TemplateParamConfigDTO dto = templateService.getParamConfig(id);
        if (dto != null) {
            return Result.success(dto);
        }
        return Result.error("模板不存在");
    }

    /**
     * 保存模板参数配置页数据。
     *
     * 接口地址：PUT /api/template/{id}/param-config
     */
    @PutMapping("/{id}/param-config")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<TemplateParamConfigDTO> updateParamConfig(@PathVariable Long id,
                                                            @RequestBody TemplateParamConfigDTO dto) {
        TemplateParamConfigDTO result = templateService.updateParamConfig(id, dto);
        return Result.success("参数配置保存成功", result);
    }

    /**
     * 创建模板（默认创建为草稿状态）
     *
     * 接口地址：POST /api/template
     *
     * @param dto 模板DTO（包含关联数据）
     * @return 创建后的模板VO（状态为草稿）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:create')")
    public Result<InterfaceTemplateVO> createTemplate(@RequestBody InterfaceTemplateDTO dto) {
        InterfaceTemplateVO vo = templateService.createTemplate(dto);
        return Result.success("草稿创建成功", vo);
    }

    /**
     * 更新模板
     *
     * 接口地址：PUT /api/template/{id}
     *
     * @param id 模板ID
     * @param dto 模板DTO（包含关联数据）
     * @return 是否成功
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<String> updateTemplate(@PathVariable Long id, @RequestBody InterfaceTemplateDTO dto) {
        boolean success = templateService.updateTemplate(id, dto);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除模板
     *
     * 接口地址：DELETE /api/template/{id}
     *
     * @param id 模板ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:delete')")
    public Result<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        Map<String, Object> result = templateService.deleteTemplate(id);
        if (Boolean.TRUE.equals(result.get("deleted"))) {
            Integer cleanedRelationCount = (Integer) result.get("cleanedRelationCount");
            return Result.success("删除成功，清理关联数据 " + cleanedRelationCount + " 条", result);
        }
        return Result.error("删除失败");
    }

    /**
     * 清理模板关联数据，但保留模板主记录
     *
     * 接口地址：DELETE /api/template/{id}/cleanup
     *
     * @param id 模板ID
     * @return 清理结果
     */
    @DeleteMapping("/{id}/cleanup")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:delete')")
    public Result<Map<String, Object>> cleanTemplateRelations(@PathVariable Long id) {
        Map<String, Object> result = templateService.cleanTemplateRelations(id);
        if (Boolean.TRUE.equals(result.get("deleted"))) {
            Integer cleanedRelationCount = (Integer) result.get("cleanedRelationCount");
            return Result.success("清理成功，清理关联数据 " + cleanedRelationCount + " 条", result);
        }
        return Result.error("清理失败");
    }

    /**
     * 批量删除模板
     *
     * 接口地址：DELETE /api/template/batch
     *
     * @param ids 模板ID数组
     * @return 删除结果，包含成功和失败的ID列表
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:delete')")
    public Result<Map<String, Object>> batchDeleteTemplates(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            return Result.error("模板ID列表不能为空");
        }

        Map<String, Object> result = templateService.batchDeleteTemplates(ids);

        Map<String, Object> data = new HashMap<>();
        List<Long> successIds = (List<Long>) result.get("success");
        List<Long> failIds = (List<Long>) result.get("fail");
        data.put("successIds", successIds);
        data.put("failIds", failIds);
        data.put("successCount", successIds.size());
        data.put("failCount", failIds.size());
        data.put("cleanedRelationCount", result.get("cleanedRelationCount"));
        data.put("cleanupDetails", result.get("cleanupDetails"));

        if (failIds.isEmpty()) {
            return Result.success("批量删除成功，清理关联数据 " + result.get("cleanedRelationCount") + " 条", data);
        } else {
            return Result.success("批量删除部分成功，清理关联数据 " + result.get("cleanedRelationCount") + " 条", data);
        }
    }

    /**
     * 批量清理模板关联数据，但保留模板主记录
     *
     * 接口地址：DELETE /api/template/batch/cleanup
     *
     * @param ids 模板ID数组
     * @return 清理结果
     */
    @DeleteMapping("/batch/cleanup")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:delete')")
    public Result<Map<String, Object>> batchCleanTemplateRelations(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            return Result.error("模板ID列表不能为空");
        }

        Map<String, Object> result = templateService.batchCleanTemplateRelations(ids);

        Map<String, Object> data = new HashMap<>();
        List<Long> successIds = (List<Long>) result.get("success");
        List<Long> failIds = (List<Long>) result.get("fail");
        data.put("successIds", successIds);
        data.put("failIds", failIds);
        data.put("successCount", successIds.size());
        data.put("failCount", failIds.size());
        data.put("cleanedRelationCount", result.get("cleanedRelationCount"));
        data.put("cleanupDetails", result.get("cleanupDetails"));

        if (failIds.isEmpty()) {
            return Result.success("批量清理成功，清理关联数据 " + result.get("cleanedRelationCount") + " 条", data);
        } else {
            return Result.success("批量清理部分成功，清理关联数据 " + result.get("cleanedRelationCount") + " 条", data);
        }
    }

    /**
     * 清理模板关联数据，单个模板版本
     *
     * 接口地址：DELETE /api/template/{id}/relations
     */
    @DeleteMapping("/{id}/relations")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:delete')")
    public Result<Map<String, Object>> cleanTemplateRelationsOnly(@PathVariable Long id) {
        return cleanTemplateRelations(id);
    }

    /**
     * 复制模板
     *
     * 接口地址：POST /api/template/{id}/copy
     *
     * @param id 原模板ID
     * @param newName 新模板名称
     * @return 新模板VO
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<InterfaceTemplateVO> copyTemplate(@PathVariable Long id, @RequestParam String newName) {
        InterfaceTemplateVO vo = templateService.copyTemplate(id, newName);
        return Result.success("复制成功", vo);
    }

    /**
     * 发布模板
     *
     * 接口地址：PUT /api/template/{id}/publish
     *
     * @param id 模板ID
     * @return 是否成功
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<String> publishTemplate(@PathVariable Long id) {
        boolean success = templateService.publishTemplate(id);
        if (success) {
            return Result.success("发布成功");
        }
        return Result.error("发布失败");
    }

    /**
     * 归档模板
     *
     * 接口地址：PUT /api/template/{id}/archive
     *
     * @param id 模板ID
     * @return 是否成功
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:version')")
    public Result<String> archiveTemplate(@PathVariable Long id) {
        boolean success = templateService.archiveTemplate(id);
        if (success) {
            return Result.success("归档成功");
        }
        return Result.error("归档失败");
    }

    /**
     * 禁用模板
     *
     * 接口地址：PUT /api/template/{id}/disable
     *
     * @param id 模板ID
     * @return 是否成功
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<String> disableTemplate(@PathVariable Long id) {
        boolean success = templateService.disableTemplate(id);
        if (success) {
            return Result.success("禁用成功");
        }
        return Result.error("禁用失败，只有已发布状态的模板可以禁用");
    }

    /**
     * 启用模板
     *
     * 接口地址：PUT /api/template/{id}/enable
     *
     * @param id 模板ID
     * @return 是否成功
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<String> enableTemplate(@PathVariable Long id) {
        boolean success = templateService.enableTemplate(id);
        if (success) {
            return Result.success("启用成功");
        }
        return Result.error("启用失败，只有已禁用状态的模板可以启用");
    }



    /**
     * 移动模板
     *
     * 接口地址：PUT /api/template/{id}/move
     *
     * @param id 模板ID
     * @param folderId 目标文件夹ID
     * @return 是否成功
     */
    @PutMapping("/{id}/move")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<String> moveTemplate(@PathVariable Long id, @RequestParam Long folderId) {
        boolean success = templateService.moveTemplate(id, folderId);
        if (success) {
            return Result.success("移动成功");
        }
        return Result.error("移动失败");
    }

    // ==================== 草稿与审核接口 ====================

    /**
     * 保存草稿（创建新模板）
     *
     * 接口地址：POST /api/template/draft
     *
     * @param dto 模板DTO
     * @return 保存后的模板VO
     */
    @PostMapping("/draft")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<InterfaceTemplateVO> saveDraft(@RequestBody InterfaceTemplateDTO dto) {
        InterfaceTemplateVO vo = templateService.saveDraft(dto);
        return Result.success("草稿保存成功", vo);
    }

    /**
     * 保存草稿（更新现有模板）
     *
     * 接口地址：PUT /api/template/{id}/draft
     *
     * @param id 模板ID
     * @param dto 模板DTO
     * @return 保存后的模板VO
     */
    @PutMapping("/{id}/draft")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<InterfaceTemplateVO> updateDraft(@PathVariable Long id, @RequestBody InterfaceTemplateDTO dto) {
        InterfaceTemplateVO vo = templateService.saveDraft(id, dto);
        return Result.success("草稿更新成功", vo);
    }

    /**
     * 提交审核
     *
     * 接口地址：POST /api/template/{id}/submit
     *
     * @param id 模板ID
     * @param dto 模板DTO
     * @return 提交后的模板VO
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<InterfaceTemplateVO> submitForReview(@PathVariable Long id, @RequestBody InterfaceTemplateDTO dto) {
        InterfaceTemplateVO vo = templateService.submitForReview(id, dto);
        return Result.success("提交审核成功", vo);
    }

    /**
     * 提交审核（仅传 id，使用当前模板数据提交）
     * 接口地址：POST /api/template/{id}/submit/simple
     */
    @PostMapping("/submit/simple/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<InterfaceTemplateVO> submitForReviewById(@PathVariable Long id) {
        InterfaceTemplateVO current = templateService.getTemplateDetail(id);
        if (current == null) {
            return Result.error("模板不存在");
        }

        InterfaceTemplateDTO dto = new InterfaceTemplateDTO();
        // 复制基本字段（VO -> DTO），列表类可能需要单独转换，但校验通常依赖基础字段
        BeanUtils.copyProperties(current, dto);

        InterfaceTemplateVO vo = templateService.submitForReview(id, dto);
        return Result.success("提交审核成功", vo);
    }

    /**
     * 审核通过
     *
     * 接口地址：PUT /api/template/{id}/approve
     *
     * @param id 模板ID
     * @return 是否成功
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:version')")
    public Result<String> approveTemplate(@PathVariable Long id) {
        boolean success = templateService.approveTemplate(id);
        if (success) {
            return Result.success("审核通过");
        }
        return Result.error("审核通过失败");
    }

    /**
     * 审核驳回
     *
     * 接口地址：PUT /api/template/{id}/reject
     *
     * @param id 模板ID
     * @param reason 驳回原因
     * @return 是否成功
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:version')")
    public Result<String> rejectTemplate(@PathVariable Long id, @RequestParam(required = false) String reason) {
        boolean success = templateService.rejectTemplate(id, reason);
        if (success) {
            return Result.success("已驳回");
        }
        return Result.error("驳回失败");
    }

    // ==================== 文件附件接口 ====================

    /**
     * 上传文件附件
     *
     * 接口地址：POST /api/template/{id}/files
     *
     * @param id 模板ID
     * @param file 文件
     * @param fileCategory 文件类别（ATTACHMENT/REQUEST/RESPONSE）
     * @param description 文件描述
     * @param remark 文件备注
     * @return 上传的文件信息
     */
    @PostMapping("/{id}/files")
    public Result<TemplateFile> uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "ATTACHMENT") String fileCategory,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String remark) {

        TemplateFile templateFile = fileService.uploadFile(id, file, fileCategory,
                remark != null && !remark.isEmpty() ? remark : description);
        return Result.success("上传成功", templateFile);
    }

    /**
     * 批量上传文件
     *
     * 接口地址：POST /api/template/{id}/files/batch
     */
    @PostMapping("/{id}/files/batch")
    public Result<List<TemplateFile>> uploadFiles(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false, defaultValue = "ATTACHMENT") String fileCategory) {

        List<TemplateFile> result = fileService.uploadFiles(id, files, fileCategory);
        return Result.success("上传成功", result);
    }

    /**
     * 获取模板文件列表
     *
     * 接口地址：GET /api/template/{id}/files
     */
    @GetMapping("/{id}/files")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<List<TemplateFile>> getFiles(@PathVariable Long id) {
        List<TemplateFile> files = fileService.getFilesByTemplateId(id);
        return Result.success(files);
    }

    /**
     * 删除文件
     *
     * 接口地址：DELETE /api/template/files/{fileId}
     */
    @DeleteMapping("/files/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public Result<String> deleteFile(@PathVariable Long fileId) {
        boolean success = fileService.deleteFile(fileId);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 下载文件
     *
     * 接口地址：GET /api/template/files/{fileId}/download
     */
    @GetMapping("/files/{fileId}/download")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        TemplateFile file = fileService.getFileById(fileId);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileContent = fileService.downloadFile(fileId);

        // 对文件名进行URL编码，防止中文乱码
        String encodedFileName = URLEncoder.encode(file.getFileOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(fileContent);
    }

    /**
     * 通过文件名下载文件（用于直接URL访问）
     *
     * 接口地址：GET /api/template/file/download/{filename}
     */
    @GetMapping("/file/download/{filename:.+}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('test:template:edit')")
    public ResponseEntity<byte[]> downloadFileByName(@PathVariable String filename) {
        // 根据文件名查找文件记录
        List<TemplateFile> files = fileService.getFilesByTemplateId(0L); // 这里需要从service层提供按文件名查询的方法

        // 简化为直接通过路径访问，实际应该通过数据库查询
        return ResponseEntity.notFound().build();
    }
}