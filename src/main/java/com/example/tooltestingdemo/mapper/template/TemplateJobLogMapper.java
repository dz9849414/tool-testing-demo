package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板定时任务日志 Mapper
 */
@Mapper
public interface TemplateJobLogMapper extends BaseMapper<TemplateJobLog> {

    /**
     * 根据任务ID查询最近N条日志
     */
    List<TemplateJobLog> selectRecentByJobId(@Param("jobId") Long jobId, @Param("limit") Integer limit);

    /**
     * 批量查询
     * @param jobIds
     * @return
     */
    List<TemplateJobLog> selectLastLogsByJobIds(@Param("jobIds") List<Long> jobIds);
}
