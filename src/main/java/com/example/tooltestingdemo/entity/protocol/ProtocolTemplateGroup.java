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
 * 协议参数模板分组表实体类
 * 表名：protocol_template_group
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "protocol_template_group", autoResultMap = true)
public class ProtocolTemplateGroup extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 参数模板ID
     */
    private Long protocolTemplateId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 模板分组参数配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String paramsConfig;
}

