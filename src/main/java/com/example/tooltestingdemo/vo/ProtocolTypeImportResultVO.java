package com.example.tooltestingdemo.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议类型导入结果
 */
@Data
@Builder
public class ProtocolTypeImportResultVO {

    private Boolean success;

    private String message;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private Integer skipCount;

    private String strategy;

    private String failureReportId;

    private String failureReportDownloadUrl;

    private LocalDateTime importTime;
}