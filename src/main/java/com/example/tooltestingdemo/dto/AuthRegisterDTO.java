package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class AuthRegisterDTO {
    private String username;
    private String password;
    private String email;
    private String realName;
    private String organizationId;
    private Integer status = 0; // 0-待激活，1-已启用，默认待激活
}