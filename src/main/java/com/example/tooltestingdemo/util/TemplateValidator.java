package com.example.tooltestingdemo.util;

import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.constants.TemplateConstants;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.mapper.template.InterfaceTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 模板校验工具类
 */
@Component
@RequiredArgsConstructor
public class TemplateValidator {

    private final InterfaceTemplateMapper templateMapper;

    /**
     * 校验必填项（用于提交审核）
     */
    public void validateRequired(InterfaceTemplateDTO dto, Long excludeId) {
        List<String> errors = new ArrayList<>();

        validateField(errors, dto.getName(), "模板名称不能为空",
                name -> name.length() <= 100, "模板名称不能超过100个字符");

        validateField(errors, dto.getProtocolType(), "协议类型不能为空", null, null);
        if (isSocketProtocol(dto.getProtocolType())) {
            if (!StringUtils.hasText(dto.getFullUrl())
                && !StringUtils.hasText(dto.getBaseUrl())
                && !StringUtils.hasText(dto.getPath())) {
                errors.add("TCP/UDP目标地址不能为空");
            }
        } else {
            validateField(errors, dto.getMethod(), "请求方法不能为空", null, null);
            validateField(errors, dto.getPath(), "请求路径不能为空", null, null);
        }

        checkDuplicate(dto.getName(), dto.getMethod(), excludeId);

        if (!errors.isEmpty()) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY, errors);
        }
    }

    /**
     * 轻量校验（用于保存草稿）
     */
    public void validateDraft(InterfaceTemplateDTO dto, Long excludeId) {
        List<String> errors = new ArrayList<>();

        validateField(errors, dto.getName(), "模板名称不能为空",
                name -> name.length() <= 100, "模板名称不能超过100个字符");

        checkDuplicate(dto.getName(), dto.getMethod(), excludeId);

        if (StringUtils.hasText(dto.getDescription()) && dto.getDescription().length() > 500) {
            errors.add("模板描述不能超过500个字符");
        }

        if (!errors.isEmpty()) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.VALIDATION_FAILED, errors);
        }
    }

    private void validateField(List<String> errors, String value, String requiredMsg,
                               Predicate<String> extraCheck, String extraMsg) {
        if (!StringUtils.hasText(value)) {
            errors.add(requiredMsg);
        } else if (extraCheck != null && !extraCheck.test(value)) {
            errors.add(extraMsg);
        }
    }

    /**
     * 验证模板名称是否重复
     * @param name
     * @param method
     * @param excludeId
     */
    private void checkDuplicate(String name, String method, Long excludeId) {
        if (!StringUtils.hasText(name)) return;
        var existing = templateMapper.selectByNameAndMethod(name, method);
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            throw new TemplateValidationException(
                    TemplateValidationException.ErrorType.NAME_DUPLICATE,
                    "模板名称【" + name + "】已存在，请更换名称"
            );
        }
    }

    private boolean isSocketProtocol(String protocolType) {
        return TemplateConstants.PROTOCOL_TCP.equalsIgnoreCase(protocolType)
            || TemplateConstants.PROTOCOL_UDP.equalsIgnoreCase(protocolType);
    }
}
