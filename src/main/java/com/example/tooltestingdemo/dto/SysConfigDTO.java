package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 系统配置DTO
 */
@Data
public class SysConfigDTO {
    private String configKey;
    private String configValue;
    private String configName;
    private String description;
    private String type;
    private Integer isEncrypted;
    private Integer status;
    private String updateUser;
    private Boolean isBuiltIn;
}