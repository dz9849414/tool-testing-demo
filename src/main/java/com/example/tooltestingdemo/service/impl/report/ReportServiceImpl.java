package com.example.tooltestingdemo.service.impl.report;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.common.PageResult;
import com.example.tooltestingdemo.dto.report.*;
import com.example.tooltestingdemo.entity.report.Report;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.example.tooltestingdemo.mapper.report.ReportMapper;
import com.example.tooltestingdemo.mapper.template.TemplateJobLogMapper;
import com.example.tooltestingdemo.service.report.IReportService;
import com.example.tooltestingdemo.service.report.ITemplateStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
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
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
// Standard14Fonts is not public, use PDType1Font directly

/**
 * 报告服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    private final ReportMapper reportMapper;
    private final TemplateJobLogMapper templateJobLogMapper;
    private final ITemplateStatisticsService templateStatisticsService;

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
    public java.util.List<FailureTimelineDTO> getFailureTimeline(Long templateId, String timeRange) {
        try {
            // 根据时间范围计算开始时间
            LocalDateTime startTime = calculateStartTime(timeRange);
            LocalDateTime endTime = LocalDateTime.now();
            
            // 查询失败记录
            List<FailureTimelineDTO> timeline = templateStatisticsService.getFailureTimelineData(templateId, startTime, endTime);
            
            return timeline != null ? timeline : new java.util.ArrayList<>();
        } catch (Exception e) {
            log.error("获取失败时间线失败 - templateId: {}, timeRange: {}", templateId, timeRange, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public Long autoGenerateReport(String reportType, String dataSourceIds) {
        try {
            // 根据reportType从pdm_tool_template_execute_log表获取最近的测试数据
            Object statisticsData = getRecentTestDataFromExecuteLog(reportType, dataSourceIds);
            
            // 构建报告DTO
            ReportDTO reportDTO = new ReportDTO();
            reportDTO.setName("自动生成报告 - " + reportType + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            reportDTO.setDescription("基于最近测试数据自动生成的" + reportType + "报告");
            reportDTO.setReportType(reportType);
            reportDTO.setGenerateType("AUTO");
            reportDTO.setDataSourceIds(dataSourceIds);
            reportDTO.setStatus("DRAFT");
            
            // 将统计数据转换为JSON字符串
            if (statisticsData != null) {
                if (statisticsData instanceof StatisticsReportDTO) {
                    StatisticsReportDTO statsReport = (StatisticsReportDTO) statisticsData;
                    reportDTO.setContent(statsReport.getContent() != null ? 
                        statsReport.getContent().toString() : "{}");
                } else if (statisticsData instanceof List) {
                    // 简化格式的数据
                    reportDTO.setContent(new JSONArray((List) statisticsData).toString());
                } else {
                    reportDTO.setContent("{}");
                }
            } else {
                reportDTO.setContent("{}");
            }
            
            return createReport(reportDTO);
            
        } catch (Exception e) {
            log.error("自动生成报告失败", e);
            throw new RuntimeException("自动生成报告失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从pdm_tool_template_execute_log表获取最近的测试数据
     * 
     * @param reportType 报告类型（支持数字和字符串）
     * @param dataSourceIds 数据源ID
     * @return 统计数据
     */
    private Object getRecentTestDataFromExecuteLog(String reportType, String dataSourceIds) {
        try {
            // 获取最近7天的数据
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(7);
            
            // 根据reportType调用不同的统计方法（支持数字和字符串）
            String normalizedReportType = normalizeReportType(reportType);
            
            switch (normalizedReportType) {
                case "PROTOCOL_DISTRIBUTION":
                case "1":
                    return templateStatisticsService.getProtocolDistributionReport(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "CATEGORY");
                case "RESPONSE_TIME":
                case "2":
                    return templateStatisticsService.getHourlyResponseTimeReportSimple(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "UNIFIED");
                case "FAILURE_REASONS":
                case "3":
                    return templateStatisticsService.getTopFailureReasonsReportSimple(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "UNIFIED");
                case "WEEKLY_EXECUTION":
                case "4":
                    return templateStatisticsService.getWeeklyExecutionReport(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "UNIFIED");
                case "SUCCESS_RATE":
                case "5":
                    return templateStatisticsService.getSuccessRateReport(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "UNIFIED");
                default:
                    // 默认返回空数据
                    return null;
            }
        } catch (Exception e) {
            log.error("获取最近测试数据失败", e);
            return null;
        }
    }
    
    /**
     * 标准化报告类型参数
     * 
     * @param reportType 原始报告类型参数
     * @return 标准化的报告类型
     */
    private String normalizeReportType(String reportType) {
        if (reportType == null) {
            return "UNKNOWN";
        }
        
        // 数字映射
        switch (reportType) {
            case "1": return "PROTOCOL_DISTRIBUTION";
            case "2": return "RESPONSE_TIME";
            case "3": return "FAILURE_REASONS";
            case "4": return "WEEKLY_EXECUTION";
            case "5": return "SUCCESS_RATE";
            default: return reportType.toUpperCase();
        }
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

            try {
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
            }catch (Exception e) {
                throw new RuntimeException("导出报告文件失败：" + e.getMessage(), e);
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

    /**
     * 生成 PDF 报告（支持中文，不乱码）
     */
    public void generatePdfReport(Report report, File exportFile, String pageRange) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // ========= 关键：加载支持中文的字体 =========
                PDType0Font font = PDType0Font.load(document, getClass().getResourceAsStream("/fonts/simhei.ttf"));

                // ========= 报告标题 =========
                contentStream.beginText();
                contentStream.setFont(font, 16); // 标题字体大小
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("测试报告详情");
                contentStream.endText();

                // ========= 报告基本信息 =========
                contentStream.beginText();
                contentStream.setFont(font, 12); // 正文字体大小
                contentStream.newLineAtOffset(50, 720);
                
                // 报告名称（加粗效果）
                contentStream.setFont(font, 14);
                contentStream.showText("报告名称：");
                contentStream.setFont(font, 12);
                contentStream.showText(report.getName() != null ? report.getName() : "未命名");
                contentStream.newLineAtOffset(0, -25);
                
                // 生成时间
                contentStream.showText("生成时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                contentStream.newLineAtOffset(0, -25);
                
                // 数据范围
                contentStream.showText("数据范围：" + pageRange);
                contentStream.newLineAtOffset(0, -25);
                
                // 报告描述
                if (report.getDescription() != null && !report.getDescription().trim().isEmpty()) {
                    contentStream.showText("报告描述：" + report.getDescription());
                    contentStream.newLineAtOffset(0, -25);
                }
                
                // 报告类型（中文翻译）
                contentStream.setFont(font, 14);
                contentStream.showText("报告类型：");
                contentStream.setFont(font, 12);
                contentStream.showText(report.getReportType() != null ? getReadableReportType(report.getReportType()) : "未知类型");
                contentStream.newLineAtOffset(0, -25);
                
                // 报告状态（中文翻译）
                contentStream.setFont(font, 14);
                contentStream.showText("报告状态：");
                contentStream.setFont(font, 12);
                contentStream.showText(report.getStatus() != null ? getReadableReportStatus(report.getStatus()) : "未知状态");
                contentStream.newLineAtOffset(0, -25);
                
                // 报告ID
                contentStream.showText("报告ID：" + report.getId());
                contentStream.newLineAtOffset(0, -25);
                
                // 分隔线
                contentStream.endText();
                contentStream.moveTo(50, 600);
                contentStream.lineTo(562, 600);
                contentStream.stroke();
                
                // 报告内容
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, 580);
                contentStream.showText("报告内容：");
                contentStream.newLineAtOffset(0, -20);
                
                String reportContent = buildReportContent(report, pageRange);
                if (reportContent != null && !reportContent.trim().isEmpty()) {
                    String[] allLines = reportContent.split("\\r?\\n");
                    int y = 560;
                    for (String line : allLines) {
                        if (y < 50) break;
                        if (line.trim().isEmpty()) continue;
                        contentStream.newLineAtOffset(0, -15);
                        contentStream.showText(line.trim());
                        y -= 15;
                    }
                } else {
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("报告内容为空");
                }

                contentStream.endText();
            }catch (Exception e) {
                throw new RuntimeException("导出报告文件失败：" + e.getMessage(), e);
            }

            // 输出文件
            document.save(new FileOutputStream(exportFile));
        }catch (Exception e) {
            throw new RuntimeException("导出报告文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建简单的PDF页面内容（使用ASCII字符）
     */
    private String buildSimplePdfPageContent(Report report, String pageRange) {
        StringBuilder content = new StringBuilder();
        
        // 开始文本对象
        content.append("BT\n");
        content.append("/F1 24 Tf\n");  // 设置字体和大小
        content.append("50 750 Td\n");  // 设置文本位置
        content.append("(").append(escapeAscii(report.getName())).append(") Tj\n");  // 显示文本
        content.append("ET\n");  // 结束文本对象
        
        // 生成时间
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append("50 720 Td\n");
        content.append("(").append(escapeAscii("Generate Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))).append(") Tj\n");
        content.append("ET\n");
        
        // 报告描述（英文显示）
        if (report.getDescription() != null && !report.getDescription().trim().isEmpty()) {
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append("50 700 Td\n");
            content.append("(").append(escapeAscii("Description: " + report.getDescription())).append(") Tj\n");
            content.append("ET\n");
        }
        
        // 报告类型
        if (report.getReportType() != null) {
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append("50 680 Td\n");
            content.append("(").append(escapeAscii("Type: " + report.getReportType())).append(") Tj\n");
            content.append("ET\n");
        }
        
        // 报告状态
        if (report.getStatus() != null) {
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append("50 660 Td\n");
            content.append("(").append(escapeAscii("Status: " + report.getStatus())).append(") Tj\n");
            content.append("ET\n");
        }
        
        // 分隔线
        content.append("50 640 m\n");
        content.append("562 640 l\n");
        content.append("S\n");
        
        // 报告内容标题
        content.append("BT\n");
        content.append("/F1 14 Tf\n");
        content.append("50 620 Td\n");
        content.append("(").append(escapeAscii("Report Content:")).append(") Tj\n");
        content.append("ET\n");
        
        // 报告内容
        String reportContent = buildReportContent(report, pageRange);
        if (reportContent != null && !reportContent.trim().isEmpty()) {
            String[] lines = wrapText(reportContent, 80);
            int y = 600;
            for (String line : lines) {
                if (y < 50) break;  // 页面底部限制
                content.append("BT\n");
                content.append("/F1 10 Tf\n");
                content.append("50 ").append(y).append(" Td\n");
                content.append("(").append(escapeAscii(line)).append(") Tj\n");
                content.append("ET\n");
                y -= 15;
            }
        } else {
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append("50 600 Td\n");
            content.append("(").append(escapeAscii("No content available")).append(") Tj\n");
            content.append("ET\n");
        }
        
        // 页脚
        content.append("BT\n");
        content.append("/F1 10 Tf\n");
        content.append("50 30 Td\n");
        content.append("(").append(escapeAscii("Report ID: " + report.getId() + " | Generated by Tool Testing Platform")).append(") Tj\n");
        content.append("ET\n");
        
        return content.toString();
    }
    
    /**
     * 转义ASCII字符串中的特殊字符
     */
    private String escapeAscii(String text) {
        if (text == null) return "";
        // 只保留ASCII字符，过滤掉非ASCII字符
        return text.replaceAll("[^\\x00-\\x7F]", "?")
                  .replace("\\", "\\\\")
                  .replace("(", "\\(")
                  .replace(")", "\\)");
    }
    
    /**
     * 将字符串转换为UTF-16BE十六进制编码
     */
    private String stringToHex(String text) {
        if (text == null) return "";
        try {
            byte[] bytes = text.getBytes("UTF-16BE");
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (Exception e) {
            // 如果转换失败，返回空字符串
            return "";
        }
    }
    
    /**
     * 转义PDF字符串中的特殊字符
     */
    private String escapePdfString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("(", "\\(")
                  .replace(")", "\\)");
    }
    
    /**
     * 构建HTML报告（备用方案）
     */
    private String buildHtmlReport(Report report, String pageRange) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>").append(report.getName()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append(".separator { border-top: 1px solid #ccc; margin: 20px 0; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<h1>").append(report.getName()).append("</h1>\n");
        html.append("<p><strong>生成时间：</strong>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");
        
        if (report.getDescription() != null) {
            html.append("<p><strong>报告描述：</strong>").append(report.getDescription()).append("</p>\n");
        }
        
        if (report.getReportType() != null) {
            html.append("<p><strong>报告类型：</strong>").append(report.getReportType()).append("</p>\n");
        }
        
        if (report.getStatus() != null) {
            html.append("<p><strong>报告状态：</strong>").append(report.getStatus()).append("</p>\n");
        }
        
        html.append("<div class=\"separator\"></div>\n");
        html.append("<h2>报告内容</h2>\n");
        
        String content = buildReportContent(report, pageRange);
        if (content != null && !content.trim().isEmpty()) {
            html.append("<pre>").append(content).append("</pre>\n");
        } else {
            html.append("<p>报告内容为空</p>\n");
        }
        
        html.append("<div class=\"separator\"></div>\n");
        html.append("<p><small>报告ID：").append(report.getId()).append(" | 生成系统：工具测试平台</small></p>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
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

    private String buildReportContent(Report report, String pageRange) {
        StringBuilder readableContent = new StringBuilder();

        // 1. 报告基本信息（全部中文，一行一句话）
        readableContent.append("=== 报告基本信息 ===\n");
        readableContent.append("报告名称：").append(report.getName() != null ? report.getName() : "未命名").append("\n");
        readableContent.append("报告描述：").append(report.getDescription() != null ? report.getDescription() : "无描述").append("\n");
        readableContent.append("报告类型：").append(getReadableReportType(report.getReportType())).append("\n");
        readableContent.append("报告状态：").append(getReadableReportStatus(report.getStatus())).append("\n");
        readableContent.append("生成方式：").append(getReadableGenerateType(report.getGenerateType())).append("\n");
        readableContent.append("创建时间：").append(report.getCreateTime() != null ?
                report.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "未知").append("\n");
        readableContent.append("导出次数：").append(report.getExportCount() != null ? report.getExportCount() : 0).append("\n");
        readableContent.append("\n");

        // 2. 报告内容（JSON解析为易读格式，一行一句话）
        String content = report.getContent();
        if (content != null && !content.trim().isEmpty()) {
            readableContent.append("=== 报告统计内容 ===\n");
            try {
                // 尝试解析JSON内容
                Object jsonContent = parseJsonContent(content);
                String formattedContent = formatJsonToReadable(jsonContent);
                // 将内容按句子分行，一行一句话
                readableContent.append(formatContentToSentences(formattedContent));
            } catch (Exception e) {
                // 如果JSON解析失败，显示原始内容
                readableContent.append("原始JSON内容：\n");
                readableContent.append(content);
            }
        } else {
            readableContent.append("=== 报告统计内容 ===\n");
            readableContent.append("报告内容为空\n");
        }

        return readableContent.toString();
    }

    // ===================== 核心：JSON 字段名 自动 转 中文 =====================
    private void appendFlatJson(com.alibaba.fastjson2.JSONObject json, StringBuilder sb, String prefix) {
        for (String key : json.keySet()) {
            Object val = json.get(key);
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (val instanceof com.alibaba.fastjson2.JSONObject) {
                appendFlatJson((com.alibaba.fastjson2.JSONObject) val, sb, fullKey);
            } else if (val instanceof com.alibaba.fastjson2.JSONArray) {
                com.alibaba.fastjson2.JSONArray array = (com.alibaba.fastjson2.JSONArray) val;
                for (int i = 0; i < array.size(); i++) {
                    Object item = array.get(i);
                    if (item instanceof com.alibaba.fastjson2.JSONObject) {
                        appendFlatJson((com.alibaba.fastjson2.JSONObject) item, sb, fullKey + "[" + i + "]");
                    }
                }
            } else {
                // 👇👇👇 这里自动把英文key转换成中文 👇👇👇
                String chineseKey = convertToChineseKey(key);
                sb.append(chineseKey).append("：").append(val).append("\n");
            }
        }
    }

    // ===================== 字段名映射：英文 → 中文（你要的全部在这里） =====================
    private String convertToChineseKey(String key) {
        switch (key) {
            case "minDuration": return "最小耗时";
            case "totalExecutions": return "总执行次数";
            case "endDate": return "结束日期";
            case "startDate": return "开始日期";
            case "generateTime": return "生成时间";
            case "overallAvgDuration": return "平均耗时";
            case "maxDuration": return "最大耗时";
            case "dataSource": return "数据源";
            case "dataSourceName": return "数据源名称";
            case "timeSlot": return "统计时段";
            case "hourGroup": return "小时分组";
            case "executionCount": return "执行次数";
            case "avgDuration": return "平均耗时";
            default: return key; // 没有匹配的返回原key
        }
    }
    
    /**
     * 获取可读的报告类型
     */
    private String getReadableReportType(String reportType) {
        if (reportType == null) return "未知类型";
        
        switch (reportType.toUpperCase()) {
            case "PROTOCOL_DISTRIBUTION": return "协议类型分布统计";
            case "RESPONSE_TIME": return "响应时间分析";
            case "FAILURE_REASONS": return "失败原因分析";
            case "WEEKLY_EXECUTION": return "周执行量统计";
            case "SUCCESS_RATE": return "成功率分析";
            case "AUTO_GENERATED": return "自动生成报告";
            default: return reportType;
        }
    }
    

    /**
     * 根据时间范围计算开始时间
     */
    private LocalDateTime calculateStartTime(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        switch (timeRange.toUpperCase()) {
            case "TODAY":
                return now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "WEEK":
                return now.minusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "MONTH":
                return now.minusMonths(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default:
                return now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
    }

    /**
     * 获取可读的报告状态
     */
    private String getReadableReportStatus(String status) {
        if (status == null) return "未知状态";
        switch (status.toUpperCase()) {
            case "DRAFT": return "草稿";
            case "PUBLISHED": return "已发布";
            case "ARCHIVED": return "已归档";
            case "SCHEDULED": return "定时任务";
            default: return status;
        }
    }
    
    /**
     * 获取可读的生成方式
     */
    private String getReadableGenerateType(String generateType) {
        if (generateType == null) return "未知方式";
        
        switch (generateType.toUpperCase()) {
            case "AUTO": return "自动生成";
            case "MANUAL": return "手动创建";
            default: return generateType;
        }
    }
    


    /**
     * 解析JSON内容（FastJSON版）
     */
    private Object parseJsonContent(String content) {
        try {
            if (content.trim().startsWith("{")) {
                return JSON.parseObject(content);
            } else if (content.trim().startsWith("[")) {
                return JSON.parseArray(content);
            } else {
                throw new RuntimeException("不是标准JSON格式");
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    /**
     * 将JSON格式化为易读的文本（FastJSON版）
     */
    private String formatJsonToReadable(Object jsonContent) {
        StringBuilder result = new StringBuilder();

        if (jsonContent instanceof JSONObject) {
            // 检查是否为周执行量统计数据
            if (isWeeklyExecutionData(jsonContent)) {
                // 周执行量数据特殊处理
                String weeklyData = formatWeeklyExecutionData((JSONObject) jsonContent);
                if (!weeklyData.trim().isEmpty()) {
                    result.append(weeklyData);
                } else {
                    // 如果没有周执行量数据，使用默认格式化
                    formatJsonObject((JSONObject) jsonContent, result, 0);
                }
            } else {
                // 其他数据使用默认格式化
                formatJsonObject((JSONObject) jsonContent, result, 0);
            }
        } else if (jsonContent instanceof JSONArray) {
            formatJsonArray((JSONArray) jsonContent, result, 0);
        }

        return result.toString();
    }

    /**
     * 检查是否为周执行量统计数据
     */
    private boolean isWeeklyExecutionData(Object jsonContent) {
        if (jsonContent instanceof JSONObject) {
            JSONObject obj = (JSONObject) jsonContent;
            // 检查是否包含dayNameCn字段
            for (String key : obj.keySet()) {
                if (key.contains("dayNameCn")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 格式化周执行量数据（中文周几、执行次数，然后换行）
     */
    private String formatWeeklyExecutionData(Object jsonContent) {
        if (!(jsonContent instanceof JSONObject)) {
            return "";
        }
        
        JSONObject obj = (JSONObject) jsonContent;
        StringBuilder result = new StringBuilder();
        
        // 检查数据结构，可能是包含dayNameCn和executionCount的数组
        if (obj.containsKey("dayNameCn") && obj.containsKey("执行次数")) {
            // 直接使用dayNameCn和执行次数
            String dayNameCn = obj.getString("dayNameCn");
            Integer executionCount = obj.getInteger("执行次数");
            
            if (dayNameCn != null) {
                result.append(dayNameCn).append(" 执行次数 ").append(executionCount != null ? executionCount : 0).append("\n");
            }
        } else {
            // 遍历所有字段，查找dayNameCn和对应的执行次数
            for (String key : obj.keySet()) {
                if (key.contains("dayNameCn")) {
                    String dayNameCn = obj.getString(key);
                    if (dayNameCn != null) {
                        // 查找对应的执行次数字段
                        String countKey = key.replace("dayNameCn", "执行次数");
                        Integer executionCount = obj.getInteger(countKey);
                        
                        result.append(dayNameCn).append(" 执行次数 ").append(executionCount != null ? executionCount : 0).append("\n");
                    }
                }
            }
        }
        
        return result.toString();
    }

    /**
     * 将英文周几转换为中文
     */
    private String getChineseWeekDay(String weekDay) {
        if (weekDay == null) return "未知";
        
        switch (weekDay.toUpperCase()) {
            case "MONDAY": return "周一";
            case "TUESDAY": return "周二";
            case "WEDNESDAY": return "周三";
            case "THURSDAY": return "周四";
            case "FRIDAY": return "周五";
            case "SATURDAY": return "周六";
            case "SUNDAY": return "周日";
            default: return weekDay;
        }
    }

    /**
     * 将内容按句子分行，一行一句话
     */
    private String formatContentToSentences(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        
        // 按句子分割：句号、问号、感叹号、冒号、分号、换行符
        String[] sentences = content.split("([。！？：；]|\\n)");
        
        for (String sentence : sentences) {
            String trimmedSentence = sentence.trim();
            if (!trimmedSentence.isEmpty()) {
                // 移除多余的空白字符
                String cleanSentence = trimmedSentence.replaceAll("\\s+", " ");
                result.append(cleanSentence).append("\n");
            }
        }
        
        // 如果没有找到句子分隔符，按逗号分割
        if (result.length() == 0) {
            String[] phrases = content.split("，");
            for (String phrase : phrases) {
                String trimmedPhrase = phrase.trim();
                if (!trimmedPhrase.isEmpty()) {
                    String cleanPhrase = trimmedPhrase.replaceAll("\\s+", " ");
                    result.append(cleanPhrase).append("\n");
                }
            }
        }
        
        // 如果还是没有内容，按原样返回
        if (result.length() == 0) {
            return content;
        }
        
        return result.toString();
    }

    /**
     * 格式化JSON对象（FastJSON）
     */
    private void formatJsonObject(JSONObject jsonObject, StringBuilder result, int indent) {
        String indentStr = "  ".repeat(indent);

        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            String readableKey = getReadableKey(key);

            if (value instanceof JSONObject) {
                result.append(indentStr).append(readableKey).append(":\n");
                formatJsonObject((JSONObject) value, result, indent + 1);
            } else if (value instanceof JSONArray) {
                result.append(indentStr).append(readableKey).append(":\n");
                formatJsonArray((JSONArray) value, result, indent + 1);
            } else {
                result.append(indentStr).append(readableKey).append(": ").append(formatValue(value)).append("\n");
            }
        }
    }

    /**
     * 格式化JSON数组（FastJSON）
     */
    private void formatJsonArray(JSONArray jsonArray, StringBuilder result, int indent) {
        String indentStr = "  ".repeat(indent);

        for (int i = 0; i < jsonArray.size(); i++) {
            Object value = jsonArray.get(i);

            if (value instanceof JSONObject) {
                result.append(indentStr).append("[").append(i + 1).append("]\n");
                formatJsonObject((JSONObject) value, result, indent + 1);
            } else if (value instanceof JSONArray) {
                result.append(indentStr).append("[").append(i + 1).append("]\n");
                formatJsonArray((JSONArray) value, result, indent + 1);
            } else {
                result.append(indentStr).append("[").append(i + 1).append("] ").append(formatValue(value)).append("\n");
            }
        }
    }
    
    /**
     * 获取可读的键名
     */
    private String getReadableKey(String key) {
        switch (key.toLowerCase()) {
            case "categorydata": return "分类数据";
            case "category": return "分类";
            case "categoryname": return "分类名称";
            case "protocolcount": return "协议数量";
            case "totalprotocolcount": return "总协议数量";
            case "avgduration": return "平均响应时间(ms)";
            case "maxduration": return "最大响应时间(ms)";
            case "minduration": return "最小响应时间(ms)";
            case "executioncount": return "执行次数";
            case "failurereason": return "失败原因";
            case "failurecount": return "失败次数";
            case "errorcode": return "错误代码";
            case "successrate": return "成功率(%)";
            case "failurerate": return "失败率(%)";
            case "timeslot": return "时间区间";
            case "hourgroup": return "小时分组";
            default: return key;
        }
    }
    
    /**
     * 格式化值
     */
    private String formatValue(Object value) {
        if (value == null) return "空";
        if (value instanceof Number) {
            // 数字格式化
            Number number = (Number) value;
            if (number.doubleValue() == number.intValue()) {
                return String.valueOf(number.intValue());
            } else {
                return String.format("%.2f", number.doubleValue());
            }
        }
        return value.toString();
    }
    
    /**
     * 文本换行处理
     */
    private String[] wrapText(String text, int maxLineLength) {
        if (text == null || text.trim().isEmpty()) {
            return new String[]{"报告内容为空"};
        }
        
        // 简单的换行逻辑
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : text.split("\\s+")) {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                // 处理超长单词
                if (word.length() > maxLineLength) {
                    for (int i = 0; i < word.length(); i += maxLineLength) {
                        int end = Math.min(i + maxLineLength, word.length());
                        lines.add(word.substring(i, end));
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    
    /**
     * 将内容添加到PDF文档（支持多页）
     */
    private void addContentToDocument(PDDocument document, String content, int startY) throws Exception {
        String[] lines = wrapText(content, 80);
        int currentY = startY;
        PDPage currentPage = document.getPage(document.getNumberOfPages() - 1);
        
        // 处理第一页的内容
        try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, currentY);
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                if (currentY < 50) {
                    // 当前页面空间不足，结束当前页面
                    contentStream.endText();
                    
                    // 创建新页面
                    PDPage newPage = new PDPage();
                    document.addPage(newPage);
                    currentY = 750;
                    
                    // 在新页面继续添加剩余内容
                    addContentToNewPage(document, newPage, lines, i, currentY);
                    break;
                } else {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                    currentY -= 15;
                }
            }
            
            if (currentY >= 50) {
                contentStream.endText();
            }
        }
    }
    
    /**
     * 在新页面添加内容
     */
    private void addContentToNewPage(PDDocument document, PDPage page, String[] lines, int startIndex, int startY) throws Exception {
        int currentY = startY;
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, currentY);
            
            for (int i = startIndex; i < lines.length; i++) {
                String line = lines[i];
                
                if (currentY < 50) {
                    // 当前页面空间不足，递归处理
                    contentStream.endText();
                    
                    PDPage newPage = new PDPage();
                    document.addPage(newPage);
                    currentY = 750;
                    
                    addContentToNewPage(document, newPage, lines, i, currentY);
                    break;
                } else {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                    currentY -= 15;
                }
            }
            
            if (currentY >= 50) {
                contentStream.endText();
            }
        }
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