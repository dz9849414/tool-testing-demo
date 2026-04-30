package com.example.tooltestingdemo.entity.cad;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pdm_tool_cad_data_convert_mapping")
public class CadDataConvertMapping extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long mockInterfaceId;
    private String cadType;
    private String applyFlow;
    private String convertDirection;
    private String sourceField;
    private String sourceFieldName;
    private String targetModule;
    private String targetField;
    private String targetFieldName;
    private String fieldType;
    private Integer required;
    private String defaultValue;
    private String transformRule;
    private Integer sortNo;
    private Integer status;
}
