package com.example.tooltestingdemo.dto;

import lombok.Data;

import java.util.List;

/**
 * 模板分页查询参数。
 */
@Data
public class TemplatePageQueryDTO {
    private Long current = 1L;
    private Long size = 10L;
    private Long folderId;
    private String keyword;
    private String name;
    private String extField2;
    private String extField3;
    private String pdmSystemType;
    private Long protocolId;
    private String protocolType;
    private List<Integer> status;
    private Long extNum1;
}
