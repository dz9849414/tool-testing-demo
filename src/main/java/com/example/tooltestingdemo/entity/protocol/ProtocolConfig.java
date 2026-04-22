package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 协议参数配置表实体类
 * 表名：protocol_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "pdm_tool_protocol_config", autoResultMap = true)
public class ProtocolConfig extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联协议类型ID
     */
    private Long protocolId;

    /**
     * 协议类型名称
     */
    private String protocolName;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * URL配置（支持多个，JSON格式存储）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String urlConfig;

    /**
     * 认证配置（JSON格式，加密存储）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String authConfig;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer timeoutConnect;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer timeoutRead;

    /**
     * 重试次数（0-10）
     */
    private Integer retryCount;

    /**
     * 重试间隔（毫秒）
     */
    private Integer retryInterval;

    /**
     * 重试触发条件：1-链接超时，2-响应超时，3-响应错误码
     */
    private String retryCondition;

    /**
     * 数据格式：JSON/XML/FORM/TEXT/BINARY
     */
    private String dataFormat;

    /**
     * 格式校验配置（如JSON Schema、XSD等）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String formatConfig;

    /**
     * 额外参数（JSON格式存储）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String additionalParams;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 协议参数配置描述（文本说明）
     */
    private String description;
}