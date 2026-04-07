package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.template.TemplateFavorite;

import java.util.List;

/**
 * 模板收藏/关注 Service 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/template/TemplateFavoriteService.java
 */
public interface TemplateFavoriteService extends IService<TemplateFavorite> {

    /**
     * 收藏模板
     * 
     * @param userId 用户ID
     * @param templateId 模板ID
     * @param remark 备注
     * @return 收藏记录
     */
    TemplateFavorite favoriteTemplate(Long userId, Long templateId, String remark);

    /**
     * 取消收藏
     * 
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return 是否成功
     */
    boolean unfavoriteTemplate(Long userId, Long templateId);

    /**
     * 关注模板
     * 
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return 关注记录
     */
    TemplateFavorite followTemplate(Long userId, Long templateId);

    /**
     * 取消关注
     * 
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return 是否成功
     */
    boolean unfollowTemplate(Long userId, Long templateId);

    /**
     * 获取用户的收藏列表
     * 
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<TemplateFavorite> getUserFavorites(Long userId);

    /**
     * 获取用户的关注列表
     * 
     * @param userId 用户ID
     * @return 关注列表
     */
    List<TemplateFavorite> getUserFollows(Long userId);

    /**
     * 检查用户是否已收藏模板
     * 
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Long templateId);
}
