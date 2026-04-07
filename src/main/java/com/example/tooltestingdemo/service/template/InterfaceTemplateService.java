package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.template.*;

import java.util.List;

/**
 * 接口模板 Service 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/template/InterfaceTemplateService.java
 */
public interface InterfaceTemplateService extends IService<InterfaceTemplate> {

    /**
     * 创建模板（包含所有关联信息）
     * 
     * @param template 模板信息
     * @param headers 请求头列表
     * @param parameters 参数列表
     * @param formDataList FormData列表
     * @param assertions 断言列表
     * @param preProcessors 前置处理器列表
     * @param postProcessors 后置处理器列表
     * @param variables 变量列表
     * @return 创建后的模板
     */
    InterfaceTemplate createTemplate(InterfaceTemplate template,
                                      List<TemplateHeader> headers,
                                      List<TemplateParameter> parameters,
                                      List<TemplateFormData> formDataList,
                                      List<TemplateAssertion> assertions,
                                      List<TemplatePreProcessor> preProcessors,
                                      List<TemplatePostProcessor> postProcessors,
                                      List<TemplateVariable> variables);

    /**
     * 更新模板（包含所有关联信息）
     * 
     * @param template 模板信息
     * @param headers 请求头列表
     * @param parameters 参数列表
     * @param formDataList FormData列表
     * @param assertions 断言列表
     * @param preProcessors 前置处理器列表
     * @param postProcessors 后置处理器列表
     * @param variables 变量列表
     * @return 是否成功
     */
    boolean updateTemplate(InterfaceTemplate template,
                           List<TemplateHeader> headers,
                           List<TemplateParameter> parameters,
                           List<TemplateFormData> formDataList,
                           List<TemplateAssertion> assertions,
                           List<TemplatePreProcessor> preProcessors,
                           List<TemplatePostProcessor> postProcessors,
                           List<TemplateVariable> variables);

    /**
     * 获取模板详情（包含所有关联信息）
     * 
     * @param id 模板ID
     * @return 模板详情
     */
    InterfaceTemplate getTemplateDetail(Long id);

    /**
     * 分页查询模板列表
     * 
     * @param page 分页参数
     * @param folderId 文件夹ID
     * @param keyword 关键词
     * @param protocolType 协议类型
     * @param status 状态
     * @return 分页结果
     */
    IPage<InterfaceTemplate> pageTemplates(Page<InterfaceTemplate> page,
                                            Long folderId,
                                            String keyword,
                                            String protocolType,
                                            Integer status);

    /**
     * 复制模板
     * 
     * @param id 原模板ID
     * @param newName 新模板名称
     * @return 新模板
     */
    InterfaceTemplate copyTemplate(Long id, String newName);

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
    boolean deleteTemplate(Long id);

    /**
     * 移动模板到指定文件夹
     * 
     * @param id 模板ID
     * @param folderId 目标文件夹ID
     * @return 是否成功
     */
    boolean moveTemplate(Long id, Long folderId);
}
