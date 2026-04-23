package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    
    /**
     * 根据权限编码查找权限
     */
    @Select("SELECT * FROM pdm_tool_sys_permission WHERE code = #{code}")
    SysPermission selectByCode(@Param("code") String code);
    
    /**
     * 根据模块名称查找权限列表
     */
    @Select("SELECT * FROM pdm_tool_sys_permission WHERE module = #{module}")
    List<SysPermission> selectByModule(@Param("module") String module);
    
    /**
     * 根据权限类型查找权限列表
     */
    @Select("SELECT * FROM pdm_tool_sys_permission WHERE type = #{type}")
    List<SysPermission> selectByType(@Param("type") String type);
    
    /**
     * 根据父权限ID查找权限列表
     */
    @Select("SELECT * FROM pdm_tool_sys_permission WHERE parent_id = #{parentId}")
    List<SysPermission> selectByParentId(@Param("parentId") String parentId);
    
    /**
     * 根据权限层级查找权限列表
     */
    @Select("SELECT * FROM pdm_tool_sys_permission WHERE level = #{level}")
    List<SysPermission> selectByLevel(@Param("level") Integer level);
    
    /**
     * 根据角色ID查找权限列表
     */
    @Select("SELECT p.* FROM pdm_tool_sys_permission p JOIN pdm_tool_sys_role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = #{roleId}")
    List<SysPermission> selectByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据用户ID查找权限列表
     */
    @Select("SELECT DISTINCT p.* FROM pdm_tool_sys_permission p " +
           "JOIN pdm_tool_sys_role_permission rp ON p.id = rp.permission_id " +
           "JOIN pdm_tool_sys_user_role ur ON rp.role_id = ur.role_id " +
           "WHERE ur.user_id = #{userId}")
    List<SysPermission> selectByUserId(@Param("userId") String userId);
}