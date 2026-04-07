package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateFormData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板FormData参数 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateFormDataMapper.java
 */
@Mapper
public interface TemplateFormDataMapper extends BaseMapper<TemplateFormData> {

    /**
     * 根据模板ID查询FormData列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateFormDataMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateFormData> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量插入FormData参数
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateFormDataMapper.xml
     * SQL ID：batchInsert
     */
    int batchInsert(@Param("list") List<TemplateFormData> formDataList);

    /**
     * 根据模板ID删除所有FormData参数
     * 
     * 使用MyBatis-Plus基础方法
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateFormData>()
                .eq(TemplateFormData::getTemplateId, templateId)
        );
    }
}
