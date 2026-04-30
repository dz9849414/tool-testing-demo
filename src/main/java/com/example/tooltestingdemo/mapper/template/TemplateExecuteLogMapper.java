package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模板执行统一日志 Mapper
 */
@Mapper
public interface TemplateExecuteLogMapper extends BaseMapper<TemplateExecuteLog> {

    /**
     * 根据模板ID删除执行日志
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateExecuteLog>()
                .eq(TemplateExecuteLog::getTemplateId, templateId)
        );
    }
}
