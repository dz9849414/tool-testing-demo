package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 登录日志Mapper接口
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {
    
    /**
     * 根据用户名查询登录日志
     */
    @Select("SELECT * FROM pdm_tool_sys_login_log WHERE username = #{username} ORDER BY login_time DESC")
    List<SysLoginLog> selectByUsername(@Param("username") String username);
    
    /**
     * 根据用户ID查询登录日志
     */
    @Select("SELECT * FROM pdm_tool_sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC")
    List<SysLoginLog> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据登录状态查询登录日志
     */
    @Select("SELECT * FROM pdm_tool_sys_login_log WHERE status = #{status} ORDER BY login_time DESC")
    List<SysLoginLog> selectByStatus(@Param("status") Integer status);
    
    /**
     * 查询最近的登录日志
     */
    @Select("SELECT * FROM pdm_tool_sys_login_log ORDER BY login_time DESC LIMIT #{limit}")
    List<SysLoginLog> selectRecent(@Param("limit") Integer limit);
}