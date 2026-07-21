package com.pet_connect.backend_service.entity;

public class Photo {
    private int phototId;
    private String url;
    private String description;
    private int uploaderId;

    public Photo() {
    }

    public int getPhototId() {
        return phototId;
    }

    public void setPhototId(int phototId) {
        this.phototId = phototId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(int uploaderId) {
        this.uploaderId = uploaderId;
    }
}
