package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 用户更新DTO
 */
@Data
public class SysUserUpdateDTO {
    private String username;
    private String email;
    private String phone;
    private String realName;
    private String organizationId;
    private String source;
    private String password;
    // 注意：这里不包含status字段，因为不允许修改用户状态
}