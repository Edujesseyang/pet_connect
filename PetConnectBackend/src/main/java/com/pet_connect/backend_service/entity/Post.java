package com.pet_connect.backend_service.entity;

import java.sql.Timestamp;

public class Post {
    private int postId;
    private int userId;
    private int petId;
    private Timestamp createdAt;
    private String title;
    private String content;
    private String type;
    private double adoptionFee;
    private Address pickupLocation;

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAdoptionFee() {
        return adoptionFee;
    }

    public void setAdoptionFee(double adoptionFee) {
        this.adoptionFee = adoptionFee;
    }

    public Address getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(Address pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
}
