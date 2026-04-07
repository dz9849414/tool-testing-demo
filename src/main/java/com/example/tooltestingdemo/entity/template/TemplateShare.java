package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板共享/授权实体类
 */
@Data
@TableName("template_share")
public class TemplateShare {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "share_type")
    private String shareType;

    @TableField(value = "share_target_id")
    private Long shareTargetId;

    @TableField(value = "share_target_name")
    private String shareTargetName;

    @TableField(value = "permission")
    private String permission;

    @TableField(value = "can_share")
    private Integer canShare;

    @TableField(value = "expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "share_code")
    private String shareCode;

    @TableField(value = "share_link")
    private String shareLink;

    @TableField(value = "access_password")
    private String accessPassword;

    @TableField(value = "created_by")
    private Long createdBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
