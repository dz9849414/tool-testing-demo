package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板收藏/关注 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateFavoriteVO.java
 */
@Data
public class TemplateFavoriteVO {

    private Long id;

    private Long userId;

    private Long templateId;

    /**
     * 类型：1-收藏 2-关注
     */
    private Integer favoriteType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ==================== 关联数据 ====================

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String templateDescription;
}
