package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 接口模板主表 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/InterfaceTemplateMapper.java
 */
@Mapper
public interface InterfaceTemplateMapper extends BaseMapper<InterfaceTemplate> {

    /**
     * 分页查询模板列表（包含文件夹名称）
     * 
     * 文件位置：src/main/resources/mapper/template/InterfaceTemplateMapper.xml
     */
    IPage<InterfaceTemplate> selectTemplatePage(Page<InterfaceTemplate> page, 
                                                 @Param("folderId") Long folderId,
                                                 @Param("keyword") String keyword,
                                                 @Param("protocolType") String protocolType,
                                                 @Param("status") Integer status);

    /**
     * 查询模板的所有版本
     * 
     * 文件位置：src/main/resources/mapper/template/InterfaceTemplateMapper.xml
     */
    List<InterfaceTemplate> selectVersionsByRefId(@Param("refTemplateId") Long refTemplateId);

    /**
     * 根据ID查询模板详情（包含所有关联信息）
     * 
     * 文件位置：src/main/resources/mapper/template/InterfaceTemplateMapper.xml
     */
    InterfaceTemplate selectTemplateDetailById(@Param("id") Long id);

    /**
     * 根据名称和方法查找模板（用于导入时检查重复）
     *
     * @param name   模板名称
     * @param method 请求方法
     * @return 模板对象
     */
    /**
     * 根据名称和方法查找模板（用于导入时检查重复）
     * 
     * 对应XML: InterfaceTemplateMapper.xml
     */
    InterfaceTemplate selectByNameAndMethod(@Param("name") String name, @Param("method") String method);
}
