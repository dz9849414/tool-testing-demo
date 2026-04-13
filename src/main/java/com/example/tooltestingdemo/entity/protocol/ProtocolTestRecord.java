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
 * 协议测试记录表
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Data
@TableName("protocol_test_record")
public class ProtocolTestRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 测试记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 协议类型ID
     */
    @TableField("protocol_id")
    private Long protocolId;

    /**
     * 测试类型：CONNECT-连接测试，DATA_TRANSFER-数据传输测试
     */
    @TableField("test_type")
    private String testType;

    /**
     * 测试结果：0-失败，1-成功
     */
    @TableField("test_status")
    private Integer testStatus;

    /**
     * 响应时间（毫秒）
     */
    @TableField("response_time")
    private Integer responseTime;

    /**
     * 错误码（如401、500）
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 测试数据样本ID
     */
    @TableField("test_data_sample_id")
    private Long testDataSampleId;

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
