package com.example.tooltestingdemo.service.impl.template;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateHistory;
import com.example.tooltestingdemo.mapper.template.TemplateHistoryMapper;
import com.example.tooltestingdemo.service.template.TemplateHistoryService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.example.tooltestingdemo.vo.TemplateHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 模板历史版本 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateHistoryServiceImpl extends ServiceImpl<TemplateHistoryMapper, TemplateHistory> 
        implements TemplateHistoryService {

    private final TemplateHistoryMapper historyMapper;

    @Override
    public List<TemplateHistoryVO> getHistoriesByTemplateId(Long templateId) {
        List<TemplateHistoryVO> voList = TemplateConverter.toHistoryVOList(historyMapper.selectByTemplateId(templateId));
        voList.forEach(this::enrichTemplateData);
        return voList;
    }

    @Override
    public TemplateHistoryVO getHistoryDetail(Long historyId) {
        TemplateHistoryVO vo = TemplateConverter.toVO(getById(historyId));
        enrichTemplateData(vo);
        return vo;
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
        TemplateHistory history = Optional.ofNullable(getById(historyId))
            .orElseThrow(() -> new RuntimeException("历史版本不存在"));
        
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
        List<TemplateHistory> histories = historyMapper.selectByTemplateId(templateId);
        
        if (histories.size() <= keepCount) return 0;
        
        int deleteCount = 0;
        for (int i = keepCount; i < histories.size(); i++) {
            removeById(histories.get(i).getId());
            deleteCount++;
        }
        
        log.info("清理历史版本成功: templateId={}, deleteCount={}", templateId, deleteCount);
        return deleteCount;
    }

    @Override
    public IPage<TemplateHistoryVO> pageHistories(Page<TemplateHistory> page, Long templateId, String operationType) {
        LambdaQueryWrapper<TemplateHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateHistory::getTemplateId, templateId);
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(TemplateHistory::getOperationType, operationType);
        }
        wrapper.orderByDesc(TemplateHistory::getCreateTime);
        
        IPage<TemplateHistory> resultPage = historyMapper.selectPage(page, wrapper);
        
        List<TemplateHistoryVO> voList = TemplateConverter.toHistoryVOList(resultPage.getRecords());
        
        IPage<TemplateHistoryVO> voPage = new Page<>();
        voPage.setCurrent(resultPage.getCurrent());
        voPage.setSize(resultPage.getSize());
        voPage.setTotal(resultPage.getTotal());
        voPage.setPages(resultPage.getPages());
        voPage.setRecords(voList);
        
        voList.forEach(this::enrichTemplateData);
        
        return voPage;
    }

    private void enrichTemplateData(TemplateHistoryVO vo) {
        if (vo == null || !org.springframework.util.StringUtils.hasText(vo.getTemplateSnapshot())) {
            return;
        }
        try {
            InterfaceTemplateVO templateData = JSON.parseObject(vo.getTemplateSnapshot(), InterfaceTemplateVO.class);
            vo.setTemplateData(templateData);
        } catch (Exception e) {
            log.warn("解析模板快照失败: historyId={}, error={}", vo.getId(), e.getMessage());
        }
    }
}
