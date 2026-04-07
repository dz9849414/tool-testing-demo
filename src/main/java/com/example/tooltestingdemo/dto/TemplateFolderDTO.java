package com.example.tooltestingdemo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板分类/文件夹 DTO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/TemplateFolderDTO.java
 */
@Data
public class TemplateFolderDTO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 父分类ID，0表示根分类
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色标识
     */
    private String color;

    /**
     * 创建人ID
     */
    private Long ownerId;

    /**
     * 创建人姓名
     */
    private String ownerName;

    /**
     * 所属团队ID
     */
    private Long teamId;

    /**
     * 可见性：1-私有 2-团队 3-公开
     */
    private Integer visibility;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 子文件夹数量
     */
    private Integer childrenCount;

    /**
     * 模板数量
     */
    private Integer templateCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
