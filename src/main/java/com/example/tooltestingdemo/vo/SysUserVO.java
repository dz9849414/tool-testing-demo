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
    private String realName;
    private String organizationId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
}