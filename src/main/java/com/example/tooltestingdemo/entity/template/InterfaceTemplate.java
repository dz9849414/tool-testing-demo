package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口模板主表实体类
 */
@Data
@TableName("interface_template")
public class InterfaceTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "folder_id")
    private Long folderId;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "protocol_type")
    private String protocolType;

    @TableField(value = "method")
    private String method;

    @TableField(value = "base_url")
    private String baseUrl;

    @TableField(value = "path")
    private String path;

    @TableField(value = "full_url")
    private String fullUrl;

    @TableField(value = "auth_type")
    private String authType;

    @TableField(value = "auth_config")
    private String authConfig;

    @TableField(value = "content_type")
    private String contentType;

    @TableField(value = "charset")
    private String charset;

    @TableField(value = "body_type")
    private String bodyType;

    @TableField(value = "body_content")
    private String bodyContent;

    @TableField(value = "body_raw_type")
    private String bodyRawType;

    @TableField(value = "connect_timeout")
    private Integer connectTimeout;

    @TableField(value = "read_timeout")
    private Integer readTimeout;

    @TableField(value = "retry_count")
    private Integer retryCount;

    @TableField(value = "retry_interval")
    private Integer retryInterval;

    @TableField(value = "version")
    private String version;

    @TableField(value = "version_remark")
    private String versionRemark;

    @TableField(value = "is_latest")
    private Integer isLatest;

    @TableField(value = "ref_template_id")
    private Long refTemplateId;

    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    @TableField(value = "create_name", fill = FieldFill.INSERT)
    private String createName;

    @TableField(value = "team_id")
    private Long teamId;

    @TableField(value = "visibility")
    private Integer visibility;

    @TableField(value = "tags")
    private String tags;

    @TableField(value = "pdm_system_type")
    private String pdmSystemType;

    @TableField(value = "pdm_module")
    private String pdmModule;

    @TableField(value = "business_scene")
    private String businessScene;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "use_count")
    private Integer useCount;

    @TableField(value = "last_use_time")
    private LocalDateTime lastUseTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "delete_time")
    private LocalDateTime deleteTime;
}
