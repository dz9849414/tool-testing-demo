package com.example.tooltestingdemo.vo;

import lombok.Data;

import java.util.List;

/**
 * 协议类型批量删除结果
 */
@Data
public class ProtocolTypeDeleteResultVO {

    /**
     * 已成功删除的协议类型ID
     */
    private List<Long> deletedIds;

    /**
     * 不可删除的协议类型明细
     */
    private List<UndeletableItem> undeletableItems;

    /**
     * 已删除数量
     */
    private Integer deletedCount;

    /**
     * 不可删除数量
     */
    private Integer undeletableCount;

    /**
     * 批量删除汇总提示
     */
    private String summaryMessage;

    /**
     * 不可删除项明细
     */
    @Data
    public static class UndeletableItem {
        private Long id;
        private String protocolName;
        private Long relatedProjectCount;
        private Long relatedTemplateCount;
        private String reason;
    }
}