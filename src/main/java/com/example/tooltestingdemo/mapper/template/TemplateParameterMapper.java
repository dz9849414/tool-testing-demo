package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateParameter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板请求参数 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateParameterMapper.java
 */
@Mapper
public interface TemplateParameterMapper extends BaseMapper<TemplateParameter> {

    /**
     * 根据模板ID查询参数列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateParameterMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateParameter> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 根据模板ID和参数类型查询参数列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateParameterMapper.xml
     * SQL ID：selectByTemplateIdAndType
     */
    List<TemplateParameter> selectByTemplateIdAndType(@Param("templateId") Long templateId, 
                                                       @Param("paramType") String paramType);

    /**
     * 批量插入参数
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateParameterMapper.xml
     * SQL ID：batchInsert
     */
    int batchInsert(@Param("list") List<TemplateParameter> parameters);

    /**
     * 根据模板ID删除所有参数
     * 
     * 使用MyBatis-Plus基础方法
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateParameter>()
                .eq(TemplateParameter::getTemplateId, templateId)
        );
    }
}
