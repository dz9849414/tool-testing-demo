package com.example.tooltestingdemo.dto;

import com.example.tooltestingdemo.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 协议类型查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "协议类型分页查询参数")
public class ProtocolTypeQueryDTO extends PageQuery {

    @Schema(description = "协议编码（模糊查询）", example = "HTTP")
    private String protocolCode;

    @Schema(description = "协议名称（模糊查询）", example = "HTTP协议")
    private String protocolName;

    @Schema(description = "协议分类（精确匹配）", example = "应用层")
    private String protocolCategory;

    @Schema(description = "系统类型（精确匹配）", example = "第三方系统")
    private String systemType;

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
