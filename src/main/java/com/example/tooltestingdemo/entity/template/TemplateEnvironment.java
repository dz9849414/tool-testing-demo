package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板环境配置实体类
 */
@Data
@TableName("pdm_tool_template_environment")
public class TemplateEnvironment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "env_name")
    private String envName;

    @TableField(value = "env_code")
    private String envCode;

    @TableField(value = "base_url")
    private String baseUrl;

    @TableField(value = "headers")
    private String headers;

    @TableField(value = "variables")
    private String variables;

    @TableField(value = "auth_type")
    private String authType;

    @TableField(value = "auth_config")
    private String authConfig;

    @TableField(value = "proxy_enabled")
    private Integer proxyEnabled;

    @TableField(value = "proxy_host")
    private String proxyHost;

    @TableField(value = "proxy_port")
    private Integer proxyPort;

    @TableField(value = "proxy_username")
    private String proxyUsername;

    @TableField(value = "proxy_password")
    private String proxyPassword;

    @TableField(value = "is_default")
    private Integer isDefault;

    @TableField(value = "description")
    private String description;

    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    @TableField(value = "create_name", fill = FieldFill.INSERT)
    private String createName;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_id", fill = FieldFill.UPDATE)
    private Long updateId;

    @TableField(value = "update_name", fill = FieldFill.UPDATE)
    private String updateName;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(value = "deleted_by")
    private Long deletedBy;

    @TableField(value = "deleted_time")
    private LocalDateTime deletedTime;
}