package com.pet_connect.backend_service.dto.respond;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PhotoUploadResponse {
    private int photoId;
    private String url;
    private String description;
}