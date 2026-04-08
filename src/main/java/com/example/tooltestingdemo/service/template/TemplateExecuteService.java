package com.example.tooltestingdemo.service.template;

import com.example.tooltestingdemo.vo.InterfaceTemplateVO;

import java.util.Map;

/**
 * 模板执行 Service 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/template/TemplateExecuteService.java
 */
public interface TemplateExecuteService {

    /**
     * 执行模板请求
     * 
     * @param templateId 模板ID
     * @param environmentId 环境ID（可选）
     * @param variables 执行变量
     * @return 执行结果
     */
    Map<String, Object> executeTemplate(Long templateId, Long environmentId, Map<String, Object> variables);

    /**
     * 执行模板请求（使用默认环境）
     * 
     * @param templateId 模板ID
     * @param variables 执行变量
     * @return 执行结果
     */
    Map<String, Object> executeTemplate(Long templateId, Map<String, Object> variables);

    /**
     * 验证模板（检查配置是否正确）
     * 
     * @param templateId 模板ID
     * @return 验证结果
     */
    Map<String, Object> validateTemplate(Long templateId);

    /**
     * 预览请求（生成最终的请求内容，但不发送）
     * 
     * @param templateId 模板ID
     * @param environmentId 环境ID（可选）
     * @param variables 执行变量
     * @return 请求预览
     */
    Map<String, Object> previewRequest(Long templateId, Long environmentId, Map<String, Object> variables);
}
