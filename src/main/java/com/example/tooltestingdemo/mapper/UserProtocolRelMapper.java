package com.example.tooltestingdemo.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 用户协议关联映射器
 */
@Mapper
public interface UserProtocolRelMapper {
    
    /**
     * 根据用户ID查询允许的协议代码列表
     */
    @Select("SELECT protocol_code FROM pdm_tool_user_protocol_rel WHERE user_id = #{userId} AND status = 1")
    List<String> selectProtocolByUserId(Long userId);
    
    /**
     * 查询所有启用的用户协议权限
     */
    @Select("SELECT user_id, protocol_code FROM pdm_tool_user_protocol_rel WHERE status = 1")
    List<Map<String, Object>> selectAllEnabledUserProtocols();
    
    /**
     * 查询用户允许的协议代码（包括角色分配的协议）
     */
    @Select("SELECT DISTINCT p.protocol_code " +
            "FROM pdm_tool_user_protocol_rel p " +
            "WHERE p.user_id = #{userId} AND p.status = 1 " +
            "UNION " +
            "SELECT DISTINCT r.protocol_code " +
            "FROM pdm_tool_role_protocol_rel r " +
            "JOIN pdm_tool_sys_user u ON u.id = #{userId} AND u.role_id = r.role_id " +
            "WHERE r.status = 1")
    List<String> selectUserAllowedProtocols(Long userId);
    
    /**
     * 检查用户协议关联是否存在
     */
    @Select("SELECT COUNT(*) FROM pdm_tool_user_protocol_rel WHERE user_id = #{userId} AND protocol_code = #{protocolCode}")
    boolean existsByUserIdAndProtocolCode(@Param("userId") Long userId, @Param("protocolCode") String protocolCode);
    
    /**
     * 插入用户协议关联记录
     */
    @Insert("INSERT INTO pdm_tool_user_protocol_rel (user_id, protocol_code, description, status, create_time, update_time) " +
            "VALUES (#{userId}, #{protocolCode}, #{description}, #{status}, #{createTime}, #{updateTime})")
    int insertUserProtocolRel(@Param("userId") Long userId, @Param("protocolCode") String protocolCode, 
                             @Param("description") String description, @Param("status") Integer status,
                             @Param("createTime") java.time.LocalDateTime createTime, @Param("updateTime") java.time.LocalDateTime updateTime);
    
    /**
     * 删除用户协议关联
     */
    @Delete("DELETE FROM pdm_tool_user_protocol_rel WHERE user_id = #{userId} AND protocol_code = #{protocolCode}")
    int deleteByUserIdAndProtocolCode(@Param("userId") Long userId, @Param("protocolCode") String protocolCode);
}