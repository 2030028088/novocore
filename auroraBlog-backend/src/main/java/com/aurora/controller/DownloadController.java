package com.aurora.controller;

import com.aurora.entity.Download;
import com.aurora.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/downloads")
@CrossOrigin(origins = "*")
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    @PostMapping
    public ResponseEntity<Download> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "published", defaultValue = "true") Boolean published) throws IOException {
        
        Download created = downloadService.uploadFile(file, description, published);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Download>> getFiles(
            @RequestParam(value = "published", required = false) Boolean publishedOnly) {
        
        List<Download> files = downloadService.getFiles(publishedOnly);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Download> getFile(@PathVariable Long id) {
        Download file = downloadService.getFile(id);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(file);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        Path filePath = downloadService.getFilePath(id);
        if (filePath == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        Download download = downloadService.getFile(id);
        String fileName = download != null ? download.getOriginalName() : "download";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) throws IOException {
        Path filePath = downloadService.getFilePath(id);
        if (filePath == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        Download download = downloadService.getFile(id);
        String contentType = download != null ? download.getFileType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Download> updateFile(
            @PathVariable Long id,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "published", required = false) Boolean published) {
        
        Download updated = downloadService.updateFile(id, description, published);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteFile(@PathVariable Long id) throws IOException {
        boolean deleted = downloadService.deleteFile(id);
        return ResponseEntity.ok(Map.of("success", deleted));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Boolean>> togglePublish(@PathVariable Long id) {
        boolean newStatus = downloadService.togglePublish(id);
        return ResponseEntity.ok(Map.of("published", newStatus));
    }
}