package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplatePostProcessor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板后置处理器 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplatePostProcessorMapper.java
 */
@Mapper
public interface TemplatePostProcessorMapper extends BaseMapper<TemplatePostProcessor> {

    /**
     * 根据模板ID查询后置处理器列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplatePostProcessorMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplatePostProcessor> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量插入后置处理器
     * 
     * 对应XML：src/main/resources/mapper/template/TemplatePostProcessorMapper.xml
     * SQL ID：batchInsert
     */
    int batchInsert(@Param("list") List<TemplatePostProcessor> processors);

    /**
     * 根据模板ID删除所有后置处理器
     * 
     * 使用MyBatis-Plus基础方法
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplatePostProcessor>()
                .eq(TemplatePostProcessor::getTemplateId, templateId)
        );
    }
}
