package com.example.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /**
     * Validates and stores a PDF. Returns the relative path (e.g. "english/uuid.pdf"),
     * or null if no file was provided.
     */
    public String storePdf(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        validatePdf(file);

        try {
            Path dir = root.resolve(subDir).normalize();
            Files.createDirectories(dir);

            // Never use the client's filename: avoids path traversal and collisions.
            String storedName = UUID.randomUUID() + ".pdf";
            Path target = dir.resolve(storedName);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target);
            }
            return subDir + "/" + storedName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file", e);
        }
    }

    private void validatePdf(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only .pdf files are allowed");
        }
        // The content type header is client-controlled, so also check the file's magic bytes.
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(5);
            if (!"%PDF-".equals(new String(header, StandardCharsets.US_ASCII))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not a valid PDF");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read file", e);
        }
    }
}