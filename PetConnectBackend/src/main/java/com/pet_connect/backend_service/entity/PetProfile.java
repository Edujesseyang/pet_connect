package com.pet_connect.backend_service.entity;

import java.sql.Date;
public class PetProfile {
    private int petId;

    private String sex;
    private Date dateOfBirth;
    private String color;
    private String microchipId;
    private String size;
    private String weight;
    private String description;
    private String friendlyLevel;
    private Boolean isTrained;
    private String energyLevel;
    private String activityLevel;
    private String favoriteActivities;
    private String favoriteToys;
    private String favoriteTreats;

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMicrochipId() {
        return microchipId;
    }

    public void setMicrochipId(String microchipId) {
        this.microchipId = microchipId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFriendlyLevel() {
        return friendlyLevel;
    }

    public void setFriendlyLevel(String friendlyLevel) {
        this.friendlyLevel = friendlyLevel;
    }

    public Boolean getIsTrained() {
        return isTrained;
    }

    public void setIsTrained(Boolean isTrained) {
        this.isTrained = isTrained;
    }

    public String getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(String energyLevel) {
        this.energyLevel = energyLevel;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getFavoriteActivities() {
        return favoriteActivities;
    }

    public void setFavoriteActivities(String favoriteActivities) {
        this.favoriteActivities = favoriteActivities;
    }

    public String getFavoriteToys() {
        return favoriteToys;
    }

    public void setFavoriteToys(String favoriteToys) {
        this.favoriteToys = favoriteToys;
    }

    public String getFavoriteTreats() {
        return favoriteTreats;
    }

    public void setFavoriteTreats(String favoriteTreats) {
        this.favoriteTreats = favoriteTreats;
    }

    
}
