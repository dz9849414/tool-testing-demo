package com.example.tooltestingdemo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户查询响应VO
 */
@Data
public class SysUserVO {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private String organizationId;
    private Integer status;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
    private Integer isDeleted;
    private Long deletedBy;
    private LocalDateTime deletedTime;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private String source;
}