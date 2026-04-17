package com.example.tooltestingdemo.service.impl.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.report.ReportTemplateDTO;
import com.example.tooltestingdemo.entity.report.ReportTemplate;
import com.example.tooltestingdemo.mapper.report.ReportTemplateMapper;
import com.example.tooltestingdemo.service.report.IReportTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报告模板服务实现类
 */
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
        
        return reportTemplateMapper.updateById(template) > 0;
    }

    @Override
    public List<ReportTemplateDTO> getTemplateList(String templateType, Boolean isPublic) {
        LambdaQueryWrapper<ReportTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportTemplate::getIsDeleted, 0);
        
        if (templateType != null) {
            queryWrapper.eq(ReportTemplate::getTemplateType, templateType);
        }
        
        if (isPublic != null) {
            queryWrapper.eq(ReportTemplate::getIsPublic, isPublic);
        }
        
        queryWrapper.orderByAsc(ReportTemplate::getSortOrder);
        
        List<ReportTemplate> templates = reportTemplateMapper.selectList(queryWrapper);
        
        return templates.stream().map(this::convertToDTO).collect(Collectors.toList());
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
    public Boolean importTemplate(String xmlContent) {
        // 实现XML导入逻辑
        // 这里简化处理，实际需要解析XML并创建模板
        return true;
    }

    @Override
    public String exportTemplate(Long id) {
        ReportTemplate template = reportTemplateMapper.selectById(id);
        if (template == null || template.getIsDeleted() == 1) {
            return null;
        }
        
        // 构建XML格式的导出内容
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<template>\n");
        xml.append("  <name>").append(template.getName()).append("</name>\n");
        xml.append("  <description>").append(template.getDescription()).append("</description>\n");
        xml.append("  <type>").append(template.getTemplateType()).append("</type>\n");
        xml.append("  <structure>").append(template.getTemplateStructure()).append("</structure>\n");
        xml.append("</template>");
        
        return xml.toString();
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