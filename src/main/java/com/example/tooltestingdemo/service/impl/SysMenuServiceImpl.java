package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysMenu;
import com.example.tooltestingdemo.mapper.SysMenuMapper;
import com.example.tooltestingdemo.service.SysMenuService;
import com.example.tooltestingdemo.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
@Slf4j
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysUserService userService;

    @Override
    public List<Map<String, Object>> getCurrentUserMenuTree() {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户的权限列表
        List<String> permissions = userService.getPermissionsByUsername(currentUsername);
        
        // 获取所有启用的菜单
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByAsc("sort");
        List<SysMenu> allMenus = this.list(queryWrapper);
        
        // 过滤用户有权限的菜单
        List<SysMenu> userMenus = allMenus.stream()
                .filter(menu -> {
                    // 一级菜单不需要权限检查
                    if ("0".equals(menu.getParentId())) {
                        return true;
                    }
                    // 检查用户是否有该菜单的权限
                    return menu.getCode() == null || permissions.contains(menu.getCode());
                })
                .collect(Collectors.toList());
        
        // 构建菜单树
        return buildMenuTree(userMenus);
    }

    @Override
    public List<Map<String, Object>> getAllMenuTree() {
        // 获取所有菜单
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        List<SysMenu> allMenus = this.list(queryWrapper);
        
        return buildMenuTree(allMenus);
    }

    /**
     * 构建菜单树结构
     */
    private List<Map<String, Object>> buildMenuTree(List<SysMenu> menus) {
        // 按父ID分组
        Map<String, List<SysMenu>> menuMap = menus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        
        // 获取根菜单
        List<SysMenu> rootMenus = menuMap.getOrDefault("0", new ArrayList<>());
        
        // 构建树结构
        return rootMenus.stream()
                .map(menu -> convertToTreeMap(menu, menuMap))
                .collect(Collectors.toList());
    }

    /**
     * 将菜单转换为树形结构Map
     */
    private Map<String, Object> convertToTreeMap(SysMenu menu, Map<String, List<SysMenu>> menuMap) {
        Map<String, Object> menuNode = new HashMap<>();
        menuNode.put("id", menu.getId());
        menuNode.put("name", menu.getName());
        menuNode.put("code", menu.getCode());
        menuNode.put("description", menu.getDescription());
        menuNode.put("module", menu.getModule());
        menuNode.put("type", menu.getType());
        menuNode.put("level", menu.getLevel());
        menuNode.put("sort", menu.getSort());
        menuNode.put("status", menu.getStatus());
        menuNode.put("createTime", menu.getCreateTime());
        menuNode.put("updateTime", menu.getUpdateTime());
        
        // 如果是菜单类型，添加children字段
        if ("MENU".equals(menu.getType())) {
            List<SysMenu> children = menuMap.getOrDefault(menu.getId(), new ArrayList<>());
            List<Map<String, Object>> childNodes = children.stream()
                    .map(child -> convertToTreeMap(child, menuMap))
                    .collect(Collectors.toList());
            menuNode.put("children", childNodes);
        }
        
        return menuNode;
    }

    @Override
    public boolean checkCodeUnique(String code, String excludeId) {
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        if (excludeId != null && !excludeId.isEmpty()) {
            queryWrapper.ne("id", excludeId);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean batchUpdateStatus(List<String> ids, Integer status) {
        try {
            List<SysMenu> menus = this.listByIds(ids);
            for (SysMenu menu : menus) {
                menu.setStatus(status);
            }
            return this.updateBatchById(menus);
        } catch (Exception e) {
            log.error("批量更新菜单状态失败", e);
            return false;
        }
    }

    @Override
    public List<String> getDistinctModules() {
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT module");
        queryWrapper.isNotNull("module");
        queryWrapper.ne("module", "");
        
        List<SysMenu> menus = this.list(queryWrapper);
        return menus.stream()
                .map(SysMenu::getModule)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}