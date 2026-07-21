package com.pet_connect.backend_service.service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PhotoService {
    private static final String UPLOAD_DIR = "uploads/";

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Empty file.");
        }
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename =
                    UUID.randomUUID()
                    + "_"
                    + file.getOriginalFilename();
            Path target = uploadPath.resolve(filename);
            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
