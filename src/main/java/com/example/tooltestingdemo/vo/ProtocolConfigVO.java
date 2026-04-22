package com.example.tooltestingdemo.vo;

import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 协议配置返回对象。
 */
@Data
public class ProtocolConfigVO {
    private Long id;
    private Long protocolId;
    private String protocolName;
    private String configName;
    private List<ProtocolConfigCreateDTO.UrlConfigItemDTO> urlConfigList;
    private List<ProtocolConfigCreateDTO.AuthConfigItemDTO> authConfigList;
    private Integer timeoutConnect;
    private Integer timeoutRead;
    private Integer retryCount;
    private Integer retryInterval;
    private String retryCondition;
    private String dataFormat;
    private String formatConfig;
    private String additionalParams;
    private Integer status;
    private String description;
    private Long createId;
    private LocalDateTime createTime;
    private Long updateId;
    private LocalDateTime updateTime;
}
