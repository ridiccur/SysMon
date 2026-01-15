package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockMultipartFile;

class FileServiceTest {

    @Test
    void storeAndExists_and_getFilePath() throws Exception {
        FileService svc = new FileService();
        Path tempDir = Files.createTempDirectory("upl");

        Field f = FileService.class.getDeclaredField("uploadDir");
        f.setAccessible(true);
        f.set(svc, tempDir.toString());

        MockMultipartFile file = new MockMultipartFile("file", "a.csv", "text/csv", "1,2".getBytes());
        String path = svc.storeFile(file);
        assertThat(path).isNotBlank();

        Path p = svc.getFilePath(Path.of(path).getFileName().toString());
        assertThat(Files.exists(p)).isTrue();

        assertThat(svc.fileExists(p.getFileName().toString())).isTrue();

        Files.deleteIfExists(p);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void storeFile_nonCsv_throws() throws Exception {
        FileService svc = new FileService();
        Path tempDir = Files.createTempDirectory("upl2");
        Field f = FileService.class.getDeclaredField("uploadDir");
        f.setAccessible(true);
        f.set(svc, tempDir.toString());

        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());
        assertThrows(IOException.class, () -> svc.storeFile(file));

        Files.deleteIfExists(tempDir);
    }
}
