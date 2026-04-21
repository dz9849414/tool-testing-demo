package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 协议参数模板表实体类
 * 表名：protocol_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("protocol_template")
public class ProtocolTemplate extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板编码（唯一）
     */
    private String templateCode;

    /**
     * 关联的协议分类
     */
    private String protocolCategory;

    /**
     * 参数快照（JSON格式存储完整参数配置）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String paramsSnapshot;

    /**
     * 参数分组配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String paramGroups;

    /**
     * 是否公开模板：0-私有，1-公开
     */
    private Integer isPublic;
}