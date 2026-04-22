package com.example.tooltestingdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 协议配置编辑请求体。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolConfigModifyDTO {

    @NotNull(message = "id不能为空")
    private Long id;

    private Long protocolId;

    private String configName;

    @Valid
    private List<ProtocolConfigCreateDTO.UrlConfigItemDTO> urlConfigList;

    @Valid
    private List<ProtocolConfigCreateDTO.AuthConfigItemDTO> authConfigList;

    @Min(value = 1, message = "连接超时时间必须大于0")
    private Integer timeoutConnect;

    @Min(value = 1, message = "读取超时时间必须大于0")
    private Integer timeoutRead;

    @Min(value = 0, message = "重试次数不能小于0")
    @Max(value = 10, message = "重试次数不能大于10")
    private Integer retryCount;

    @Min(value = 0, message = "重试间隔时间不能小于0")
    private Integer retryInterval;

    private String retryCondition;

    private String dataFormat;

    private String formatConfig;

    private String additionalParams;

    private Integer status;

    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
}
