package com.example.tooltestingdemo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜单VO
 */
@Data
public class SysMenuVO {
    private String id;
    private String name;
    private String code;
    private String description;
    private String module;
    private String type;
    private String parentId;
    private Integer level;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}