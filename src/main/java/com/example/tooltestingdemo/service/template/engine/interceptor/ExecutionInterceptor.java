package com.example.tooltestingdemo.service.template.engine.interceptor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;

/**
 * 执行拦截器接口
 * 
 * 在执行流程的各个阶段插入自定义逻辑，如日志记录、变量处理、断言校验等
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
public interface ExecutionInterceptor {

    /**
     * 执行前拦截
     * 
     * <p>在 Executor.execute() 之前调用</p>
     * <p>可用于：变量解析、认证处理、请求修改等</p>
     *
     * @param context 执行上下文
     */
    void beforeExecute(TemplateContext context);

    /**
     * 执行后拦截
     * 
     * <p>在 Executor.execute() 之后调用</p>
     * <p>可用于：后置处理、断言校验、结果提取、日志记录等</p>
     *
     * @param context 执行上下文
     * @param result  执行结果
     */
    void afterExecute(TemplateContext context, ExecutionResult result);

    /**
     * 执行顺序（数字越小越先执行）
     * 
     * <p>建议顺序：</p>
     * <ul>
     *   <li>变量解析：100</li>
     *   <li>前置处理器：200</li>
     *   <li>认证处理：300</li>
     *   <li>后置处理器：400</li>
     *   <li>断言校验：500</li>
     *   <li>日志记录：600</li>
     * </ul>
     *
     * @return 顺序值
     */
    int getOrder();

    /**
     * 是否启用
     *
     * @param context 执行上下文
     * @return 是否启用
     */
    default boolean isEnabled(TemplateContext context) {
        return true;
    }
}
