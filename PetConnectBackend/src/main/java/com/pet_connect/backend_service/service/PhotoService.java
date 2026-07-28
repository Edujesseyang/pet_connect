package com.pet_connect.backend_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pet_connect.backend_service.dto.respond.PhotoUploadResponse;
import com.pet_connect.backend_service.dto.respond.UserPhotoResponse;
import com.pet_connect.backend_service.repository.PhotoDAO;

@Service
public class PhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp");

    private final PhotoDAO photoDAO;
    private final Path uploadPath;

    public PhotoService(
            PhotoDAO photoDAO,
            @Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.photoDAO = photoDAO;
        this.uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(this.uploadPath);
    }

    public PhotoUploadResponse uploadPetPhoto(
            MultipartFile file,
            int petId,
            int uploaderId,
            String description) throws IOException {

        validateFile(file);

        String extension = getExtension(
                file.getOriginalFilename());

        String storedFileName = UUID.randomUUID() + extension;

        Path targetPath = uploadPath.resolve(
                storedFileName);

        Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING);

        String photoUrl = "http://localhost:8080/uploads/"
                + storedFileName;

        int photoId = photoDAO.insertPetPhoto(
                petId,
                uploaderId,
                photoUrl,
                description);

        return new PhotoUploadResponse(
                photoId,
                photoUrl,
                description);
    }

    public UserPhotoResponse uploadUserPhoto(MultipartFile file, int userId) throws IOException {
        validateFile(file);
        String extension = getExtension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + extension;
        Path targetPath = uploadPath.resolve(storedFileName);

        Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING);

        String photoUrl = "http://localhost:8080/uploads/"
                + storedFileName;

        photoDAO.insertUserPhoto(userId, photoUrl);
        return new UserPhotoResponse(userId, photoUrl);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Photo file cannot be empty.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(
                file.getContentType())) {
            throw new IllegalArgumentException(
                    "Only JPEG, PNG, and WebP images are allowed.");
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null ||
                !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(
                fileName.lastIndexOf(".")).toLowerCase();
    }
}