package com.example.tooltestingdemo.dto;

import com.example.tooltestingdemo.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 协议配置分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProtocolConfigQueryDTO extends PageQuery {

    /**
     * 关联协议类型 ID
     */
    private Long protocolId;

    /**
     * 配置名称（模糊查询）
     */
    private String configName;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
