package com.example.tooltestingdemo.entity.cad;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pdm_tool_cad_file_convert_task")
public class CadFileConvertTask extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private Long fileConvertConfigId;
    private String cadType;
    private String applyFlow;
    private Long sourceFileId;
    private String sourceFileName;
    private String sourceFormat;
    private Long targetFileId;
    private String targetFileName;
    private String targetFormat;
    private Integer taskStatus;
    private String requestContent;
    private String responseContent;
    private String errorMessage;
    private Long costTimeMs;
}
