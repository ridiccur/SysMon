package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.example.demo.service.FileService;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    FileService fileService;

    @InjectMocks
    FileController controller;

    @Test
    void uploadFile_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.csv", "text/csv", "a,b\n1,2".getBytes());
        when(fileService.storeFile(file)).thenReturn("/tmp/a.csv");

        var resp = controller.uploadFile(file);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void downloadFile_badExtension_returnsBadRequest() throws IOException {
        var resp = controller.downloadFile("data.txt");
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void downloadFile_fileNotFound_returnsNotFound() throws Exception {
        when(fileService.getFilePath("no.csv")).thenReturn(Path.of("no.csv"));
        var resp = controller.downloadFile("no.csv");
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void downloadFile_success_returnsResource() throws Exception {
        Path tempDir = Files.createTempDirectory("up");
        Path f = tempDir.resolve("ok.csv");
        Files.writeString(f, "hello");

        when(fileService.getFilePath("ok.csv")).thenReturn(f);
        when(fileService.getUploadDir()).thenReturn(tempDir.toString());

        ResponseEntity<InputStreamResource> resp = controller.downloadFile("ok.csv");
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        InputStreamResource resource = resp.getBody();
        if (resource != null) resource.getInputStream().close();
        Files.deleteIfExists(f);
        Files.deleteIfExists(tempDir);
    }
}
