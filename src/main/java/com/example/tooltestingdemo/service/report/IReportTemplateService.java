package com.example.tooltestingdemo.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.report.ReportTemplateDTO;
import com.example.tooltestingdemo.entity.report.ReportTemplate;
import com.example.tooltestingdemo.entity.report.TemplateXml;

import java.util.List;

/**
 * 报告模板服务接口
 */
public interface IReportTemplateService extends IService<ReportTemplate> {
    
    /**
     * 创建报告模板
     */
    Long createTemplate(ReportTemplateDTO templateDTO);
    
    /**
     * 更新报告模板
     */
    Boolean updateTemplate(Long id, ReportTemplateDTO templateDTO);
    
    /**
     * 删除报告模板
     */
    Boolean deleteTemplate(Long id);
    
    /**
     * 获取模板列表
     */
    List<ReportTemplateDTO> getTemplateList(String templateType, Boolean isPublic, String name);

    /**
     * 分页获取模板列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReportTemplateDTO> getTemplateListPage(
            Integer page, Integer size, String templateType, Boolean isPublic, String name);
    
    /**
     * 获取模板详情
     */
    ReportTemplateDTO getTemplateDetail(Long id);
    
    /**
     * 导入模板
     */
    Long importTemplate(String xmlContent, String newName);

    /**
     * 导出模板
     */
    String exportTemplate(Long id);

    /**
     * 预览XML模板内容
     */
    TemplateXml previewTemplateXml(String xmlContent);
    
    /**
     * 关联业务对象
     */
    Boolean relateBusinessType(Long templateId, String businessType);
    
    /**
     * 获取模板使用记录
     */
    List<Object> getTemplateUsageRecords(Long templateId);
}