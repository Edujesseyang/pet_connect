package com.pet_connect.backend_service.entity;


public class MedicalRecord {
    private String vaccination;
    private String allergies;
    private String medications;
    private String surgeries;
    private String labResults;
    private String imagingResults;
    private String notes;
    private String specialCare;
    private Boolean spayedNeutered;
    
    public MedicalRecord(){
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

    public String getVaccination() {
        return vaccination;
    }

    public void setVaccination(String vaccination) {
        this.vaccination = vaccination;
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
  
