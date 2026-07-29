package com.pet_connect.backend_service.dto.respond;

public class UserPhotoResponse {
    private int userId;
    private String url;

    public UserPhotoResponse(int userId, String url) {
        this.url = url;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
