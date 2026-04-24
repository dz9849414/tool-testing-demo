package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.SysConfig;
import com.example.tooltestingdemo.service.SysConfigService;
import com.example.tooltestingdemo.dto.SysConfigDTO;
import com.example.tooltestingdemo.vo.SysConfigVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    /**
     * 新增系统配置
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:config:update')")
    public Result<?> createConfig(@RequestBody SysConfigDTO configDTO) {
        // 检查配置键是否已存在
        boolean exists = configService.existsByConfigKey(configDTO.getConfigKey(), null);
        if (exists) {
            return Result.error("配置键已存在，请勿重复添加");
        }
        
        SysConfig config = new SysConfig();
        try {
            BeanUtils.copyProperties(config, configDTO);
        } catch (Exception e) {
            return Result.error("参数转换失败");
        }
        
        boolean saved = configService.saveConfig(config);
        if (saved) {
            return Result.success("新增配置成功");
        } else {
            return Result.error("新增配置失败");
        }
    }

    /**
     * 编辑系统配置
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:config:update')")
    public Result<?> updateConfig(@PathVariable String id, @RequestBody SysConfigDTO configDTO) {
        SysConfig config = new SysConfig();
        config.setId(id);
        
        try {
            BeanUtils.copyProperties(config, configDTO);
        } catch (Exception e) {
            return Result.error("参数转换失败");
        }
        
        // 检查是否为内置配置
        if (configService.isBuiltInConfigById(id)) {
            // 内置配置只允许编辑值和描述，不允许修改配置键
            SysConfig existingConfig = configService.getById(id);
            if (existingConfig != null) {
                config.setConfigKey(existingConfig.getConfigKey());
            }
        }
        boolean updated = configService.updateConfig(config);
        if (updated) {
            return Result.success("编辑配置成功");
        } else {
            return Result.error("编辑配置失败");
        }
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:config:update')")
    public Result<?> deleteConfig(@PathVariable String id) {
        // 检查是否为内置配置
        if (configService.isBuiltInConfigById(id)) {
            return Result.error("内置配置不能删除");
        }
        boolean deleted = configService.deleteConfig(id);
        if (deleted) {
            return Result.success("删除配置成功");
        } else {
            return Result.error("删除配置失败");
        }
    }

    /**
     * 分页查询系统配置
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:config:query')")
    public Result<?> getConfigs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String configKey,
            @RequestParam(required = false) String status) {
        Page<SysConfig> pageParam = new Page<>(page, size);
        Page<SysConfig> result = configService.getConfigsByPage(pageParam, configKey, status);
        return Result.success("查询配置成功", result);
    }

    /**
     * 根据ID获取系统配置
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:config:query')")
    public Result<?> getConfigById(@PathVariable String id) {
        SysConfig config = configService.getById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        
        // 转换为VO
        SysConfigVO configVO = new SysConfigVO();
        try {
            BeanUtils.copyProperties(configVO, config);
        } catch (Exception e) {
            return Result.error("数据转换失败");
        }
        configVO.setIsBuiltIn(configService.isBuiltInConfigById(id));
        
        return Result.success("获取配置成功", configVO);
    }

    /**
     * 获取所有系统配置
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:config:query')")
    public Result<?> getAllConfigs() {
        List<SysConfig> configs = configService.getAllConfigs();
        return Result.success("获取所有配置成功", configs);
    }

    /**
     * 检查配置键是否存在
     */
    @GetMapping("/check-key")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:config:query')")
    public Result<?> checkKeyUnique(
            @RequestParam String configKey,
            @RequestParam(required = false) String id) {
        boolean unique = !configService.existsByConfigKey(configKey, id);
        return Result.success("检查配置键成功", Map.of("unique", unique));
    }

    /**
     * 获取配置详情（包含是否内置信息）
     */
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:config:query')")
    public Result<?> getConfigDetail(@PathVariable String id) {
        SysConfig config = configService.getById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        Map<String, Object> detail = new java.util.HashMap<>();
        
        // 添加非空字段到Map中
        addIfNotNull(detail, "id", config.getId());
        addIfNotNull(detail, "configKey", config.getConfigKey());
        addIfNotNull(detail, "configValue", config.getConfigValue());
        addIfNotNull(detail, "configName", config.getConfigName());
        addIfNotNull(detail, "description", config.getDescription());
        addIfNotNull(detail, "type", config.getType());
        addIfNotNull(detail, "isEncrypted", config.getIsEncrypted());
        detail.put("isBuiltIn", configService.isBuiltInConfigById(id)); // 总是添加
        addIfNotNull(detail, "status", config.getStatus());
        addIfNotNull(detail, "createTime", config.getCreateTime());
        addIfNotNull(detail, "updateTime", config.getUpdateTime());
        addIfNotNull(detail, "updateUser", config.getUpdateUser());
        
        return Result.success("获取配置详情成功", detail);
    }
    
    /**
     * 修改配置是否内置状态
     */
    @PutMapping("/{id}/built-in")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateBuiltInStatus(@PathVariable String id, @RequestBody Map<String, Boolean> request) {
        Boolean isBuiltIn = request.get("isBuiltIn");
        if (isBuiltIn == null) {
            return Result.error("参数isBuiltIn不能为空");
        }
        
        // 检查配置是否存在
        SysConfig config = configService.getById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        
        // 更新是否内置状态
        boolean updated = configService.updateBuiltInStatus(id, isBuiltIn);
        if (updated) {
            return Result.success(isBuiltIn ? "配置已设置为内置" : "配置已设置为非内置");
        } else {
            return Result.error("更新内置状态失败");
        }
    }
    
    /**
     * 如果值不为null，则添加到Map中
     */
    private void addIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}