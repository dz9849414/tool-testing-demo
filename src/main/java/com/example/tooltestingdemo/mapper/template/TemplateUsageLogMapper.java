package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateUsageLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板使用记录 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateUsageLogMapper.java
 */
@Mapper
public interface TemplateUsageLogMapper extends BaseMapper<TemplateUsageLog> {

    /**
     * 根据模板ID查询使用记录
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateUsageLogMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateUsageLog> selectByTemplateId(@Param("templateId") Long templateId, @Param("limit") Integer limit);

    /**
     * 查询用户的使用记录
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateUsageLogMapper.xml
     * SQL ID：selectByUserId
     */
    List<TemplateUsageLog> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计模板使用次数
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateUsageLogMapper.xml
     * SQL ID：countByTemplateId
     */
    int countByTemplateId(@Param("templateId") Long templateId);
}
