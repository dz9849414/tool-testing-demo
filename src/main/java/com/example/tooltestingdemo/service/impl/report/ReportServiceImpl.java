package com.example.tooltestingdemo.service.impl.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.common.PageResult;
import com.example.tooltestingdemo.dto.report.*;
import com.example.tooltestingdemo.entity.report.Report;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.example.tooltestingdemo.mapper.report.ReportMapper;
import com.example.tooltestingdemo.mapper.template.TemplateJobLogMapper;
import com.example.tooltestingdemo.service.report.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * 报告服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    private final ReportMapper reportMapper;
    private final TemplateJobLogMapper templateJobLogMapper;

    @Override
    public Long createReport(ReportDTO reportDTO) {
        Report report = new Report();
        BeanUtils.copyProperties(reportDTO, report);
        
        // 设置默认值
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        report.setIsDeleted(0);
        report.setExportCount(0);
        report.setIsNotified(false);
        
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public Boolean updateReport(Long id, ReportDTO reportDTO) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return false;
        }
        
        BeanUtils.copyProperties(reportDTO, report);
        report.setUpdateTime(LocalDateTime.now());
        
        return reportMapper.updateById(report) > 0;
    }

    @Override
    public Boolean deleteReport(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return false;
        }
        
        // 软删除
        report.setIsDeleted(1);
        report.setUpdateTime(LocalDateTime.now());
        
        return reportMapper.deleteById(report) > 0;
    }

    @Override
    public List<ReportDTO> getReportList(String reportType, String status) {
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Report::getIsDeleted, 0);
        
        if (reportType != null) {
            queryWrapper.eq(Report::getReportType, reportType);
        }
        
        if (status != null) {
            queryWrapper.eq(Report::getStatus, status);
        }
        
        queryWrapper.orderByDesc(Report::getCreateTime);
        
        List<Report> reports = reportMapper.selectList(queryWrapper);
        
        return reports.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ReportDTO getReportDetail(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        return convertToDTO(report);
    }

    @Override
    public Long autoGenerateReport(String reportType, String dataSourceIds) {
        // 实现自动生成报告的逻辑
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setName("自动生成报告 - " + LocalDateTime.now());
        reportDTO.setDescription("基于数据源自动生成的报告");
        reportDTO.setReportType(reportType);
        reportDTO.setGenerateType("AUTO");
        reportDTO.setDataSourceIds(dataSourceIds);
        reportDTO.setStatus("DRAFT");
        
        return createReport(reportDTO);
    }

    @Override
    public String previewReport(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        // 构建预览内容
        StringBuilder preview = new StringBuilder();
        preview.append("报告预览：").append(report.getName()).append("\n");
        preview.append("描述：").append(report.getDescription()).append("\n");
        preview.append("类型：").append(report.getReportType()).append("\n");
        preview.append("状态：").append(report.getStatus()).append("\n");
        
        return preview.toString();
    }

    @Override
    public String exportReport(Long id, String format, String pageRange) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        // 更新导出统计
        report.setExportCount(report.getExportCount() + 1);
        report.setLastExportTime(LocalDateTime.now());
        reportMapper.updateById(report);
        
        // 构建导出文件路径
        return "/exports/reports/" + id + "." + format.toLowerCase();
    }
    
    @Override
    public File exportReportFile(Long id, String format, String pageRange) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        try {
            // 创建导出目录
            File exportDir = new File("exports/reports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            // 生成文件名
            String filename = "report_" + id + "_" + System.currentTimeMillis() + "." + format.toLowerCase();
            File exportFile = new File(exportDir, filename);
            
            // 根据格式生成文件
            switch (format.toLowerCase()) {
                case "pdf":
                    generatePdfReport(report, exportFile, pageRange);
                    break;
                case "docx":
                    generateWordReport(report, exportFile, pageRange);
                    break;
                case "xlsx":
                    generateExcelReport(report, exportFile, pageRange);
                    break;
                case "html":
                    generateHtmlReport(report, exportFile, pageRange);
                    break;
                case "json":
                    generateJsonReport(report, exportFile, pageRange);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的导出格式：" + format);
            }
            
            // 更新导出统计
            report.setExportCount(report.getExportCount() + 1);
            report.setLastExportTime(LocalDateTime.now());
            reportMapper.updateById(report);
            
            return exportFile;
            
        } catch (Exception e) {
            throw new RuntimeException("导出报告文件失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 生成PDF报告
     */
    private void generatePdfReport(Report report, File exportFile, String pageRange) throws Exception {
        // 使用Apache PDFBox生成真正的PDF文件
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // 设置字体
            PDFont font = PDType1Font.HELVETICA_BOLD;
            PDFont normalFont = PDType1Font.HELVETICA;
            
            // 设置起始位置
            float startY = page.getMediaBox().getHeight() - 50;
            float currentY = startY;
            float margin = 50;
            float lineHeight = 15;
            
            // 添加标题
            contentStream.beginText();
            contentStream.setFont(font, 20);
            contentStream.newLineAtOffset(margin, currentY);
            contentStream.showText(report.getName());
            contentStream.endText();
            currentY -= lineHeight * 2;
            
            // 添加生成时间
            contentStream.beginText();
            contentStream.setFont(normalFont, 12);
            contentStream.newLineAtOffset(margin, currentY);
            contentStream.showText("生成时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            contentStream.endText();
            currentY -= lineHeight * 1.5;
            
            // 添加分隔线
            contentStream.moveTo(margin, currentY);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, currentY);
            contentStream.stroke();
            currentY -= lineHeight;
            
            // 添加报告描述
            if (report.getDescription() != null && !report.getDescription().trim().isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(normalFont, 12);
                contentStream.newLineAtOffset(margin, currentY);
                contentStream.showText("报告描述：" + report.getDescription());
                contentStream.endText();
                currentY -= lineHeight * 1.5;
            }
            
            // 添加报告内容
            String content = buildReportContent(report, pageRange);
            if (content != null && !content.trim().isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(font, 14);
                contentStream.newLineAtOffset(margin, currentY);
                contentStream.showText("报告内容：");
                contentStream.endText();
                currentY -= lineHeight;
                
                // 处理内容，确保适合PDF显示
                String formattedContent = content.replace("\n", "\n\n");
                
                // 简单的文本换行处理
                String[] lines = formattedContent.split("\n");
                for (String line : lines) {
                    if (currentY < 50) {
                        // 如果页面空间不足，创建新页面
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        currentY = page.getMediaBox().getHeight() - 50;
                    }
                    
                    contentStream.beginText();
                    contentStream.setFont(normalFont, 11);
                    contentStream.newLineAtOffset(margin, currentY);
                    contentStream.showText(line);
                    contentStream.endText();
                    currentY -= lineHeight;
                }
            } else {
                contentStream.beginText();
                contentStream.setFont(normalFont, 12);
                contentStream.setNonStrokingColor(128, 128, 128); // 灰色
                contentStream.newLineAtOffset(margin, currentY);
                contentStream.showText("报告内容为空");
                contentStream.endText();
                currentY -= lineHeight;
            }
            
            // 添加页脚
            if (currentY < 100) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                currentY = page.getMediaBox().getHeight() - 50;
            }
            
            contentStream.moveTo(margin, currentY);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, currentY);
            contentStream.stroke();
            currentY -= lineHeight;
            
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin, currentY);
            contentStream.showText("报告ID：" + report.getId() + " | 生成系统：工具测试平台");
            contentStream.endText();
            
            contentStream.close();
            
            // 保存文档
            document.save(exportFile);
        }
    }
    
    /**
     * 生成Word报告
     */
    private void generateWordReport(Report report, File exportFile, String pageRange) throws Exception {
        String content = buildReportContent(report, pageRange);
        
        // 这里应该使用Word生成库，这里用简单的文本文件模拟
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write("Word Report - " + report.getName() + "\n\n");
            writer.write("生成时间：" + LocalDateTime.now() + "\n\n");
            writer.write("报告内容：\n");
            writer.write(content);
        }
    }
    
    /**
     * 生成Excel报告
     */
    private void generateExcelReport(Report report, File exportFile, String pageRange) throws Exception {
        String content = buildReportContent(report, pageRange);
        
        // 这里应该使用Excel生成库，这里用简单的文本文件模拟
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write("Excel Report - " + report.getName() + "\n\n");
            writer.write("生成时间：" + LocalDateTime.now() + "\n\n");
            writer.write("报告内容：\n");
            writer.write(content);
        }
    }
    
    /**
     * 生成HTML报告
     */
    private void generateHtmlReport(Report report, File exportFile, String pageRange) throws Exception {
        String content = buildReportContent(report, pageRange);
        
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>" + report.getName() + "</title>\n");
            writer.write("<style>\n");
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.write("h1 { color: #333; }\n");
            writer.write("p { line-height: 1.6; }\n");
            writer.write("</style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("<h1>" + report.getName() + "</h1>\n");
            writer.write("<p><strong>生成时间：</strong>" + LocalDateTime.now() + "</p>\n");
            writer.write("<div>" + content.replace("\n", "<br>") + "</div>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        }
    }
    
    /**
     * 生成JSON报告
     */
    private void generateJsonReport(Report report, File exportFile, String pageRange) throws Exception {
        String content = buildReportContent(report, pageRange);
        
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write("{\n");
            writer.write("  \"reportId\": " + report.getId() + ",\n");
            writer.write("  \"name\": \"" + report.getName() + "\",\n");
            writer.write("  \"description\": \"" + report.getDescription() + "\",\n");
            writer.write("  \"generateTime\": \"" + LocalDateTime.now() + "\",\n");
            writer.write("  \"content\": \"" + content.replace("\"", "\\\"") + "\"\n");
            writer.write("}\n");
        }
    }
    
    /**
     * 构建报告内容
     */
    private String buildReportContent(Report report, String pageRange) {
        // 根据页面范围过滤内容
        String content = report.getContent();
        if (content == null) {
            return "报告内容为空";
        }
        
        // 这里可以实现更复杂的内容过滤逻辑
        // 目前简单返回所有内容
        return content;
    }

    @Override
    public String batchExportReports(List<Long> reportIds) {
        // 实现批量导出逻辑
        StringBuilder result = new StringBuilder();
        for (Long reportId : reportIds) {
            String exportPath = exportReport(reportId, "pdf", "all");
            if (exportPath != null) {
                result.append(exportPath).append("\n");
            }
        }
        
        return result.toString();
    }

    @Override
    public Boolean setSchedule(Long id, String frequency, String nextTime) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return false;
        }
        
        report.setGenerateFrequency(frequency);
        report.setNextGenerateTime(LocalDateTime.parse(nextTime));
        report.setIsScheduled(true);
        report.setUpdateTime(LocalDateTime.now());
        
        return reportMapper.updateById(report) > 0;
    }

    @Override
    public Object getReportStatistics(String startTime, String endTime, String reportType) {
        // 实现报告统计逻辑
        // 这里简化处理，返回统计对象
        return new Object() {
            public String period = startTime + " 至 " + endTime;
            public String type = reportType;
            public int totalCount = 100;
            public int draftCount = 20;
            public int publishedCount = 70;
            public int archivedCount = 10;
        };
    }

    private ReportDTO convertToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        BeanUtils.copyProperties(report, dto);
        return dto;
    }

    // ====================== 测试结果展示相关方法实现 ======================

    @Override
    public PageResult<TestResultTableDTO> getTestResultsTable(String testType, Integer page, Integer size, String status, String timeRange) {
        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange);
        
        // 从数据库获取实际的测试结果数据
        List<Map<String, Object>> jobLogs = getJobLogsByDateRange(dateRange[0], dateRange[1]);
        
        // 转换为TestResultTableDTO
        List<TestResultTableDTO> testResults = jobLogs.stream()
            .map(this::convertToTestResultTableDTO)
            .filter(result -> testType == null || testType.equals(result.getTestType()))
            .filter(result -> status == null || status.equals(result.getStatus()))
            .collect(Collectors.toList());

        // 分页处理
        int start = (page - 1) * size;
        int end = Math.min(start + size, testResults.size());
        List<TestResultTableDTO> pageData = testResults.subList(start, end);

        return new PageResult<>(page, size, (long) testResults.size(), pageData);
    }

    @Override
    public Boolean updateTestResultField(String id, String field, String value) {
        // 模拟更新测试结果字段
        // 实际实现应该调用测试模块的Service
        System.out.println("更新测试结果 " + id + " 的字段 " + field + " 为 " + value);
        return true;
    }

    @Override
    public List<TimelineNodeDTO> getTestResultsTimeline(String timeRange, String keyword, String nodeType) {
        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange);
        
        // 从数据库获取实际的执行日志数据
        List<Map<String, Object>> jobLogs = getJobLogsByDateRange(dateRange[0], dateRange[1]);
        
        // 转换为TimelineNodeDTO
        List<TimelineNodeDTO> timeline = jobLogs.stream()
            .map(this::convertToTimelineNodeDTO)
            .filter(node -> nodeType == null || nodeType.equals(node.getNodeType()))
            .filter(node -> keyword == null || node.getContent().contains(keyword))
            .collect(Collectors.toList());

        return timeline;
    }

    // ====================== 自动报告生成相关方法实现 ======================

    @Override
    public Long setupAutoReport(AutoReportConfigDTO config) {
        // 创建自动报告配置
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setName(config.getReportName());
        reportDTO.setReportType(config.getReportType());
        reportDTO.setTemplateId(config.getTemplateId());
        reportDTO.setGenerateType("AUTO");
        reportDTO.setGenerateFrequency(config.getFrequency());
        reportDTO.setIsScheduled(true);
        reportDTO.setStatus("SCHEDULED");

        Long reportId = createReport(reportDTO);
        
        // 记录自动报告配置
        System.out.println("设置自动报告配置：" + config.getReportName() + ", 频率：" + config.getFrequency());
        
        return reportId;
    }

    @Override
    public ReportPreviewDTO previewReportPdf(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }

        ReportPreviewDTO preview = new ReportPreviewDTO();
        preview.setReportId(reportId);
        preview.setReportName(report.getName());
        preview.setPreviewContent("<div>PDF预览内容 - " + report.getName() + "</div>");
        preview.setPdfPreviewUrl("/preview/reports/" + reportId + ".pdf");
        preview.setTotalPages(10);
        preview.setCurrentPage(1);
        preview.setScale(1.0);

        return preview;
    }

    // ====================== 辅助方法 ======================

    private TestResultTableDTO createTestResult(String id, String testType, String name, String status, 
                                               String executor, Long duration, Double successRate) {
        TestResultTableDTO result = new TestResultTableDTO();
        result.setId(id);
        result.setTestType(testType);
        result.setName(name);
        result.setStatus(status);
        result.setExecutor(executor);
        result.setDuration(duration);
        result.setSuccessRate(successRate);
        return result;
    }

    private TimelineNodeDTO createTimelineNode(String id, String time, String title, String content, 
                                              String operator, String nodeType, String status) {
        TimelineNodeDTO node = new TimelineNodeDTO();
        node.setId(id);
        node.setTime(java.time.LocalDateTime.parse(time, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        node.setTitle(title);
        node.setContent(content);
        node.setOperator(operator);
        node.setNodeType(nodeType);
        node.setStatus(status);
        node.setExpandable(true);
        return node;
    }

    // ====================== 基于实际数据的辅助方法 ======================

    /**
     * 计算时间范围
     */
    private LocalDateTime[] calculateDateRange(String timeRange) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;
        
        switch (timeRange) {
            case "TODAY":
                startTime = LocalDate.now().atStartOfDay();
                break;
            case "7DAYS":
                startTime = LocalDate.now().minusDays(7).atStartOfDay();
                break;
            case "30DAYS":
                startTime = LocalDate.now().minusDays(30).atStartOfDay();
                break;
            default:
                startTime = LocalDate.now().minusDays(7).atStartOfDay();
        }
        
        return new LocalDateTime[]{startTime, endTime};
    }

    /**
     * 根据时间范围获取任务日志
     */
    private List<Map<String, Object>> getJobLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        // 使用MyBatis Plus查询
        LambdaQueryWrapper<TemplateJobLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(TemplateJobLog::getCreateTime, startTime, endTime);
        queryWrapper.orderByDesc(TemplateJobLog::getCreateTime);
        
        List<TemplateJobLog> jobLogs = templateJobLogMapper.selectList(queryWrapper);
        
        // 转换为Map格式便于处理
        List<Map<String, Object>> result = new ArrayList<>();
        for (TemplateJobLog log : jobLogs) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("id", log.getId());
            logMap.put("jobId", log.getJobId());
            logMap.put("templateId", log.getTemplateId());
            logMap.put("success", log.getSuccess());
            logMap.put("durationMs", log.getDurationMs());
            logMap.put("errorMsg", log.getErrorMsg());
            logMap.put("createTime", log.getCreateTime());
            result.add(logMap);
        }
        
        return result;
    }

    /**
     * 将任务日志转换为测试结果表格DTO
     */
    private TestResultTableDTO convertToTestResultTableDTO(Map<String, Object> jobLog) {
        TestResultTableDTO result = new TestResultTableDTO();
        
        Long logId = (Long) jobLog.get("id");
        Long templateId = (Long) jobLog.get("templateId");
        Integer success = (Integer) jobLog.get("success");
        Long durationMs = (Long) jobLog.get("durationMs");
        LocalDateTime createTime = (LocalDateTime) jobLog.get("createTime");
        
        result.setId("log_" + logId);
        result.setTestType("TEMPLATE_EXECUTE");
        result.setName("模板执行 - " + (templateId != null ? "模板" + templateId : "未知模板"));
        result.setStatus(success != null && success == 1 ? "SUCCESS" : "FAILED");
        result.setExecutor("系统用户"); // 实际项目中需要关联用户表
        result.setDuration(durationMs != null ? durationMs : 0L);
        result.setSuccessRate(success != null && success == 1 ? 100.0 : 0.0);
        result.setExecuteTime(createTime);
        
        return result;
    }

    /**
     * 将任务日志转换为时间线节点DTO
     */
    private TimelineNodeDTO convertToTimelineNodeDTO(Map<String, Object> jobLog) {
        TimelineNodeDTO node = new TimelineNodeDTO();
        
        Long logId = (Long) jobLog.get("id");
        Long templateId = (Long) jobLog.get("templateId");
        Integer success = (Integer) jobLog.get("success");
        String errorMsg = (String) jobLog.get("errorMsg");
        LocalDateTime createTime = (LocalDateTime) jobLog.get("createTime");
        
        node.setId("node_" + logId);
        node.setTime(createTime);
        node.setTitle("模板任务执行");
        
        String content = "执行模板：" + (templateId != null ? "模板" + templateId : "未知模板");
        if (success != null && success == 1) {
            content += "（成功）";
        } else {
            content += "（失败）";
            if (errorMsg != null && !errorMsg.isEmpty()) {
                content += " - " + errorMsg;
            }
        }
        
        node.setContent(content);
        node.setOperator("系统用户"); // 实际项目中需要关联用户表
        node.setNodeType("TASK_EXECUTE");
        node.setStatus(success != null && success == 1 ? "SUCCESS" : "FAILED");
        node.setExpandable(true);
        
        return node;
    }
}