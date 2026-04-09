package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysLoginLog;
import com.example.tooltestingdemo.mapper.SysLoginLogMapper;
import com.example.tooltestingdemo.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * 登录日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {
    
    private final SysLoginLogMapper loginLogMapper;
    
    @Override
    public void recordLoginLog(SysLoginLog loginLog) {
        // 设置日志ID
        if (loginLog.getId() == null) {
            loginLog.setId(UUID.randomUUID().toString().replace("-", "_"));
        }
        
        // 设置登录时间
        if (loginLog.getLoginTime() == null) {
            loginLog.setLoginTime(LocalDateTime.now());
        }
        
        // 设置默认登录类型
        if (loginLog.getLoginType() == null) {
            loginLog.setLoginType("LOCAL");
        }
        
        // 保存登录日志
        save(loginLog);
    }
    
    @Override
    public List<SysLoginLog> getLoginLogsByUsername(String username) {
        return loginLogMapper.selectByUsername(username);
    }
    
    @Override
    public List<SysLoginLog> getLoginLogsByUserId(String userId) {
        return loginLogMapper.selectByUserId(userId);
    }
    
    @Override
    public List<SysLoginLog> getLoginLogsByStatus(Integer status) {
        return loginLogMapper.selectByStatus(status);
    }
    
    @Override
    public List<SysLoginLog> getRecentLoginLogs(Integer limit) {
        return loginLogMapper.selectRecent(limit);
    }
}