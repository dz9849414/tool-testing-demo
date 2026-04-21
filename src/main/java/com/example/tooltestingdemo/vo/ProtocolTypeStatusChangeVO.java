package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 协议类型状态切换结果
 */
@Data
public class ProtocolTypeStatusChangeVO {

    private Long id;

    private String protocolName;

    private Integer currentStatus;

    private Integer targetStatus;

    private Boolean statusChanged;

    private Boolean requiresConfirm;

    private String message;

    private Long relatedProjectCount;

    private Long relatedTemplateCount;

    private String relationImpactScope;
}