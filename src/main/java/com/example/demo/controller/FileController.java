package com.example.demo.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controller for handling file uploads and downloads
@Tag(name = "File handling controller")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // Upload a file to the server
    @Operation(summary = "Загрузка CSV файла на сервер ")
    @PostMapping(value = "/upload",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String resultFile = fileService.storeFile(file);
                return ResponseEntity.ok(resultFile);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Download a CSV file by name from the upload folder
    @Operation(summary = "Выгрузка CSV файла по названию из папки upload")
    @GetMapping(value = "/download/{filename:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String filename) throws IOException {
        // Validate that the file name is a CSV file to prevent path traversal attacks
        if (!filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().build();
        }

        // Get the file path from the service
        Path filePath = fileService.getFilePath(filename);

        // Check if file exists
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        // Check if the resolved path is within the upload directory (security check)
        String uploadDir = fileService.getUploadDir();
        Path uploadDirPath = Path.of(uploadDir).normalize();
        Path normalizedFilePath = filePath.normalize();
        if (!normalizedFilePath.startsWith(uploadDirPath)) {
            return ResponseEntity.badRequest().build();
        }

        // Verify that the file is actually inside the upload directory to prevent path traversal
        String filePathStr = normalizedFilePath.toString();
        String uploadDirStr = uploadDirPath.toString();
        if (!filePathStr.startsWith(uploadDirStr)) {
            return ResponseEntity.badRequest().build();
        }

        // Create input stream resource
        FileInputStream fileInputStream = new FileInputStream(normalizedFilePath.toFile());
        InputStreamResource resource = new InputStreamResource(fileInputStream);

        // Return file with appropriate headers
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(Files.size(normalizedFilePath))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
