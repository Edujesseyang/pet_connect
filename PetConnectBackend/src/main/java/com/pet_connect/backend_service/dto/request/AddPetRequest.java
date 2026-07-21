package com.pet_connect.backend_service.dto.request;

import java.math.BigDecimal;
import java.sql.Date;


public class AddPetRequest {
    private String name;
    private String species;
    private String breed;

    // profile
    private String sex;
    private Date dateOfBirth;
    private String color;
    private String microchipNumber;
    private String size;
    private BigDecimal weight;
    private String description;
    private Integer friendlyLevel;
    private Boolean isTrained;

    // address
    private String country;
    private String state;
    private String city;
    private String street;
    private String zipCode;

    // medical record
    private String vaccination;
    private String allergies;
    private String medications;
    private String surgeries;
    private String labResults;
    private String imagingResults;
    private String note;
    private String specialCare;
    private Boolean spayedNeutered;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public String getSpecies() {
        return species;
    }
    public void setSpecies(String species) {
        this.species = species;
    }
    public String getBreed() {
        return breed;
    }
    public void setBreed(String breed) {
        this.breed = breed;
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
    public String getMicrochipNumber() {
        return microchipNumber;
    }
    public void setMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
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
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public String getVaccination() {
        return vaccination;
    }
    public void setVaccination(String vaccination) {
        this.vaccination = vaccination;
    }
    public String getAllergies() {
        return allergies;
    }
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
    public String getMedications() {
        return medications;
    }
    public void setMedications(String medications) {
        this.medications = medications;
    }
    public String getSurgeries() {
        return surgeries;
    }
    public void setSurgeries(String surgeries) {
        this.surgeries = surgeries;
    }
    public String getLabResults() {
        return labResults;
    }
    public void setLabResults(String labResults) {
        this.labResults = labResults;
    }
    public String getImagingResults() {
        return imagingResults;
    }
    public void setImagingResults(String imagingResults) {
        this.imagingResults = imagingResults;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public String getSpecialCare() {
        return specialCare;
    }
    public void setSpecialCare(String specialCare) {
        this.specialCare = specialCare;
    }
    public Boolean getSpayedNeutered() {
        return spayedNeutered;
    }
    public void setSpayedNeutered(Boolean spayedNeutered) {
        this.spayedNeutered = spayedNeutered;
    }
}
