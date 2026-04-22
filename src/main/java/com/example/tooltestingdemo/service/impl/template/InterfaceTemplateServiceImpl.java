package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.entity.template.*;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.mapper.template.*;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplateVO createTemplate(InterfaceTemplateDTO dto) {
        return saveDraft(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(Long id, InterfaceTemplateDTO dto) {
        return Optional.ofNullable(getById(id)).map(template -> {
            InterfaceTemplate oldTemplate = new InterfaceTemplate();
            BeanUtils.copyProperties(template, oldTemplate);
            InterfaceTemplateVO oldVO = getTemplateDetail(id);

            BeanUtils.copyProperties(dto, template, "id", "version", "status", "createTime");
            String newVersion = VersionGenerator.incrementMinorVersion(template.getVersion());
            template.setVersion(newVersion);
            updateById(template);
            deleteRelatedData(id);
            saveRelatedData(id, dto);

            InterfaceTemplateVO newVO = getTemplateDetail(id);
            saveHistory(oldTemplate, oldVO, newVO, "UPDATE", "更新模板，版本号：" + newVersion);
            log.info("更新模板成功: id={}, version={}", id, newVersion);
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
            .map(this::enrichTemplateVO)
            .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterfaceTemplateVO copyTemplate(Long id, String newName) {
        InterfaceTemplate source = Optional.ofNullable(getById(id))
            .orElseThrow(() -> new TemplateValidationException(TemplateValidationException.ErrorType.NOT_FOUND, "模板不存在"));

        InterfaceTemplate copy = new InterfaceTemplate();
        BeanUtils.copyProperties(source, copy, "id", "version", "status", "createTime", "updateTime", "useCount");
        copy.setName(newName);
        copy.setVersion(VersionGenerator.generateInitialVersion());
        copy.setStatus(TemplateEnums.TemplateStatus.DRAFT.getCode());
        copy.setUseCount(0);
        save(copy);

        copyRelatedData(id, copy.getId());
        InterfaceTemplateVO newVO = getTemplateDetail(copy.getId());
        saveHistory(copy, null, newVO, "COPY", "复制自模板[ID=" + id + "]");

        log.info("复制模板成功: newId={}, sourceId={}", copy.getId(), id);
        return newVO;
    }

    // ========== 状态变更 ==========

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
        return Optional.ofNullable(getById(id)).map(t -> {
            InterfaceTemplateVO oldVO = getTemplateDetail(id);
            t.setStatus(TemplateEnums.TemplateStatus.DISABLED.getCode());
            if (!updateById(t)) {
                return false;
            }

            InterfaceTemplateVO newVO = getTemplateDetail(id);
            saveHistory(t, oldVO, newVO, "DELETE", "删除模板");
            return removeById(id);
        }).orElse(false);
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
            .orElseThrow(() -> new TemplateValidationException(TemplateValidationException.ErrorType.NOT_FOUND, "模板不存在"));

        Optional.ofNullable(TemplateEnums.TemplateStatus.getByCode(existing.getStatus()))
            .filter(TemplateEnums.TemplateStatus::isSubmittable)
            .orElseThrow(() -> new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, "当前状态不可提交审核"));

        templateValidator.validateRequired(dto, id);

        InterfaceTemplateVO oldVO = getTemplateDetail(id);

        doSaveTemplate(id, dto, TemplateEnums.TemplateStatus.PENDING_REVIEW.getCode(),
            VersionGenerator.incrementMinorVersion(existing.getVersion()), "提交审核");

        InterfaceTemplateVO newVO = getTemplateDetail(id);
        saveHistory(existing, oldVO, newVO, "PUBLISH", "提交审核，版本号：" + existing.getVersion());

        return newVO;
    }

    @Override
    public boolean approveTemplate(Long id) {
        return checkAndUpdateStatus(id, TemplateEnums.TemplateStatus.PENDING_REVIEW.getCode(),
            TemplateEnums.TemplateStatus.PUBLISHED.getCode(), "只有待审核状态的模板可以审核通过", "APPROVE", "审核通过");
    }

    @Override
    public boolean rejectTemplate(Long id, String reason) {
        return checkAndUpdateStatus(id, TemplateEnums.TemplateStatus.PENDING_REVIEW.getCode(),
            TemplateEnums.TemplateStatus.REJECTED.getCode(), "只有待审核状态的模板可以驳回", "REJECT",
            "审核驳回，原因：" + Optional.ofNullable(reason).orElse("无"));
    }

    // ========== 私有方法 ==========

    private InterfaceTemplateVO enrichTemplateVO(InterfaceTemplate template) {
        InterfaceTemplateVO vo = TemplateConverter.toVO(template);
        List<TemplateFile> files = fileMapper.selectByTemplateId(template.getId());
        vo.setFileCount(files.size());
        vo.setHasRequestFile(files.stream().anyMatch(f -> "REQUEST".equals(f.getFileCategory())) ? 1 : 0);
        vo.setHasResponseFile(files.stream().anyMatch(f -> "RESPONSE".equals(f.getFileCategory())) ? 1 : 0);
        return vo;
    }

    private InterfaceTemplateVO saveDraftInternal(InterfaceTemplateDTO dto, Long id) {
        boolean isUpdate = id != null;

        InterfaceTemplate existing = isUpdate ? getById(id) : null;
        if (isUpdate && existing == null) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.NOT_FOUND, "模板不存在");
        }

        templateValidator.validateDraft(dto, id);

        String newVersion = isUpdate
            ? VersionGenerator.incrementMinorVersion(existing.getVersion())
            : VersionGenerator.generateInitialVersion();

        InterfaceTemplateVO oldVO = null;
        if (isUpdate) {
            Optional.ofNullable(TemplateEnums.TemplateStatus.getByCode(existing.getStatus()))
                .filter(TemplateEnums.TemplateStatus::isEditable)
                .orElseThrow(() -> new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, "当前状态不可编辑"));

            oldVO = getTemplateDetail(id);
            deleteRelatedData(id);
        }

        Long templateId = doSaveTemplate(id, dto, TemplateEnums.TemplateStatus.DRAFT.getCode(), newVersion,
            isUpdate ? "更新草稿" : "创建草稿");

        InterfaceTemplateVO newVO = getTemplateDetail(templateId);
        if (isUpdate) {
            saveHistory(existing, oldVO, newVO, "UPDATE", "更新草稿，版本号：" + existing.getVersion());
        } else {
            InterfaceTemplate newTemplate = getById(templateId);
            saveHistory(newTemplate, null, newVO, "CREATE", "创建草稿，版本号：" + newVersion);
        }

        return newVO;
    }

    private Long doSaveTemplate(Long id, InterfaceTemplateDTO dto, Integer status, String version, String desc) {
        InterfaceTemplate template = new InterfaceTemplate();
        BeanUtils.copyProperties(dto, template);
        if (!StringUtils.hasText(template.getBodyContent()) && StringUtils.hasText(dto.getBody())) {
            template.setBodyContent(dto.getBody());
        }
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
                throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, errorMsg);
            }
            InterfaceTemplateVO oldVO = getTemplateDetail(id);
            t.setStatus(newStatus);
            updateById(t);
            InterfaceTemplateVO newVO = getTemplateDetail(id);
            saveHistory(t, oldVO, newVO, opType, desc);
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
            InterfaceTemplateVO oldVO = getTemplateDetail(id);
            updater.accept(t);
            boolean result = updateById(t);
            if (result) {
                InterfaceTemplateVO newVO = getTemplateDetail(id);
                saveHistory(t, oldVO, newVO, opType, desc);
            }
            return result;
        }).orElse(false);
    }

    // ========== 关联数据操作 ==========

    private void saveRelatedData(Long templateId, InterfaceTemplateDTO dto) {
        insertBatch(dto.getHeaders(), TemplateConverter::toHeaderEntityList,
            h -> h.setTemplateId(templateId), headerMapper::batchInsert);
        insertBatch(dto.getParameters(), TemplateConverter::toParameterEntityList,
            p -> p.setTemplateId(templateId), parameterMapper::batchInsert);
        insertBatch(dto.getFormDataList(), TemplateConverter::toFormDataEntityList,
            f -> f.setTemplateId(templateId), formDataMapper::batchInsert);
        insertBatch(dto.getAssertions(), TemplateConverter::toAssertionEntityList,
            a -> a.setTemplateId(templateId), assertionMapper::batchInsert);
        insertBatch(dto.getPreProcessors(), TemplateConverter::toPreProcessorEntityList,
            p -> p.setTemplateId(templateId), preProcessorMapper::batchInsert);
        insertBatch(dto.getPostProcessors(), TemplateConverter::toPostProcessorEntityList,
            p -> p.setTemplateId(templateId), postProcessorMapper::batchInsert);
        insertBatch(dto.getVariables(), TemplateConverter::toVariableEntityList,
            v -> v.setTemplateId(templateId), variableMapper::batchInsert);
    }

    private <D, E> void insertBatch(List<D> data, java.util.function.Function<List<D>, List<E>> converter,
                                     Consumer<E> setter, Consumer<List<E>> inserter) {
        Optional.ofNullable(data).filter(l -> !l.isEmpty()).map(converter).ifPresent(list -> {
            list.forEach(setter);
            inserter.accept(list);
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
        copyEntities(headerMapper.selectByTemplateId(sourceId), targetId,
            h -> { h.setId(null); h.setTemplateId(targetId); }, headerMapper::batchInsert);
        copyEntities(parameterMapper.selectByTemplateId(sourceId), targetId,
            p -> { p.setId(null); p.setTemplateId(targetId); }, parameterMapper::batchInsert);
        copyEntities(formDataMapper.selectByTemplateId(sourceId), targetId,
            f -> { f.setId(null); f.setTemplateId(targetId); }, formDataMapper::batchInsert);
        copyEntities(assertionMapper.selectByTemplateId(sourceId), targetId,
            a -> { a.setId(null); a.setTemplateId(targetId); }, assertionMapper::batchInsert);
        copyEntities(preProcessorMapper.selectByTemplateId(sourceId), targetId,
            p -> { p.setId(null); p.setTemplateId(targetId); }, preProcessorMapper::batchInsert);
        copyEntities(postProcessorMapper.selectByTemplateId(sourceId), targetId,
            p -> { p.setId(null); p.setTemplateId(targetId); }, postProcessorMapper::batchInsert);
        copyEntities(variableMapper.selectByTemplateId(sourceId), targetId,
            v -> { v.setId(null); v.setTemplateId(targetId); }, variableMapper::batchInsert);

        copyFiles(sourceId, targetId);
    }

    private <T> void copyEntities(List<T> list, Long targetId, Consumer<T> preparer, Consumer<List<T>> inserter) {
        if (CollectionUtils.isEmpty(list)) return;
        list.forEach(preparer);
        inserter.accept(list);
    }

    private void copyFiles(Long sourceId, Long targetId) {
        List<TemplateFile> files = fileMapper.selectByTemplateId(sourceId);
        if (CollectionUtils.isEmpty(files)) return;

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

    private void deleteRelatedData(Long templateId) {
        headerMapper.deleteByTemplateId(templateId);
        parameterMapper.deleteByTemplateId(templateId);
        formDataMapper.deleteByTemplateId(templateId);
        assertionMapper.deleteByTemplateId(templateId);
        preProcessorMapper.deleteByTemplateId(templateId);
        postProcessorMapper.deleteByTemplateId(templateId);
        variableMapper.deleteByTemplateId(templateId);

        deleteFiles(templateId);
    }

    private void deleteFiles(Long templateId) {
        fileMapper.selectByTemplateId(templateId).forEach(file -> {
            try {
                Files.deleteIfExists(Paths.get(file.getFilePath()));
            } catch (Exception e) {
                log.warn("删除物理文件失败");
            }
        });
        fileMapper.deleteByTemplateId(templateId);
    }

    private void saveHistory(InterfaceTemplate template, InterfaceTemplateVO oldVO, InterfaceTemplateVO newVO, String opType, String summary) {
        TemplateHistory h = new TemplateHistory();
        h.setTemplateId(template.getId());
        h.setVersion(template.getVersion());
        h.setVersionType("AUTO");
        h.setChangeSummary(summary);
        h.setOperationType(opType);
        h.setCanRollback(1);
        h.setCreateId(Optional.ofNullable(template.getCreateId()).orElse(1L));
        h.setCreateName(Optional.ofNullable(template.getCreateName()).orElse("管理员"));

        // 保存变更前快照
        if (oldVO != null) {
            h.setTemplateSnapshot(JSON.toJSONString(oldVO));
            if (newVO != null) {
                h.setChangeDetails(buildChangeDetails(oldVO, newVO));
            }
        } else if (newVO != null) {
            // 创建/复制操作：保存新数据快照
            h.setTemplateSnapshot(JSON.toJSONString(newVO));
        }

        historyMapper.insert(h);
    }

    private String buildChangeDetails(InterfaceTemplateVO oldVO, InterfaceTemplateVO newVO) {
        try {
            JSONObject oldJson = JSON.parseObject(JSON.toJSONString(oldVO));
            JSONObject newJson = JSON.parseObject(JSON.toJSONString(newVO));
            JSONObject before = new JSONObject();
            JSONObject after = new JSONObject();

            for (String key : oldJson.keySet()) {
                Object oldVal = oldJson.get(key);
                Object newVal = newJson.get(key);
                if (!Objects.equals(oldVal, newVal)) {
                    before.put(key, oldVal);
                    after.put(key, newVal);
                }
            }

            JSONObject diff = new JSONObject();
            diff.put("before", before);
            diff.put("after", after);
            return diff.toJSONString();
        } catch (Exception e) {
            log.warn("构建变更详情失败: {}", e.getMessage());
            return null;
        }
    }
}
