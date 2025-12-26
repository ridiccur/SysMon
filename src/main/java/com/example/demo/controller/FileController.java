package com.example.demo.controller;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.service.FileService;

import io.swagger.v3.oas.annotations.Operation;

// Controller for handling file uploads
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // Upload a file to the server
    @Operation(summary = "Загрузка файла на сервер")
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
}
