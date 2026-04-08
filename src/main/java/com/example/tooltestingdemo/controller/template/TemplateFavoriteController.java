package com.example.tooltestingdemo.controller.template;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.service.template.TemplateFavoriteService;
import com.example.tooltestingdemo.vo.TemplateFavoriteVO;
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
     * 
     * @param templateId 模板ID
     * @param userId 用户ID
     * @param remark 备注
     * @return 收藏记录VO
     */
    @PostMapping("/{templateId}")
    public Result<TemplateFavoriteVO> favoriteTemplate(@PathVariable Long templateId,
                                                      @RequestAttribute("userId") Long userId,
                                                      @RequestParam(required = false) String remark) {
        TemplateFavoriteVO vo = favoriteService.favoriteTemplate(userId, templateId, remark);
        return Result.success("收藏成功", vo);
    }

    /**
     * 取消收藏
     * 
     * 接口地址：DELETE /api/template/favorite/{templateId}
     * 
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @DeleteMapping("/{templateId}")
    public Result<String> unfavoriteTemplate(@PathVariable Long templateId,
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
     * 
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 关注记录VO
     */
    @PostMapping("/follow/{templateId}")
    public Result<TemplateFavoriteVO> followTemplate(@PathVariable Long templateId,
                                                    @RequestAttribute("userId") Long userId) {
        TemplateFavoriteVO vo = favoriteService.followTemplate(userId, templateId);
        return Result.success("关注成功", vo);
    }

    /**
     * 取消关注
     * 
     * 接口地址：DELETE /api/template/favorite/follow/{templateId}
     * 
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @DeleteMapping("/follow/{templateId}")
    public Result<String> unfollowTemplate(@PathVariable Long templateId,
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
     * 
     * @param userId 用户ID
     * @return 收藏VO列表
     */
    @GetMapping("/my-favorites")
    public Result<List<TemplateFavoriteVO>> getMyFavorites(@RequestAttribute("userId") Long userId) {
        List<TemplateFavoriteVO> favorites = favoriteService.getUserFavorites(userId);
        return Result.success(favorites);
    }

    /**
     * 获取用户的关注列表
     * 
     * 接口地址：GET /api/template/favorite/my-follows
     * 
     * @param userId 用户ID
     * @return 关注VO列表
     */
    @GetMapping("/my-follows")
    public Result<List<TemplateFavoriteVO>> getMyFollows(@RequestAttribute("userId") Long userId) {
        List<TemplateFavoriteVO> follows = favoriteService.getUserFollows(userId);
        return Result.success(follows);
    }

    /**
     * 检查是否已收藏
     * 
     * 接口地址：GET /api/template/favorite/check/{templateId}
     * 
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 是否已收藏
     */
    @GetMapping("/check/{templateId}")
    public Result<Boolean> isFavorited(@PathVariable Long templateId,
                                      @RequestAttribute("userId") Long userId) {
        boolean isFavorited = favoriteService.isFavorited(userId, templateId);
        return Result.success(isFavorited);
    }
}
