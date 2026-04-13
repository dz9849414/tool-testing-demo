package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 根据角色名称查找角色
     */
    @Select("SELECT * FROM sys_role WHERE name = #{name}")
    SysRole selectByName(@Param("name") String name);
    
    /**
     * 根据角色类型查找角色列表
     */
    @Select("SELECT * FROM sys_role WHERE type = #{type}")
    List<SysRole> selectByType(@Param("type") String type);
    
    /**
     * 根据作用域ID查找角色列表
     */
    @Select("SELECT * FROM sys_role WHERE scope_id = #{scopeId}")
    List<SysRole> selectByScopeId(@Param("scopeId") String scopeId);
    
    /**
     * 根据角色名称和作用域ID查找角色列表
     */
    @Select({"<script>",
            "SELECT * FROM sys_role WHERE name = #{name}",
            "<if test='scopeId != null'>",
            "AND scope_id = #{scopeId}",
            "</if>",
            "<if test='scopeId == null'>",
            "AND scope_id IS NULL",
            "</if>",
            "</script>"})
    List<SysRole> selectByNameAndScopeId(@Param("name") String name, @Param("scopeId") String scopeId);
    
    /**
     * 根据用户ID查找角色列表
     */
    @Select("SELECT r.* FROM sys_role r JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId} AND r.status = 1")
    List<SysRole> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据角色状态查找角色列表
     */
    @Select("SELECT * FROM sys_role WHERE status = #{status}")
    List<SysRole> selectByStatus(@Param("status") Integer status);
}