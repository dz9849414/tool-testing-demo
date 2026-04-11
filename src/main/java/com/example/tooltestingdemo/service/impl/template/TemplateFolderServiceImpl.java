package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateFolder;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.mapper.template.TemplateFolderMapper;
import com.example.tooltestingdemo.service.template.TemplateFolderService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.TemplateFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 模板分类/文件夹 Service 实现类
 */
@Slf4j
@Service
public class TemplateFolderServiceImpl extends ServiceImpl<TemplateFolderMapper, TemplateFolder> 
        implements TemplateFolderService {

    @Override
    public List<TemplateFolderVO> getFolderTree(Long parentId) {
        return TemplateConverter.toFolderVOList(
            lambdaQuery()
                .eq(TemplateFolder::getParentId, Optional.ofNullable(parentId).orElse(0L))
                .eq(TemplateFolder::getStatus, 1)
                .orderByAsc(TemplateFolder::getSortOrder)
                .list()
        );
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
        TemplateFolder folder = new TemplateFolder();
        folder.setId(id);
        folder.setIsDeleted(1);
        folder.setDeletedTime(LocalDateTime.now());
        folder.setStatus(TemplateEnums.TemplateStatus.DISABLED.getCode());
        return updateById(folder);
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
