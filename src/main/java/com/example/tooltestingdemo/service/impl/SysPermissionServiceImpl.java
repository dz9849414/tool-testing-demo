package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.mapper.SysPermissionMapper;
import com.example.tooltestingdemo.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {
    
    private final SysPermissionMapper permissionMapper;
    
    @Override
    public List<SysPermission> getAllPermissions(Integer moduleType) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据moduleType参数进行筛选
        if (moduleType != null) {
            if (moduleType == 2) {
                // 传2：只查协议模块的权限
                queryWrapper.eq(SysPermission::getModule, "protocol");
            }
            // 其他值暂时不处理，按默认逻辑
        } else {
            // 不传值：查除了协议模块的范围
            queryWrapper.ne(SysPermission::getModule, "protocol");
        }
        
        queryWrapper.orderByAsc(SysPermission::getModule)
                   .orderByAsc(SysPermission::getSort);
        return permissionMapper.selectList(queryWrapper);
    }
    
    @Override
    public Page<SysPermission> getPermissionsByPage(Page<SysPermission> page, String name, String code, String module, String type) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        
        // 名称模糊查询
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(SysPermission::getName, name.trim());
        }
        
        // 权限代码模糊查询
        if (code != null && !code.trim().isEmpty()) {
            queryWrapper.like(SysPermission::getCode, code.trim());
        }
        
        // 模块精确查询
        if (module != null && !module.trim().isEmpty()) {
            queryWrapper.eq(SysPermission::getModule, module.trim());
        }
        
        // 类型精确查询
        if (type != null && !type.trim().isEmpty()) {
            queryWrapper.eq(SysPermission::getType, type.trim());
        }
        
        // 按模块和排序字段排序
        queryWrapper.orderByAsc(SysPermission::getModule)
                   .orderByAsc(SysPermission::getSort);
        
        return permissionMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public SysPermission getPermissionById(String id) {
        return permissionMapper.selectById(id);
    }
    
    @Override
    public List<SysPermission> getPermissionsByModule(String module) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getModule, module)
                   .orderByAsc(SysPermission::getSort);
        return permissionMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<SysPermission> getPermissionsByType(String type) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getType, type)
                   .orderByAsc(SysPermission::getModule)
                   .orderByAsc(SysPermission::getSort);
        return permissionMapper.selectList(queryWrapper);
    }
    
    @Override
    public SysPermission getPermissionByCode(String code) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getCode, code);
        return permissionMapper.selectOne(queryWrapper);
    }
    
    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getCode, code);
        return permissionMapper.selectCount(queryWrapper) > 0;
    }
    
    @Override
    public boolean existsByCode(String code, String excludeId) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getCode, code)
                   .ne(SysPermission::getId, excludeId);
        return permissionMapper.selectCount(queryWrapper) > 0;
    }
}