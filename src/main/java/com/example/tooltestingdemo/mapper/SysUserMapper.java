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
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email}")
    SysUser selectByEmail(@Param("email") String email);
    
    /**
     * 根据状态查找用户列表
     */
    @Select("SELECT * FROM sys_user WHERE status = #{status}")
    List<SysUser> selectByStatus(@Param("status") Integer status);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    Integer countByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE email = #{email}")
    Integer countByEmail(@Param("email") String email);
    
    /**
     * 根据用户名和状态查找用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND status = #{status}")
    SysUser selectByUsernameAndStatus(@Param("username") String username, @Param("status") Integer status);
    
    /**
     * 根据角色ID查找用户列表
     */
    @Select("SELECT u.* FROM sys_user u JOIN sys_user_role ur ON u.id = ur.user_id WHERE ur.role_id = #{roleId}")
    List<SysUser> selectByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据用户ID查找角色列表
     */
    @Select("SELECT r.id FROM sys_role r JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> selectRolesByUserId(@Param("userId") String userId);
    
    /**
     * 根据用户ID查找权限列表
     */
    @Select("SELECT p.code FROM sys_permission p JOIN sys_role_permission rp ON p.id = rp.permission_id JOIN sys_user_role ur ON rp.role_id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> selectPermissionsByUserId(@Param("userId") String userId);
}