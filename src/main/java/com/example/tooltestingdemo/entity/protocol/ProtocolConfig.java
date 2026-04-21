package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 协议参数配置表实体类
 * 表名：protocol_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("protocol_config")
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
     * 配置名称
     */
    private String configName;

    /**
     * 访问URL（支持多个，JSON格式存储）
     */
    private String url;

    /**
     * 端口号（1-65535）
     */
    private Integer port;

    /**
     * 认证方式：NONE-无认证, BASIC-基础认证, TOKEN-Token认证, OAUTH2-OAuth2, CERT-证书认证
     */
    private String authType;

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

    // 枚举类定义
    @Getter
    public enum AuthType {
        NONE("无认证"),
        BASIC("基础认证"),
        TOKEN("Token认证"),
        OAUTH2("OAuth2"),
        CERT("证书认证");

        private final String description;

        AuthType(String description) {
            this.description = description;
        }

    }

    @Getter
    public enum DataFormat {
        JSON("JSON"),
        XML("XML"),
        FORM("Form Data"),
        TEXT("Text"),
        BINARY("Binary");

        private final String description;

        DataFormat(String description) {
            this.description = description;
        }

    }
}