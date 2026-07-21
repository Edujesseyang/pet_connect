package com.pet_connect.backend_service.entity;

import java.math.BigDecimal;
import java.sql.Date;

public class PetProfile {
    private int petId;
    private String sex;
    private Date dateOfBirth;
    private String color;
    private String microchipNumber;
    private String size;
    private BigDecimal weight;
    private String description;
    private Integer friendlyLevel;
    private Boolean isTrained;

    public String getSex() {
        return sex;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFriendlyLevel() {
        return friendlyLevel;
    }

    public void setFriendlyLevel(Integer friendlyLevel) {
        this.friendlyLevel = friendlyLevel;
    }

    public Boolean getIsTrained() {
        return isTrained;
    }

    public void setIsTrained(Boolean isTrained) {
        this.isTrained = isTrained;
    }

    public String getMicrochipNumber() {
        return microchipNumber;
    }

    public void setMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
    }

}
