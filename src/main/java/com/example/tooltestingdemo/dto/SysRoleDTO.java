package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 角色DTO
 */
@Data
public class SysRoleDTO {
    private String id;
    private String name;
    private String description;
    private String type;
    private Integer status;
    private String scopeId;
}