package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateHeader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板请求头 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateHeaderMapper.java
 */
@Mapper
public interface TemplateHeaderMapper extends BaseMapper<TemplateHeader> {

    /**
     * 根据模板ID查询请求头列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateHeaderMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateHeader> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量插入请求头
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateHeaderMapper.xml
     * SQL ID：batchInsert
     */
    int batchInsert(@Param("list") List<TemplateHeader> headers);

    /**
     * 根据模板ID删除所有请求头
     * 
     * 使用MyBatis-Plus基础方法
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateHeader>()
                .eq(TemplateHeader::getTemplateId, templateId)
        );
    }
}
