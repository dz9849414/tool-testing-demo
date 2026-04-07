package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板分类/文件夹实体类
 */
@Data
@TableName("template_folder")
public class TemplateFolder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "parent_id")
    private Long parentId;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "sort_order")
    private Integer sortOrder;

    @TableField(value = "icon")
    private String icon;

    @TableField(value = "color")
    private String color;

    @TableField(value = "owner_id")
    private Long ownerId;

    @TableField(value = "owner_name")
    private String ownerName;

    @TableField(value = "team_id")
    private Long teamId;

    @TableField(value = "visibility")
    private Integer visibility;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "delete_time")
    private LocalDateTime deleteTime;
}
