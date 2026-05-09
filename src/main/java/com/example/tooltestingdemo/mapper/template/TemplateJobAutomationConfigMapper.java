package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateJobAutomationConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 模板任务自动化配置 Mapper
 */
@Mapper
public interface TemplateJobAutomationConfigMapper extends BaseMapper<TemplateJobAutomationConfig> {

    void ensureTable();

    TemplateJobAutomationConfig selectByJobId(@Param("jobId") Long jobId);

    int upsert(TemplateJobAutomationConfig config);
}
