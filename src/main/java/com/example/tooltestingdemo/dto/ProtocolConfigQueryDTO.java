package com.example.tooltestingdemo.dto;

import com.example.tooltestingdemo.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 协议配置分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "协议配置分页查询参数")
public class ProtocolConfigQueryDTO extends PageQuery {

    /**
     * 关联协议类型 ID
     */
    @Schema(description = "关联协议类型ID", example = "1001")
    private Long protocolId;

    /**
     * 配置名称（模糊查询）
     */
    @Schema(description = "配置名称（模糊查询）", example = "默认配置")
    private String configName;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "创建开始时间（yyyy-MM-dd HH:mm:ss）", example = "2026-04-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @Schema(description = "创建结束时间（yyyy-MM-dd HH:mm:ss）", example = "2026-04-30 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;

    @Schema(description = "修改开始时间（yyyy-MM-dd HH:mm:ss）", example = "2026-04-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTimeStart;

    @Schema(description = "修改结束时间（yyyy-MM-dd HH:mm:ss）", example = "2026-04-30 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTimeEnd;
}
