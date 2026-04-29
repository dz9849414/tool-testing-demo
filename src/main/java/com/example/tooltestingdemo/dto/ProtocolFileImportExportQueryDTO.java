package com.example.tooltestingdemo.dto;

import com.example.tooltestingdemo.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 协议文件导入导出记录分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "协议文件导入导出记录分页查询参数")
public class ProtocolFileImportExportQueryDTO extends PageQuery {

    @Schema(description = "操作类型：EXPORT/IMPORT", example = "EXPORT")
    private String operationType;

    @Schema(description = "协议配置ID（在 protocolConfigIds 中模糊匹配）", example = "1001")
    private Long protocolConfigId;

    @Schema(description = "文件名称（模糊查询）", example = "协议测试记录")
    private String fileName;

    @Schema(description = "文件格式（模糊查询）", example = "xlsx")
    private String fileFormat;

    @Schema(description = "状态：0-处理中，1-成功，2-部分成功，3-失败", example = "1")
    private Integer status;

    @Schema(description = "开始时间-创建任务开始（yyyy-MM-dd HH:mm:ss）", example = "2026-04-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeStart;

    @Schema(description = "开始时间-创建任务结束（yyyy-MM-dd HH:mm:ss）", example = "2026-04-30 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeEnd;

    @Schema(description = "结束时间-结束任务开始（yyyy-MM-dd HH:mm:ss）", example = "2026-04-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTimeStart;

    @Schema(description = "结束时间-结束任务结束（yyyy-MM-dd HH:mm:ss）", example = "2026-04-30 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTimeEnd;

    @Schema(description = "创建时间-开始（yyyy-MM-dd HH:mm:ss）", example = "2026-04-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @Schema(description = "创建时间-结束（yyyy-MM-dd HH:mm:ss）", example = "2026-04-30 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;
}

