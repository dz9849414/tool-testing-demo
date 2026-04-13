package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 测试数据样本库
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Data
@TableName("test_data_sample")
public class TestDataSample implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 样本ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 样本名称
     */
    @TableField("sample_name")
    private String sampleName;

    /**
     * 样本描述
     */
    @TableField("sample_description")
    private String sampleDescription;

    /**
     * 样本数据内容
     */
    @TableField("sample_data")
    private String sampleData;

    /**
     * 数据格式：JSON、XML、CSV
     */
    @TableField("data_format")
    private String dataFormat;

    /**
     * 是否公开：0-私有，1-公开
     */
    @TableField("is_public")
    private Integer isPublic;

    /**
     * 创建人
     */
    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-否，1-是
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除人
     */
    @TableField("deleted_by")
    private Long deletedBy;

    /**
     * 删除时间
     */
    @TableField("deleted_time")
    private LocalDateTime deletedTime;
}
