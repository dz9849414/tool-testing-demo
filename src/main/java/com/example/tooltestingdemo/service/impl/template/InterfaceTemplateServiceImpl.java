package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.entity.template.*;
import com.example.tooltestingdemo.mapper.template.*;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口模板 Service 实现类
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/impl/template/InterfaceTemplateServiceImpl.java
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterfaceTemplateServiceImpl extends ServiceImpl<InterfaceTemplateMapper, InterfaceTemplate> 
        implements InterfaceTemplateService {

    private final TemplateHeaderMapper headerMapper;
    private final TemplateParameterMapper parameterMapper;
    private final TemplateFormDataMapper formDataMapper;
    private final TemplateAssertionMapper assertionMapper;
    private final TemplatePreProcessorMapper preProcessorMapper;
    private final TemplatePostProcessorMapper postProcessorMapper;
    private final TemplateVariableMapper variableMapper;
    private final TemplateHistoryMapper historyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplateVO createTemplate(InterfaceTemplateDTO dto) {
        // DTO -> Entity
        InterfaceTemplate template = TemplateConverter.toEntity(dto);
        
        // 设置初始版本
        template.setVersion("1.0.0");
        template.setIsLatest(1);
        template.setUseCount(0);
        template.setStatus(1);
        
        // 设置创建人（如果未设置）
        if (template.getCreateId() == null) {
            template.setCreateId(1L);
            template.setCreateName("管理员");
        }
        
        save(template);
        
        // 保存关联数据
        saveRelatedData(template.getId(), dto);
        
        // 记录历史版本
        saveHistory(template, "CREATE", "创建模板");
        
        log.info("创建模板成功: id={}, name={}", template.getId(), template.getName());
        
        // 返回VO
        return getTemplateDetail(template.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(Long id, InterfaceTemplateDTO dto) {
        // 检查模板是否存在
        InterfaceTemplate existing = getById(id);
        if (existing == null) {
            log.warn("更新模板失败: 模板不存在, id={}", id);
            return false;
        }
        
        // DTO -> Entity
        InterfaceTemplate template = TemplateConverter.toEntity(dto);
        template.setId(id);
        
        // 更新模板主表
        updateById(template);
        
        // 删除旧的关联数据
        deleteRelatedData(id);
        
        // 保存新的关联数据
        saveRelatedData(id, dto);
        
        // 记录历史版本
        saveHistory(getById(id), "UPDATE", "更新模板");
        
        log.info("更新模板成功: id={}, name={}", id, template.getName());
        return true;
    }

    @Override
    public InterfaceTemplateVO getTemplateDetail(Long id) {
        InterfaceTemplate template = getById(id);
        if (template == null) {
            return null;
        }
        
        // Entity -> VO
        InterfaceTemplateVO vo = TemplateConverter.toVO(template);
        
        // 加载关联数据
        loadRelatedData(vo);
        
        return vo;
    }

    @Override
    public IPage<InterfaceTemplateVO> pageTemplates(Page<InterfaceTemplate> page,
                                                     Long folderId,
                                                     String keyword,
                                                     String protocolType,
                                                     Integer status) {
        IPage<InterfaceTemplate> entityPage = baseMapper.selectTemplatePage(page, folderId, keyword, protocolType, status);
        
        // 转换为VO分页
        Page<InterfaceTemplateVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setRecords(TemplateConverter.toVOList(entityPage.getRecords()));
        
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplateVO copyTemplate(Long id, String newName) {
        InterfaceTemplate source = getById(id);
        if (source == null) {
            throw new RuntimeException("模板不存在");
        }
        
        // 复制主表数据
        InterfaceTemplate copy = new InterfaceTemplate();
        copy.setName(newName);
        copy.setFolderId(source.getFolderId());
        copy.setDescription(source.getDescription());
        copy.setProtocolType(source.getProtocolType());
        copy.setMethod(source.getMethod());
        copy.setBaseUrl(source.getBaseUrl());
        copy.setPath(source.getPath());
        copy.setFullUrl(source.getFullUrl());
        copy.setAuthType(source.getAuthType());
        copy.setAuthConfig(source.getAuthConfig());
        copy.setContentType(source.getContentType());
        copy.setCharset(source.getCharset());
        copy.setBodyType(source.getBodyType());
        copy.setBodyContent(source.getBodyContent());
        copy.setBodyRawType(source.getBodyRawType());
        copy.setConnectTimeout(source.getConnectTimeout());
        copy.setReadTimeout(source.getReadTimeout());
        copy.setRetryCount(source.getRetryCount());
        copy.setRetryInterval(source.getRetryInterval());
        copy.setCreateId(source.getCreateId());
        copy.setCreateName(source.getCreateName());
        copy.setTeamId(source.getTeamId());
        copy.setVisibility(source.getVisibility());
        copy.setTags(source.getTags());
        copy.setPdmSystemType(source.getPdmSystemType());
        copy.setPdmModule(source.getPdmModule());
        copy.setBusinessScene(source.getBusinessScene());
        copy.setVersion("1.0.0");
        copy.setIsLatest(1);
        copy.setUseCount(0);
        copy.setStatus(1);
        
        save(copy);
        
        // 复制关联数据
        copyRelatedData(id, copy.getId());
        
        // 记录历史
        saveHistory(copy, "COPY", "复制自模板[ID=" + id + "]");
        
        log.info("复制模板成功: newId={}, sourceId={}, name={}", copy.getId(), id, newName);
        
        return getTemplateDetail(copy.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishTemplate(Long id) {
        InterfaceTemplate template = getById(id);
        if (template == null) {
            return false;
        }
        
        template.setStatus(1);
        boolean result = updateById(template);
        
        if (result) {
            saveHistory(template, "PUBLISH", "发布模板");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveTemplate(Long id) {
        InterfaceTemplate template = getById(id);
        if (template == null) {
            return false;
        }
        
        template.setStatus(2);
        boolean result = updateById(template);
        
        if (result) {
            saveHistory(template, "ARCHIVE", "归档模板");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long id) {
        InterfaceTemplate template = getById(id);
        if (template == null) {
            return false;
        }
        
        template.setStatus(3);
        template.setDeleteTime(LocalDateTime.now());
        boolean result = updateById(template);
        
        if (result) {
            saveHistory(template, "DELETE", "删除模板");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveTemplate(Long id, Long folderId) {
        InterfaceTemplate template = getById(id);
        if (template == null) {
            return false;
        }
        
        template.setFolderId(folderId);
        return updateById(template);
    }

    /**
     * 保存关联数据
     */
    private void saveRelatedData(Long templateId, InterfaceTemplateDTO dto) {
        // 保存请求头
        if (!CollectionUtils.isEmpty(dto.getHeaders())) {
            List<TemplateHeader> headers = TemplateConverter.toHeaderEntityList(dto.getHeaders());
            headers.forEach(h -> h.setTemplateId(templateId));
            headerMapper.batchInsert(headers);
        }
        
        // 保存参数
        if (!CollectionUtils.isEmpty(dto.getParameters())) {
            List<TemplateParameter> parameters = TemplateConverter.toParameterEntityList(dto.getParameters());
            parameters.forEach(p -> p.setTemplateId(templateId));
            parameterMapper.batchInsert(parameters);
        }
        
        // 保存FormData
        if (!CollectionUtils.isEmpty(dto.getFormDataList())) {
            List<TemplateFormData> formDataList = TemplateConverter.toFormDataEntityList(dto.getFormDataList());
            formDataList.forEach(f -> f.setTemplateId(templateId));
            formDataMapper.batchInsert(formDataList);
        }
        
        // 保存断言
        if (!CollectionUtils.isEmpty(dto.getAssertions())) {
            List<TemplateAssertion> assertions = TemplateConverter.toAssertionEntityList(dto.getAssertions());
            assertions.forEach(a -> a.setTemplateId(templateId));
            assertionMapper.batchInsert(assertions);
        }
        
        // 保存前置处理器
        if (!CollectionUtils.isEmpty(dto.getPreProcessors())) {
            List<TemplatePreProcessor> preProcessors = TemplateConverter.toPreProcessorEntityList(dto.getPreProcessors());
            preProcessors.forEach(p -> p.setTemplateId(templateId));
            preProcessorMapper.batchInsert(preProcessors);
        }
        
        // 保存后置处理器
        if (!CollectionUtils.isEmpty(dto.getPostProcessors())) {
            List<TemplatePostProcessor> postProcessors = TemplateConverter.toPostProcessorEntityList(dto.getPostProcessors());
            postProcessors.forEach(p -> p.setTemplateId(templateId));
            postProcessorMapper.batchInsert(postProcessors);
        }
        
        // 保存变量
        if (!CollectionUtils.isEmpty(dto.getVariables())) {
            List<TemplateVariable> variables = TemplateConverter.toVariableEntityList(dto.getVariables());
            variables.forEach(v -> v.setTemplateId(templateId));
            variableMapper.batchInsert(variables);
        }
    }

    /**
     * 加载关联数据到VO
     */
    private void loadRelatedData(InterfaceTemplateVO vo) {
        Long templateId = vo.getId();
        
        // 加载请求头
        List<TemplateHeader> headers = headerMapper.selectByTemplateId(templateId);
        vo.setHeaders(TemplateConverter.toHeaderVOList(headers));
        
        // 加载参数
        List<TemplateParameter> parameters = parameterMapper.selectByTemplateId(templateId);
        vo.setParameters(TemplateConverter.toParameterVOList(parameters));
        
        // 加载FormData
        List<TemplateFormData> formDataList = formDataMapper.selectByTemplateId(templateId);
        vo.setFormDataList(TemplateConverter.toFormDataVOList(formDataList));
        
        // 加载断言
        List<TemplateAssertion> assertions = assertionMapper.selectByTemplateId(templateId);
        vo.setAssertions(TemplateConverter.toAssertionVOList(assertions));
        
        // 加载前置处理器
        List<TemplatePreProcessor> preProcessors = preProcessorMapper.selectByTemplateId(templateId);
        vo.setPreProcessors(TemplateConverter.toPreProcessorVOList(preProcessors));
        
        // 加载后置处理器
        List<TemplatePostProcessor> postProcessors = postProcessorMapper.selectByTemplateId(templateId);
        vo.setPostProcessors(TemplateConverter.toPostProcessorVOList(postProcessors));
        
        // 加载变量
        List<TemplateVariable> variables = variableMapper.selectByTemplateId(templateId);
        vo.setVariables(TemplateConverter.toVariableVOList(variables));
    }

    /**
     * 复制关联数据
     */
    private void copyRelatedData(Long sourceTemplateId, Long targetTemplateId) {
        // 复制请求头
        List<TemplateHeader> headers = headerMapper.selectByTemplateId(sourceTemplateId);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(h -> {
                h.setId(null);
                h.setTemplateId(targetTemplateId);
            });
            headerMapper.batchInsert(headers);
        }
        
        // 复制参数
        List<TemplateParameter> parameters = parameterMapper.selectByTemplateId(sourceTemplateId);
        if (!CollectionUtils.isEmpty(parameters)) {
            parameters.forEach(p -> {
                p.setId(null);
                p.setTemplateId(targetTemplateId);
            });
            parameterMapper.batchInsert(parameters);
        }
        
        // 复制FormData
        List<TemplateFormData> formDataList = formDataMapper.selectByTemplateId(sourceTemplateId);
        if (!CollectionUtils.isEmpty(formDataList)) {
            formDataList.forEach(f -> {
                f.setId(null);
                f.setTemplateId(targetTemplateId);
            });
            formDataMapper.batchInsert(formDataList);
        }
        
        // 复制断言
        List<TemplateAssertion> assertions = assertionMapper.selectByTemplateId(sourceTemplateId);
        if (!CollectionUtils.isEmpty(assertions)) {
            assertions.forEach(a -> {
                a.setId(null);
                a.setTemplateId(targetTemplateId);
            });
            assertionMapper.batchInsert(assertions);
        }
        
        // 复制前置处理器
        List<TemplatePreProcessor> preProcessors = preProcessorMapper.selectByTemplateId(sourceTemplateId);
        if (!CollectionUtils.isEmpty(preProcessors)) {
            preProcessors.forEach(p -> {
                p.setId(null);
                p.setTemplateId(targetTemplateId);
            });
            preProcessorMapper.batchInsert(preProcessors);
        }
        
        // 复制后置处理器
        List<TemplatePostProcessor> postProcessors = postProcessorMapper.selectByTemplateId(sourceTemplateId);
        if (!CollectionUtils.isEmpty(postProcessors)) {
            postProcessors.forEach(p -> {
                p.setId(null);
                p.setTemplateId(targetTemplateId);
            });
            postProcessorMapper.batchInsert(postProcessors);
        }
        
        // 复制变量
        List<TemplateVariable> variables = variableMapper.selectByTemplateId(sourceTemplateId);
        if (!CollectionUtils.isEmpty(variables)) {
            variables.forEach(v -> {
                v.setId(null);
                v.setTemplateId(targetTemplateId);
            });
            variableMapper.batchInsert(variables);
        }
    }

    /**
     * 删除关联数据
     */
    private void deleteRelatedData(Long templateId) {
        headerMapper.deleteByTemplateId(templateId);
        parameterMapper.deleteByTemplateId(templateId);
        formDataMapper.deleteByTemplateId(templateId);
        assertionMapper.deleteByTemplateId(templateId);
        preProcessorMapper.deleteByTemplateId(templateId);
        postProcessorMapper.deleteByTemplateId(templateId);
        variableMapper.deleteByTemplateId(templateId);
    }

    /**
     * 保存历史版本
     */
    private void saveHistory(InterfaceTemplate template, String operationType, String changeSummary) {
        TemplateHistory history = new TemplateHistory();
        history.setTemplateId(template.getId());
        history.setVersion(template.getVersion());
        history.setVersionType("AUTO");
        history.setChangeSummary(changeSummary);
        history.setOperationType(operationType);
        history.setCanRollback(1);
        
        // 设置操作人（如果未设置）
        if (history.getOperatorId() == null) {
            history.setOperatorId(1L);
            history.setOperatorName("管理员");
        }
        
        historyMapper.insert(history);
    }
}
