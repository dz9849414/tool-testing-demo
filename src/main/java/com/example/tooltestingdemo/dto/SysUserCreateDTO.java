package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 用户创建DTO
 */
@Data
public class SysUserCreateDTO {
    private String username;
    private String password;
    private String email;
    private String realName;
    private String organizationId;
    private Integer status;
}