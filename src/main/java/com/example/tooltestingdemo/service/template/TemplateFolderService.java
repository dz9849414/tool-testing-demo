package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.template.TemplateFolder;
import com.example.tooltestingdemo.vo.TemplateFolderVO;

import java.util.List;

/**
 * 模板分类/文件夹 Service 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/template/TemplateFolderService.java
 */
public interface TemplateFolderService extends IService<TemplateFolder> {

    /**
     * 获取文件夹树形结构
     * 
     * @param parentId 父文件夹ID
     * @return 文件夹VO列表（包含子文件夹）
     */
    List<TemplateFolderVO> getFolderTree(Long parentId);

    /**
     * 创建文件夹
     * 
     * @param folder 文件夹信息
     * @return 创建后的文件夹VO
     */
    TemplateFolderVO createFolder(TemplateFolder folder);

    /**
     * 更新文件夹
     * 
     * @param folder 文件夹信息
     * @return 是否成功
     */
    boolean updateFolder(TemplateFolder folder);

    /**
     * 删除文件夹（逻辑删除）
     * 
     * @param id 文件夹ID
     * @return 是否成功
     */
    boolean deleteFolder(Long id);

    /**
     * 移动文件夹
     * 
     * @param id 文件夹ID
     * @param targetParentId 目标父文件夹ID
     * @return 是否成功
     */
    boolean moveFolder(Long id, Long targetParentId);

    /**
     * 获取文件夹详情
     * 
     * @param id 文件夹ID
     * @return 文件夹VO
     */
    TemplateFolderVO getFolderDetail(Long id);
}
