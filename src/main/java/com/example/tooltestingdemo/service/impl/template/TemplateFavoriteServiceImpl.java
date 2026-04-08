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
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/impl/template/TemplateFavoriteServiceImpl.java
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFavoriteServiceImpl extends ServiceImpl<TemplateFavoriteMapper, TemplateFavorite> 
        implements TemplateFavoriteService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateFavoriteVO favoriteTemplate(Long userId, Long templateId, String remark) {
        // 检查是否已收藏
        if (isFavorited(userId, templateId)) {
            throw new RuntimeException("已收藏该模板");
        }
        
        TemplateFavorite favorite = new TemplateFavorite();
        favorite.setUserId(userId);
        favorite.setTemplateId(templateId);
        favorite.setFavoriteType(1); // 收藏
        favorite.setRemark(remark);
        
        save(favorite);
        log.info("收藏模板成功: userId={}, templateId={}", userId, templateId);
        return TemplateConverter.toVO(favorite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfavoriteTemplate(Long userId, Long templateId) {
        LambdaQueryWrapper<TemplateFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateFavorite::getUserId, userId)
               .eq(TemplateFavorite::getTemplateId, templateId)
               .eq(TemplateFavorite::getFavoriteType, 1);
        
        boolean result = remove(wrapper);
        if (result) {
            log.info("取消收藏成功: userId={}, templateId={}", userId, templateId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateFavoriteVO followTemplate(Long userId, Long templateId) {
        // 检查是否已关注
        LambdaQueryWrapper<TemplateFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateFavorite::getUserId, userId)
               .eq(TemplateFavorite::getTemplateId, templateId)
               .eq(TemplateFavorite::getFavoriteType, 2);
        
        if (count(wrapper) > 0) {
            throw new RuntimeException("已关注该模板");
        }
        
        TemplateFavorite follow = new TemplateFavorite();
        follow.setUserId(userId);
        follow.setTemplateId(templateId);
        follow.setFavoriteType(2); // 关注
        
        save(follow);
        log.info("关注模板成功: userId={}, templateId={}", userId, templateId);
        return TemplateConverter.toVO(follow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollowTemplate(Long userId, Long templateId) {
        LambdaQueryWrapper<TemplateFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateFavorite::getUserId, userId)
               .eq(TemplateFavorite::getTemplateId, templateId)
               .eq(TemplateFavorite::getFavoriteType, 2);
        
        boolean result = remove(wrapper);
        if (result) {
            log.info("取消关注成功: userId={}, templateId={}", userId, templateId);
        }
        return result;
    }

    @Override
    public List<TemplateFavoriteVO> getUserFavorites(Long userId) {
        List<TemplateFavorite> favorites = baseMapper.selectFavoritesByUserId(userId);
        return TemplateConverter.toFavoriteVOList(favorites);
    }

    @Override
    public List<TemplateFavoriteVO> getUserFollows(Long userId) {
        List<TemplateFavorite> follows = baseMapper.selectFollowsByUserId(userId);
        return TemplateConverter.toFavoriteVOList(follows);
    }

    @Override
    public boolean isFavorited(Long userId, Long templateId) {
        return baseMapper.checkFavoriteExists(userId, templateId) > 0;
    }
}
