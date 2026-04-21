package com.example.tooltestingdemo.vo;

import lombok.Data;

import java.util.List;

/**
 * TraceId 对应的运行时日志。
 */
@Data
public class TraceRuntimeLogVO {

    private String traceId;

    private Integer entryCount;

    private List<TraceLogEntryVO> entries;
}
