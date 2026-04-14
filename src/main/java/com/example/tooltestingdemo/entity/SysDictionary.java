package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典实体类
 */
@Data
@TableName("sys_dict")
public class SysDictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 字典类型
     */
    @TableField("dict_type")
    private String type;

    /**
     * 字典键
     */
    @TableField("dict_code")
    private String code;

    /**
     * 字典值
     */
    @TableField("dict_value")
    private String value;

    /**
     * 字典描述
     */
    @TableField("description")
    private String description;

    /**
     * 排序号
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}