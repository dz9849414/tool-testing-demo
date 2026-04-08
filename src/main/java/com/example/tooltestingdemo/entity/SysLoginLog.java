package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 登录日志实体类
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {
    
    /**
     * 日志ID
     */
    private String id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 登录IP
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;
    
    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;
    
    /**
     * 登录状态：0-失败，1-成功
     */
    private Integer status;
    
    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;
    
    /**
     * 登录类型：LOCAL-本地登录，LDAP-LDAP登录，OIDC-OIDC登录
     */
    @TableField("login_type")
    private String loginType;
}