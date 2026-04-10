package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.entity.template.*;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.mapper.template.*;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.util.TemplateValidator;
import com.example.tooltestingdemo.util.VersionGenerator;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 接口模板 Service 实现类
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
    private final TemplateFileMapper fileMapper;
    private final TemplateValidator templateValidator;

    // ========== 基础 CRUD ==========

    @Override
    public InterfaceTemplateVO createTemplate(InterfaceTemplateDTO dto) {
        return saveDraft(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(Long id, InterfaceTemplateDTO dto) {
        return Optional.ofNullable(getById(id)).map(template -> {
            BeanUtils.copyProperties(dto, template, "id", "version", "status", "createTime");
            updateById(template);
            deleteRelatedData(id);
            saveRelatedData(id, dto);
            saveHistory(template, "UPDATE", "更新模板");
            log.info("更新模板成功: id={}", id);
            return true;
        }).orElse(false);
    }

    @Override
    public InterfaceTemplateVO getTemplateDetail(Long id) {
        return Optional.ofNullable(getById(id)).map(template -> {
            InterfaceTemplateVO vo = TemplateConverter.toVO(template);
            loadRelatedData(vo);
            return vo;
        }).orElse(null);
    }

    @Override
    public IPage<InterfaceTemplateVO> pageTemplates(Page<InterfaceTemplate> page, Long folderId, 
                                                     String keyword, String protocolType, Integer status) {
        IPage<InterfaceTemplate> entityPage = baseMapper.selectTemplatePage(page, folderId, keyword, protocolType, status);
        
        Page<InterfaceTemplateVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setRecords(entityPage.getRecords().stream()
            .map(template -> {
                InterfaceTemplateVO vo = TemplateConverter.toVO(template);
                List<TemplateFile> files = fileMapper.selectByTemplateId(template.getId());
                vo.setFileCount(files.size());
                vo.setHasRequestFile(files.stream().anyMatch(f -> "REQUEST".equals(f.getFileCategory())) ? 1 : 0);
                vo.setHasResponseFile(files.stream().anyMatch(f -> "RESPONSE".equals(f.getFileCategory())) ? 1 : 0);
                return vo;
            }).collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplateVO copyTemplate(Long id, String newName) {
        InterfaceTemplate source = Optional.ofNullable(getById(id))
            .orElseThrow(() -> new RuntimeException("模板不存在"));
        
        InterfaceTemplate copy = new InterfaceTemplate();
        BeanUtils.copyProperties(source, copy, "id", "version", "status", "createTime", "updateTime", "useCount");
        copy.setName(newName);
        copy.setVersion(VersionGenerator.generateInitialVersion());
        copy.setStatus(TemplateEnums.TemplateStatus.DRAFT.getCode());
        copy.setUseCount(0);
        save(copy);
        
        copyRelatedData(id, copy.getId());
        saveHistory(copy, "COPY", "复制自模板[ID=" + id + "]");
        
        log.info("复制模板成功: newId={}, sourceId={}", copy.getId(), id);
        return getTemplateDetail(copy.getId());
    }

    // ========== 状态变更（一行一个） ==========

    @Override
    public boolean publishTemplate(Long id) {
        return updateStatus(id, TemplateEnums.TemplateStatus.PUBLISHED.getCode(), "PUBLISH", "发布模板");
    }

    @Override
    public boolean archiveTemplate(Long id) {
        return updateStatus(id, TemplateEnums.TemplateStatus.ARCHIVED.getCode(), "ARCHIVE", "归档模板");
    }

    @Override
    public boolean moveTemplate(Long id, Long folderId) {
        return updateTemplate(id, t -> t.setFolderId(folderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long id) {
        return updateTemplate(id, t -> {
            t.setStatus(TemplateEnums.TemplateStatus.DISABLED.getCode());
            t.setIsDeleted(1);
            t.setDeletedTime(LocalDateTime.now());
        }, "DELETE", "删除模板");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, List<Long>> batchDeleteTemplates(Long[] ids) {
        Map<Boolean, List<Long>> result = Stream.of(ids)
            .collect(Collectors.partitioningBy(this::deleteTemplate));
        
        Map<String, List<Long>> map = new HashMap<>();
        map.put("success", result.get(true));
        map.put("fail", result.get(false));
        log.info("批量删除完成: 成功={}, 失败={}", result.get(true).size(), result.get(false).size());
        return map;
    }

    // ========== 草稿与审核 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplateVO saveDraft(InterfaceTemplateDTO dto) {
        return saveDraftInternal(dto, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplateVO saveDraft(Long id, InterfaceTemplateDTO dto) {
        return saveDraftInternal(dto, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplateVO submitForReview(Long id, InterfaceTemplateDTO dto) {
        InterfaceTemplate existing = Optional.ofNullable(getById(id))
            .orElseThrow(() -> new RuntimeException("模板不存在"));
        
        Optional.ofNullable(TemplateEnums.TemplateStatus.getByCode(existing.getStatus()))
            .filter(TemplateEnums.TemplateStatus::isSubmittable)
            .orElseThrow(() -> new RuntimeException("当前状态不可提交审核"));
        
        templateValidator.validateRequired(dto, id);
        
        doSaveTemplate(id, dto, TemplateEnums.TemplateStatus.PENDING_REVIEW.getCode(),
            VersionGenerator.incrementMajorVersion(existing.getVersion()), "提交审核");
        
        return getTemplateDetail(id);
    }

    @Override
    public boolean approveTemplate(Long id) {
        return checkAndUpdateStatus(id, TemplateEnums.TemplateStatus.PENDING_REVIEW.getCode(),
            TemplateEnums.TemplateStatus.PUBLISHED.getCode(), "只有待审核状态的模板可以审核通过", "PUBLISH", "审核通过");
    }

    @Override
    public boolean rejectTemplate(Long id, String reason) {
        return checkAndUpdateStatus(id, TemplateEnums.TemplateStatus.PENDING_REVIEW.getCode(),
            TemplateEnums.TemplateStatus.REJECTED.getCode(), "只有待审核状态的模板可以驳回", "ARCHIVE", 
            "审核驳回，原因：" + Optional.ofNullable(reason).orElse("无"));
    }

    // ========== 私有方法 ==========

    private InterfaceTemplateVO saveDraftInternal(InterfaceTemplateDTO dto, Long id) {
        boolean isUpdate = id != null;
        
        InterfaceTemplate existing = isUpdate ? getById(id) : null;
        if (isUpdate && existing == null) {
            throw new RuntimeException("模板不存在");
        }
        
        templateValidator.validateDraft(dto, id);
        
        String newVersion = isUpdate 
            ? VersionGenerator.incrementMinorVersion(existing.getVersion())
            : VersionGenerator.generateInitialVersion();
        
        if (isUpdate) {
            Optional.ofNullable(TemplateEnums.TemplateStatus.getByCode(existing.getStatus()))
                .filter(TemplateEnums.TemplateStatus::isEditable)
                .orElseThrow(() -> new RuntimeException("当前状态不可编辑"));
            deleteRelatedData(id);
        }
        
        Long templateId = doSaveTemplate(id, dto, TemplateEnums.TemplateStatus.DRAFT.getCode(), newVersion, 
            isUpdate ? "更新草稿" : "创建草稿");
        
        return getTemplateDetail(templateId);
    }

    private Long doSaveTemplate(Long id, InterfaceTemplateDTO dto, Integer status, String version, String desc) {
        InterfaceTemplate template = new InterfaceTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setId(id);
        template.setStatus(status);
        template.setVersion(version);
        
        if (id == null) {
            template.setIsLatest(1);
            template.setUseCount(0);
            if (template.getCreateId() == null) {
                template.setCreateId(1L);
                template.setCreateName("管理员");
            }
        }
        
        saveOrUpdate(template);
        saveRelatedData(template.getId(), dto);
        saveHistory(template, id == null ? "CREATE" : "UPDATE", desc + "，版本号：" + version);
        
        log.info("{}成功: id={}, version={}", desc, template.getId(), version);
        return template.getId();
    }

    private boolean updateStatus(Long id, Integer status, String opType, String desc) {
        return updateTemplate(id, t -> t.setStatus(status), opType, desc);
    }

    private boolean checkAndUpdateStatus(Long id, Integer requiredStatus, Integer newStatus, 
                                          String errorMsg, String opType, String desc) {
        return Optional.ofNullable(getById(id)).map(t -> {
            if (!requiredStatus.equals(t.getStatus())) {
                throw new RuntimeException(errorMsg);
            }
            t.setStatus(newStatus);
            updateById(t);
            saveHistory(t, opType, desc);
            return true;
        }).orElse(false);
    }

    private boolean updateTemplate(Long id, Consumer<InterfaceTemplate> updater) {
        return Optional.ofNullable(getById(id)).map(t -> {
            updater.accept(t);
            return updateById(t);
        }).orElse(false);
    }

    private boolean updateTemplate(Long id, Consumer<InterfaceTemplate> updater, String opType, String desc) {
        return Optional.ofNullable(getById(id)).map(t -> {
            updater.accept(t);
            boolean result = updateById(t);
            if (result) saveHistory(t, opType, desc);
            return result;
        }).orElse(false);
    }

    private void saveRelatedData(Long templateId, InterfaceTemplateDTO dto) {
        Optional.ofNullable(dto.getHeaders()).filter(l -> !l.isEmpty()).ifPresent(l -> {
            List<TemplateHeader> list = TemplateConverter.toHeaderEntityList(l);
            list.forEach(h -> h.setTemplateId(templateId));
            headerMapper.batchInsert(list);
        });
        
        Optional.ofNullable(dto.getParameters()).filter(l -> !l.isEmpty()).ifPresent(l -> {
            List<TemplateParameter> list = TemplateConverter.toParameterEntityList(l);
            list.forEach(p -> p.setTemplateId(templateId));
            parameterMapper.batchInsert(list);
        });
        
        Optional.ofNullable(dto.getFormDataList()).filter(l -> !l.isEmpty()).ifPresent(l -> {
            List<TemplateFormData> list = TemplateConverter.toFormDataEntityList(l);
            list.forEach(f -> f.setTemplateId(templateId));
            formDataMapper.batchInsert(list);
        });
        
        Optional.ofNullable(dto.getAssertions()).filter(l -> !l.isEmpty()).ifPresent(l -> {
            List<TemplateAssertion> list = TemplateConverter.toAssertionEntityList(l);
            list.forEach(a -> a.setTemplateId(templateId));
            assertionMapper.batchInsert(list);
        });
        
        Optional.ofNullable(dto.getPreProcessors()).filter(l -> !l.isEmpty()).ifPresent(l -> {
            List<TemplatePreProcessor> list = TemplateConverter.toPreProcessorEntityList(l);
            list.forEach(p -> p.setTemplateId(templateId));
            preProcessorMapper.batchInsert(list);
        });
        
        Optional.ofNullable(dto.getPostProcessors()).filter(l -> !l.isEmpty()).ifPresent(l -> {
            List<TemplatePostProcessor> list = TemplateConverter.toPostProcessorEntityList(l);
            list.forEach(p -> p.setTemplateId(templateId));
            postProcessorMapper.batchInsert(list);
        });
        
        Optional.ofNullable(dto.getVariables()).filter(l -> !l.isEmpty()).ifPresent(l -> {
            List<TemplateVariable> list = TemplateConverter.toVariableEntityList(l);
            list.forEach(v -> v.setTemplateId(templateId));
            variableMapper.batchInsert(list);
        });
    }

    private void loadRelatedData(InterfaceTemplateVO vo) {
        Long tid = vo.getId();
        vo.setHeaders(TemplateConverter.toHeaderVOList(headerMapper.selectByTemplateId(tid)));
        vo.setParameters(TemplateConverter.toParameterVOList(parameterMapper.selectByTemplateId(tid)));
        vo.setFormDataList(TemplateConverter.toFormDataVOList(formDataMapper.selectByTemplateId(tid)));
        vo.setAssertions(TemplateConverter.toAssertionVOList(assertionMapper.selectByTemplateId(tid)));
        vo.setPreProcessors(TemplateConverter.toPreProcessorVOList(preProcessorMapper.selectByTemplateId(tid)));
        vo.setPostProcessors(TemplateConverter.toPostProcessorVOList(postProcessorMapper.selectByTemplateId(tid)));
        vo.setVariables(TemplateConverter.toVariableVOList(variableMapper.selectByTemplateId(tid)));
        
        List<TemplateFile> files = fileMapper.selectByTemplateId(tid);
        vo.setFiles(TemplateConverter.toFileVOList(files));
        vo.setFileCount(files.size());
        vo.setHasRequestFile(files.stream().anyMatch(f -> "REQUEST".equals(f.getFileCategory())) ? 1 : 0);
        vo.setHasResponseFile(files.stream().anyMatch(f -> "RESPONSE".equals(f.getFileCategory())) ? 1 : 0);
    }

    private void copyRelatedData(Long sourceId, Long targetId) {
        copyList(headerMapper.selectByTemplateId(sourceId), targetId, headerMapper::batchInsert);
        copyList(parameterMapper.selectByTemplateId(sourceId), targetId, parameterMapper::batchInsert);
        copyList(formDataMapper.selectByTemplateId(sourceId), targetId, formDataMapper::batchInsert);
        copyList(assertionMapper.selectByTemplateId(sourceId), targetId, assertionMapper::batchInsert);
        copyList(preProcessorMapper.selectByTemplateId(sourceId), targetId, preProcessorMapper::batchInsert);
        copyList(postProcessorMapper.selectByTemplateId(sourceId), targetId, postProcessorMapper::batchInsert);
        copyList(variableMapper.selectByTemplateId(sourceId), targetId, variableMapper::batchInsert);
        
        // 复制文件（需复制物理文件）
        List<TemplateFile> files = fileMapper.selectByTemplateId(sourceId);
        if (!CollectionUtils.isEmpty(files)) {
            files.forEach(file -> {
                try {
                    Path sourcePath = Paths.get(file.getFilePath());
                    String newName = UUID.randomUUID().toString().replace("-", "") + "." + file.getFileExtension();
                    Path targetPath = sourcePath.getParent().resolve(newName);
                    Files.copy(sourcePath, targetPath);
                    
                    file.setId(null);
                    file.setTemplateId(targetId);
                    file.setFileName(newName);
                    file.setFilePath(targetPath.toString());
                    fileMapper.insert(file);
                } catch (Exception e) {
                    log.warn("复制文件失败: {}", file.getFileOriginalName(), e);
                }
            });
        }
    }

    private <T> void copyList(List<T> list, Long targetId, Consumer<List<T>> inserter) {
        if (CollectionUtils.isEmpty(list)) return;
        list.forEach(item -> {
            try {
                Field idField = item.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(item, null);
                Field tidField = item.getClass().getDeclaredField("templateId");
                tidField.setAccessible(true);
                tidField.set(item, targetId);
            } catch (Exception e) {
                log.warn("复制数据失败", e);
            }
        });
        inserter.accept(list);
    }

    private void deleteRelatedData(Long templateId) {
        headerMapper.deleteByTemplateId(templateId);
        parameterMapper.deleteByTemplateId(templateId);
        formDataMapper.deleteByTemplateId(templateId);
        assertionMapper.deleteByTemplateId(templateId);
        preProcessorMapper.deleteByTemplateId(templateId);
        postProcessorMapper.deleteByTemplateId(templateId);
        variableMapper.deleteByTemplateId(templateId);
        
        fileMapper.selectByTemplateId(templateId).forEach(file -> {
            try {
                Files.deleteIfExists(Paths.get(file.getFilePath()));
            } catch (Exception e) {
                log.warn("删除物理文件失败");
            }
        });
        fileMapper.deleteByTemplateId(templateId);
    }

    private void saveHistory(InterfaceTemplate template, String opType, String summary) {
        TemplateHistory h = new TemplateHistory();
        h.setTemplateId(template.getId());
        h.setVersion(template.getVersion());
        h.setVersionType("AUTO");
        h.setChangeSummary(summary);
        h.setOperationType(opType);
        h.setCanRollback(1);
        if (h.getCreateId() == null) {
            h.setCreateId(1L);
            h.setCreateName("管理员");
        }
        historyMapper.insert(h);
    }
}
