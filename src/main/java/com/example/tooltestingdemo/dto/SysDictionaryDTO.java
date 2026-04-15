package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 数据字典DTO
 */
@Data
public class SysDictionaryDTO {
    private String code;
    private String value;
    private String type;
    private String description;
    private Integer sort;
    private Integer status;
}