package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class AuthLoginDTO {
    private String username;
    private String password;
}