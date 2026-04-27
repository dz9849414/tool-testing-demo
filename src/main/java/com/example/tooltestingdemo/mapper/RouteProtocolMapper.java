package com.example.tooltestingdemo.mapper;

import com.example.tooltestingdemo.entity.protocol.SysRouteProtocol;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 路由协议映射器
 */
@Mapper
public interface RouteProtocolMapper {
    
    /**
     * 根据URI查询绑定的协议
     */
    @Select("SELECT * FROM pdm_tool_sys_route_protocol WHERE request_uri = #{uri}")
    SysRouteProtocol selectProtocolByUri(String uri);
    
    /**
     * 查询所有启用的路由协议绑定
     */
    @Select("SELECT * FROM pdm_tool_sys_route_protocol WHERE status = 1")
    List<SysRouteProtocol> selectAllEnabledRoutes();
}