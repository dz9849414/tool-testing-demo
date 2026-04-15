package com.example.tooltestingdemo.vo;

import lombok.Data;
import java.util.Date;

/**
 * 认证响应VO
 */
@Data
public class AuthVO {
    private String token;
    private String username;
    private Date expiresIn;
}