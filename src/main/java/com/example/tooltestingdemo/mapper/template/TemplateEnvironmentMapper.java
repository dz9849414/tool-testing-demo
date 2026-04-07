package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板环境配置 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateEnvironmentMapper.java
 */
@Mapper
public interface TemplateEnvironmentMapper extends BaseMapper<TemplateEnvironment> {

    /**
     * 根据模板ID查询环境配置列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateEnvironmentMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateEnvironment> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查询模板的默认环境
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateEnvironmentMapper.xml
     * SQL ID：selectDefaultByTemplateId
     */
    TemplateEnvironment selectDefaultByTemplateId(@Param("templateId") Long templateId);

    /**
     * 取消模板的所有默认环境设置
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateEnvironmentMapper.xml
     * SQL ID：clearDefaultByTemplateId
     */
    int clearDefaultByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量插入环境配置
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateEnvironmentMapper.xml
     * SQL ID：batchInsert
     */
    int batchInsert(@Param("list") List<TemplateEnvironment> environments);
}
