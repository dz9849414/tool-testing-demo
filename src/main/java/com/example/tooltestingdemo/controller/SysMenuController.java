package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.entity.SysMenu;
import com.example.tooltestingdemo.service.SysMenuService;
import com.example.tooltestingdemo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/api/menus")
@Slf4j
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    /**
     * 获取当前用户的菜单树（前端导航菜单使用）
     */
    @GetMapping("/current")
    public Result<List<Map<String, Object>>> getCurrentUserMenus() {
        try {
            List<Map<String, Object>> menuTree = menuService.getCurrentUserMenuTree();
            return Result.success("获取菜单成功", menuTree);
        } catch (Exception e) {
            log.error("获取用户菜单失败", e);
            return Result.error("获取菜单失败");
        }
    }

    /**
     * 获取所有菜单树（管理员使用）
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> getAllMenuTree() {
        try {
            List<Map<String, Object>> menuTree = menuService.getAllMenuTree();
            return Result.success("获取菜单树成功", menuTree);
        } catch (Exception e) {
            log.error("获取菜单树失败", e);
            return Result.error("获取菜单树失败");
        }
    }

    /**
     * 分页获取菜单列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SysMenu>> getMenusByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        
        Page<SysMenu> pageParam = new Page<>(page, size);
        QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        if (module != null && !module.isEmpty()) {
            queryWrapper.eq("module", module);
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq("type", type);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByAsc("sort");
        
        Page<SysMenu> menuPage = menuService.page(pageParam, queryWrapper);
        return Result.success("获取菜单列表成功", menuPage);
    }

    /**
     * 根据ID获取菜单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysMenu> getMenuById(@PathVariable String id) {
        SysMenu menu = menuService.getById(id);
        if (menu == null) {
            return Result.error("菜单不存在");
        }
        return Result.success("获取菜单成功", menu);
    }

    /**
     * 新增菜单
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> createMenu(@RequestBody SysMenu menu) {
        try {
            // 检查权限编码是否重复
            boolean exists = menuService.checkCodeUnique(menu.getCode(), null);
            if (exists) {
                return Result.error("权限编码已存在");
            }
            
            boolean saved = menuService.save(menu);
            if (saved) {
                return Result.success("新增菜单成功");
            } else {
                return Result.error("新增菜单失败");
            }
        } catch (Exception e) {
            log.error("新增菜单失败", e);
            return Result.error("新增菜单失败");
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateMenu(@PathVariable String id, @RequestBody SysMenu menu) {
        try {
            SysMenu existingMenu = menuService.getById(id);
            if (existingMenu == null) {
                return Result.error("菜单不存在");
            }
            
            // 检查权限编码是否重复（排除当前菜单）
            boolean exists = menuService.checkCodeUnique(menu.getCode(), id);
            if (exists) {
                return Result.error("权限编码已存在");
            }
            
            menu.setId(id);
            boolean updated = menuService.updateById(menu);
            if (updated) {
                return Result.success("更新菜单成功");
            } else {
                return Result.error("更新菜单失败");
            }
        } catch (Exception e) {
            log.error("更新菜单失败", e);
            return Result.error("更新菜单失败");
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteMenu(@PathVariable String id) {
        try {
            // 检查是否有子菜单
            QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", id);
            long childCount = menuService.count(queryWrapper);
            if (childCount > 0) {
                return Result.error("存在子菜单，无法删除");
            }
            
            boolean deleted = menuService.removeById(id);
            if (deleted) {
                return Result.success("删除菜单成功");
            } else {
                return Result.error("删除菜单失败");
            }
        } catch (Exception e) {
            log.error("删除菜单失败", e);
            return Result.error("删除菜单失败");
        }
    }

    /**
     * 批量更新菜单状态
     */
    @PutMapping("/batch/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> batchUpdateMenuStatus(@RequestParam List<String> ids, @RequestParam Integer status) {
        try {
            if (status != 0 && status != 1) {
                return Result.error("状态值必须是0（禁用）或1（启用）");
            }
            
            boolean updated = menuService.batchUpdateStatus(ids, status);
            if (updated) {
                return Result.success(status == 1 ? "菜单批量启用成功" : "菜单批量禁用成功");
            } else {
                return Result.error("批量更新菜单状态失败");
            }
        } catch (Exception e) {
            log.error("批量更新菜单状态失败", e);
            return Result.error("批量更新菜单状态失败");
        }
    }

    /**
     * 获取模块列表（用于前端筛选）
     */
    @GetMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> getModules() {
        try {
            List<String> modules = menuService.getDistinctModules();
            return Result.success("获取模块列表成功", modules);
        } catch (Exception e) {
            log.error("获取模块列表失败", e);
            return Result.error("获取模块列表失败");
        }
    }
}