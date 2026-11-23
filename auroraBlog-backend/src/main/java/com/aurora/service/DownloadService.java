package com.aurora.service;

import com.aurora.entity.Download;
import com.aurora.mapper.DownloadMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class DownloadService {

    @Autowired
    private DownloadMapper downloadMapper;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public Download uploadFile(MultipartFile file, String description, Boolean published) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String storedFileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), filePath);

        Download download = new Download();
        download.setFileName(storedFileName);
        download.setOriginalName(originalFileName);
        download.setDescription(description);
        download.setFileSize(file.getSize());
        download.setFileType(file.getContentType());
        download.setFilePath(filePath.toString());
        download.setPublished(published != null ? published : true);

        downloadMapper.insert(download);
        return download;
    }

    public List<Download> getFiles(Boolean publishedOnly) {
        QueryWrapper<Download> wrapper = new QueryWrapper<>();
        if (publishedOnly != null && publishedOnly) {
            wrapper.eq("published", true);
        }
        wrapper.orderByDesc("created_at");
        return downloadMapper.selectList(wrapper);
    }

    public Download getFile(Long id) {
        return downloadMapper.selectById(id);
    }

    public Download updateFile(Long id, String description, Boolean published) {
        Download download = downloadMapper.selectById(id);
        if (download != null) {
            if (StringUtils.hasText(description)) {
                download.setDescription(description);
            }
            if (published != null) {
                download.setPublished(published);
            }
            downloadMapper.updateById(download);
        }
        return download;
    }

    public boolean deleteFile(Long id) throws IOException {
        Download download = downloadMapper.selectById(id);
        if (download != null) {
            Path filePath = Paths.get(download.getFilePath());
            Files.deleteIfExists(filePath);
            return downloadMapper.deleteById(id) > 0;
        }
        return false;
    }

    public boolean togglePublish(Long id) {
        Download download = downloadMapper.selectById(id);
        if (download != null) {
            download.setPublished(!download.getPublished());
            return downloadMapper.updateById(download) > 0;
        }
        return false;
    }

    public Path getFilePath(Long id) {
        Download download = downloadMapper.selectById(id);
        if (download != null) {
            Path filePath = Paths.get(download.getFilePath()).normalize();
            if (Files.exists(filePath)) {
                return filePath;
            }
            
            String fileName = download.getFileName();
            if (fileName != null) {
                Path uploadPath = Paths.get(uploadDir).normalize();
                Path fallbackPath = uploadPath.resolve(fileName).normalize();
                if (Files.exists(fallbackPath)) {
                    return fallbackPath;
                }
            }
        }
        return null;
    }
}