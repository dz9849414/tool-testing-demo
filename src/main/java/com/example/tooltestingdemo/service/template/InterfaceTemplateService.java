package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;

import java.util.List;
import java.util.Map;

/**
 * 接口模板 Service 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/template/InterfaceTemplateService.java
 */
public interface InterfaceTemplateService extends IService<InterfaceTemplate> {

    /**
     * 创建模板（默认创建为草稿状态）
     * 
     * @param dto 模板DTO（包含关联数据）
     * @return 创建后的模板VO（状态为草稿）
     */
    InterfaceTemplateVO createTemplate(InterfaceTemplateDTO dto);

    /**
     * 更新模板（包含所有关联信息）
     * 
     * @param id 模板ID
     * @param dto 模板DTO（包含关联数据）
     * @return 是否成功
     */
    boolean updateTemplate(Long id, InterfaceTemplateDTO dto);

    /**
     * 获取模板详情（包含所有关联信息）
     * 
     * @param id 模板ID
     * @return 模板详情VO
     */
    InterfaceTemplateVO getTemplateDetail(Long id);

    /**
     * 分页查询模板列表
     * 
     * @param page 分页参数
     * @param folderId 文件夹ID
     * @param keyword 关键词
     * @param protocolId 协议ID
     * @param protocolType 协议类型
     * @param status 状态
     * @param extNum1 扩展数字字段1
     * @return 分页结果VO
     */
    IPage<InterfaceTemplateVO> pageTemplates(Page<InterfaceTemplate> page,
                                              Long folderId,
                                              String keyword,
                                              Long protocolId,
                                              String protocolType,
                                              Integer status,
                                              Long extNum1);

    /**
     * 复制模板
     * 
     * @param id 原模板ID
     * @param newName 新模板名称
     * @return 新模板VO
     */
    InterfaceTemplateVO copyTemplate(Long id, String newName);

    /**
     * 发布模板
     * 
     * @param id 模板ID
     * @return 是否成功
     */
    boolean publishTemplate(Long id);

    /**
     * 归档模板
     * 
     * @param id 模板ID
     * @return 是否成功
     */
    boolean archiveTemplate(Long id);

    /**
     * 删除模板（逻辑删除）
     * 
     * @param id 模板ID
     * @return 是否成功
     */
    Map<String, Object> deleteTemplate(Long id);

    /**
     * 批量删除模板（逻辑删除）
     * 
     * @param ids 模板ID数组
     * @return 删除结果，包含成功和失败的ID列表
     */
    Map<String, Object> batchDeleteTemplates(Long[] ids);

    /**
     * 移动模板到指定文件夹
     * 
     * @param id 模板ID
     * @param folderId 目标文件夹ID
     * @return 是否成功
     */
    boolean moveTemplate(Long id, Long folderId);

    /**
     * 保存草稿（不校验必填项，仅保存当前内容）
     * 自动生成或递增版本号（V1.0 -> V1.1 -> V1.2）
     * 
     * @param dto 模板DTO
     * @return 保存后的模板VO
     */
    InterfaceTemplateVO saveDraft(InterfaceTemplateDTO dto);

    /**
     * 保存草稿（更新现有模板）
     * 
     * @param id 模板ID
     * @param dto 模板DTO
     * @return 保存后的模板VO
     */
    InterfaceTemplateVO saveDraft(Long id, InterfaceTemplateDTO dto);

    /**
     * 提交审核（校验必填项，通过后状态变为待审核）
     * 自动生成或递增主版本号（V1.5 -> V2.0）
     * 
     * @param id 模板ID
     * @param dto 模板DTO
     * @return 提交后的模板VO
     */
    InterfaceTemplateVO submitForReview(Long id, InterfaceTemplateDTO dto);

    /**
     * 审核通过
     * 
     * @param id 模板ID
     * @return 是否成功
     */
    boolean approveTemplate(Long id);

    /**
     * 审核驳回
     * 
     * @param id 模板ID
     * @param reason 驳回原因
     * @return 是否成功
     */
    boolean rejectTemplate(Long id, String reason);
}
