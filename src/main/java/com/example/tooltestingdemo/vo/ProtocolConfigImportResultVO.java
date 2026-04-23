package com.example.tooltestingdemo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议配置导入结果
 */
@Data
@Builder
@Schema(description = "协议配置导入结果")
public class ProtocolConfigImportResultVO {

    @Schema(description = "导入是否成功", example = "true")
    private Boolean success;

    @Schema(description = "导入结果消息")
    private String message;

    @Schema(description = "导入总条数", example = "10")
    private Integer totalCount;

    @Schema(description = "导入成功条数", example = "8")
    private Integer successCount;

    @Schema(description = "导入失败条数", example = "2")
    private Integer failCount;

    @Schema(description = "失败报告ID")
    private String failureReportId;

    @Schema(description = "失败报告下载地址")
    private String failureReportDownloadUrl;

    @Schema(description = "导入时间")
    private LocalDateTime importTime;
}
