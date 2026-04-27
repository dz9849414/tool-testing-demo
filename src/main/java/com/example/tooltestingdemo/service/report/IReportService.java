package com.example.tooltestingdemo.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.common.PageResult;
import com.example.tooltestingdemo.dto.report.*;
import com.example.tooltestingdemo.entity.report.Report;

import java.io.File;
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

     void generatePdfReport(Report report, File exportFile, String pageRange);

    /**
     * 自动生成统计报告
     * 
     * @return 报告ID
     */
    Long autoGenerateReport(String reportType, String dataSourceIds);



    /**
     * 获取失败时间线
     * 
     * @param templateId 模板ID
     * @param timeRange 时间范围：TODAY/WEEK/MONTH
     * @return 失败时间线数据
     */
    java.util.List<FailureTimelineDTO> getFailureTimeline(Long templateId, String timeRange);
    
    /**
     * 预览报告 - HTML格式
     */
    String previewReportHtml(Long id, String pageRange, String dataSource);
    
    /**
     * 预览报告 - 图片格式
     */
    byte[] previewReportImage(Long id, String format, String pageRange, String dataSource);
    
    /**
     * 预览报告 - JSON格式
     */
    Object previewReportJson(Long id, String pageRange, String dataSource);
    
    /**
     * 导出报告（返回文件路径）
     */
    String exportReport(Long id, String format, String pageRange);
    
    /**
     * 导出报告文件（返回File对象）
     */
    File exportReportFile(Long id, String format, String pageRange);
    
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

    // ====================== 测试结果展示相关方法 ======================

    /**
     * 获取测试结果表格数据
     */
    PageResult<TestResultTableDTO> getTestResultsTable(String testType, Integer page, Integer size, String status, String timeRange);

    /**
     * 行内编辑测试结果字段
     */
    Boolean updateTestResultField(String id, String field, String value);

    /**
     * 获取测试结果时间线
     */
    List<TimelineNodeDTO> getTestResultsTimeline(String timeRange, String keyword, String nodeType);

    // ====================== 自动报告生成相关方法 ======================

    /**
     * 设置自动生成报告配置
     */
    Long setupAutoReport(AutoReportConfigDTO config);

    /**
     * 预览报告PDF
     */
    ReportPreviewDTO previewReportPdf(Long reportId);
}