package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.*;
import com.example.tooltestingdemo.mapper.template.*;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
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
    public InterfaceTemplate createTemplate(InterfaceTemplate template,
                                             List<TemplateHeader> headers,
                                             List<TemplateParameter> parameters,
                                             List<TemplateFormData> formDataList,
                                             List<TemplateAssertion> assertions,
                                             List<TemplatePreProcessor> preProcessors,
                                             List<TemplatePostProcessor> postProcessors,
                                             List<TemplateVariable> variables) {
        // 设置初始版本
        template.setVersion("1.0.0");
        template.setIsLatest(1);
        template.setUseCount(0);
        template.setStatus(1);
        
        save(template);
        
        // 保存关联数据
        saveRelatedData(template.getId(), headers, parameters, formDataList, 
                assertions, preProcessors, postProcessors, variables);
        
        // 记录历史版本
        saveHistory(template, "CREATE", "创建模板");
        
        log.info("创建模板成功: id={}, name={}", template.getId(), template.getName());
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(InterfaceTemplate template,
                                  List<TemplateHeader> headers,
                                  List<TemplateParameter> parameters,
                                  List<TemplateFormData> formDataList,
                                  List<TemplateAssertion> assertions,
                                  List<TemplatePreProcessor> preProcessors,
                                  List<TemplatePostProcessor> postProcessors,
                                  List<TemplateVariable> variables) {
        // 更新模板主表
        updateById(template);
        
        Long templateId = template.getId();
        
        // 删除旧的关联数据
        deleteRelatedData(templateId);
        
        // 保存新的关联数据
        saveRelatedData(templateId, headers, parameters, formDataList,
                assertions, preProcessors, postProcessors, variables);
        
        // 记录历史版本
        saveHistory(template, "UPDATE", "更新模板");
        
        log.info("更新模板成功: id={}, name={}", template.getId(), template.getName());
        return true;
    }

    @Override
    public InterfaceTemplate getTemplateDetail(Long id) {
        InterfaceTemplate template = getById(id);
        if (template == null) {
            return null;
        }
        
        // 加载关联数据
        template.setId(id); // 保持ID一致
        
        return template;
    }

    @Override
    public IPage<InterfaceTemplate> pageTemplates(Page<InterfaceTemplate> page,
                                                   Long folderId,
                                                   String keyword,
                                                   String protocolType,
                                                   Integer status) {
        return baseMapper.selectTemplatePage(page, folderId, keyword, protocolType, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplate copyTemplate(Long id, String newName) {
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
        copy.setOwnerId(source.getOwnerId());
        copy.setOwnerName(source.getOwnerName());
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
        
        // TODO: 复制关联数据
        
        // 记录历史
        saveHistory(copy, "COPY", "复制自模板[ID=" + id + "]");
        
        log.info("复制模板成功: newId={}, sourceId={}, name={}", copy.getId(), id, newName);
        return copy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishTemplate(Long id) {
        InterfaceTemplate template = new InterfaceTemplate();
        template.setId(id);
        template.setStatus(1);
        boolean result = updateById(template);
        
        if (result) {
            saveHistory(getById(id), "PUBLISH", "发布模板");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveTemplate(Long id) {
        InterfaceTemplate template = new InterfaceTemplate();
        template.setId(id);
        template.setStatus(2);
        boolean result = updateById(template);
        
        if (result) {
            saveHistory(getById(id), "ARCHIVE", "归档模板");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long id) {
        InterfaceTemplate template = new InterfaceTemplate();
        template.setId(id);
        template.setStatus(3);
        template.setDeleteTime(LocalDateTime.now());
        boolean result = updateById(template);
        
        if (result) {
            saveHistory(getById(id), "DELETE", "删除模板");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveTemplate(Long id, Long folderId) {
        InterfaceTemplate template = new InterfaceTemplate();
        template.setId(id);
        template.setFolderId(folderId);
        return updateById(template);
    }

    /**
     * 保存关联数据
     */
    private void saveRelatedData(Long templateId,
                                  List<TemplateHeader> headers,
                                  List<TemplateParameter> parameters,
                                  List<TemplateFormData> formDataList,
                                  List<TemplateAssertion> assertions,
                                  List<TemplatePreProcessor> preProcessors,
                                  List<TemplatePostProcessor> postProcessors,
                                  List<TemplateVariable> variables) {
        
        // 保存请求头
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(h -> h.setTemplateId(templateId));
            headerMapper.batchInsert(headers);
        }
        
        // 保存参数
        if (!CollectionUtils.isEmpty(parameters)) {
            parameters.forEach(p -> p.setTemplateId(templateId));
            parameterMapper.batchInsert(parameters);
        }
        
        // 保存FormData
        if (!CollectionUtils.isEmpty(formDataList)) {
            formDataList.forEach(f -> f.setTemplateId(templateId));
            formDataMapper.batchInsert(formDataList);
        }
        
        // 保存断言
        if (!CollectionUtils.isEmpty(assertions)) {
            assertions.forEach(a -> a.setTemplateId(templateId));
            assertionMapper.batchInsert(assertions);
        }
        
        // 保存前置处理器
        if (!CollectionUtils.isEmpty(preProcessors)) {
            preProcessors.forEach(p -> p.setTemplateId(templateId));
            preProcessorMapper.batchInsert(preProcessors);
        }
        
        // 保存后置处理器
        if (!CollectionUtils.isEmpty(postProcessors)) {
            postProcessors.forEach(p -> p.setTemplateId(templateId));
            postProcessorMapper.batchInsert(postProcessors);
        }
        
        // 保存变量
        if (!CollectionUtils.isEmpty(variables)) {
            variables.forEach(v -> v.setTemplateId(templateId));
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
        historyMapper.insert(history);
    }
}
