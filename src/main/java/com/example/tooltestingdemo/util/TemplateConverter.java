package com.example.tooltestingdemo.util;

import com.example.tooltestingdemo.dto.*;
import com.example.tooltestingdemo.entity.template.*;
import com.example.tooltestingdemo.entity.template.TemplateFavorite;
import com.example.tooltestingdemo.vo.*;
import com.example.tooltestingdemo.vo.TemplateFavoriteVO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板对象转换工具类
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/util/TemplateConverter.java
 */
public class TemplateConverter {

    // ==================== InterfaceTemplate 转换 ====================

    /**
     * DTO -> Entity
     */
    public static InterfaceTemplate toEntity(InterfaceTemplateDTO dto) {
        if (dto == null) {
            return null;
        }
        InterfaceTemplate entity = new InterfaceTemplate();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * Entity -> VO
     */
    public static InterfaceTemplateVO toVO(InterfaceTemplate entity) {
        if (entity == null) {
            return null;
        }
        InterfaceTemplateVO vo = new InterfaceTemplateVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * Entity列表 -> VO列表
     */
    public static List<InterfaceTemplateVO> toVOList(List<InterfaceTemplate> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateHeader 转换 ====================

    public static TemplateHeader toEntity(TemplateHeaderDTO dto) {
        if (dto == null) {
            return null;
        }
        TemplateHeader entity = new TemplateHeader();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public static List<TemplateHeader> toHeaderEntityList(List<TemplateHeaderDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(TemplateConverter::toEntity)
                .collect(Collectors.toList());
    }

    public static TemplateHeaderVO toVO(TemplateHeader entity) {
        if (entity == null) {
            return null;
        }
        TemplateHeaderVO vo = new TemplateHeaderVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateHeaderVO> toHeaderVOList(List<TemplateHeader> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateParameter 转换 ====================

    public static TemplateParameter toEntity(TemplateParameterDTO dto) {
        if (dto == null) {
            return null;
        }
        TemplateParameter entity = new TemplateParameter();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public static List<TemplateParameter> toParameterEntityList(List<TemplateParameterDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(TemplateConverter::toEntity)
                .collect(Collectors.toList());
    }

    public static TemplateParameterVO toVO(TemplateParameter entity) {
        if (entity == null) {
            return null;
        }
        TemplateParameterVO vo = new TemplateParameterVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateParameterVO> toParameterVOList(List<TemplateParameter> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateFormData 转换 ====================

    public static TemplateFormData toEntity(TemplateFormDataDTO dto) {
        if (dto == null) {
            return null;
        }
        TemplateFormData entity = new TemplateFormData();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public static List<TemplateFormData> toFormDataEntityList(List<TemplateFormDataDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(TemplateConverter::toEntity)
                .collect(Collectors.toList());
    }

    public static TemplateFormDataVO toVO(TemplateFormData entity) {
        if (entity == null) {
            return null;
        }
        TemplateFormDataVO vo = new TemplateFormDataVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateFormDataVO> toFormDataVOList(List<TemplateFormData> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateAssertion 转换 ====================

    public static TemplateAssertion toEntity(TemplateAssertionDTO dto) {
        if (dto == null) {
            return null;
        }
        TemplateAssertion entity = new TemplateAssertion();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public static List<TemplateAssertion> toAssertionEntityList(List<TemplateAssertionDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(TemplateConverter::toEntity)
                .collect(Collectors.toList());
    }

    public static TemplateAssertionVO toVO(TemplateAssertion entity) {
        if (entity == null) {
            return null;
        }
        TemplateAssertionVO vo = new TemplateAssertionVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateAssertionVO> toAssertionVOList(List<TemplateAssertion> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplatePreProcessor 转换 ====================

    public static TemplatePreProcessor toEntity(TemplatePreProcessorDTO dto) {
        if (dto == null) {
            return null;
        }
        TemplatePreProcessor entity = new TemplatePreProcessor();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public static List<TemplatePreProcessor> toPreProcessorEntityList(List<TemplatePreProcessorDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(TemplateConverter::toEntity)
                .collect(Collectors.toList());
    }

    public static TemplatePreProcessorVO toVO(TemplatePreProcessor entity) {
        if (entity == null) {
            return null;
        }
        TemplatePreProcessorVO vo = new TemplatePreProcessorVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplatePreProcessorVO> toPreProcessorVOList(List<TemplatePreProcessor> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplatePostProcessor 转换 ====================

    public static TemplatePostProcessor toEntity(TemplatePostProcessorDTO dto) {
        if (dto == null) {
            return null;
        }
        TemplatePostProcessor entity = new TemplatePostProcessor();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public static List<TemplatePostProcessor> toPostProcessorEntityList(List<TemplatePostProcessorDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(TemplateConverter::toEntity)
                .collect(Collectors.toList());
    }

    public static TemplatePostProcessorVO toVO(TemplatePostProcessor entity) {
        if (entity == null) {
            return null;
        }
        TemplatePostProcessorVO vo = new TemplatePostProcessorVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplatePostProcessorVO> toPostProcessorVOList(List<TemplatePostProcessor> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateVariable 转换 ====================

    public static TemplateVariable toEntity(TemplateVariableDTO dto) {
        if (dto == null) {
            return null;
        }
        TemplateVariable entity = new TemplateVariable();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public static List<TemplateVariable> toVariableEntityList(List<TemplateVariableDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(TemplateConverter::toEntity)
                .collect(Collectors.toList());
    }

    public static TemplateVariableVO toVO(TemplateVariable entity) {
        if (entity == null) {
            return null;
        }
        TemplateVariableVO vo = new TemplateVariableVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateVariableVO> toVariableVOList(List<TemplateVariable> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateEnvironment 转换 ====================

    public static TemplateEnvironmentVO toVO(TemplateEnvironment entity) {
        if (entity == null) {
            return null;
        }
        TemplateEnvironmentVO vo = new TemplateEnvironmentVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateEnvironmentVO> toEnvironmentVOList(List<TemplateEnvironment> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateFolder 转换 ====================

    public static TemplateFolderVO toVO(TemplateFolder entity) {
        if (entity == null) {
            return null;
        }
        TemplateFolderVO vo = new TemplateFolderVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateFolderVO> toFolderVOList(List<TemplateFolder> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateHistory 转换 ====================

    public static TemplateHistoryVO toVO(TemplateHistory entity) {
        if (entity == null) {
            return null;
        }
        TemplateHistoryVO vo = new TemplateHistoryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateHistoryVO> toHistoryVOList(List<TemplateHistory> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }

    // ==================== TemplateFavorite 转换 ====================

    public static TemplateFavoriteVO toVO(TemplateFavorite entity) {
        if (entity == null) {
            return null;
        }
        TemplateFavoriteVO vo = new TemplateFavoriteVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public static List<TemplateFavoriteVO> toFavoriteVOList(List<TemplateFavorite> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(TemplateConverter::toVO)
                .collect(Collectors.toList());
    }
}
