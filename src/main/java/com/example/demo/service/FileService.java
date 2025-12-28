package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

// Service for handling file storage operations
@Service
public class FileService {
    @Value("${upload.path}")
    private String uploadDir;

    // Store uploaded file to server
    public String storeFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        String contentType = file.getContentType();
        // Allow only CSV
        if (contentType != null && !contentType.contains("csv")) {
            throw new IOException("Only CSV files are allowed. Received: " + contentType);
        }

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Unique filename
        String filename = UUID.randomUUID().toString() + ".csv";
        Path target = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    // Get the upload directory path
    public String getUploadDir() {
        return uploadDir;
    }

    // Check if a file exists in the upload directory
    public boolean fileExists(String filename) {
        Path uploadPath = Paths.get(uploadDir);
        Path filePath = uploadPath.resolve(filename);
        return Files.exists(filePath);
    }

    // Get the full path to a file in the upload directory
    public Path getFilePath(String filename) {
        Path uploadPath = Paths.get(uploadDir);
        return uploadPath.resolve(filename);
    }
}
