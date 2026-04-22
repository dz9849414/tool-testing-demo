package com.example.tooltestingdemo.mapper.protocol;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 协议类型主表 Mapper 接口
 * </p>
 *
 * @author wanggang
 * @since 2026-04-11
 */
public interface ProtocolTypeMapper extends BaseMapper<ProtocolType> {

    @Select("""
            SELECT COUNT(*)
            FROM pdm_tool_protocol_project
            WHERE protocol_id = #{protocolId}
              AND (is_deleted = 0 OR is_deleted IS NULL)
            """)
    Long countRelatedProjects(@Param("protocolId") Long protocolId);

    @Select("""
            SELECT COUNT(*)
            FROM pdm_tool_interface_template
            WHERE protocol_id = #{protocolId}
              AND (is_deleted = 0 OR is_deleted IS NULL)
            """)
    Long countRelatedTemplates(@Param("protocolId") Long protocolId);

}
