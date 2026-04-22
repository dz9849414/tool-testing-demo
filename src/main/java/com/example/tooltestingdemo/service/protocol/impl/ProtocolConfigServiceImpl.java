package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigModifyDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigQueryDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigStatusUpdateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.mapper.protocol.ProtocolConfigMapper;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import com.example.tooltestingdemo.vo.ProtocolConfigVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 协议参数配置表服务实现：新增场景仅持久化主表及 JSON 字段（url_config、auth_config），不再联动参数模板表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolConfigServiceImpl extends ServiceImpl<ProtocolConfigMapper, ProtocolConfig> implements IProtocolConfigService {

    private final ObjectMapper objectMapper;
    private final SecurityService securityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfig createProtocolConfig(ProtocolConfigCreateDTO dto) {
        validateUrlConfig(dto.getUrlConfigList());
        validateAuthConfig(dto.getAuthConfigList());

        ProtocolConfig entity = new ProtocolConfig()
                .setProtocolId(dto.getProtocolId())
                .setProtocolName(dto.getProtocolName())
                .setConfigName(dto.getConfigName())
                .setTimeoutConnect(defaultInt(dto.getTimeoutConnect(), 5000))
                .setTimeoutRead(defaultInt(dto.getTimeoutRead(), 30000))
                .setRetryCount(defaultInt(dto.getRetryCount(), 3))
                .setRetryInterval(defaultInt(dto.getRetryInterval(), 1000))
                .setRetryCondition(dto.getRetryCondition())
                .setDataFormat(defaultString(dto.getDataFormat(), "JSON"))
                .setStatus(defaultInt(dto.getStatus(), 0))
                .setFormatConfig(dto.getFormatConfig())
                .setAdditionalParams(dto.getAdditionalParams())
                .setDescription(dto.getDescription());

        entity.setUrlConfig(toJsonOrNull(dto.getUrlConfigList()));
        entity.setAuthConfig(toJsonOrNull(dto.getAuthConfigList()));

        this.save(entity);
        return entity;
    }

    @Override
    public IPage<ProtocolConfigVO> getProtocolConfigList(ProtocolConfigQueryDTO dto) {
        ProtocolConfigQueryDTO query = dto == null ? new ProtocolConfigQueryDTO() : dto;
        IPage<ProtocolConfig> page = this.page(query.toPage(), buildQueryWrapper(query));
        return page.convert(this::toVO);
    }

    @Override
    public ProtocolConfigVO getProtocolConfigDetail(Long id) {
        ProtocolConfig config = this.getById(id);
        if (config == null) {
            throw new RuntimeException("协议配置不存在");
        }
        return toVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfigVO modifyProtocolConfig(ProtocolConfigModifyDTO dto) {
        ProtocolConfig existing = this.getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("协议配置不存在");
        }
        if (dto.getUrlConfigList() != null) {
            validateUrlConfig(dto.getUrlConfigList());
        }
        if (dto.getAuthConfigList() != null) {
            validateAuthConfig(dto.getAuthConfigList());
        }

        ProtocolConfig updateEntity = new ProtocolConfig();
        updateEntity.setId(existing.getId());
        updateEntity.setProtocolId(dto.getProtocolId() == null ? existing.getProtocolId() : dto.getProtocolId());
        updateEntity.setProtocolName(dto.getConfigName() == null ? existing.getConfigName() : dto.getConfigName());
        updateEntity.setConfigName(resolveNullableText(dto.getConfigName(), existing.getConfigName()));
        updateEntity.setTimeoutConnect(dto.getTimeoutConnect() == null ? existing.getTimeoutConnect() : dto.getTimeoutConnect());
        updateEntity.setTimeoutRead(dto.getTimeoutRead() == null ? existing.getTimeoutRead() : dto.getTimeoutRead());
        updateEntity.setRetryCount(dto.getRetryCount() == null ? existing.getRetryCount() : dto.getRetryCount());
        updateEntity.setRetryInterval(dto.getRetryInterval() == null ? existing.getRetryInterval() : dto.getRetryInterval());
        updateEntity.setRetryCondition(resolveNullableText(dto.getRetryCondition(), existing.getRetryCondition()));
        updateEntity.setDataFormat(resolveNullableText(dto.getDataFormat(), existing.getDataFormat()));
        updateEntity.setFormatConfig(resolveNullableText(dto.getFormatConfig(), existing.getFormatConfig()));
        updateEntity.setAdditionalParams(resolveNullableText(dto.getAdditionalParams(), existing.getAdditionalParams()));
        updateEntity.setDescription(resolveNullableText(dto.getDescription(), existing.getDescription()));
        updateEntity.setStatus(resolveStatus(dto.getStatus(), existing.getStatus()));
        updateEntity.setUrlConfig(dto.getUrlConfigList() == null ? existing.getUrlConfig() : toJsonOrNull(dto.getUrlConfigList()));
        updateEntity.setAuthConfig(dto.getAuthConfigList() == null ? existing.getAuthConfig() : toJsonOrNull(dto.getAuthConfigList()));

        if (!this.updateById(updateEntity)) {
            throw new RuntimeException("协议配置编辑失败");
        }
        return toVO(this.getById(existing.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfigVO updateProtocolConfigStatus(ProtocolConfigStatusUpdateDTO dto) {
        ProtocolConfig existing = this.getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("协议配置不存在");
        }
        Integer targetStatus = resolveStatus(dto.getStatus(), null);
        if (Objects.equals(existing.getStatus(), targetStatus)) {
            return toVO(existing);
        }

        ProtocolConfig updateEntity = new ProtocolConfig();
        updateEntity.setId(existing.getId());
        updateEntity.setStatus(targetStatus);
        if (!this.updateById(updateEntity)) {
            throw new RuntimeException("协议配置状态更新失败");
        }
        return toVO(this.getById(existing.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProtocolConfig(Long id) {
        ProtocolConfig existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("协议配置不存在");
        }

        Long operatorId = getCurrentOperatorId();
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<ProtocolConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProtocolConfig::getId, id)
                .eq(ProtocolConfig::getIsDeleted, 0)
                .set(ProtocolConfig::getIsDeleted, 1)
                .set(ProtocolConfig::getDeletedBy, operatorId)
                .set(ProtocolConfig::getDeletedTime, now)
                .set(ProtocolConfig::getUpdateTime, now);

        if (this.baseMapper.update(null, updateWrapper) <= 0) {
            throw new RuntimeException("协议配置删除失败");
        }
    }

    /**
     * URL 列表业务规则：非空、主 URL 恰好一个、序号唯一、非默认端口时必须带合法端口。
     */
    private void validateUrlConfig(List<ProtocolConfigCreateDTO.UrlConfigItemDTO> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("urlConfigList不能为空");
        }
        long primaryTrue = list.stream().filter(i -> Boolean.TRUE.equals(i.getPrimary())).count();
        if (primaryTrue != 1) {
            throw new IllegalArgumentException("主URL必须且只能有一个");
        }
        Set<Integer> seqSeen = new HashSet<>();
        for (ProtocolConfigCreateDTO.UrlConfigItemDTO item : list) {
            if (!seqSeen.add(item.getSeq())) {
                throw new IllegalArgumentException("urlConfigList.seq不能重复");
            }
            if (Boolean.FALSE.equals(item.getUseDefaultPort())) {
                if (item.getPort() == null) {
                    throw new IllegalArgumentException("未使用默认端口时，端口号不能为空");
                }
                if (item.getPort() < 1 || item.getPort() > 65535) {
                    throw new IllegalArgumentException("端口范围必须为1-65535");
                }
            }
        }
    }

    /**
     * 认证列表按类型做字段级校验；空列表表示「无额外认证配置」，允许不写 auth_config。
     */
    private void validateAuthConfig(List<ProtocolConfigCreateDTO.AuthConfigItemDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ProtocolConfigCreateDTO.AuthConfigItemDTO item : list) {
            String type = upper(item.getType());
            switch (type) {
                case "NONE" -> { /* 无附加字段 */ }
                case "BASIC" -> {
                    requireText(item.getUsername(), "Basic认证用户名不能为空");
                    requireText(item.getPassword(), "Basic认证密码不能为空");
                }
                case "TOKEN" -> {
                    requireText(item.getToken(), "Token不能为空");
                    requireText(item.getTokenLocation(), "Token位置不能为空");
                    String loc = upper(item.getTokenLocation());
                    if (!"HEADER".equals(loc) && !"QUERY".equals(loc)) {
                        throw new IllegalArgumentException("Token位置仅支持HEADER/QUERY");
                    }
                    if ("HEADER".equals(loc)) {
                        requireText(item.getHeaderName(), "Token位置为HEADER时headerName不能为空");
                    }
                }
                case "OAUTH2" -> {
                    requireText(item.getAuthEndpoint(), "OAuth2授权端点不能为空");
                    requireText(item.getClientId(), "OAuth2 clientId不能为空");
                    requireText(item.getClientSecret(), "OAuth2 clientSecret不能为空");
                }
                case "CERT" -> {
                    requireText(item.getCertFileName(), "证书文件名不能为空");
                    requireText(item.getCertFileBase64(), "证书文件内容不能为空");
                    requireText(item.getCertPassword(), "证书密码不能为空");
                }
                default -> throw new IllegalArgumentException("不支持的认证方式：" + item.getType());
            }
        }
    }

    private String toJsonOrNull(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON序列化失败：" + e.getMessage(), e);
        }
    }

    private static String upper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private static void requireText(String s, String message) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static int defaultInt(Integer v, int defaultValue) {
        return v == null ? defaultValue : v;
    }

    private static String defaultString(String v, String defaultValue) {
        if (v == null || v.trim().isEmpty()) {
            return defaultValue;
        }
        return v;
    }

    private Integer resolveStatus(Integer status, Integer defaultValue) {
        if (status == null) {
            return defaultValue;
        }
        if (status == 0 || status == 1) {
            return status;
        }
        throw new RuntimeException("状态不合法，仅支持 0（禁用）或 1（启用）");
    }

    private String resolveNullableText(String incomingValue, String oldValue) {
        return StringUtils.isBlank(incomingValue) ? oldValue : incomingValue.trim();
    }

    private LambdaQueryWrapper<ProtocolConfig> buildQueryWrapper(ProtocolConfigQueryDTO dto) {
        LambdaQueryWrapper<ProtocolConfig> queryWrapper = new LambdaQueryWrapper<>();
        if (dto.getProtocolId() != null) {
            queryWrapper.eq(ProtocolConfig::getProtocolId, dto.getProtocolId());
        }
        if (StringUtils.isNotBlank(dto.getConfigName())) {
            queryWrapper.like(ProtocolConfig::getConfigName, dto.getConfigName());
        }
        if (dto.getStatus() != null) {
            queryWrapper.eq(ProtocolConfig::getStatus, dto.getStatus());
        }
        queryWrapper.orderByDesc(ProtocolConfig::getCreateTime).orderByDesc(ProtocolConfig::getId);
        return queryWrapper;
    }

    private ProtocolConfigVO toVO(ProtocolConfig entity) {
        ProtocolConfigVO vo = new ProtocolConfigVO();
        vo.setId(entity.getId());
        vo.setProtocolId(entity.getProtocolId());
        vo.setProtocolName(entity.getProtocolName());
        vo.setConfigName(entity.getConfigName());
        vo.setTimeoutConnect(entity.getTimeoutConnect());
        vo.setTimeoutRead(entity.getTimeoutRead());
        vo.setRetryCount(entity.getRetryCount());
        vo.setRetryInterval(entity.getRetryInterval());
        vo.setRetryCondition(entity.getRetryCondition());
        vo.setDataFormat(entity.getDataFormat());
        vo.setFormatConfig(entity.getFormatConfig());
        vo.setAdditionalParams(entity.getAdditionalParams());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setCreateId(entity.getCreateId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateId(entity.getUpdateId());
        vo.setUpdateTime(entity.getUpdateTime());

        List<ProtocolConfigCreateDTO.UrlConfigItemDTO> urlConfigList = parseList(
                entity.getUrlConfig(),
                new TypeReference<List<ProtocolConfigCreateDTO.UrlConfigItemDTO>>() {
                }
        );
        if (urlConfigList != null) {
            urlConfigList.sort(Comparator.comparing(
                    ProtocolConfigCreateDTO.UrlConfigItemDTO::getSeq,
                    Comparator.nullsLast(Integer::compareTo)
            ));
        }
        vo.setUrlConfigList(urlConfigList);
        vo.setAuthConfigList(parseList(
                entity.getAuthConfig(),
                new TypeReference<List<ProtocolConfigCreateDTO.AuthConfigItemDTO>>() {
                }
        ));
        return vo;
    }

    private <T> List<T> parseList(String json, TypeReference<List<T>> typeReference) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.warn("协议配置JSON反序列化失败: {}", e.getMessage());
            return null;
        }
    }

    private Long getCurrentOperatorId() {
        Long currentUserId = securityService.getCurrentUserId();
        return currentUserId == null ? 1L : currentUserId;
    }
}
