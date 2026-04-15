package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 协议类型导出行
 */
@Data
public class ProtocolTypeExportVO {

    /**
     * 类型编码
     */
    private String protocolIdentifier;

    /**
     * 名称
     */
    private String protocolName;

    /**
     * 分类（当前按适用系统导出）
     */
    private String classification;

    /**
     * 状态文本
     */
    private String status;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 描述
     */
    private String description;
}