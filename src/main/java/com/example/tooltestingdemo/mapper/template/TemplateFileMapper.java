package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板文件附件 Mapper 接口
 */
@Mapper
public interface TemplateFileMapper extends BaseMapper<TemplateFile> {

    /**
     * 根据模板ID查询文件列表
     */
    List<TemplateFile> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 根据模板ID和文件类别查询
     */
    List<TemplateFile> selectByTemplateIdAndCategory(@Param("templateId") Long templateId, 
                                                      @Param("fileCategory") String fileCategory);

    /**
     * 批量插入文件
     */
    int batchInsert(@Param("list") List<TemplateFile> files);

    /**
     * 根据模板ID删除所有文件
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateFile>()
                .eq(TemplateFile::getTemplateId, templateId)
        );
    }

    /**
     * 统计模板文件数量
     */
    default Long countByTemplateId(Long templateId) {
        return this.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateFile>()
                .eq(TemplateFile::getTemplateId, templateId)
        );
    }
}
