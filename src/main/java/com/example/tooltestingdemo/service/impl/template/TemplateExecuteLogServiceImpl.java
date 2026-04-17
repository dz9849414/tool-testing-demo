package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;
import com.example.tooltestingdemo.mapper.template.TemplateExecuteLogMapper;
import com.example.tooltestingdemo.service.template.TemplateExecuteLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 模板执行统一日志 Service 实现
 */
@Service
public class TemplateExecuteLogServiceImpl extends ServiceImpl<TemplateExecuteLogMapper, TemplateExecuteLog>
        implements TemplateExecuteLogService {

    @Override
    public IPage<TemplateExecuteLog> pageLogs(Page<TemplateExecuteLog> page,
                                              Long templateId,
                                              Long jobId,
                                              String executeType,
                                              Integer success,
                                              String keyword) {
        LambdaQueryWrapper<TemplateExecuteLog> wrapper = new LambdaQueryWrapper<>();

        if (templateId != null) {
            wrapper.eq(TemplateExecuteLog::getTemplateId, templateId);
        }
        if (jobId != null) {
            wrapper.eq(TemplateExecuteLog::getJobId, jobId);
        }
        if (StringUtils.hasText(executeType)) {
            wrapper.eq(TemplateExecuteLog::getExecuteType, executeType);
        }
        if (success != null) {
            wrapper.eq(TemplateExecuteLog::getSuccess, success);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TemplateExecuteLog::getTemplateName, keyword)
                    .or()
                    .like(TemplateExecuteLog::getJobName, keyword)
                    .or()
                    .like(TemplateExecuteLog::getCreateName, keyword));
        }

        wrapper.orderByDesc(TemplateExecuteLog::getCreateTime);
        return page(page, wrapper);
    }
}
