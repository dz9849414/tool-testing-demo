package com.example.tooltestingdemo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议文件导入导出记录 VO
 */
@Data
@Schema(description = "协议文件导入导出记录返回对象")
public class ProtocolFileImportExportVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "操作类型：EXPORT/IMPORT")
    private String operationType;

    @Schema(description = "操作类型说明")
    private String operationTypeText;

    @Schema(description = "协议配置ID集合（逗号分隔）")
    private String protocolConfigIds;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件格式")
    private String fileFormat;

    @Schema(description = "状态码：0-处理中，1-成功，2-部分成功，3-失败")
    private Integer status;

    @Schema(description = "状态说明")
    private String statusText;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "导出/导入开始时间")
    private LocalDateTime startTime;

    @Schema(description = "导出/导入结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建人ID")
    private Long createId;

    @Schema(description = "创建人名称")
    private String createName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

