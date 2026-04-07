package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateFolder;
import com.example.tooltestingdemo.mapper.template.TemplateFolderMapper;
import com.example.tooltestingdemo.service.template.TemplateFolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模板分类/文件夹 Service 实现类
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/impl/template/TemplateFolderServiceImpl.java
 */
@Slf4j
@Service
public class TemplateFolderServiceImpl extends ServiceImpl<TemplateFolderMapper, TemplateFolder> 
        implements TemplateFolderService {

    @Override
    public List<TemplateFolder> getFolderTree(Long parentId) {
        // 查询指定父ID下的所有文件夹
        return lambdaQuery()
                .eq(TemplateFolder::getParentId, parentId == null ? 0L : parentId)
                .eq(TemplateFolder::getStatus, 1)
                .orderByAsc(TemplateFolder::getSortOrder)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateFolder createFolder(TemplateFolder folder) {
        if (folder.getParentId() == null) {
            folder.setParentId(0L);
        }
        if (folder.getSortOrder() == null) {
            folder.setSortOrder(0);
        }
        folder.setStatus(1);
        save(folder);
        log.info("创建文件夹成功: id={}, name={}", folder.getId(), folder.getName());
        return folder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFolder(TemplateFolder folder) {
        return updateById(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFolder(Long id) {
        // 逻辑删除
        TemplateFolder folder = new TemplateFolder();
        folder.setId(id);
        folder.setDeleteTime(LocalDateTime.now());
        folder.setStatus(0);
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
}
