package com.prueba.transport_orders.service.impl;

import com.prueba.transport_orders.exception.FileStorageException;
import com.prueba.transport_orders.exception.InvalidFileException;
import com.prueba.transport_orders.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;


@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf");
    private static final Set<String> DOCUMENT_CONTENT_TYPES = Set.of("application/pdf");

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg");
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/png", "image/jpeg");

    private final Path rootDir;

    public LocalFileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String storeDocument(UUID assignmentId, MultipartFile file) {
        validate(file, DOCUMENT_EXTENSIONS, DOCUMENT_CONTENT_TYPES, "Solo se permiten archivos PDF");
        return store(assignmentId, "document.pdf", file);
    }

    @Override
    public String storeImage(UUID assignmentId, MultipartFile file) {
        String extension = validate(file, IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES,
                "Solo se permiten imágenes .png o .jpg");
        return store(assignmentId, "image." + extension, file);
    }

    private String validate(MultipartFile file, Set<String> allowedExtensions,
                            Set<String> allowedContentTypes, String errorMessage) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("El archivo está vacío");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !allowedExtensions.contains(extension.toLowerCase())) {
            throw new InvalidFileException(errorMessage);
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(errorMessage);
        }

        return extension.toLowerCase();
    }

    private String store(UUID assignmentId, String fileName, MultipartFile file) {
        try {
            Path directory = rootDir.resolve("assignments").resolve(assignmentId.toString());
            Files.createDirectories(directory);

            Path target = directory.resolve(fileName);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Archivo almacenado asignacion={} archivo={} tamaño={} bytes",
                    assignmentId, fileName, file.getSize());
            return rootDir.relativize(target).toString();
        } catch (IOException ex) {
            throw new FileStorageException("No fue posible almacenar el archivo " + fileName, ex);
        }
    }

}
