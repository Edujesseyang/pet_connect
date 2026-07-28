package com.pet_connect.backend_service.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pet_connect.backend_service.dto.respond.PhotoUploadResponse;
import com.pet_connect.backend_service.dto.respond.UserPhotoResponse;
import com.pet_connect.backend_service.service.PhotoService;

@RestController
@RequestMapping("/photos")
@CrossOrigin(origins = "http://localhost:5173")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/upload/pet")
    public ResponseEntity<PhotoUploadResponse> uploadPetPhoto(
            @RequestPart("file") MultipartFile file,
            @RequestParam int petId,
            @RequestParam int uploaderId,
            @RequestParam(required = false) String description) throws IOException {

        PhotoUploadResponse response = photoService.uploadPetPhoto(
                file,
                petId,
                uploaderId,
                description);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUsersPhoto(
            @RequestPart("file") MultipartFile file,
            @RequestParam int userId) throws IOException {

        UserPhotoResponse response = photoService.uploadUserPhoto(file, userId);

        return ResponseEntity.ok(response);
    }
}