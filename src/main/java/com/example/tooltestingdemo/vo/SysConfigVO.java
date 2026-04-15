package com.example.tooltestingdemo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置VO
 */
@Data
public class SysConfigVO {
    private String id;
    private String configKey;
    private String configValue;
    private String configName;
    private String description;
    private String type;
    private Integer isEncrypted;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String updateUser;
    private Boolean isBuiltIn;
}