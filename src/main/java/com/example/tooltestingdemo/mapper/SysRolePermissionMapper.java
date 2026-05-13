package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
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
    @Select("SELECT * FROM pdm_tool_sys_role_permission WHERE role_id = #{roleId}")
    List<SysRolePermission> selectByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据权限ID查找关联列表
     */
    @Select("SELECT * FROM pdm_tool_sys_role_permission WHERE permission_id = #{permissionId}")
    List<SysRolePermission> selectByPermissionId(@Param("permissionId") String permissionId);
    
    /**
     * 根据角色ID和权限ID查找关联
     */
    @Select("SELECT * FROM pdm_tool_sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    SysRolePermission selectByRoleIdAndPermissionId(@Param("roleId") String roleId, @Param("permissionId") String permissionId);
    
    /**
     * 检查角色权限关联是否存在
     */
    @Select("SELECT COUNT(*) FROM pdm_tool_sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    Integer countByRoleIdAndPermissionId(@Param("roleId") String roleId, @Param("permissionId") String permissionId);
    
    /**
     * 根据角色ID删除关联
     */
    @Delete("DELETE FROM pdm_tool_sys_role_permission WHERE role_id = #{roleId}")
    Integer deleteByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据权限ID删除关联
     */
    @Delete("DELETE FROM pdm_tool_sys_role_permission WHERE permission_id = #{permissionId}")
    Integer deleteByPermissionId(@Param("permissionId") String permissionId);
    
    /**
     * 插入角色权限关联（使用INSERT IGNORE避免重复键冲突）
     */
    @Insert("INSERT IGNORE INTO pdm_tool_sys_role_permission (id, role_id, permission_id, create_time, create_user) VALUES (#{id}, #{roleId}, #{permissionId}, #{createTime}, #{createUser})")
    Integer insertIgnore(SysRolePermission rolePermission);
}