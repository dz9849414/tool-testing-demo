package com.example.tooltestingdemo.entity.cad;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pdm_tool_cad_mock_test_log")
public class CadMockTestLog extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long mockInterfaceId;
    private String cadType;
    private String convertType;
    private String requestMethod;
    private String interfaceUrl;
    private String requestContent;
    private String responseContent;
    private String convertedContent;
    private String fileConvertResult;
    private Integer testResult;
    private String errorMessage;
    private Long costTimeMs;
}
