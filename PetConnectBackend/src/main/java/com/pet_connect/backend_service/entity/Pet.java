package com.pet_connect.backend_service.entity;

import java.util.ArrayList;
import java.util.List;

public class Pet {

    private int petId;
    private String name;
    private PetProfile petProfile;
    private List<MedicalRecord> medicalRecordsList;
    private Address address;
    private String species;
    private String breed;

    public Pet() {
        this.medicalRecordsList = new ArrayList<>();
    }

    public void addMedicalRecord(MedicalRecord medicalRecord) {
        medicalRecordsList.add(medicalRecord);
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PetProfile getPetProfile() {
        return petProfile;
    }

    public void setPetProfile(PetProfile petProfile) {
        this.petProfile = petProfile;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<MedicalRecord> getMedicalRecordsList() {
        return medicalRecordsList;
    }

    public void setMedicalRecordsList(List<MedicalRecord> medicalRecordsList) {
        this.medicalRecordsList = medicalRecordsList;
    }

}
