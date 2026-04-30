package com.example.tooltestingdemo.entity.cad;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pdm_tool_cad_file_convert_config")
public class CadFileConvertConfig extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long mockInterfaceId;
    private String cadType;
    private String applyFlow;
    private String sourceFormat;
    private String targetFormat;
    private String convertUrl;
    private Integer asyncConvert;
    private Integer timeoutSeconds;
    private Integer generatePreview;
    private Integer keepSourceFile;
    private Integer overwriteTargetFile;
    private String requestTemplate;
    private String responseTemplate;
    private Integer status;
    private String remark;
}
