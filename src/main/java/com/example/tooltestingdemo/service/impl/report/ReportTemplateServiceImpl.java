package com.example.tooltestingdemo.service.impl.report;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.report.ReportTemplateDTO;
import com.example.tooltestingdemo.entity.report.ReportTemplate;
import com.example.tooltestingdemo.entity.report.TemplateXml;
import com.example.tooltestingdemo.mapper.report.ReportTemplateMapper;
import com.example.tooltestingdemo.service.report.IReportTemplateService;
import com.example.tooltestingdemo.utils.XmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报告模板服务实现类
 */@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTemplateServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> implements IReportTemplateService {

    private final ReportTemplateMapper reportTemplateMapper;

    @Override
    public Long createTemplate(ReportTemplateDTO templateDTO) {
        ReportTemplate template = new ReportTemplate();
        BeanUtils.copyProperties(templateDTO, template);
        
        // 设置默认值
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        template.setIsDeleted(0);
        template.setUsageCount(0);
        
        reportTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public Boolean updateTemplate(Long id, ReportTemplateDTO templateDTO) {
        ReportTemplate template = reportTemplateMapper.selectById(id);
        if (template == null) {
            return false;
        }
        
        // 检查是否是系统模板，系统模板不允许修改
        if (template.getIsSystemTemplate() != null && template.getIsSystemTemplate()) {
            throw new IllegalArgumentException("系统预设模板不允许修改");
        }
        
        BeanUtils.copyProperties(templateDTO, template);
        template.setUpdateTime(LocalDateTime.now());
        
        return reportTemplateMapper.updateById(template) > 0;
    }

    @Override
    public Boolean deleteTemplate(Long id) {
        ReportTemplate template = reportTemplateMapper.selectById(id);
        if (template == null) {
            return false;
        }
        
        // 检查是否是系统模板，系统模板不允许删除
        if (template.getIsSystemTemplate() != null && template.getIsSystemTemplate()) {
            throw new IllegalArgumentException("系统预设模板不允许删除");
        }
        
        // 软删除
        template.setIsDeleted(1);
        template.setUpdateTime(LocalDateTime.now());
        
        return reportTemplateMapper.deleteById(template) > 0;
    }

    @Override
    public List<ReportTemplateDTO> getTemplateList(String templateType, Boolean isPublic, String name) {
        LambdaQueryWrapper<ReportTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportTemplate::getIsDeleted, 0);
        
        if (templateType != null) {
            queryWrapper.eq(ReportTemplate::getTemplateType, templateType);
        }
        
        if (isPublic != null) {
            queryWrapper.eq(ReportTemplate::getIsPublic, isPublic);
        }
        
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(ReportTemplate::getName, name.trim());
        }
        
        queryWrapper.orderByDesc(ReportTemplate::getCreateTime);
        
        List<ReportTemplate> templates = reportTemplateMapper.selectList(queryWrapper);
        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReportTemplateDTO> getTemplateListPage(
            Integer page, Integer size, String templateType, Boolean isPublic, String name) {
        
        // 设置默认分页参数
        int currentPage = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;
        
        // 构建查询条件
        LambdaQueryWrapper<ReportTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportTemplate::getIsDeleted, 0);
        
        if (templateType != null && !templateType.trim().isEmpty()) {
            queryWrapper.eq(ReportTemplate::getTemplateType, templateType.trim());
        }
        
        if (isPublic != null) {
            queryWrapper.eq(ReportTemplate::getIsPublic, isPublic);
        }
        
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(ReportTemplate::getName, name.trim());
        }
        
        queryWrapper.orderByDesc(ReportTemplate::getCreateTime);
        
        // 使用MyBatis Plus的Service层分页方法
        Page<ReportTemplate> entityPage = new Page<>(currentPage, pageSize);
        entityPage = reportTemplateMapper.selectPage(entityPage, queryWrapper);
        
        // 转换为DTO的Page对象
        Page<ReportTemplateDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setPages(entityPage.getPages());
        dtoPage.setRecords(entityPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        
        return dtoPage;
    }

    @Override
    public ReportTemplateDTO getTemplateDetail(Long id) {
        ReportTemplate template = reportTemplateMapper.selectById(id);
        if (template == null || template.getIsDeleted() == 1) {
            return null;
        }
        
        return convertToDTO(template);
    }

    @Override
    public Long importTemplate(String xmlContent, String newName) {
        log.info("开始导入XML模板，内容长度：{}", xmlContent.length());
        
        // 1. 解析XML内容
        TemplateXml templateXml = XmlParser.parse(xmlContent, TemplateXml.class);
        
        // 2. 校验模板名称唯一性
        String templateName = newName != null ? newName : templateXml.getName();
        if (reportTemplateMapper.existsByName(templateName)) {
            throw new RuntimeException("模板名称已存在：" + templateName);
        }
        
        // 3. 创建模板实体
        ReportTemplate template = new ReportTemplate();
        template.setName(templateName);
        template.setDescription(templateXml.getDescription());
        template.setTemplateType(templateXml.getTemplateType());
        template.setApplicableScene(templateXml.getApplicableScene());
        template.setTemplateStructure(templateXml.getTemplateStructure());
        template.setChapterStructure(templateXml.getChapterStructure().isEmpty() ? new JSONObject().toJSONString() : templateXml.getChapterStructure());
        template.setContent(templateXml.getContent());
        template.setStyleConfig(templateXml.getStyleConfig());
        template.setIsSystemTemplate(false);
        template.setIsPublic(templateXml.getIsPublic() != null ? templateXml.getIsPublic() : true);
        template.setRelatedBusinessType(templateXml.getRelatedBusinessType());
        template.setSortOrder(templateXml.getSortOrder() != null ? templateXml.getSortOrder() : 0);
        template.setPreviewImage(templateXml.getPreviewImage());
        
        // 4. 设置默认值
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        template.setIsDeleted(0);
        template.setUsageCount(0);
        template.setStatus(1);
        
        // 5. 保存到数据库
        reportTemplateMapper.insert(template);
        
        log.info("XML模板导入成功，模板ID：{}", template.getId());
        return template.getId();
    }

    @Override
    public String exportTemplate(Long id) {
        ReportTemplate template = reportTemplateMapper.selectById(id);
        if (template == null || template.getIsDeleted() == 1) {
            return null;
        }
        
        // 构建XML格式的导出内容
        TemplateXml templateXml = new TemplateXml();
        templateXml.setName(template.getName());
        templateXml.setDescription(template.getDescription());
        templateXml.setTemplateType(template.getTemplateType());
        templateXml.setApplicableScene(template.getApplicableScene());
        templateXml.setTemplateStructure(template.getTemplateStructure());
        templateXml.setChapterStructure(template.getChapterStructure());
        templateXml.setContent(template.getContent());
        templateXml.setStyleConfig(template.getStyleConfig());
        templateXml.setIsSystemTemplate(template.getIsSystemTemplate());
        templateXml.setIsPublic(template.getIsPublic());
        templateXml.setRelatedBusinessType(template.getRelatedBusinessType());
        templateXml.setSortOrder(template.getSortOrder());
        templateXml.setPreviewImage(template.getPreviewImage());
        templateXml.setVersion("1.0");
        templateXml.setAuthor("System");
        templateXml.setCreateTime(template.getCreateTime() != null ? template.getCreateTime().toString() : null);
        templateXml.setUpdateTime(template.getUpdateTime() != null ? template.getUpdateTime().toString() : null);
        
        // 格式化XML输出
        String xmlContent = XmlParser.toXml(templateXml);
        return XmlParser.formatXml(xmlContent);
    }

    @Override
    public TemplateXml previewTemplateXml(String xmlContent) {
        log.info("预览XML模板内容，长度：{}", xmlContent.length());
        
        // 解析XML内容
        TemplateXml templateXml = XmlParser.parse(xmlContent, TemplateXml.class);
        
        // 校验必填字段
        if (templateXml.getName() == null || templateXml.getName().trim().isEmpty()) {
            throw new RuntimeException("模板名称不能为空");
        }
        
        if (templateXml.getTemplateType() == null || templateXml.getTemplateType().trim().isEmpty()) {
            throw new RuntimeException("模板类型不能为空");
        }
        
        log.info("XML模板预览成功，名称：{}", templateXml.getName());
        return templateXml;
    }

    @Override
    public Boolean relateBusinessType(Long templateId, String businessType) {
        ReportTemplate template = reportTemplateMapper.selectById(templateId);
        if (template == null) {
            return false;
        }
        
        template.setRelatedBusinessType(businessType);
        template.setUpdateTime(LocalDateTime.now());
        
        return reportTemplateMapper.updateById(template) > 0;
    }

    @Override
    public List<Object> getTemplateUsageRecords(Long templateId) {
        // 实现获取模板使用记录的逻辑
        // 这里简化处理，返回空列表
        return List.of();
    }

    @Override
    public java.util.Map<String, Object> getSystemTemplateStructure(String templateType) {
        // 构建查询条件：查询系统模板(is_system_template=1)且指定templateType，按创建时间升序排序取最早创建的
        LambdaQueryWrapper<ReportTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportTemplate::getIsDeleted, 0);
        queryWrapper.eq(ReportTemplate::getIsSystemTemplate, true);
        queryWrapper.eq(ReportTemplate::getTemplateType, templateType);
        queryWrapper.orderByAsc(ReportTemplate::getCreateTime);
        queryWrapper.last("LIMIT 1");
        
        // 查询模板（按创建时间升序，取最早创建的一条）
        ReportTemplate template = reportTemplateMapper.selectOne(queryWrapper);
        
        // 构建返回结果
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (template != null) {
            result.put("templateStructure", template.getTemplateStructure());
            result.put("chapterStructure", template.getChapterStructure());
            result.put("templateType", template.getTemplateType());
            result.put("name", template.getName());
            result.put("description", template.getDescription());
            result.put("id", template.getId());
        }
        
        // 添加前端下拉框数据
        result.put("chapterTypes", getChapterTypes());
        result.put("chartTypes", getChartTypes());
        result.put("dataSources", getDataSources());
        
        return result;
    }
    
    /**
     * 获取章节类型下拉框数据
     */
    private java.util.List<java.util.Map<String, Object>> getChapterTypes() {
        java.util.List<java.util.Map<String, Object>> types = new java.util.ArrayList<>();
        
        java.util.Map<String, Object> chartType = new java.util.HashMap<>();
        chartType.put("value", "chart");
        chartType.put("label", "图表");
        types.add(chartType);
        
        java.util.Map<String, Object> textType = new java.util.HashMap<>();
        textType.put("value", "text");
        textType.put("label", "文本");
        types.add(textType);
        
        return types;
    }
    
    /**
     * 获取图表类型下拉框数据
     */
    private java.util.List<java.util.Map<String, Object>> getChartTypes() {
        java.util.List<java.util.Map<String, Object>> types = new java.util.ArrayList<>();
        
        java.util.Map<String, Object> histogram = new java.util.HashMap<>();
        histogram.put("value", "HISTOGRAM");
        histogram.put("label", "直方图");
        types.add(histogram);
        
        java.util.Map<String, Object> bar = new java.util.HashMap<>();
        bar.put("value", "BAR");
        bar.put("label", "柱状图");
        types.add(bar);
        
        java.util.Map<String, Object> line = new java.util.HashMap<>();
        line.put("value", "LINE");
        line.put("label", "折线图");
        types.add(line);
        
        java.util.Map<String, Object> pie = new java.util.HashMap<>();
        pie.put("value", "PIE");
        pie.put("label", "饼图");
        types.add(pie);
        
        java.util.Map<String, Object> scatter = new java.util.HashMap<>();
        scatter.put("value", "SCATTER");
        scatter.put("label", "散点图");
        types.add(scatter);
        
        return types;
    }
    
    /**
     * 获取数据来源下拉框数据
     */
    private java.util.List<java.util.Map<String, Object>> getDataSources() {
        java.util.List<java.util.Map<String, Object>> sources = new java.util.ArrayList<>();
        
        java.util.Map<String, Object> responseTime = new java.util.HashMap<>();
        responseTime.put("value", "RESPONSE_TIME");
        responseTime.put("label", "响应时间");
        sources.add(responseTime);
        
        java.util.Map<String, Object> failureReasons = new java.util.HashMap<>();
        failureReasons.put("value", "FAILURE_REASONS");
        failureReasons.put("label", "失败原因");
        sources.add(failureReasons);
        
        java.util.Map<String, Object> optimization = new java.util.HashMap<>();
        optimization.put("value", "OPTIMIZATION_SUGGESTIONS");
        optimization.put("label", "优化建议");
        sources.add(optimization);
        
        java.util.Map<String, Object> overview = new java.util.HashMap<>();
        overview.put("value", "OVERVIEW");
        overview.put("label", "概览");
        sources.add(overview);
        
        java.util.Map<String, Object> weeklyExecution = new java.util.HashMap<>();
        weeklyExecution.put("value", "WEEKLY_EXECUTION");
        weeklyExecution.put("label", "周执行量");
        sources.add(weeklyExecution);
        
        java.util.Map<String, Object> dailyExecution = new java.util.HashMap<>();
        dailyExecution.put("value", "DAILY_EXECUTION");
        dailyExecution.put("label", "日执行量");
        sources.add(dailyExecution);
        
        java.util.Map<String, Object> successRate = new java.util.HashMap<>();
        successRate.put("value", "SUCCESS_RATE");
        successRate.put("label", "成功率");
        sources.add(successRate);
        
        java.util.Map<String, Object> protocolDistribution = new java.util.HashMap<>();
        protocolDistribution.put("value", "PROTOCOL_DISTRIBUTION");
        protocolDistribution.put("label", "协议分布");
        sources.add(protocolDistribution);
        
        java.util.Map<String, Object> anomalyDetection = new java.util.HashMap<>();
        anomalyDetection.put("value", "ANOMALY_DETECTION");
        anomalyDetection.put("label", "异常检测");
        sources.add(anomalyDetection);
        
        java.util.Map<String, Object> influencingFactors = new java.util.HashMap<>();
        influencingFactors.put("value", "INFLUENCING_FACTORS");
        influencingFactors.put("label", "影响因素");
        sources.add(influencingFactors);
        
        java.util.Map<String, Object> interfaceDetails = new java.util.HashMap<>();
        interfaceDetails.put("value", "INTERFACE_DETAILS");
        interfaceDetails.put("label", "接口详情");
        sources.add(interfaceDetails);
        
        return sources;
    }

    private ReportTemplateDTO convertToDTO(ReportTemplate template) {
        ReportTemplateDTO dto = new ReportTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }
}