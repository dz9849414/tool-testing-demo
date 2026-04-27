package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM pdm_tool_sys_user WHERE username = #{username} AND is_deleted = 0")
    SysUser selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM pdm_tool_sys_user WHERE email = #{email} AND is_deleted = 0")
    SysUser selectByEmail(@Param("email") String email);
    
    /**
     * 根据状态查找用户列表
     */
    @Select("SELECT * FROM pdm_tool_sys_user WHERE status = #{status} AND is_deleted = 0")
    List<SysUser> selectByStatus(@Param("status") Integer status);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM pdm_tool_sys_user WHERE username = #{username} AND is_deleted = 0")
    Integer countByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM pdm_tool_sys_user WHERE email = #{email} AND is_deleted = 0")
    Integer countByEmail(@Param("email") String email);
    
    /**
     * 根据用户名和状态查找用户
     */
    @Select("SELECT * FROM pdm_tool_sys_user WHERE username = #{username} AND status = #{status} AND is_deleted = 0")
    SysUser selectByUsernameAndStatus(@Param("username") String username, @Param("status") Integer status);
    
    /**
     * 根据用户ID查询角色ID（用于协议权限系统）
     */
    @Select("SELECT role_id FROM pdm_tool_sys_user WHERE id = #{userId} AND is_deleted = 0")
    Long selectRoleIdByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查找用户列表
     */
    @Select("SELECT u.* FROM pdm_tool_sys_user u JOIN pdm_tool_sys_user_role ur ON u.id = ur.user_id WHERE ur.role_id = #{roleId} AND u.is_deleted = 0")
    List<SysUser> selectByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据用户ID查找角色列表
     */
    @Select("SELECT r.id FROM pdm_tool_sys_role r JOIN pdm_tool_sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> selectRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查找权限列表
     */
    @Select("SELECT p.code FROM pdm_tool_sys_permission p JOIN pdm_tool_sys_role_permission rp ON p.id = rp.permission_id JOIN pdm_tool_sys_user_role ur ON rp.role_id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}