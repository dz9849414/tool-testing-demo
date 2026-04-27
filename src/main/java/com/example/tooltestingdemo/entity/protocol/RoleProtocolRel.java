package com.example.tooltestingdemo.entity.protocol;

import lombok.Data;

/**
 * 角色-协议关联实体
 */
@Data
public class RoleProtocolRel {
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 协议代码
     */
    private String protocolCode;
}