package com.example.tooltestingdemo.vo;

import lombok.Data;

import java.util.List;

/**
 * 协议类型批量状态切换结果
 */
@Data
public class ProtocolTypeBatchStatusChangeVO {

    /**
     * 是否成功执行
     */
    private Boolean success;

    /**
     * 成功变更数量
     */
    private Integer statusChangedCount;

    /**
     * 状态未变化数量
     */
    private Integer unchangedCount;

    /**
     * 阻断数量
     */
    private Integer blockedCount;

    /**
     * 结果提示文案
     */
    private String message;

    /**
     * 阻断明细
     */
    private List<BlockedItem> blockedItems;

    @Data
    public static class BlockedItem {
        private Long id;
        private String protocolName;
        private Long relatedProjectCount;
        private Long relatedTemplateCount;
        private String reason;
    }
}
