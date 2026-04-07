package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    
    /**
     * 根据角色ID查找关联列表
     */
    @Select("SELECT * FROM sys_role_permission WHERE role_id = #{roleId}")
    List<SysRolePermission> selectByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据权限ID查找关联列表
     */
    @Select("SELECT * FROM sys_role_permission WHERE permission_id = #{permissionId}")
    List<SysRolePermission> selectByPermissionId(@Param("permissionId") String permissionId);
    
    /**
     * 根据角色ID和权限ID查找关联
     */
    @Select("SELECT * FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    SysRolePermission selectByRoleIdAndPermissionId(@Param("roleId") String roleId, @Param("permissionId") String permissionId);
    
    /**
     * 检查角色权限关联是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int countByRoleIdAndPermissionId(@Param("roleId") String roleId, @Param("permissionId") String permissionId);
    
    /**
     * 根据角色ID删除关联
     */
    @Select("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据权限ID删除关联
     */
    @Select("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") String permissionId);
}