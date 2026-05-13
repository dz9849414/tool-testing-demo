package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateJobGenerationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模板任务批量生成记录 Mapper
 */
@Mapper
public interface TemplateJobGenerationLogMapper extends BaseMapper<TemplateJobGenerationLog> {
}
