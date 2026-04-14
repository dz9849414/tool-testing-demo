package com.example.tooltestingdemo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.SysConfig;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 新增系统配置
     */
    boolean saveConfig(SysConfig config);

    /**
     * 更新系统配置
     */
    boolean updateConfig(SysConfig config);

    /**
     * 删除系统配置
     */
    boolean deleteConfig(String id);

    /**
     * 分页查询系统配置
     */
    Page<SysConfig> getConfigsByPage(Page<SysConfig> page, String configKey, String status);

    /**
     * 根据配置键查询配置
     */
    SysConfig getConfigByKey(String configKey);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey, String excludeId);

    /**
     * 获取所有系统配置
     */
    List<SysConfig> getAllConfigs();

    /**
     * 检查是否为内置配置
     */
    boolean isBuiltInConfig(String configKey);
    
    /**
     * 检查是否为内置配置（通过ID）
     */
    boolean isBuiltInConfigById(String id);
}