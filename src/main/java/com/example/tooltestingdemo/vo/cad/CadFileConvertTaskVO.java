package com.example.tooltestingdemo.vo.cad;

import lombok.Data;

@Data
public class CadFileConvertTaskVO {
    private String taskNo;
    private Integer taskStatus;
    private Long targetFileId;
    private String targetFileName;
    private String targetFormat;
    private String errorMessage;
}
