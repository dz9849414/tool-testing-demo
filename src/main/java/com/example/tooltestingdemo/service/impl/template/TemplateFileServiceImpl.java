package com.example.tooltestingdemo.service.impl.template;

import com.example.tooltestingdemo.entity.template.TemplateFile;
import com.example.tooltestingdemo.mapper.template.TemplateFileMapper;
import com.example.tooltestingdemo.service.template.TemplateFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 模板文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFileServiceImpl implements TemplateFileService {

    private final TemplateFileMapper fileMapper;

    //TODO-没有文件服务器，先上传到本地
    @Value("${app.file.upload-path:}")
    private String uploadPath;

    /**
     * 获取实际的上传路径
     */
    private Path getUploadBasePath() {
        if (uploadPath != null && !uploadPath.isEmpty()) {
            return Paths.get(uploadPath).toAbsolutePath();
        }
        // 默认使用用户目录下的 uploads 文件夹
        return Paths.get(System.getProperty("user.home"), "tool-testing-uploads").toAbsolutePath();
    }

    @Value("${app.file.base-url:}")
    private String fileBaseUrl;

    @Override
    public TemplateFile uploadFile(Long templateId, MultipartFile file, String fileCategory, String description) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 获取上传基础路径
            Path uploadBasePath = getUploadBasePath();

            // 创建上传目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path targetPath = uploadBasePath.resolve("templates").resolve(datePath);
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

            // 保存文件
            Path filePath = targetPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // 构建文件访问URL
            String fileUrl = buildFileUrl(targetPath, newFilename);

            // 保存文件信息到数据库
            TemplateFile templateFile = new TemplateFile();
            templateFile.setTemplateId(templateId);
            templateFile.setFileName(newFilename);
            templateFile.setFileOriginalName(originalFilename);
            templateFile.setFilePath(filePath.toString());
            templateFile.setFileUrl(fileUrl);
            templateFile.setFileSize(file.getSize());
            templateFile.setFileType(file.getContentType());
            templateFile.setFileExtension(extension);
            templateFile.setFileCategory(StringUtils.hasText(fileCategory) ? fileCategory : TemplateFile.CATEGORY_ATTACHMENT);
            templateFile.setFileDescription(description);
            templateFile.setIsDeleted(0);
            templateFile.setCreateId(1L);
            templateFile.setCreateName("管理员");

            fileMapper.insert(templateFile);

            log.info("文件上传成功: templateId={}, fileName={}", templateId, originalFilename);
            return templateFile;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<TemplateFile> uploadFiles(Long templateId, List<MultipartFile> files, String fileCategory) {
        List<TemplateFile> result = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return result;
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                TemplateFile templateFile = uploadFile(templateId, file, fileCategory, null);
                result.add(templateFile);
            }
        }
        return result;
    }

    @Override
    public List<TemplateFile> getFilesByTemplateId(Long templateId) {
        return fileMapper.selectByTemplateId(templateId);
    }

    @Override
    public List<TemplateFile> getFilesByTemplateIdAndCategory(Long templateId, String fileCategory) {
        return fileMapper.selectByTemplateIdAndCategory(templateId, fileCategory);
    }

    @Override
    public boolean deleteFile(Long fileId) {
        TemplateFile file = fileMapper.selectById(fileId);
        if (file == null) {
            return false;
        }

        // 删除物理文件
        try {
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", file.getFilePath(), e);
        }

        // 删除数据库记录
        return fileMapper.deleteById(fileId) > 0;
    }

    @Override
    public boolean deleteFilesByTemplateId(Long templateId) {
        List<TemplateFile> files = fileMapper.selectByTemplateId(templateId);

        // 删除所有物理文件
        for (TemplateFile file : files) {
            try {
                Path filePath = Paths.get(file.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("删除物理文件失败: {}", file.getFilePath(), e);
            }
        }

        // 删除数据库记录
        return fileMapper.deleteByTemplateId(templateId) >= 0;
    }

    @Override
    public String getFileUrl(Long fileId) {
        TemplateFile file = fileMapper.selectById(fileId);
        return file != null ? file.getFileUrl() : null;
    }

    @Override
    public byte[] downloadFile(Long fileId) {
        TemplateFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        try {
            Path filePath = Paths.get(file.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("读取文件失败: {}", file.getFilePath(), e);
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateFileDescription(Long fileId, String description) {
        TemplateFile file = fileMapper.selectById(fileId);
        if (file == null) {
            return false;
        }
        file.setFileDescription(description);
        return fileMapper.updateById(file) > 0;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    /**
     * 构建文件访问URL
     */
    private String buildFileUrl(Path filePath, String filename) {
        if (StringUtils.hasText(fileBaseUrl)) {
            return fileBaseUrl + "/" + filePath.getFileName() + "/" + filename;
        }
        return "/api/template/files/download/" + filename;
    }
}
