package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;

/**
 * 模板执行统一日志 Service
 */
public interface TemplateExecuteLogService extends IService<TemplateExecuteLog> {

    /**
     * 分页查询执行日志（管理页面）
     *
     * @param page          分页参数
     * @param templateId    模板ID
     * @param jobId         任务ID
     * @param executeType   执行类型：MANUAL / JOB
     * @param success       是否成功：0-否 1-是
     * @param keyword       模板名称/任务名称/执行人姓名模糊搜索
     * @return 分页结果
     */
    IPage<TemplateExecuteLog> pageLogs(Page<TemplateExecuteLog> page,
                                       Long templateId,
                                       Long jobId,
                                       String executeType,
                                       Integer success,
                                       String keyword);
}
