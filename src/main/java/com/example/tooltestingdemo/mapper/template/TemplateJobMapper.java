package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模板定时任务 Mapper
 */
@Mapper
public interface TemplateJobMapper extends BaseMapper<TemplateJob> {
}
