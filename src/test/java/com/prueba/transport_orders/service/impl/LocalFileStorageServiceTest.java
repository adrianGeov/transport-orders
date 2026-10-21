package com.prueba.transport_orders.service.impl;

import com.prueba.transport_orders.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

     @TempDir
    Path tempDir;

    private LocalFileStorageService storageService;
    private UUID assignmentId;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService(tempDir.toString());
        assignmentId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Guarda un PDF válido en disco")
    void storeDocument_validPdf_writesFile() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "pdf".getBytes());

        String path = storageService.storeDocument(assignmentId, file);

        assertThat(path).endsWith("document.pdf");
        assertThat(Files.exists(tempDir.resolve(path))).isTrue();
    }

    @Test
    @DisplayName("Guarda una imagen PNG válida en disco")
    void storeImage_validPng_writesFile() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "png".getBytes());

        String path = storageService.storeImage(assignmentId, file);

        assertThat(path).endsWith("image.png");
        assertThat(Files.exists(tempDir.resolve(path))).isTrue();
    }

    @Test
    @DisplayName("Guarda una imagen JPG válida en disco")
    void storeImage_validJpg_writesFile() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "jpg".getBytes());

        String path = storageService.storeImage(assignmentId, file);

        assertThat(path).endsWith("image.jpg");
    }

    @Test
    @DisplayName("Rechaza un archivo vacío")
    void storeDocument_emptyFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> storageService.storeDocument(assignmentId, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("Rechaza una imagen enviada como documento")
    void storeDocument_imageFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "png".getBytes());

        assertThatThrownBy(() -> storageService.storeDocument(assignmentId, file))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    @DisplayName("Rechaza un archivo renombrado a .pdf con otro tipo de contenido")
    void storeDocument_fakePdf_throwsException() {
        MockMultipartFile file = new MockMultipartFile("file", "virus.pdf", "application/x-msdownload", "exe".getBytes());

        assertThatThrownBy(() -> storageService.storeDocument(assignmentId, file))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    @DisplayName("Rechaza un PDF enviado como imagen")
    void storeImage_pdfFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "pdf".getBytes());

        assertThatThrownBy(() -> storageService.storeImage(assignmentId, file))
                .isInstanceOf(InvalidFileException.class);
    }

}
