package com.example.tooltestingdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.SysMenu;

import java.util.List;
import java.util.Map;

/**
 * 菜单服务接口
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取当前用户的菜单树
     */
    List<Map<String, Object>> getCurrentUserMenuTree();

    /**
     * 获取所有菜单树
     */
    List<Map<String, Object>> getAllMenuTree();

    /**
     * 检查权限编码是否唯一
     */
    boolean checkCodeUnique(String code, String excludeId);

    /**
     * 批量更新菜单状态
     */
    boolean batchUpdateStatus(List<String> ids, Integer status);

    /**
     * 获取不重复的模块列表
     */
    List<String> getDistinctModules();
}