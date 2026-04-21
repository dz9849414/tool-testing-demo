package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.mapper.protocol.ProtocolConfigMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 协议参数配置表 服务实现类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolConfigServiceImpl extends ServiceImpl<ProtocolConfigMapper, ProtocolConfig> implements IProtocolConfigService {
    private final ProtocolConfigMapper protocolConfigMapper;

    /**
     * 新增协议配置。
     * 根据实体注释做核心约束校验，并复用实体内部枚举进行字段合法性判断。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfig createProtocolConfig(ProtocolConfigCreateDTO dto) {
        validateStatus(dto.getStatus());
        validateAuthType(dto.getAuthType());
        validateDataFormat(dto.getDataFormat());

        ProtocolConfig entity = new ProtocolConfig();
        entity.setProtocolId(dto.getProtocolId());
        entity.setConfigName(StringUtils.trim(dto.getConfigName()));
        entity.setUrl(StringUtils.trim(dto.getUrl()));
        entity.setPort(dto.getPort());
        entity.setAuthType(normalizeUpper(dto.getAuthType()));
        entity.setAuthConfig(dto.getAuthConfig());
        entity.setTimeoutConnect(dto.getTimeoutConnect());
        entity.setTimeoutRead(dto.getTimeoutRead());
        entity.setRetryCount(dto.getRetryCount());
        entity.setRetryInterval(dto.getRetryInterval());
        entity.setDataFormat(normalizeUpper(dto.getDataFormat()));
        entity.setFormatConfig(dto.getFormatConfig());
        entity.setAdditionalParams(dto.getAdditionalParams());
        entity.setStatus(dto.getStatus());

        protocolConfigMapper.insert(entity);
        log.info("新增协议配置成功: id={}, protocolId={}, configName={}, authType={}, dataFormat={}, status={}",
                entity.getId(), entity.getProtocolId(), entity.getConfigName(),
                entity.getAuthType(), entity.getDataFormat(), entity.getStatus());
        return entity;
    }

    private void validateStatus(Integer status) {
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status)) {
            throw new RuntimeException("状态不合法，仅支持 0（禁用）或 1（启用）");
        }
    }

    private void validateAuthType(String authType) {
        String normalized = normalizeUpper(authType);
        for (ProtocolConfig.AuthType value : ProtocolConfig.AuthType.values()) {
            if (value.name().equals(normalized)) {
                return;
            }
        }
        throw new RuntimeException("认证方式不合法，仅支持 NONE/BASIC/TOKEN/OAUTH2/CERT");
    }

    private void validateDataFormat(String dataFormat) {
        String normalized = normalizeUpper(dataFormat);
        for (ProtocolConfig.DataFormat value : ProtocolConfig.DataFormat.values()) {
            if (value.name().equals(normalized)) {
                return;
            }
        }
        throw new RuntimeException("数据格式不合法，仅支持 JSON/XML/FORM/TEXT/BINARY");
    }

    private String normalizeUpper(String value) {
        return StringUtils.trimToEmpty(value).toUpperCase();
    }
}
