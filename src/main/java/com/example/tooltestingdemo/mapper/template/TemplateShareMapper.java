package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板共享/授权 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateShareMapper.java
 */
@Mapper
public interface TemplateShareMapper extends BaseMapper<TemplateShare> {

    /**
     * 根据模板ID查询共享列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateShareMapper.xml
     * SQL ID：selectByTemplateId
     */
    List<TemplateShare> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 根据共享码查询共享信息
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateShareMapper.xml
     * SQL ID：selectByShareCode
     */
    TemplateShare selectByShareCode(@Param("shareCode") String shareCode);

    /**
     * 查询用户被共享的模板列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateShareMapper.xml
     * SQL ID：selectByShareTarget
     */
    List<TemplateShare> selectByShareTarget(@Param("shareType") String shareType, 
                                             @Param("shareTargetId") Long shareTargetId);
}
