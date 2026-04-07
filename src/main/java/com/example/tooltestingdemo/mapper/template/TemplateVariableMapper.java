package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateVariable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板变量定义 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateVariableMapper.java
 */
@Mapper
public interface TemplateVariableMapper extends BaseMapper<TemplateVariable> {

    /**
     * 根据模板ID查询变量列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateVariableMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateVariable> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量插入变量
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateVariableMapper.xml
     * SQL ID：batchInsert
     */
    int batchInsert(@Param("list") List<TemplateVariable> variables);

    /**
     * 根据模板ID删除所有变量
     * 
     * 使用MyBatis-Plus基础方法
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateVariable>()
                .eq(TemplateVariable::getTemplateId, templateId)
        );
    }
}
