package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysConfig;
import com.example.tooltestingdemo.mapper.SysConfigMapper;
import com.example.tooltestingdemo.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统配置服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private final SysConfigMapper configMapper;

    @Override
    public boolean saveConfig(SysConfig config) {
        // 检查配置键是否已存在
        if (existsByConfigKey(config.getConfigKey(), null)) {
            return false;
        }
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        config.setCreateTime(now);
        config.setUpdateTime(now);
        return save(config);
    }

    @Override
    public boolean updateConfig(SysConfig config) {
        // 检查配置键是否已存在（排除当前配置）
        if (existsByConfigKey(config.getConfigKey(), config.getId())) {
            return false;
        }
        // 设置更新时间
        config.setUpdateTime(LocalDateTime.now());
        return updateById(config);
    }

    @Override
    public boolean deleteConfig(String id) {
        // 检查是否为内置配置
        SysConfig config = getById(id);
        if (config != null && isBuiltInConfig(config.getConfigKey())) {
            return false;
        }
        return removeById(id);
    }

    @Override
    public Page<SysConfig> getConfigsByPage(Page<SysConfig> page, String configKey, String status) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();

        if (configKey != null && !configKey.isEmpty()) {
            queryWrapper.like(SysConfig::getConfigKey, configKey);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(SysConfig::getStatus, Integer.parseInt(status));
        }

        queryWrapper.orderByAsc(SysConfig::getConfigKey);

        return page(page, queryWrapper);
    }

    @Override
    public SysConfig getConfigByKey(String configKey) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, configKey);
        return getOne(queryWrapper);
    }

    @Override
    public boolean existsByConfigKey(String configKey, String excludeId) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, configKey);
        if (excludeId != null) {
            queryWrapper.ne(SysConfig::getId, excludeId);
        }
        return count(queryWrapper) > 0;
    }

    @Override
    public List<SysConfig> getAllConfigs() {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysConfig::getConfigKey);
        return list(queryWrapper);
    }

    @Override
    public boolean isBuiltInConfig(String configKey) {
        SysConfig config = getConfigByKey(configKey);
        return config != null && config.getIsBuiltIn() != null && config.getIsBuiltIn() == 1;
    }
    
    /**
     * 检查是否为内置配置（通过ID）
     */
    public boolean isBuiltInConfigById(String id) {
        SysConfig config = getById(id);
        return config != null && config.getIsBuiltIn() != null && config.getIsBuiltIn() == 1;
    }
    
    @Override
    public boolean updateBuiltInStatus(String id, boolean isBuiltIn) {
        SysConfig config = getById(id);
        if (config == null) {
            return false;
        }
        
        // 创建更新对象
        SysConfig updateConfig = new SysConfig();
        updateConfig.setId(id);
        updateConfig.setIsBuiltIn(isBuiltIn ? 1 : 0);
        updateConfig.setUpdateTime(LocalDateTime.now());
        
        return updateById(updateConfig);
    }
}