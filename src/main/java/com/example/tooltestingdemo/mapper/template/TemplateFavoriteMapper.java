package com.example.tooltestingdemo.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.template.TemplateFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板收藏/关注 Mapper 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/mapper/template/TemplateFavoriteMapper.java
 */
@Mapper
public interface TemplateFavoriteMapper extends BaseMapper<TemplateFavorite> {

    /**
     * 根据用户ID和模板ID查询收藏记录
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateFavoriteMapper.xml
     * SQL ID：selectByUserAndTemplate
     */
    TemplateFavorite selectByUserAndTemplate(@Param("userId") Long userId, @Param("templateId") Long templateId);

    /**
     * 查询用户的收藏列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateFavoriteMapper.xml
     * SQL ID：selectFavoritesByUserId
     */
    List<TemplateFavorite> selectFavoritesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的关注列表
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateFavoriteMapper.xml
     * SQL ID：selectFollowsByUserId
     */
    List<TemplateFavorite> selectFollowsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已收藏模板
     * 
     * 对应XML：src/main/resources/mapper/template/TemplateFavoriteMapper.xml
     * SQL ID：checkFavoriteExists
     */
    int checkFavoriteExists(@Param("userId") Long userId, @Param("templateId") Long templateId);
}
