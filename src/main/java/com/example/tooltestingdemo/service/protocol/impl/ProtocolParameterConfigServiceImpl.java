package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolParameterConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.mapper.protocol.ProtocolParameterConfigMapper;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTypeMapper;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.protocol.IProtocolParameterConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

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
public class ProtocolParameterConfigServiceImpl extends ServiceImpl<ProtocolParameterConfigMapper, ProtocolConfig> implements IProtocolParameterConfigService {

    private static final Pattern PROTOCOL_PREFIX_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*://.+$");

    private final ProtocolParameterConfigMapper protocolParameterConfigMapper;
    private final ProtocolTypeMapper protocolTypeMapper;
    private final SecurityService securityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfig createProtocolParameterConfig(ProtocolParameterConfigCreateDTO dto) {
        validateCreateDTO(dto);

        ProtocolType protocolType = protocolTypeMapper.selectById(dto.getProtocolId());
        if (protocolType == null) {
            throw new RuntimeException("协议类型不存在！");
        }

        String normalizedParameterName = StringUtils.trim(dto.getParameterName());
        String normalizedParameterValue = StringUtils.trim(dto.getParameterValue());
        ensureParameterUnique(dto.getProtocolId(), normalizedParameterName);

        ProtocolConfig entity = new ProtocolConfig();
        entity.setProtocolId(dto.getProtocolId());

        Long operatorId = getCurrentOperatorId();
        entity.setCreateId(operatorId);
        entity.setUpdateId(operatorId);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        if (protocolParameterConfigMapper.insert(entity) <= 0) {
            throw new RuntimeException("协议参数配置新增失败！");
        }

        log.info("新增协议参数配置成功: id={}, protocolId={}, parameterName={}", entity.getId(), entity.getProtocolId(), entity.getConfigName());
        return entity;
    }

    private void validateCreateDTO(ProtocolParameterConfigCreateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("请求参数不能为空！");
        }
        if (dto.getProtocolId() == null) {
            throw new RuntimeException("协议类型ID不能为空！");
        }
        if (StringUtils.isBlank(dto.getParameterName())) {
            throw new RuntimeException("参数名称不能为空！");
        }
        if (dto.getParameterName().trim().length() > 50) {
            throw new RuntimeException("参数名称长度不能超过50！");
        }
        if (StringUtils.isBlank(dto.getParameterValue())) {
            throw new RuntimeException("参数值不能为空！");
        }
        if (dto.getParameterValue().trim().length() > 500) {
            throw new RuntimeException("参数值长度不能超过500！");
        }
        if (dto.getEncryptedValue() != null && dto.getEncryptedValue().trim().length() > 500) {
            throw new RuntimeException("加密参数值长度不能超过500！");
        }
        Integer isSensitive = dto.getIsSensitive();
        if (isSensitive != null && !Integer.valueOf(0).equals(isSensitive) && !Integer.valueOf(1).equals(isSensitive)) {
            throw new RuntimeException("是否敏感字段只能是0或1！");
        }
        if (isUrlParameter(dto.getParameterName())) {
            validateProtocolPrefix(dto.getParameterValue());
        }
        if (isPortParameter(dto.getParameterName())) {
            validateProtocolPort(dto.getParameterValue());
        }
    }

    private void ensureParameterUnique(Long protocolId, String parameterName) {
        LambdaQueryWrapper<ProtocolConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProtocolConfig::getProtocolId, protocolId)
                .eq(ProtocolConfig::getConfigName, parameterName);
        if (protocolParameterConfigMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("同一协议下参数名称重复，请重新输入！");
        }
    }

    private boolean isUrlParameter(String parameterName) {
        return StringUtils.equalsIgnoreCase(StringUtils.trim(parameterName), "URL");
    }

    private boolean isPortParameter(String parameterName) {
        return StringUtils.equalsIgnoreCase(StringUtils.trim(parameterName), "PORT");
    }

    private void validateProtocolPort(String parameterValue) {
        String normalizedValue = StringUtils.trim(parameterValue);
        int port = Integer.parseInt(normalizedValue);
        if (port < 0 || port > 65535) {
            throw new RuntimeException("端口号只能是【0-65535】范围区间!");
        }
    }

    private void validateProtocolPrefix(String parameterValue) {
        String normalizedValue = StringUtils.trim(parameterValue);
        if (!PROTOCOL_PREFIX_PATTERN.matcher(normalizedValue).matches()) {
            throw new RuntimeException("URL参数必须以 http://、https:// 或自定义协议前缀（如 mqtt://）开头！");
        }
    }

    private Long getCurrentOperatorId() {
        String currentUserId = securityService.getCurrentUserId();
        if (StringUtils.isNumeric(currentUserId)) {
            return Long.valueOf(currentUserId);
        }
        return 1L;
    }

}
