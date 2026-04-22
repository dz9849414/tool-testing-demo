package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 协议-项目关联表实体类
 * 表名：protocol_project
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pdm_tool_protocol_project")
public class ProtocolProject extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 协议类型ID
     */
    private Long protocolId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称（冗余字段，便于查询）
     */
    private String projectName;

    /**
     * 关联生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 关联失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态：ENABLED-启用, DISABLED-禁用
     */
    private String status;
}