package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import com.example.tooltestingdemo.vo.TemplateEnvironmentVO;

import java.util.List;

/**
 * 模板环境配置 Service 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/template/TemplateEnvironmentService.java
 */
public interface TemplateEnvironmentService extends IService<TemplateEnvironment> {

    /**
     * 根据ID获取环境配置
     * 
     * @param id 环境配置ID
     * @return 环境配置VO
     */
    TemplateEnvironmentVO getEnvironmentById(Long id);

    /**
     * 获取模板的所有环境配置
     * 
     * @param templateId 模板ID
     * @return 环境配置VO列表
     */
    List<TemplateEnvironmentVO> getEnvironmentsByTemplateId(Long templateId);

    /**
     * 获取模板的默认环境
     * 
     * @param templateId 模板ID
     * @return 默认环境配置VO
     */
    TemplateEnvironmentVO getDefaultEnvironment(Long templateId);

    /**
     * 创建环境配置
     * 
     * @param environment 环境配置
     * @return 创建后的环境配置VO
     */
    TemplateEnvironmentVO createEnvironment(TemplateEnvironment environment);

    /**
     * 更新环境配置
     * 
     * @param environment 环境配置
     * @return 是否成功
     */
    boolean updateEnvironment(TemplateEnvironment environment);

    /**
     * 删除环境配置
     * 
     * @param id 环境配置ID
     * @return 是否成功
     */
    boolean deleteEnvironment(Long id);

    /**
     * 设置默认环境
     * 
     * @param templateId 模板ID
     * @param envId 环境配置ID
     * @return 是否成功
     */
    boolean setDefaultEnvironment(Long templateId, Long envId);

    /**
     * 克隆环境配置
     * 
     * @param sourceEnvId 源环境配置ID
     * @param newName 新环境名称
     * @return 新环境配置VO
     */
    TemplateEnvironmentVO cloneEnvironment(Long sourceEnvId, String newName);
}
