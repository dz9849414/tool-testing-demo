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
 * 协议测试参数表
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Data
@TableName("protocol_test_parameter")
public class ProtocolTestParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 测试参数ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 测试记录ID
     */
    @TableField("test_record_id")
    private Long testRecordId;

    /**
     * 参数名称
     */
    @TableField("parameter_name")
    private String parameterName;

    /**
     * 参数值
     */
    @TableField("parameter_value")
    private String parameterValue;

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
