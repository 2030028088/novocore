package com.aurora.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private Path getUploadPath() {
        return Paths.get(uploadDir, "images").toAbsolutePath().normalize();
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "请选择要上传的文件"
            ));
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String newFilename = UUID.randomUUID().toString() + extension;
        
        Path uploadPath = getUploadPath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        String url = "/api/images/" + newFilename;

        return ResponseEntity.ok(Map.of(
            "success", true,
            "url", url,
            "filename", newFilename,
            "originalFilename", originalFilename
        ));
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        Path filePath = getUploadPath().resolve(filename).normalize();
        
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = Files.readAllBytes(filePath);
        
        String contentType = getContentType(filename);
        
        return ResponseEntity.ok()
            .header("Content-Type", contentType)
            .header("Cache-Control", "max-age=31536000")
            .body(imageBytes);
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, Boolean>> deleteImage(@PathVariable String filename) throws IOException {
        Path filePath = getUploadPath().resolve(filename).normalize();
        
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Files.delete(filePath);
        
        return ResponseEntity.ok(Map.of("success", true));
    }

    private String getContentType(String filename) {
        if (filename.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (filename.toLowerCase().endsWith(".webp")) {
            return "image/webp";
        } else if (filename.toLowerCase().endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }
}