package com.example.tooltestingdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.SysLoginLog;

import java.util.List;

/**
 * 登录日志服务接口
 */
public interface SysLoginLogService extends IService<SysLoginLog> {
    
    /**
     * 记录登录日志
     */
    void recordLoginLog(SysLoginLog loginLog);
    
    /**
     * 根据用户名查询登录日志
     */
    List<SysLoginLog> getLoginLogsByUsername(String username);
    
    /**
     * 根据用户ID查询登录日志
     */
    List<SysLoginLog> getLoginLogsByUserId(String userId);
    
    /**
     * 根据登录状态查询登录日志
     */
    List<SysLoginLog> getLoginLogsByStatus(Integer status);
    
    /**
     * 查询最近的登录日志
     */
    List<SysLoginLog> getRecentLoginLogs(Integer limit);
}