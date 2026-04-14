package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTypeMapper;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import com.example.tooltestingdemo.vo.ProtocolTypeDeleteResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 协议类型主表 服务实现类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Slf4j
@Service
public class ProtocolTypeServiceImpl extends ServiceImpl<ProtocolTypeMapper, ProtocolType> implements IProtocolTypeService {

    private final ProtocolTypeMapper protocolTypeMapper;
    private final SecurityService securityService;

    public ProtocolTypeServiceImpl(ProtocolTypeMapper protocolTypeMapper, SecurityService securityService) {
        this.protocolTypeMapper = protocolTypeMapper;
        this.securityService = securityService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolType createProtocolType(ProtocolType protocolType) {
        // 校验协议标识符（协议编码）唯一性
        LambdaQueryWrapper<ProtocolType> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(ProtocolType::getProtocolIdentifier, protocolType.getProtocolIdentifier());
        lambdaQuery.eq(ProtocolType::getIsDeleted, 0);
        if (protocolTypeMapper.selectCount(lambdaQuery) > 0) {
            throw new RuntimeException("协议编码重复，请重新输入！");
        }

        // 分类ID
        if (protocolType.getClassificationId() == null) {
            protocolType.setClassificationId(1L);
        }


        if (protocolType.getCreateId() == null) {
            protocolType.setCreateId(1L);
        }

        save(protocolType);
        log.info("新增协议类型成功: id={}, name={}", protocolType.getId(), protocolType.getProtocolName());
        return protocolType;
    }

    @Override
    public List<ProtocolType> getProtocolTypeList(ProtocolType protocolType) {
        LambdaQueryWrapper<ProtocolType> lambdaQuery = new LambdaQueryWrapper<>();

        // 协议名称模糊查询（非空时）
        if (StringUtils.isNotBlank(protocolType.getProtocolName())) {
            lambdaQuery.like(ProtocolType::getProtocolName, protocolType.getProtocolName());
        }

        // 适用系统精确查询（非空时）
        if (protocolType.getApplicableSystem() != null) {
            lambdaQuery.eq(ProtocolType::getApplicableSystem, protocolType.getApplicableSystem());
        }

        // 状态精确查询（非空时）
        if (protocolType.getStatus() != null) {
            lambdaQuery.eq(ProtocolType::getStatus, protocolType.getStatus());
        }

        return protocolTypeMapper.selectList(lambdaQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolType modifyProtocolType(ProtocolType protocolType) {
        if (protocolType.getId() == null) {
            throw new RuntimeException("协议类型ID不能为空！");
        }

        ProtocolType existing = protocolTypeMapper.selectById(protocolType.getId());
        if (existing == null || Integer.valueOf(1).equals(existing.getIsDeleted())) {
            throw new RuntimeException("协议类型不存在！");
        }

        if (StringUtils.isBlank(protocolType.getProtocolName())) {
            throw new RuntimeException("协议类型名称不能为空！");
        }

        long relatedProjectCount = getRelatedProjectCount(protocolType.getId());
        long relatedTemplateCount = getRelatedTemplateCount(protocolType.getId());
        String relationImpactScope = buildRelationImpactScope(relatedProjectCount, relatedTemplateCount);

        if (StringUtils.isNotBlank(protocolType.getProtocolIdentifier())
                && !StringUtils.equals(existing.getProtocolIdentifier(), protocolType.getProtocolIdentifier())) {
            String message = StringUtils.isNotBlank(relationImpactScope)
                    ? String.format("协议编码不可修改，避免关联数据混乱。%s。", relationImpactScope)
                    : "协议编码不可修改，避免关联数据混乱。";
            throw new RuntimeException(message);
        }

        ProtocolType updateEntity = new ProtocolType();
        updateEntity.setId(existing.getId());
        updateEntity.setProtocolIdentifier(existing.getProtocolIdentifier());
        updateEntity.setApplicableSystem(existing.getApplicableSystem());
        updateEntity.setStatus(existing.getStatus());
        updateEntity.setProtocolName(protocolType.getProtocolName());
        updateEntity.setDescription(protocolType.getDescription());
        updateEntity.setClassificationId(protocolType.getClassificationId());
        updateEntity.setUpdateId(protocolType.getUpdateId());

        if (protocolTypeMapper.updateById(updateEntity) <= 0) {
            throw new RuntimeException("协议类型修改失败！");
        }

        ProtocolType updated = protocolTypeMapper.selectById(protocolType.getId());
        fillRelationImpactFields(updated, relatedProjectCount, relatedTemplateCount);
        log.info("编辑协议类型成功: id={}, name={}", updated.getId(), updated.getProtocolName());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProtocolType(Long id) {
        if (id == null) {
            throw new RuntimeException("协议类型ID不能为空！");
        }

        ProtocolType existing = getExistingProtocolType(id);
        RelationStats relationStats = getRelationStats(id);
        if (relationStats.hasRelatedData()) {
            String message = buildDeleteBlockedMessage(relationStats.relatedProjectCount, relationStats.relatedTemplateCount);
            log.warn("删除协议类型失败，存在关联数据: id={}, name={}, relatedProjects={}, relatedTemplates={}",
                    existing.getId(), existing.getProtocolName(), relationStats.relatedProjectCount, relationStats.relatedTemplateCount);
            throw new RuntimeException(message);
        }

        doLogicalDelete(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolTypeDeleteResultVO batchDeleteProtocolTypes(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new RuntimeException("协议类型ID列表不能为空！");
        }

        LinkedHashSet<Long> uniqueIds = Arrays.stream(ids)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (uniqueIds.isEmpty()) {
            throw new RuntimeException("协议类型ID列表不能为空！");
        }

        List<Long> deletedIds = new ArrayList<>();
        List<ProtocolTypeDeleteResultVO.UndeletableItem> undeletableItems = new ArrayList<>();

        for (Long id : uniqueIds) {
            ProtocolType existing = protocolTypeMapper.selectById(id);
            if (existing == null || Integer.valueOf(1).equals(existing.getIsDeleted())) {
                undeletableItems.add(buildUndeletableItem(id, null, 0L, 0L, "协议类型不存在"));
                continue;
            }

            RelationStats relationStats = getRelationStats(id);
            if (relationStats.hasRelatedData()) {
                undeletableItems.add(buildUndeletableItem(
                        existing.getId(),
                        existing.getProtocolName(),
                        relationStats.relatedProjectCount,
                        relationStats.relatedTemplateCount,
                        buildDeleteBlockedMessage(relationStats.relatedProjectCount, relationStats.relatedTemplateCount)
                ));
                continue;
            }

            doLogicalDelete(existing);
            deletedIds.add(existing.getId());
        }

        ProtocolTypeDeleteResultVO result = new ProtocolTypeDeleteResultVO();
        result.setDeletedIds(deletedIds);
        result.setUndeletableItems(undeletableItems);
        result.setDeletedCount(deletedIds.size());
        result.setUndeletableCount(undeletableItems.size());
        result.setSummaryMessage(buildBatchDeleteSummaryMessage(deletedIds.size(), undeletableItems));

        log.info("批量删除协议类型完成: deletedCount={}, undeletableCount={}, deletedIds={}, undeletableIds={}",
                result.getDeletedCount(),
                result.getUndeletableCount(),
                result.getDeletedIds(),
                undeletableItems.stream().map(ProtocolTypeDeleteResultVO.UndeletableItem::getId).toList());
        return result;
    }

    private long getRelatedProjectCount(Long protocolId) {
        Long count = protocolTypeMapper.countRelatedProjects(protocolId);
        return count == null ? 0L : count;
    }

    private long getRelatedTemplateCount(Long protocolId) {
        Long count = protocolTypeMapper.countRelatedTemplates(protocolId);
        return count == null ? 0L : count;
    }

    private void fillRelationImpactFields(ProtocolType protocolType, long relatedProjectCount, long relatedTemplateCount) {
        protocolType.setRelatedProjectCount(relatedProjectCount);
        protocolType.setRelatedTemplateCount(relatedTemplateCount);
        protocolType.setRelationImpactScope(buildRelationImpactScope(relatedProjectCount, relatedTemplateCount));
    }

    private String buildRelationImpactScope(long relatedProjectCount, long relatedTemplateCount) {
        if (relatedProjectCount <= 0 && relatedTemplateCount <= 0) {
            return null;
        }
        return String.format("关联影响范围：%d 个项目、%d 个模板", relatedProjectCount, relatedTemplateCount);
    }

    private ProtocolType getExistingProtocolType(Long id) {
        ProtocolType existing = protocolTypeMapper.selectById(id);
        if (existing == null || Integer.valueOf(1).equals(existing.getIsDeleted())) {
            throw new RuntimeException("协议类型不存在！");
        }
        return existing;
    }

    private RelationStats getRelationStats(Long id) {
        return new RelationStats(getRelatedProjectCount(id), getRelatedTemplateCount(id));
    }

    private String buildDeleteBlockedMessage(long relatedProjectCount, long relatedTemplateCount) {
        return String.format("请先解除关联 %d 个项目 / %d 个模板", relatedProjectCount, relatedTemplateCount);
    }

    private void doLogicalDelete(ProtocolType existing) {
        Long operatorId = getCurrentOperatorId();
        ProtocolType deleteEntity = new ProtocolType();
        deleteEntity.setId(existing.getId());
        deleteEntity.setIsDeleted(1);
        deleteEntity.setDeletedBy(operatorId);
        deleteEntity.setDeletedTime(LocalDateTime.now());
        deleteEntity.setUpdateId(operatorId);

        if (protocolTypeMapper.updateById(deleteEntity) <= 0) {
            throw new RuntimeException("协议类型删除失败！");
        }

        log.info("删除协议类型成功: id={}, name={}, operatorId={}", existing.getId(), existing.getProtocolName(), operatorId);
    }

    private Long getCurrentOperatorId() {
        String currentUserId = securityService.getCurrentUserId();
        if (StringUtils.isNumeric(currentUserId)) {
            return Long.valueOf(currentUserId);
        }
        return 1L;
    }

    private ProtocolTypeDeleteResultVO.UndeletableItem buildUndeletableItem(Long id, String protocolName,
                                                                            long relatedProjectCount,
                                                                            long relatedTemplateCount,
                                                                            String reason) {
        ProtocolTypeDeleteResultVO.UndeletableItem item = new ProtocolTypeDeleteResultVO.UndeletableItem();
        item.setId(id);
        item.setProtocolName(protocolName);
        item.setRelatedProjectCount(relatedProjectCount);
        item.setRelatedTemplateCount(relatedTemplateCount);
        item.setReason(reason);
        return item;
    }

    private String buildBatchDeleteSummaryMessage(int deletedCount, List<ProtocolTypeDeleteResultVO.UndeletableItem> undeletableItems) {
        if (undeletableItems.isEmpty()) {
            return String.format("可删除 %d 个，不可删除 0 个", deletedCount);
        }

        boolean allBlockedByRelation = undeletableItems.stream()
                .allMatch(item -> item.getReason() != null && item.getReason().startsWith("请先解除关联"));
        if (allBlockedByRelation) {
            return String.format("可删除 %d 个，不可删除 %d 个（原因：已关联数据）", deletedCount, undeletableItems.size());
        }
        return String.format("可删除 %d 个，不可删除 %d 个（原因：已关联数据或数据不存在）", deletedCount, undeletableItems.size());
    }

    private static class RelationStats {
        private final long relatedProjectCount;
        private final long relatedTemplateCount;

        private RelationStats(long relatedProjectCount, long relatedTemplateCount) {
            this.relatedProjectCount = relatedProjectCount;
            this.relatedTemplateCount = relatedTemplateCount;
        }

        private boolean hasRelatedData() {
            return relatedProjectCount > 0 || relatedTemplateCount > 0;
        }
    }
}
