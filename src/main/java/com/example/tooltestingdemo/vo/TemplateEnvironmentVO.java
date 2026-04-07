package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板环境配置 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateEnvironmentVO.java
 */
@Data
public class TemplateEnvironmentVO {

    private Long id;

    private Long templateId;

    /**
     * 环境名称
     */
    private String envName;

    /**
     * 环境代码
     */
    private String envCode;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 环境特定的Header
     */
    private String headers;

    /**
     * 环境特定的变量
     */
    private String variables;

    /**
     * 认证类型
     */
    private String authType;

    /**
     * 认证配置
     */
    private String authConfig;

    /**
     * 是否启用代理
     */
    private Integer proxyEnabled;

    /**
     * 代理主机
     */
    private String proxyHost;

    /**
     * 代理端口
     */
    private Integer proxyPort;

    /**
     * 代理用户名
     */
    private String proxyUsername;

    /**
     * 代理密码
     */
    private String proxyPassword;

    /**
     * 是否为默认环境
     */
    private Integer isDefault;

    /**
     * 描述
     */
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
