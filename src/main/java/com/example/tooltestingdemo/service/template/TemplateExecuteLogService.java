package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;
import com.example.tooltestingdemo.vo.TraceChainDetailVO;
import java.time.LocalDateTime;

/**
 * 模板执行统一日志 Service。
 */
public interface TemplateExecuteLogService extends IService<TemplateExecuteLog> {

    /**
     * 分页查询执行日志。
     */
    IPage<TemplateExecuteLog> pageLogs(Page<TemplateExecuteLog> page,
                                       Long templateId,
                                       Long jobId,
                                       String executeType,
                                       Integer success,
                                       String keyword,
                                       LocalDateTime startTime,
                                       LocalDateTime endTime);

    /**
     * 根据 traceId 查询完整链路信息。
     */
    TraceChainDetailVO getTraceChainDetail(String traceId);
}
