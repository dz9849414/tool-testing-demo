package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板共享/授权实体类
 */
@Data
@TableName("pdm_tool_template_share")
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

    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    @TableField(value = "create_name", fill = FieldFill.INSERT)
    private String createName;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "update_id", fill = FieldFill.UPDATE)
    private Long updateId;

    @TableField(value = "update_name", fill = FieldFill.UPDATE)
    private String updateName;

    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(value = "deleted_by")
    private Long deletedBy;

    @TableField(value = "deleted_time")
    private LocalDateTime deletedTime;
}