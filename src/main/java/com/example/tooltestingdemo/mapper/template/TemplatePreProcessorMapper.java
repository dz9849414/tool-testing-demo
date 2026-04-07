package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplatePreProcessor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板前置处理器 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplatePreProcessorMapper.java
 */
@Mapper
public interface TemplatePreProcessorMapper extends BaseMapper<TemplatePreProcessor> {

    /**
     * 根据模板ID查询前置处理器列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplatePreProcessorMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplatePreProcessor> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量插入前置处理器
     * 
     * 对应XML：src/main/resources/mapper/template/TemplatePreProcessorMapper.xml
     * SQL ID：batchInsert
     */
    int batchInsert(@Param("list") List<TemplatePreProcessor> processors);

    /**
     * 根据模板ID删除所有前置处理器
     * 
     * 使用MyBatis-Plus基础方法
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplatePreProcessor>()
                .eq(TemplatePreProcessor::getTemplateId, templateId)
        );
    }
}
