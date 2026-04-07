package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板收藏/关注实体类
 */
@Data
@TableName("template_favorite")
public class TemplateFavorite {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "favorite_type")
    private Integer favoriteType;

    @TableField(value = "remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
