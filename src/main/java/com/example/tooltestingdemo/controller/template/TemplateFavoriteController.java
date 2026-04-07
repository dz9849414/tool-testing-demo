package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.template.TemplateFavorite;
import com.example.tooltestingdemo.service.template.TemplateFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板收藏/关注 Controller
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/controller/template/TemplateFavoriteController.java
 */
@Slf4j
@RestController
@RequestMapping("/api/template/favorite")
@RequiredArgsConstructor
public class TemplateFavoriteController {

    private final TemplateFavoriteService favoriteService;

    /**
     * 收藏模板
     * 
     * 接口地址：POST /api/template/favorite/{templateId}
     */
    @PostMapping("/{templateId}")
    public Result<TemplateFavorite> favoriteTemplate(@PathVariable Long templateId,
                                                      @RequestAttribute("userId") Long userId,
                                                      @RequestParam(required = false) String remark) {
        TemplateFavorite favorite = favoriteService.favoriteTemplate(userId, templateId, remark);
        return Result.success("收藏成功", favorite);
    }

    /**
     * 取消收藏
     * 
     * 接口地址：DELETE /api/template/favorite/{templateId}
     */
    @DeleteMapping("/{templateId}")
    public Result<Void> unfavoriteTemplate(@PathVariable Long templateId,
                                            @RequestAttribute("userId") Long userId) {
        boolean success = favoriteService.unfavoriteTemplate(userId, templateId);
        if (success) {
            return Result.success("取消收藏成功");
        }
        return Result.error("取消收藏失败");
    }

    /**
     * 关注模板
     * 
     * 接口地址：POST /api/template/favorite/follow/{templateId}
     */
    @PostMapping("/follow/{templateId}")
    public Result<TemplateFavorite> followTemplate(@PathVariable Long templateId,
                                                    @RequestAttribute("userId") Long userId) {
        TemplateFavorite follow = favoriteService.followTemplate(userId, templateId);
        return Result.success("关注成功", follow);
    }

    /**
     * 取消关注
     * 
     * 接口地址：DELETE /api/template/favorite/follow/{templateId}
     */
    @DeleteMapping("/follow/{templateId}")
    public Result<Void> unfollowTemplate(@PathVariable Long templateId,
                                          @RequestAttribute("userId") Long userId) {
        boolean success = favoriteService.unfollowTemplate(userId, templateId);
        if (success) {
            return Result.success("取消关注成功");
        }
        return Result.error("取消关注失败");
    }

    /**
     * 获取用户的收藏列表
     * 
     * 接口地址：GET /api/template/favorite/my-favorites
     */
    @GetMapping("/my-favorites")
    public Result<List<TemplateFavorite>> getMyFavorites(@RequestAttribute("userId") Long userId) {
        List<TemplateFavorite> favorites = favoriteService.getUserFavorites(userId);
        return Result.success(favorites);
    }

    /**
     * 获取用户的关注列表
     * 
     * 接口地址：GET /api/template/favorite/my-follows
     */
    @GetMapping("/my-follows")
    public Result<List<TemplateFavorite>> getMyFollows(@RequestAttribute("userId") Long userId) {
        List<TemplateFavorite> follows = favoriteService.getUserFollows(userId);
        return Result.success(follows);
    }

    /**
     * 检查是否已收藏
     * 
     * 接口地址：GET /api/template/favorite/check/{templateId}
     */
    @GetMapping("/check/{templateId}")
    public Result<Boolean> isFavorited(@PathVariable Long templateId,
                                        @RequestAttribute("userId") Long userId) {
        boolean isFavorited = favoriteService.isFavorited(userId, templateId);
        return Result.success(isFavorited);
    }
}
