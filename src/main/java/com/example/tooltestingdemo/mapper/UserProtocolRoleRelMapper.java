package com.example.tooltestingdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.protocol.UserProtocolRoleRel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户协议角色关联表Mapper
 */
@Mapper
public interface UserProtocolRoleRelMapper extends BaseMapper<UserProtocolRoleRel> {
    
    /**
     * 批量插入用户协议角色关联
     */
    @Insert({"<script>",
            "INSERT INTO pdm_tool_user_protocol_role_rel (user_id, role_id, description, status, create_time, update_time) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.userId}, #{item.roleId}, #{item.description}, #{item.status}, #{item.createTime}, #{item.updateTime})",
            "</foreach>",
            "</script>"})
    int batchInsert(@Param("list") List<UserProtocolRoleRel> list);
    

    /**
     * 根据用户ID删除所有关联
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和角色ID删除关联
     */
    @Delete("DELETE FROM pdm_tool_user_protocol_role_rel WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 根据用户ID查询关联的角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 检查用户角色关联是否存在
     */
    @Select("SELECT COUNT(*) FROM pdm_tool_user_protocol_role_rel WHERE user_id = #{userId} AND role_id = #{roleId}")
    boolean existsByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}