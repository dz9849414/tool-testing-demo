package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateFavorite;
import com.example.tooltestingdemo.mapper.template.TemplateFavoriteMapper;
import com.example.tooltestingdemo.service.template.TemplateFavoriteService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.TemplateFavoriteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模板收藏/关注 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFavoriteServiceImpl extends ServiceImpl<TemplateFavoriteMapper, TemplateFavorite> 
        implements TemplateFavoriteService {

    private final TemplateFavoriteMapper favoriteMapper;

    private static final int TYPE_FAVORITE = 1;
    private static final int TYPE_FOLLOW = 2;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateFavoriteVO favoriteTemplate(Long userId, Long templateId, String remark) {
        if (isFavorited(userId, templateId)) {
            throw new RuntimeException("已收藏该模板");
        }
        return saveFavorite(userId, templateId, TYPE_FAVORITE, remark, "收藏");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfavoriteTemplate(Long userId, Long templateId) {
        return removeByType(userId, templateId, TYPE_FAVORITE, "取消收藏");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateFavoriteVO followTemplate(Long userId, Long templateId) {
        if (existsByType(userId, templateId, TYPE_FOLLOW)) {
            throw new RuntimeException("已关注该模板");
        }
        return saveFavorite(userId, templateId, TYPE_FOLLOW, null, "关注");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollowTemplate(Long userId, Long templateId) {
        return removeByType(userId, templateId, TYPE_FOLLOW, "取消关注");
    }

    @Override
    public List<TemplateFavoriteVO> getUserFavorites(Long userId) {
        return TemplateConverter.toFavoriteVOList(favoriteMapper.selectFavoritesByUserId(userId));
    }

    @Override
    public List<TemplateFavoriteVO> getUserFollows(Long userId) {
        return TemplateConverter.toFavoriteVOList(favoriteMapper.selectFollowsByUserId(userId));
    }

    @Override
    public boolean isFavorited(Long userId, Long templateId) {
        return favoriteMapper.checkFavoriteExists(userId, templateId) > 0;
    }

    private TemplateFavoriteVO saveFavorite(Long userId, Long templateId, int type, String remark, String action) {
        TemplateFavorite favorite = new TemplateFavorite();
        favorite.setCreateId(userId);
        favorite.setTemplateId(templateId);
        favorite.setFavoriteType(type);
        favorite.setRemark(remark);
        
        save(favorite);
        log.info("{}模板成功: userId={}, templateId={}", action, userId, templateId);
        return TemplateConverter.toVO(favorite);
    }

    private boolean removeByType(Long userId, Long templateId, int type, String action) {
        boolean result = remove(new LambdaQueryWrapper<TemplateFavorite>()
            .eq(TemplateFavorite::getCreateId, userId)
            .eq(TemplateFavorite::getTemplateId, templateId)
            .eq(TemplateFavorite::getFavoriteType, type));
        if (result) log.info("{}成功: userId={}, templateId={}", action, userId, templateId);
        return result;
    }

    private boolean existsByType(Long userId, Long templateId, int type) {
        return count(new LambdaQueryWrapper<TemplateFavorite>()
            .eq(TemplateFavorite::getCreateId, userId)
            .eq(TemplateFavorite::getTemplateId, templateId)
            .eq(TemplateFavorite::getFavoriteType, type)) > 0;
    }
}
