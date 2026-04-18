package com.example.tooltestingdemo.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.report.ReportTemplateDTO;
import com.example.tooltestingdemo.entity.report.ReportTemplate;

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
    List<ReportTemplateDTO> getTemplateList(String templateType, Boolean isPublic);
    
    /**
     * 获取模板详情
     */
    ReportTemplateDTO getTemplateDetail(Long id);
    
    /**
     * 导入模板
     */
    Boolean importTemplate(String xmlContent);
    
    /**
     * 导出模板
     */
    String exportTemplate(Long id);
    
    /**
     * 关联业务对象
     */
    Boolean relateBusinessType(Long templateId, String businessType);
    
    /**
     * 获取模板使用记录
     */
    List<Object> getTemplateUsageRecords(Long templateId);
}