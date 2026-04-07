package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateImportExport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板导入导出记录 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateImportExportMapper.java
 */
@Mapper
public interface TemplateImportExportMapper extends BaseMapper<TemplateImportExport> {

    /**
     * 查询用户的导入导出记录
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateImportExportMapper.xml
     * SQL ID：selectByOperatorId
     */
    List<TemplateImportExport> selectByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 查询指定操作类型的记录
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateImportExportMapper.xml
     * SQL ID：selectByOperationType
     */
    List<TemplateImportExport> selectByOperationType(@Param("operationType") String operationType, 
                                                      @Param("limit") Integer limit);
}
