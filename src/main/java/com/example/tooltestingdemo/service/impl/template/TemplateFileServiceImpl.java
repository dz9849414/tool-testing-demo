package com.example.tooltestingdemo.service.impl.template;

import com.example.tooltestingdemo.entity.template.TemplateFile;
import com.example.tooltestingdemo.exception.TemplateValidationException;
import com.example.tooltestingdemo.mapper.template.TemplateFileMapper;
import com.example.tooltestingdemo.service.template.TemplateFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 模板文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFileServiceImpl implements TemplateFileService {

    private final TemplateFileMapper fileMapper;

    @Value("${app.file.upload-path:}")
    private String uploadPath;

    @Value("${app.file.base-url:}")
    private String fileBaseUrl;

    private Path getUploadBasePath() {
        return Optional.ofNullable(uploadPath)
            .filter(p -> !p.isEmpty())
            .map(p -> Paths.get(p).toAbsolutePath())
            .orElse(Paths.get(System.getProperty("user.home"), "tool-testing-uploads").toAbsolutePath());
    }

    @Override
    public TemplateFile uploadFile(Long templateId, MultipartFile file, String fileCategory, String description) {
        if (file == null || file.isEmpty()) {
            throw new TemplateValidationException(TemplateValidationException.ErrorType.REQUIRED_FIELD_EMPTY, "文件不能为空");
        }

        try {
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path targetPath = getUploadBasePath().resolve("templates").resolve(datePath);
            Files.createDirectories(targetPath);

            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

            Path filePath = targetPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            TemplateFile templateFile = buildTemplateFile(templateId, originalFilename, newFilename, 
                filePath.toString(), buildFileUrl(targetPath, newFilename), file, extension, fileCategory, description);
            fileMapper.insert(templateFile);

            log.info("文件上传成功: templateId={}, fileName={}", templateId, originalFilename);
            return templateFile;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, "文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TemplateFile> uploadFiles(Long templateId, List<MultipartFile> files, String fileCategory) {
        List<TemplateFile> result = new ArrayList<>();
        Optional.ofNullable(files).ifPresent(list -> 
            list.stream().filter(f -> !f.isEmpty()).forEach(f -> result.add(uploadFile(templateId, f, fileCategory, null))));
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
    public TemplateFile getFileById(Long fileId) {
        return fileMapper.selectById(fileId);
    }

    @Override
    public boolean deleteFile(Long fileId) {
        TemplateFile file = fileMapper.selectById(fileId);
        if (file == null) return false;
        
        deletePhysicalFile(file.getFilePath());
        return fileMapper.deleteById(fileId) > 0;
    }

    @Override
    public boolean deleteFilesByTemplateId(Long templateId) {
        fileMapper.selectByTemplateId(templateId).forEach(f -> deletePhysicalFile(f.getFilePath()));
        return fileMapper.deleteByTemplateId(templateId) >= 0;
    }

    @Override
    public String getFileUrl(Long fileId) {
        return Optional.ofNullable(fileMapper.selectById(fileId)).map(TemplateFile::getFileUrl).orElse(null);
    }

    @Override
    public byte[] downloadFile(Long fileId) {
        TemplateFile file = Optional.ofNullable(fileMapper.selectById(fileId))
            .orElseThrow(() -> new TemplateValidationException(TemplateValidationException.ErrorType.NOT_FOUND, "文件不存在"));
        try {
            return Files.readAllBytes(Paths.get(file.getFilePath()));
        } catch (IOException e) {
            log.error("读取文件失败: {}", file.getFilePath(), e);
            throw new TemplateValidationException(TemplateValidationException.ErrorType.OPERATION_NOT_ALLOWED, "读取文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateFileDescription(Long fileId, String description) {
        TemplateFile file = fileMapper.selectById(fileId);
        if (file == null) return false;
        file.setFileDescription(description);
        return fileMapper.updateById(file) > 0;
    }

    private TemplateFile buildTemplateFile(Long templateId, String originalName, String newName, String path, 
                                            String url, MultipartFile file, String extension, String category, String desc) {
        TemplateFile tf = new TemplateFile();
        tf.setTemplateId(templateId);
        tf.setFileName(newName);
        tf.setFileOriginalName(originalName);
        tf.setFilePath(path);
        tf.setFileUrl(url);
        tf.setFileSize(file.getSize());
        tf.setFileType(file.getContentType());
        tf.setFileExtension(extension);
        tf.setFileCategory(StringUtils.hasText(category) ? category : TemplateFile.CATEGORY_ATTACHMENT);
        tf.setFileDescription(desc);
        tf.setIsDeleted(0);
        tf.setCreateId(1L);
        tf.setCreateName("管理员");
        return tf;
    }

    private void deletePhysicalFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", filePath, e);
        }
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    private String buildFileUrl(Path filePath, String filename) {
        return StringUtils.hasText(fileBaseUrl) 
            ? fileBaseUrl + "/" + filePath.getFileName() + "/" + filename
            : "/api/template/files/download/" + filename;
    }
}
