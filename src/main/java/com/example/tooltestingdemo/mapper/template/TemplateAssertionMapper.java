package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateAssertion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板响应验证规则 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateAssertionMapper.java
 */
@Mapper
public interface TemplateAssertionMapper extends BaseMapper<TemplateAssertion> {

    /**
     * 根据模板ID查询断言列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateAssertionMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateAssertion> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量插入断言
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateAssertionMapper.xml
     * SQL ID：batchInsert
     */
    int batchInsert(@Param("list") List<TemplateAssertion> assertions);

    /**
     * 根据模板ID删除所有断言
     * 
     * 使用MyBatis-Plus基础方法
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateAssertion>()
                .eq(TemplateAssertion::getTemplateId, templateId)
        );
    }
}
