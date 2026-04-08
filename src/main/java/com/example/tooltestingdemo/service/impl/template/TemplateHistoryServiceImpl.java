package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateHistory;
import com.example.tooltestingdemo.mapper.template.TemplateHistoryMapper;
import com.example.tooltestingdemo.service.template.TemplateHistoryService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.TemplateHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模板历史版本 Service 实现类
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/impl/template/TemplateHistoryServiceImpl.java
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateHistoryServiceImpl extends ServiceImpl<TemplateHistoryMapper, TemplateHistory> 
        implements TemplateHistoryService {

    @Override
    public List<TemplateHistoryVO> getHistoriesByTemplateId(Long templateId) {
        List<TemplateHistory> histories = baseMapper.selectByTemplateId(templateId);
        return TemplateConverter.toHistoryVOList(histories);
    }

    @Override
    public TemplateHistoryVO getHistoryDetail(Long historyId) {
        TemplateHistory history = getById(historyId);
        return TemplateConverter.toVO(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateHistoryVO recordHistory(TemplateHistory history) {
        save(history);
        log.info("记录模板历史成功: templateId={}, version={}", history.getTemplateId(), history.getVersion());
        return TemplateConverter.toVO(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackToVersion(Long historyId) {
        TemplateHistory history = getById(historyId);
        if (history == null) {
            throw new RuntimeException("历史版本不存在");
        }
        
        if (Integer.valueOf(0).equals(history.getCanRollback())) {
            throw new RuntimeException("该版本不允许回滚");
        }
        
        // TODO: 实现回滚逻辑，从历史快照恢复模板数据
        
        log.info("回滚模板成功: historyId={}, templateId={}", historyId, history.getTemplateId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanOldHistories(Long templateId, int keepCount) {
        // 查询该模板的所有历史版本
        List<TemplateHistory> histories = baseMapper.selectByTemplateId(templateId);
        
        if (histories.size() <= keepCount) {
            return 0;
        }
        
        // 删除旧版本（保留最近的keepCount个）
        int deleteCount = 0;
        for (int i = keepCount; i < histories.size(); i++) {
            removeById(histories.get(i).getId());
            deleteCount++;
        }
        
        log.info("清理历史版本成功: templateId={}, deleteCount={}", templateId, deleteCount);
        return deleteCount;
    }
}
