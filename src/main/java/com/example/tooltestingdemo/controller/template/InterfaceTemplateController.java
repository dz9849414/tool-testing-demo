package com.example.tooltestingdemo.controller.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 分页查询模板列表
     * 
     * 接口地址：GET /api/template/page
     * 
     * @param current 当前页
     * @param size 每页条数
     * @param folderId 文件夹ID
     * @param keyword 关键词
     * @param protocolType 协议类型
     * @param status 状态
     * @return 分页结果VO
     */
    @GetMapping("/page")
    public Result<IPage<InterfaceTemplateVO>> pageTemplates(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String protocolType,
            @RequestParam(required = false) Integer status) {
        
        Page<com.example.tooltestingdemo.entity.template.InterfaceTemplate> page = new Page<>(current, size);
        IPage<InterfaceTemplateVO> result = templateService.pageTemplates(page, folderId, keyword, protocolType, status);
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
    public Result<InterfaceTemplateVO> getTemplateById(@PathVariable Long id) {
        InterfaceTemplateVO vo = templateService.getTemplateDetail(id);
        if (vo != null) {
            return Result.success(vo);
        }
        return Result.error("模板不存在");
    }

    /**
     * 创建模板
     * 
     * 接口地址：POST /api/template
     * 
     * @param dto 模板DTO（包含关联数据：headers, parameters, formDataList, assertions, preProcessors, postProcessors, variables）
     * @return 创建后的模板VO
     */
    @PostMapping
    public Result<InterfaceTemplateVO> createTemplate(@RequestBody InterfaceTemplateDTO dto) {
        InterfaceTemplateVO vo = templateService.createTemplate(dto);
        return Result.success("创建成功", vo);
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
    public Result<String> deleteTemplate(@PathVariable Long id) {
        boolean success = templateService.deleteTemplate(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
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
    public Result<String> archiveTemplate(@PathVariable Long id) {
        boolean success = templateService.archiveTemplate(id);
        if (success) {
            return Result.success("归档成功");
        }
        return Result.error("归档失败");
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
    public Result<String> moveTemplate(@PathVariable Long id, @RequestParam Long folderId) {
        boolean success = templateService.moveTemplate(id, folderId);
        if (success) {
            return Result.success("移动成功");
        }
        return Result.error("移动失败");
    }
}
