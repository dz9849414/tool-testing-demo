package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateJobItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板定时任务子项 Mapper
 */
@Mapper
public interface TemplateJobItemMapper extends BaseMapper<TemplateJobItem> {

    /**
     * 根据任务ID查询子项列表
     */
    List<TemplateJobItem> selectByJobId(@Param("jobId") Long jobId);

    /**
     * 根据模板ID查询任务子项列表
     */
    default List<TemplateJobItem> selectByTemplateId(Long templateId) {
        return this.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateJobItem>()
                .eq(TemplateJobItem::getTemplateId, templateId)
                .eq(TemplateJobItem::getIsDeleted, 0)
                .orderByAsc(TemplateJobItem::getSortOrder)
        );
    }

    /**
     * 根据任务ID删除子项
     */
    int deleteByJobId(@Param("jobId") Long jobId);

    /**
     * 根据模板ID删除任务子项
     */
    default int deleteByTemplateId(Long templateId) {
        return this.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TemplateJobItem>()
                .eq(TemplateJobItem::getTemplateId, templateId)
        );
    }
}
