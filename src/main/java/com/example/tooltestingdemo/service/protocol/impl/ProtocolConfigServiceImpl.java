package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.mapper.protocol.ProtocolConfigMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 协议参数配置表服务实现：新增场景仅持久化主表及 JSON 字段（url_config、auth_config），不再联动参数模板表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolConfigServiceImpl extends ServiceImpl<ProtocolConfigMapper, ProtocolConfig> implements IProtocolConfigService {

    private final ObjectMapper objectMapper;

    /**
     * 新增一条协议配置。
     * <p>
     * 核心业务要点：
     * <ol>
     *   <li>URL：结构化列表校验通过后序列化为 JSON 写入 {@code url_config}，保证「主 URL 唯一」「序号不重复」「非默认端口时端口合法」。</li>
     *   <li>认证：多条认证按 {@code type} 分支校验敏感字段，通过后整体序列化为 JSON 写入 {@code auth_config}（生产环境建议后续接加密/脱敏）。</li>
     *   <li>标量字段：超时、重试等与表默认值对齐——请求未传时使用库表约定默认值，避免 null 落库与业务歧义。</li>
     *   <li>事务：单表写入，仍使用事务保证与后续扩展（如审计、关联表）时行为一致。</li>
     * </ol>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfig createProtocolConfig(ProtocolConfigCreateDTO dto) {
        validateUrlConfig(dto.getUrlConfigList());
        validateAuthConfig(dto.getAuthConfigList());

        ProtocolConfig entity = new ProtocolConfig()
                .setProtocolId(dto.getProtocolId())
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
}
