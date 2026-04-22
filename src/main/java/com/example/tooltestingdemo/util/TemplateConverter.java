package com.example.tooltestingdemo.util;

import com.example.tooltestingdemo.dto.*;
import com.example.tooltestingdemo.entity.template.*;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模板对象转换工具类 - 使用泛型减少重复代码
 */
public class TemplateConverter {

    // ==================== 通用转换方法 ====================

    private static <S, T> T convert(S source, Class<T> targetClass) {
        if (source == null) return null;
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.CONVERT_ERROR, "对象转换失败", e);
        }
    }

    private static <S, T> List<T> convertList(List<S> sources, Function<S, T> converter) {
        if (CollectionUtils.isEmpty(sources)) return Collections.emptyList();
        return sources.stream().map(converter).collect(Collectors.toList());
    }

    // ==================== InterfaceTemplate ====================

    public static InterfaceTemplate toEntity(InterfaceTemplateDTO dto) {
        InterfaceTemplate entity = convert(dto, InterfaceTemplate.class);
        if (entity != null && entity.getBodyContent() == null && dto != null) {
            entity.setBodyContent(dto.getBody());
        }
        return entity;
    }

    public static InterfaceTemplateVO toVO(InterfaceTemplate entity) {
        InterfaceTemplateVO vo = convert(entity, InterfaceTemplateVO.class);
        if (vo != null) {
            vo.setBody(vo.getBodyContent());
        }
        return vo;
    }

    public static List<InterfaceTemplateVO> toVOList(List<InterfaceTemplate> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateHeader ====================

    public static TemplateHeader toEntity(TemplateHeaderDTO dto) {
        return convert(dto, TemplateHeader.class);
    }

    public static TemplateHeaderVO toVO(TemplateHeader entity) {
        return convert(entity, TemplateHeaderVO.class);
    }

    public static List<TemplateHeader> toHeaderEntityList(List<TemplateHeaderDTO> dtos) {
        return convertList(dtos, TemplateConverter::toEntity);
    }

    public static List<TemplateHeaderVO> toHeaderVOList(List<TemplateHeader> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateParameter ====================

    public static TemplateParameter toEntity(TemplateParameterDTO dto) {
        return convert(dto, TemplateParameter.class);
    }

    public static TemplateParameterVO toVO(TemplateParameter entity) {
        return convert(entity, TemplateParameterVO.class);
    }

    public static List<TemplateParameter> toParameterEntityList(List<TemplateParameterDTO> dtos) {
        return convertList(dtos, TemplateConverter::toEntity);
    }

    public static List<TemplateParameterVO> toParameterVOList(List<TemplateParameter> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateFormData ====================

    public static TemplateFormData toEntity(TemplateFormDataDTO dto) {
        return convert(dto, TemplateFormData.class);
    }

    public static TemplateFormDataVO toVO(TemplateFormData entity) {
        return convert(entity, TemplateFormDataVO.class);
    }

    public static List<TemplateFormData> toFormDataEntityList(List<TemplateFormDataDTO> dtos) {
        return convertList(dtos, TemplateConverter::toEntity);
    }

    public static List<TemplateFormDataVO> toFormDataVOList(List<TemplateFormData> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateAssertion ====================

    public static TemplateAssertion toEntity(TemplateAssertionDTO dto) {
        return convert(dto, TemplateAssertion.class);
    }

    public static TemplateAssertionVO toVO(TemplateAssertion entity) {
        return convert(entity, TemplateAssertionVO.class);
    }

    public static List<TemplateAssertion> toAssertionEntityList(List<TemplateAssertionDTO> dtos) {
        return convertList(dtos, TemplateConverter::toEntity);
    }

    public static List<TemplateAssertionVO> toAssertionVOList(List<TemplateAssertion> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplatePreProcessor ====================

    public static TemplatePreProcessor toEntity(TemplatePreProcessorDTO dto) {
        return convert(dto, TemplatePreProcessor.class);
    }

    public static TemplatePreProcessorVO toVO(TemplatePreProcessor entity) {
        return convert(entity, TemplatePreProcessorVO.class);
    }

    public static List<TemplatePreProcessor> toPreProcessorEntityList(List<TemplatePreProcessorDTO> dtos) {
        return convertList(dtos, TemplateConverter::toEntity);
    }

    public static List<TemplatePreProcessorVO> toPreProcessorVOList(List<TemplatePreProcessor> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplatePostProcessor ====================

    public static TemplatePostProcessor toEntity(TemplatePostProcessorDTO dto) {
        return convert(dto, TemplatePostProcessor.class);
    }

    public static TemplatePostProcessorVO toVO(TemplatePostProcessor entity) {
        return convert(entity, TemplatePostProcessorVO.class);
    }

    public static List<TemplatePostProcessor> toPostProcessorEntityList(List<TemplatePostProcessorDTO> dtos) {
        return convertList(dtos, TemplateConverter::toEntity);
    }

    public static List<TemplatePostProcessorVO> toPostProcessorVOList(List<TemplatePostProcessor> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateVariable ====================

    public static TemplateVariable toEntity(TemplateVariableDTO dto) {
        return convert(dto, TemplateVariable.class);
    }

    public static TemplateVariableVO toVO(TemplateVariable entity) {
        return convert(entity, TemplateVariableVO.class);
    }

    public static List<TemplateVariable> toVariableEntityList(List<TemplateVariableDTO> dtos) {
        return convertList(dtos, TemplateConverter::toEntity);
    }

    public static List<TemplateVariableVO> toVariableVOList(List<TemplateVariable> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateEnvironment ====================

    public static TemplateEnvironmentVO toVO(TemplateEnvironment entity) {
        return convert(entity, TemplateEnvironmentVO.class);
    }

    public static List<TemplateEnvironmentVO> toEnvironmentVOList(List<TemplateEnvironment> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateFolder ====================

    public static TemplateFolderVO toVO(TemplateFolder entity) {
        return convert(entity, TemplateFolderVO.class);
    }

    public static List<TemplateFolderVO> toFolderVOList(List<TemplateFolder> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateHistory ====================

    public static TemplateHistoryVO toVO(TemplateHistory entity) {
        return convert(entity, TemplateHistoryVO.class);
    }

    public static List<TemplateHistoryVO> toHistoryVOList(List<TemplateHistory> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateFavorite ====================

    public static TemplateFavoriteVO toVO(TemplateFavorite entity) {
        return convert(entity, TemplateFavoriteVO.class);
    }

    public static List<TemplateFavoriteVO> toFavoriteVOList(List<TemplateFavorite> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== TemplateFile ====================

    public static TemplateFile toEntity(TemplateFileDTO dto) {
        return convert(dto, TemplateFile.class);
    }

    public static TemplateFileVO toVO(TemplateFile entity) {
        if (entity == null) return null;
        TemplateFileVO vo = convert(entity, TemplateFileVO.class);
        vo.setFileSizeDisplay(formatFileSize(entity.getFileSize()));
        return vo;
    }

    public static List<TemplateFile> toFileEntityList(List<TemplateFileDTO> dtos) {
        return convertList(dtos, TemplateConverter::toEntity);
    }

    public static List<TemplateFileVO> toFileVOList(List<TemplateFile> entities) {
        return convertList(entities, TemplateConverter::toVO);
    }

    // ==================== 工具方法 ====================

    private static String formatFileSize(Long size) {
        if (size == null || size < 0) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}
