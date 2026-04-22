package com.example.tooltestingdemo.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.report.ReportTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 报告模板Mapper接口
 */
@Mapper
public interface ReportTemplateMapper extends BaseMapper<ReportTemplate> {
    
    /**
     * 检查模板名称是否存在
     */
    @Select("SELECT COUNT(*) FROM pdm_tool_report_template WHERE name = #{name} AND is_deleted = 0")
    boolean existsByName(@Param("name") String name);
}