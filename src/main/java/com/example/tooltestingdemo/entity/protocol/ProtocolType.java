package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 协议类型主表实体类
 * 表名：protocol_type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("protocol_type")
public class ProtocolType extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 协议编码（唯一标识）
     */
    private String protocolCode;

    /**
     * 协议名称
     */
    private String protocolName;

    /**
     * 协议分类（CAD/ERP/PLM/数据交换/接口协议）
     */
    private String protocolCategory;

    /**
     * 适用系统类型（CAD/ERP/PLM等）
     */
    private String systemType;

    /**
     * 协议描述
     */
    private String description;

    /**
     * 状态：PENDING-待启用, ENABLED-已启用, DISABLED-已禁用
     */
    private String status;

    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version;

    // 枚举类定义
    @Getter
    public enum Status {
        PENDING("待启用"),
        ENABLED("已启用"),
        DISABLED("已禁用");

        private final String description;

        Status(String description) {
            this.description = description;
        }

    }
}