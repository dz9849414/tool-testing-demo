package com.example.tooltestingdemo.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.report.ReportDTO;
import com.example.tooltestingdemo.entity.report.Report;

import java.util.List;

/**
 * 报告服务接口
 */
public interface IReportService extends IService<Report> {
    
    /**
     * 创建报告
     */
    Long createReport(ReportDTO reportDTO);
    
    /**
     * 更新报告
     */
    Boolean updateReport(Long id, ReportDTO reportDTO);
    
    /**
     * 删除报告
     */
    Boolean deleteReport(Long id);
    
    /**
     * 获取报告列表
     */
    List<ReportDTO> getReportList(String reportType, String status);
    
    /**
     * 获取报告详情
     */
    ReportDTO getReportDetail(Long id);
    
    /**
     * 自动生成报告
     */
    Long autoGenerateReport(String reportType, String dataSourceIds);
    
    /**
     * 预览报告
     */
    String previewReport(Long id);
    
    /**
     * 导出报告
     */
    String exportReport(Long id, String format, String pageRange);
    
    /**
     * 批量导出报告
     */
    String batchExportReports(List<Long> reportIds);
    
    /**
     * 设置定时生成
     */
    Boolean setSchedule(Long id, String frequency, String nextTime);
    
    /**
     * 获取报告统计
     */
    Object getReportStatistics(String startTime, String endTime, String reportType);
}