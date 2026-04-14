package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTypeMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public ProtocolTypeServiceImpl(ProtocolTypeMapper protocolTypeMapper) {
        this.protocolTypeMapper = protocolTypeMapper;
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
}
