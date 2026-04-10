package com.example.tooltestingdemo.util;

import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.mapper.template.InterfaceTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

        // 校验名称
        if (!StringUtils.hasText(dto.getName())) {
            errors.add("模板名称不能为空");
        } else if (dto.getName().length() > 100) {
            errors.add("模板名称不能超过100个字符");
        } else {
            // 校验名称是否重复
            if (isNameDuplicate(dto.getName(), excludeId)) {
                throw new TemplateValidationException(
                    TemplateValidationException.ErrorType.NAME_DUPLICATE,
                    "模板名称【" + dto.getName() + "】已存在，请更换名称"
                );
            }
        }

        // 校验协议类型
        if (!StringUtils.hasText(dto.getProtocolType())) {
            errors.add("协议类型不能为空");
        }

        // 校验请求方法
        if (!StringUtils.hasText(dto.getMethod())) {
            errors.add("请求方法不能为空");
        }

        // 校验路径
        if (!StringUtils.hasText(dto.getPath())) {
            errors.add("请求路径不能为空");
        }

        // 如果有错误，抛出异常
        if (!errors.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY,
                errors
            );
        }
    }

    /**
     * 轻量校验（用于保存草稿）
     * 只校验最基本的规则
     */
    public void validateDraft(InterfaceTemplateDTO dto, Long excludeId) {
        List<String> errors = new ArrayList<>();

        // 校验名称（草稿也要求名称必填，只是其他字段不强制）
        if (!StringUtils.hasText(dto.getName())) {
            errors.add("模板名称不能为空");
        } else {
            if (dto.getName().length() > 100) {
                errors.add("模板名称不能超过100个字符");
            }
            // 校验名称是否重复（关键！）
            if (isNameDuplicate(dto.getName(), excludeId)) {
                throw new TemplateValidationException(
                    TemplateValidationException.ErrorType.NAME_DUPLICATE,
                    "模板名称【" + dto.getName() + "】已存在，请更换名称"
                );
            }
        }

        // 如果有描述，校验长度
        if (StringUtils.hasText(dto.getDescription()) && dto.getDescription().length() > 500) {
            errors.add("模板描述不能超过500个字符");
        }

        if (!errors.isEmpty()) {
            throw new TemplateValidationException(
                TemplateValidationException.ErrorType.VALIDATION_FAILED,
                errors
            );
        }
    }

    /**
     * 检查名称是否重复
     */
    private boolean isNameDuplicate(String name, Long excludeId) {
        if (excludeId != null) {
            // 更新时排除自身
            return templateMapper.selectByNameAndMethod(name, null) != null 
                && !excludeId.equals(templateMapper.selectByNameAndMethod(name, null).getId());
        }
        return templateMapper.selectByNameAndMethod(name, null) != null;
    }
}
