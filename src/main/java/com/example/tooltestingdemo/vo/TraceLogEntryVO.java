package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单条链路日志记录。
 */
@Data
public class TraceLogEntryVO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    private String level;

    private String logger;

    private String thread;

    private String message;

    private String throwable;
}
