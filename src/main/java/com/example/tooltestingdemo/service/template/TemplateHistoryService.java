package com.example.tooltestingdemo.service.template;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.template.TemplateHistory;
import com.example.tooltestingdemo.vo.TemplateHistoryVO;

import java.util.List;

/**
 * 模板历史版本 Service 接口
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/template/TemplateHistoryService.java
 */
public interface TemplateHistoryService extends IService<TemplateHistory> {

    /**
     * 获取模板的历史版本列表
     * 
     * @param templateId 模板ID
     * @return 历史版本VO列表
     */
    List<TemplateHistoryVO> getHistoriesByTemplateId(Long templateId);

    /**
     * 获取历史版本详情
     * 
     * @param historyId 历史版本ID
     * @return 历史版本详情VO
     */
    TemplateHistoryVO getHistoryDetail(Long historyId);

    /**
     * 记录模板变更历史
     * 
     * @param history 历史记录
     * @return 保存后的历史记录VO
     */
    TemplateHistoryVO recordHistory(TemplateHistory history);

    /**
     * 回滚到指定版本
     * 
     * @param historyId 历史版本ID
     * @return 是否成功
     */
    boolean rollbackToVersion(Long historyId);

    /**
     * 清理历史版本（保留最近N个版本）
     * 
     * @param templateId 模板ID
     * @param keepCount 保留数量
     * @return 清理数量
     */
    int cleanOldHistories(Long templateId, int keepCount);
}
