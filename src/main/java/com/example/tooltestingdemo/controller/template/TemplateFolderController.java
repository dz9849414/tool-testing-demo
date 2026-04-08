package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateFolder;
import com.example.tooltestingdemo.service.template.TemplateFolderService;
import com.example.tooltestingdemo.vo.TemplateFolderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板分类/文件夹 Controller
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/controller/template/TemplateFolderController.java
 */
@Slf4j
@RestController
@RequestMapping("/api/template/folder")
@RequiredArgsConstructor
public class TemplateFolderController {

    private final TemplateFolderService folderService;

    /**
     * 获取文件夹树形结构
     * 
     * 接口地址：GET /api/template/folder/tree
     * 
     * @param parentId 父文件夹ID，不传则查询根目录
     * @return 文件夹VO列表
     */
    @GetMapping("/tree")
    public Result<List<TemplateFolderVO>> getFolderTree(@RequestParam(required = false) Long parentId) {
        List<TemplateFolderVO> folders = folderService.getFolderTree(parentId);
        return Result.success(folders);
    }

    /**
     * 创建文件夹
     * 
     * 接口地址：POST /api/template/folder
     * 
     * @param folder 文件夹信息
     * @return 创建后的文件夹VO
     */
    @PostMapping
    public Result<TemplateFolderVO> createFolder(@RequestBody TemplateFolder folder) {
        TemplateFolderVO vo = folderService.createFolder(folder);
        return Result.success("创建成功", vo);
    }

    /**
     * 更新文件夹
     * 
     * 接口地址：PUT /api/template/folder/{id}
     * 
     * @param id 文件夹ID
     * @param folder 文件夹信息
     * @return 是否成功
     */
    @PutMapping("/{id}")
    public Result<String> updateFolder(@PathVariable Long id, @RequestBody TemplateFolder folder) {
        folder.setId(id);
        boolean success = folderService.updateFolder(folder);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除文件夹
     * 
     * 接口地址：DELETE /api/template/folder/{id}
     * 
     * @param id 文件夹ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteFolder(@PathVariable Long id) {
        boolean success = folderService.deleteFolder(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 移动文件夹
     * 
     * 接口地址：PUT /api/template/folder/{id}/move
     * 
     * @param id 文件夹ID
     * @param targetParentId 目标父文件夹ID
     * @return 是否成功
     */
    @PutMapping("/{id}/move")
    public Result<String> moveFolder(@PathVariable Long id, @RequestParam Long targetParentId) {
        boolean success = folderService.moveFolder(id, targetParentId);
        if (success) {
            return Result.success("移动成功");
        }
        return Result.error("移动失败");
    }

    /**
     * 获取文件夹详情
     * 
     * 接口地址：GET /api/template/folder/{id}
     * 
     * @param id 文件夹ID
     * @return 文件夹VO
     */
    @GetMapping("/{id}")
    public Result<TemplateFolderVO> getFolderById(@PathVariable Long id) {
        TemplateFolderVO vo = folderService.getFolderDetail(id);
        if (vo != null) {
            return Result.success(vo);
        }
        return Result.error("文件夹不存在");
    }
}
