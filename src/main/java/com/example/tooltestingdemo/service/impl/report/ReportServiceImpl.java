package com.example.tooltestingdemo.service.impl.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.report.ReportDTO;
import com.example.tooltestingdemo.entity.report.Report;
import com.example.tooltestingdemo.mapper.report.ReportMapper;
import com.example.tooltestingdemo.service.report.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报告服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    private final ReportMapper reportMapper;

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
        
        return reportMapper.updateById(report) > 0;
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
}