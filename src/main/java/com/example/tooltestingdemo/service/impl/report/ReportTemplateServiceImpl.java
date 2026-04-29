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
        template.setIsSystemTemplate(templateXml.getIsSystemTemplate() != null ? templateXml.getIsSystemTemplate() : false);
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

    private ReportTemplateDTO convertToDTO(ReportTemplate template) {
        ReportTemplateDTO dto = new ReportTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }
}