package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.entity.protocol.ProtocolTemplate;
import com.example.tooltestingdemo.entity.protocol.ProtocolTemplateGroup;
import com.example.tooltestingdemo.mapper.protocol.ProtocolConfigMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import com.example.tooltestingdemo.service.protocol.IProtocolTemplateGroupService;
import com.example.tooltestingdemo.service.protocol.IProtocolTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final IProtocolTemplateService protocolTemplateService;
    private final IProtocolTemplateGroupService protocolTemplateGroupService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfig createProtocolConfig(ProtocolConfigCreateDTO dto) {
        validateUrlConfig(dto.getUrlConfigList());
        validateAuthConfig(dto.getAuthConfigList());
        validateTemplates(dto.getTemplates());

        ProtocolConfig entity = new ProtocolConfig()
                .setProtocolId(dto.getProtocolId())
                .setConfigName(dto.getConfigName())
                .setTimeoutConnect(defaultIfNull(dto.getTimeoutConnect(), 5000))
                .setTimeoutRead(defaultIfNull(dto.getTimeoutRead(), 30000))
                .setRetryCount(defaultIfNull(dto.getRetryCount(), 3))
                .setRetryInterval(defaultIfNull(dto.getRetryInterval(), 1000))
                .setRetryCondition(dto.getRetryCondition())
                .setDataFormat(defaultIfBlank(dto.getDataFormat(), "JSON"))
                .setStatus(defaultIfNull(dto.getStatus(), 0))
                .setFormatConfig(dto.getFormatConfig())
                .setAdditionalParams(dto.getAdditionalParams())
                .setDescription(dto.getDescription());

        entity.setUrlConfig(writeJson(dto.getUrlConfigList()));
        entity.setAuthConfig(writeJson(dto.getAuthConfigList()));

        this.save(entity);

        // 参数模板入库（protocol_template / protocol_template_group）
        if (dto.getTemplates() != null && !dto.getTemplates().isEmpty()) {
            for (ProtocolConfigCreateDTO.ProtocolTemplateDTO t : dto.getTemplates()) {
                ProtocolTemplate template = new ProtocolTemplate()
                        .setProtocolConfigId(entity.getId())
                        .setProtocolIdStr(t.getProtocolIdStr())
                        .setTemplateName(t.getTemplateName())
                        .setTemplateCode(t.getTemplateCode())
                        .setParamsSnapshot(t.getParamsSnapshot())
                        .setIsPublic(defaultIfNull(t.getIsPublic(), 0));
                protocolTemplateService.save(template);

                if (t.getGroups() != null && !t.getGroups().isEmpty()) {
                    for (ProtocolConfigCreateDTO.ProtocolTemplateGroupDTO g : t.getGroups()) {
                        ProtocolTemplateGroup group = new ProtocolTemplateGroup()
                                .setProtocolTemplateId(template.getId())
                                .setGroupName(g.getGroupName())
                                .setParamsConfig(writeJson(g.getParamsConfig()));
                        protocolTemplateGroupService.save(group);
                    }
                }
            }
        }

        return entity;
    }

    private void validateUrlConfig(List<ProtocolConfigCreateDTO.UrlConfigItemDTO> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("urlConfigList不能为空");
        }
        long primaryCount = list.stream().filter(i -> Boolean.TRUE.equals(i.getPrimary())).count();
        if (primaryCount != 1) {
            throw new IllegalArgumentException("主URL必须且只能有一个");
        }

        Set<Integer> seqSet = new HashSet<>();
        for (ProtocolConfigCreateDTO.UrlConfigItemDTO item : list) {
            if (!seqSet.add(item.getSeq())) {
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

    private void validateAuthConfig(List<ProtocolConfigCreateDTO.AuthConfigItemDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ProtocolConfigCreateDTO.AuthConfigItemDTO item : list) {
            String type = normalize(item.getType());
            switch (type) {
                case "NONE" -> {
                    // no extra fields
                }
                case "BASIC" -> {
                    requireNotBlank(item.getUsername(), "Basic认证用户名不能为空");
                    requireNotBlank(item.getPassword(), "Basic认证密码不能为空");
                }
                case "TOKEN" -> {
                    requireNotBlank(item.getToken(), "Token不能为空");
                    requireNotBlank(item.getTokenLocation(), "Token位置不能为空");
                    String loc = normalize(item.getTokenLocation());
                    if (!Objects.equals(loc, "HEADER") && !Objects.equals(loc, "QUERY")) {
                        throw new IllegalArgumentException("Token位置仅支持HEADER/QUERY");
                    }
                    if (Objects.equals(loc, "HEADER")) {
                        requireNotBlank(item.getHeaderName(), "Token位置为HEADER时headerName不能为空");
                    }
                }
                case "OAUTH2" -> {
                    requireNotBlank(item.getAuthEndpoint(), "OAuth2授权端点不能为空");
                    requireNotBlank(item.getClientId(), "OAuth2 clientId不能为空");
                    requireNotBlank(item.getClientSecret(), "OAuth2 clientSecret不能为空");
                }
                case "CERT" -> {
                    requireNotBlank(item.getCertFileName(), "证书文件名不能为空");
                    requireNotBlank(item.getCertFileBase64(), "证书文件内容不能为空");
                    requireNotBlank(item.getCertPassword(), "证书密码不能为空");
                }
                default -> throw new IllegalArgumentException("不支持的认证方式：" + item.getType());
            }
        }
    }

    private void validateTemplates(List<ProtocolConfigCreateDTO.ProtocolTemplateDTO> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }
        Set<String> codeSet = new HashSet<>();
        for (ProtocolConfigCreateDTO.ProtocolTemplateDTO t : templates) {
            if (!codeSet.add(t.getTemplateCode())) {
                throw new IllegalArgumentException("templateCode不能重复：" + t.getTemplateCode());
            }
            if (t.getGroups() == null || t.getGroups().isEmpty()) {
                continue;
            }
            for (ProtocolConfigCreateDTO.ProtocolTemplateGroupDTO g : t.getGroups()) {
                if (g.getParamsConfig() == null || g.getParamsConfig().isEmpty()) {
                    throw new IllegalArgumentException("模板分组paramsConfig不能为空");
                }
                for (ProtocolConfigCreateDTO.ProtocolTemplateGroupParamDTO p : g.getParamsConfig()) {
                    String type = normalize(p.getType());
                    if (!Objects.equals(type, "STRING") && !Objects.equals(type, "NUMBER") && !Objects.equals(type, "BOOLEAN")) {
                        throw new IllegalArgumentException("参数类型仅支持STRING/NUMBER/BOOLEAN");
                    }
                }
            }
        }
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON序列化失败：" + e.getMessage(), e);
        }
    }

    private static String normalize(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private static void requireNotBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

    private static Integer defaultIfNull(Integer v, int defaultValue) {
        return v == null ? defaultValue : v;
    }

    private static String defaultIfBlank(String v, String defaultValue) {
        if (v == null || v.trim().isEmpty()) {
            return defaultValue;
        }
        return v;
    }
}
