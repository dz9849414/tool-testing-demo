package com.example.tooltestingdemo.dto.cad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CadMockInterfaceCreateDTO {
    private Long id;
    @NotBlank(message = "模拟接口名称不能为空")
    private String interfaceName;
    @NotBlank(message = "CAD类型不能为空")
    private String cadType;
    @NotBlank(message = "转换类型不能为空")
    private String convertType;
    @NotBlank(message = "适用流程不能为空")
    private String applyFlow;
    @NotBlank(message = "请求方式不能为空")
    private String requestMethod;
    @NotBlank(message = "接口地址不能为空")
    private String interfaceUrl;
    private String requestHeaders;
    private String requestParams;
    private String requestBodyTemplate;
    @NotBlank(message = "模拟响应内容不能为空")
    private String responseBody;
    private String successField;
    private String successValue;
    private String authType;
    private String authConfig;
    @NotNull(message = "状态不能为空")
    private Integer status;
    private String versionNo;
    private Long ownerId;
    private String ownerName;
    private String remark;
}
