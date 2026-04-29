package com.example.tooltestingdemo.entity.protocol;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.tooltestingdemo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 协议文件导入导出记录表 实体类
 * 表名：pdm_tool_protocol_file_export
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pdm_tool_protocol_file_export")
public class ProtocolFileImportExport extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作类型：EXPORT/IMPORT
     */
    private String operationType;

    /**
     * 协议配置ID集合（逗号分隔）
     */
    private String protocolConfigIds;

    /**
     * 文件名称（图片：文件名称）
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件格式（图片：文件格式，如 Excel/PDF）
     */
    private String fileFormat;

    /**
     * 状态：0-处理中，1-成功，2-部分成功，3-失败
     */
    private Integer status;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 错误信息/失败原因
     */
    private String errorMessage;

    /**
     * 开始时间（图片：开始时间）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（图片：结束时间）
     */
    private LocalDateTime endTime;
}

