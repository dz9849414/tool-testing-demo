package com.example.tooltestingdemo.service.template;

import com.example.tooltestingdemo.entity.template.TemplatePostProcessor;
import com.example.tooltestingdemo.entity.template.TemplatePreProcessor;

import java.util.List;
import java.util.Map;

/**
 * 前置/后置处理器执行 Service 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/template/ProcessorExecuteService.java
 */
public interface ProcessorExecuteService {

    /**
     * 执行前置处理器
     * 
     * @param processors 前置处理器列表
     * @param context 执行上下文（包含变量等）
     * @return 执行后的上下文
     */
    Map<String, Object> executePreProcessors(List<TemplatePreProcessor> processors, Map<String, Object> context);

    /**
     * 执行后置处理器
     * 
     * @param processors 后置处理器列表
     * @param context 执行上下文
     * @param response 响应数据（包含statusCode, headers, body等）
     * @return 执行后的上下文
     */
    Map<String, Object> executePostProcessors(List<TemplatePostProcessor> processors, 
                                               Map<String, Object> context,
                                               Map<String, Object> response);

    /**
     * 执行单个前置处理器
     * 
     * @param processor 前置处理器
     * @param context 执行上下文
     * @return 执行后的上下文
     */
    Map<String, Object> executePreProcessor(TemplatePreProcessor processor, Map<String, Object> context);

    /**
     * 执行单个后置处理器
     * 
     * @param processor 后置处理器
     * @param context 执行上下文
     * @param response 响应数据
     * @return 执行后的上下文
     */
    Map<String, Object> executePostProcessor(TemplatePostProcessor processor, 
                                              Map<String, Object> context,
                                              Map<String, Object> response);
}
