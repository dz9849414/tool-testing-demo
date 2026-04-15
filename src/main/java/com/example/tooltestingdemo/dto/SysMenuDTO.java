package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 菜单DTO
 */
@Data
public class SysMenuDTO {
    private String name;
    private String code;
    private String description;
    private String module;
    private String type;
    private String parentId;
    private Integer level;
    private Integer sort;
    private Integer status;
}