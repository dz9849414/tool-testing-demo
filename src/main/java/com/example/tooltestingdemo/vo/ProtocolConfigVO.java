package com.example.tooltestingdemo.vo;

import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 协议配置返回对象。
 */
@Data
@Schema(description = "协议配置返回对象")
public class ProtocolConfigVO {
    @Schema(description = "主键ID", example = "1")
    private Long id;
    @Schema(description = "协议类型ID", example = "1001")
    private Long protocolId;
    @Schema(description = "协议名称", example = "HTTP")
    private String protocolName;
    @Schema(description = "配置名称", example = "默认配置")
    private String configName;
    @Schema(description = "URL配置列表")
    private List<ProtocolConfigCreateDTO.UrlConfigItemDTO> urlConfigList;
    @Schema(description = "认证配置列表")
    private List<ProtocolConfigCreateDTO.AuthConfigItemDTO> authConfigList;
    @Schema(description = "连接超时（毫秒）", example = "5000")
    private Integer timeoutConnect;
    @Schema(description = "读取超时（毫秒）", example = "30000")
    private Integer timeoutRead;
    @Schema(description = "重试次数", example = "3")
    private Integer retryCount;
    @Schema(description = "重试间隔（毫秒）", example = "1000")
    private Integer retryInterval;
    @Schema(description = "重试条件", example = "5xx")
    private String retryCondition;
    @Schema(description = "数据格式", example = "JSON")
    private String dataFormat;
    @Schema(description = "格式配置")
    private String formatConfig;
    @Schema(description = "附加参数")
    private String additionalParams;
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "创建人ID", example = "1")
    private Long createId;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新人ID", example = "1")
    private Long updateId;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
