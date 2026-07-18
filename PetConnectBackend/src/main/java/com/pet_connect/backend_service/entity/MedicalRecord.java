package com.pet_connect.backend_service.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class MedicalRecord {
    private int petId;

    private Timestamp createdAt; 
    private String medicalHistory;
    private String vaccinationRecords;
    private String allergies;
    private String medications;
    private String surgeries;
    private String labResults;
    private String imagingResults;
    private String notes;
    private String specialCare;
    private List<String> document_url;
    private String dietaryRestrictions;
    
    public MedicalRecord(){
        this.document_url = new ArrayList<>();
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getVaccinationRecords() {
        return vaccinationRecords;
    }

    public void setVaccinationRecords(String vaccinationRecords) {
        this.vaccinationRecords = vaccinationRecords;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getDocument_url() {
        return document_url;
    }

    public void setDocument_url(List<String> document_url) {
        this.document_url = document_url;
    }

    public String getSpecialCare() {
        return specialCare;
    }

    public void setSpecialCare(String specialCare) {
        this.specialCare = specialCare;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }
}
