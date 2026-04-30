package com.example.tooltestingdemo.entity.cad;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pdm_tool_cad_mock_interface")
public class CadMockInterface extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String interfaceName;
    private String cadType;
    private String convertType;
    private String applyFlow;
    private String requestMethod;
    private String interfaceUrl;
    private String requestHeaders;
    private String requestParams;
    private String requestBodyTemplate;
    private String responseBody;
    private String successField;
    private String successValue;
    private String authType;
    private String authConfig;
    private Integer status;
    private String versionNo;
    private Long ownerId;
    private String ownerName;
    private String remark;
}
