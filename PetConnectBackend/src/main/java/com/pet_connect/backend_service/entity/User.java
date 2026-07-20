package com.pet_connect.backend_service.entity;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int userId;

    private String username;
    private String fullname;
    private String passwordHash;
    private String email;
    private String role;
    private Address address;
    private List<Post> posts;
    private List<Pet> ownedPets;
    private List<Pet> savedPets;

    public User() {
        posts = new ArrayList<>();
        ownedPets = new ArrayList<>();
        savedPets = new ArrayList<>();
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public void addOwnedPet(Pet pet) {
        ownedPets.add(pet);
    }

    public void addSavedPet(Pet pet) {
        savedPets.add(pet);
    }

    // getters and setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<Pet> getOwnedPets() {
        return ownedPets;
    }

    public void setOwnedPets(List<Pet> ownedPets) {
        this.ownedPets = ownedPets;
    }

    public List<Pet> getSavedPets() {
        return savedPets;
    }

    public void setSavedPets(List<Pet> savedPets) {
        this.savedPets = savedPets;
    }
}
