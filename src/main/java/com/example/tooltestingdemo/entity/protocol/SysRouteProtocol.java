package com.example.tooltestingdemo.entity.protocol;

import lombok.Data;

/**
 * 路由URL绑定协议实体
 */
@Data
public class SysRouteProtocol {
    /**
     * 请求URI：/api/test/rtsp
     */
    private String requestUri;
    
    /**
     * 协议代码：RTSP
     */
    private String protocolCode;
}