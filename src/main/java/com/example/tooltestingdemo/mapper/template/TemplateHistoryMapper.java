package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板历史版本 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateHistoryMapper.java
 */
@Mapper
public interface TemplateHistoryMapper extends BaseMapper<TemplateHistory> {

    /**
     * 根据模板ID查询历史版本列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateHistoryMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateHistory> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查询模板的最新版本
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateHistoryMapper.xml
     * SQL ID：selectLatestByTemplateId
     */
    TemplateHistory selectLatestByTemplateId(@Param("templateId") Long templateId);
}
