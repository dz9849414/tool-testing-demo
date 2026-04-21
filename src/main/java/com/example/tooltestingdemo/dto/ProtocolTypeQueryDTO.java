package com.example.tooltestingdemo.dto;

import com.example.tooltestingdemo.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 协议类型查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProtocolTypeQueryDTO extends PageQuery {

    private String protocolCode;

    private String protocolName;

    private String protocolCategory;

    private String systemType;

    private Integer status;
}
