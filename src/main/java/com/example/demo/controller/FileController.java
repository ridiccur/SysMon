package com.example.demo.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // Upload a file to the server
    @Operation(summary = "Загрузка CSV файла на сервер ")
    @PostMapping(value = "/upload",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Загрузка файла: {} ({} байт)", file.getOriginalFilename(), file.getSize());
        try {
            String resultFile = fileService.storeFile(file);
            log.info("Файл успешно загружен: {}", resultFile);
            return ResponseEntity.ok(resultFile);
        } catch (IOException e) {
            log.error("Ошибка ввода-вывода при загрузке файла: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при загрузке файла: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Download a CSV file by name from the upload folder
    @Operation(summary = "Выгрузка CSV файла по названию из папки upload")
    @GetMapping(value = "/download/{filename:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String filename) throws IOException {
        log.info("Загрузка файла для скачивания: {}", filename);
        // Validate that the file name is a CSV file
        if (!filename.toLowerCase().endsWith(".csv")) {
            log.warn("Неправильный тип файла запрошен для скачивания: {}", filename);
            return ResponseEntity.badRequest().build();
        }

        // Get the file path from the service
        Path filePath = fileService.getFilePath(filename);

        // Check if file exists
        if (!Files.exists(filePath)) {
            log.warn("Файл не найден для скачивания: {}", filename);
            return ResponseEntity.notFound().build();
        }

        // Check if the resolved path is within the upload directory
        String uploadDir = fileService.getUploadDir();
        Path uploadDirPath = Path.of(uploadDir).normalize();
        Path normalizedFilePath = filePath.normalize();
        if (!normalizedFilePath.startsWith(uploadDirPath)) {
            log.error("Нарушение безопасности: попытка атаки через путь к файлу для файла: {}", filename);
            return ResponseEntity.badRequest().build();
        }

        // Verify that the file is actually inside the upload directory
        String filePathStr = normalizedFilePath.toString();
        String uploadDirStr = uploadDirPath.toString();
        if (!filePathStr.startsWith(uploadDirStr)) {
            log.error("Нарушение безопасности: попытка атаки через путь к файлу для файла: {}", filename);
            return ResponseEntity.badRequest().build();
        }

        // Create input stream resource
        FileInputStream fileInputStream = new FileInputStream(normalizedFilePath.toFile());
        InputStreamResource resource = new InputStreamResource(fileInputStream);

        // Return file
        log.info("Файл успешно подготовлен для скачивания: {}", filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(Files.size(normalizedFilePath))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
