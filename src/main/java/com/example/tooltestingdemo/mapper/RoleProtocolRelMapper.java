package com.example.tooltestingdemo.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 角色协议关联映射器
 */
@Mapper
public interface RoleProtocolRelMapper {
    
    /**
     * 根据角色ID查询允许的协议代码列表
     */
    @Select("SELECT protocol_code FROM pdm_tool_role_protocol_rel WHERE role_id = #{roleId}")
    List<String> selectProtocolByRoleId(Long roleId);
    
    /**
     * 查询所有启用的角色协议权限
     */
    @Select("SELECT role_id, protocol_code FROM pdm_tool_role_protocol_rel WHERE status = 1")
    List<Map<String, Object>> selectAllEnabledRoleProtocols();
    
    /**
     * 检查角色协议关联是否存在
     */
    @Select("SELECT COUNT(*) FROM pdm_tool_role_protocol_rel WHERE role_id = #{roleId} AND protocol_code = #{protocolCode}")
    boolean existsByRoleIdAndProtocolCode(@Param("roleId") Long roleId, @Param("protocolCode") String protocolCode);
    
    /**
     * 插入角色协议关联记录
     */
    @Insert("INSERT INTO pdm_tool_role_protocol_rel (role_id, protocol_code, description, status, create_time, update_time) " +
            "VALUES (#{roleId}, #{protocolCode}, #{description}, #{status}, #{createTime}, #{updateTime})")
    int insertRoleProtocolRel(@Param("roleId") Long roleId, @Param("protocolCode") String protocolCode, 
                             @Param("description") String description, @Param("status") Integer status,
                             @Param("createTime") java.time.LocalDateTime createTime, @Param("updateTime") java.time.LocalDateTime updateTime);
    
    /**
     * 删除角色协议关联
     */
    @Delete("DELETE FROM pdm_tool_role_protocol_rel WHERE role_id = #{roleId} AND protocol_code = #{protocolCode}")
    int deleteByRoleIdAndProtocolCode(@Param("roleId") Long roleId, @Param("protocolCode") String protocolCode);
}