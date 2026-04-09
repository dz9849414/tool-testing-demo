package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateFolder;
import com.example.tooltestingdemo.mapper.template.TemplateFolderMapper;
import com.example.tooltestingdemo.service.template.TemplateFolderService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.TemplateFolderVO;
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
    public List<TemplateFolderVO> getFolderTree(Long parentId) {
        // 查询指定父ID下的所有文件夹
        List<TemplateFolder> folders = lambdaQuery()
                .eq(TemplateFolder::getParentId, parentId == null ? 0L : parentId)
                .eq(TemplateFolder::getStatus, 1)
                .orderByAsc(TemplateFolder::getSortOrder)
                .list();
        
        return TemplateConverter.toFolderVOList(folders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateFolderVO createFolder(TemplateFolder folder) {
        if (folder.getParentId() == null) {
            folder.setParentId(0L);
        }
        if (folder.getSortOrder() == null) {
            folder.setSortOrder(0);
        }
        folder.setStatus(1);
        
        // 设置创建人（如果未设置）
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
        // 逻辑删除
        TemplateFolder folder = new TemplateFolder();
        folder.setId(id);
        folder.setIsDeleted(1);
        folder.setDeletedTime(LocalDateTime.now());
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

    @Override
    public TemplateFolderVO getFolderDetail(Long id) {
        TemplateFolder folder = getById(id);
        return TemplateConverter.toVO(folder);
    }
}
