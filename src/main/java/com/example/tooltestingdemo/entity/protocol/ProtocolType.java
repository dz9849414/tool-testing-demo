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
 * 协议类型主表
 * </p>
 *
 * @author wanggang
 * @since 2026-04-11
 */
@Data
@TableName("protocol_type")
public class ProtocolType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 协议类型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 协议类型名称
     */
    @TableField("protocol_name")
    private String protocolName;

    /**
     * 协议标识符
     */
    @TableField("protocol_identifier")
    private String protocolIdentifier;

    /**
     * 适用系统类型（CAD、ERP、PLM等）
     */
    @TableField("applicable_system")
    private String applicableSystem;

    /**
     * 描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

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

    /**
     * 版本号，用于乐观锁
     */
    @TableField("version")
    private Integer version;
}
