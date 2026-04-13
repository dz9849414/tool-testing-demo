package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.entity.template.TemplateFolder;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTypeMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import com.example.tooltestingdemo.util.TemplateConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    public ProtocolType createProtocolTyp(ProtocolType protocolType) {

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
}
