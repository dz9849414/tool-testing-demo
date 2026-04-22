package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    
    /**
     * 根据用户ID查找关联列表
     */
    @Select("SELECT * FROM pdm_tool_sys_user_role WHERE user_id = #{userId}")
    List<SysUserRole> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据角色ID查找关联列表
     */
    @Select("SELECT * FROM pdm_tool_sys_user_role WHERE role_id = #{roleId}")
    List<SysUserRole> selectByRoleId(@Param("roleId") String roleId);
    
    /**
     * 根据用户ID和角色ID查找关联
     */
    @Select("SELECT * FROM pdm_tool_sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    SysUserRole selectByUserIdAndRoleId(@Param("userId") String userId, @Param("roleId") String roleId);
    
    /**
     * 检查用户角色关联是否存在
     */
    @Select("SELECT COUNT(*) FROM pdm_tool_sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    Integer countByUserIdAndRoleId(@Param("userId") String userId, @Param("roleId") String roleId);
    
    /**
     * 根据用户ID删除关联
     */
    @Select("DELETE FROM pdm_tool_sys_user_role WHERE user_id = #{userId}")
    Integer deleteByUserId(@Param("userId") String userId);
    
    /**
     * 根据角色ID删除关联
     */
    @Select("DELETE FROM pdm_tool_sys_user_role WHERE role_id = #{roleId}")
    Integer deleteByRoleId(@Param("roleId") String roleId);
}