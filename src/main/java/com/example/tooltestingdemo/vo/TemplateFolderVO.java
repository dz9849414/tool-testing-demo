package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模板分类/文件夹 VO（返回给前端）
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateFolderVO.java
 */
@Data
public class TemplateFolderVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 父分类ID
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
    private Long createId;

    /**
     * 创建人姓名
     */
    private String createName;

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
     * 子文件夹列表
     */
    private List<TemplateFolderVO> children;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
