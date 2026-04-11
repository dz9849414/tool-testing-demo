package com.example.tooltestingdemo.service.template;

import com.example.tooltestingdemo.entity.template.TemplateFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 模板文件服务接口
 */
public interface TemplateFileService {

    /**
     * 上传文件
     */
    TemplateFile uploadFile(Long templateId, MultipartFile file, String fileCategory, String description);

    /**
     * 批量上传文件
     */
    List<TemplateFile> uploadFiles(Long templateId, List<MultipartFile> files, String fileCategory);

    /**
     * 获取模板文件列表
     */
    List<TemplateFile> getFilesByTemplateId(Long templateId);

    /**
     * 获取模板文件列表（按类别）
     */
    List<TemplateFile> getFilesByTemplateIdAndCategory(Long templateId, String fileCategory);

    /**
     * 删除文件
     */
    boolean deleteFile(Long fileId);

    /**
     * 批量删除文件
     */
    boolean deleteFilesByTemplateId(Long templateId);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(Long fileId);

    /**
     * 下载文件
     */
    byte[] downloadFile(Long fileId);

    /**
     * 更新文件描述
     */
    boolean updateFileDescription(Long fileId, String description);
}
