package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.entity.template.TemplateFolder;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.mapper.template.InterfaceTemplateMapper;
import com.example.tooltestingdemo.mapper.template.TemplateFolderMapper;
import com.example.tooltestingdemo.service.template.TemplateFolderService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.TemplateFolderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 模板分类/文件夹 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFolderServiceImpl extends ServiceImpl<TemplateFolderMapper, TemplateFolder>
        implements TemplateFolderService {

    private final InterfaceTemplateMapper templateMapper;

    @Override
    public List<TemplateFolderVO> getFolderTree(Long parentId) {
        Long rootId = Optional.ofNullable(parentId).orElse(0L);
        List<TemplateFolder> allFolders = lambdaQuery()
                .eq(TemplateFolder::getIsDeleted, 0)
                .orderByAsc(TemplateFolder::getSortOrder)
                .list();
        return buildFolderTree(allFolders, rootId);
    }

    private List<TemplateFolderVO> buildFolderTree(List<TemplateFolder> allFolders, Long parentId) {
        return allFolders.stream()
                .filter(f -> parentId.equals(f.getParentId()))
                .map(f -> {
                    TemplateFolderVO vo = TemplateConverter.toVO(f);
                    List<TemplateFolderVO> children = buildFolderTree(allFolders, f.getId());
                    vo.setChildren(children);
                    vo.setChildrenCount(children.size());
                    return vo;
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateFolderVO createFolder(TemplateFolder folder) {
        folder.setParentId(Optional.ofNullable(folder.getParentId()).orElse(0L));
        folder.setSortOrder(Optional.ofNullable(folder.getSortOrder()).orElse(0));
        folder.setStatus(TemplateEnums.TemplateStatus.PUBLISHED.getCode());

        if (folder.getCreateId() == null) {
            folder.setCreateId(1L);
            folder.setCreateName("管理员");
        }

        save(folder);
        log.info("创建文件夹成功: id={}, name={}", folder.getId(), folder.getName());
        return TemplateConverter.toVO(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFolder(TemplateFolder folder) {
        return updateById(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFolder(Long id) {
        long templateCount = templateMapper.selectCount(
                new LambdaQueryWrapper<InterfaceTemplate>()
                        .eq(InterfaceTemplate::getFolderId, id)
                        .eq(InterfaceTemplate::getIsDeleted, 0)
        );
        if (templateCount > 0) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, "该文件夹下存在模板，无法删除");
        }

        TemplateFolder folder = new TemplateFolder();
        folder.setId(id);
        folder.setStatus(TemplateEnums.TemplateStatus.DISABLED.getCode());
        if (!updateById(folder)) {
            return false;
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveFolder(Long id, Long targetParentId) {
        TemplateFolder folder = new TemplateFolder();
        folder.setId(id);
        folder.setParentId(targetParentId);
        return updateById(folder);
    }

    @Override
    public TemplateFolderVO getFolderDetail(Long id) {
        return TemplateConverter.toVO(getById(id));
    }
}
