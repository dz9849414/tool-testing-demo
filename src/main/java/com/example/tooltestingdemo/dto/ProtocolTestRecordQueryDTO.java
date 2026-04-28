package com.example.tooltestingdemo.dto;

import com.example.tooltestingdemo.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 协议测试记录分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "协议测试记录分页查询参数")
public class ProtocolTestRecordQueryDTO extends PageQuery {

    @Schema(description = "协议类型ID", example = "1")
    private Long protocolId;

    @Schema(description = "协议配置ID", example = "10")
    private Long configId;

    @Schema(description = "测试类型：CONNECT/TRANSFER/COMPREHENSIVE", example = "TRANSFER")
    private String testType;

    @Schema(description = "测试场景：NETWORK/AUTH/PROTOCOL", example = "PROTOCOL")
    private String testScenario;

    @Schema(description = "结果状态：SUCCESS/FAILED", example = "SUCCESS")
    private String resultStatus;

    @Schema(description = "创建开始时间（yyyy-MM-dd HH:mm:ss）", example = "2026-04-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @Schema(description = "创建结束时间（yyyy-MM-dd HH:mm:ss）", example = "2026-04-30 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;
}

