package com.example.tooltestingdemo.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.report.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报告Mapper接口
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}