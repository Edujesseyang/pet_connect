package com.pet_connect.backend_service.service;

import org.springframework.stereotype.Service;

import com.pet_connect.backend_service.dto.request.AddPetRequest;
import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Address;
import com.pet_connect.backend_service.entity.MedicalRecord;
import com.pet_connect.backend_service.entity.Pet;
import com.pet_connect.backend_service.entity.PetProfile;
import com.pet_connect.backend_service.repository.PetDAO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PetService {
    private final PetDAO petDAO;

    public PetService(PetDAO petDAO) {
        this.petDAO = petDAO;
    }

    public InnerRespond<Pet> addPet(AddPetRequest petInfo) {
        if (petInfo == null) {
            InnerRespond<Pet> response = new InnerRespond<>(false, "Pet information is missing.");
            return response;
        }
        if (petInfo.getName() == null || petInfo.getName().isEmpty()) {
            InnerRespond<Pet> response = new InnerRespond<>(false, "Pet name is required.");
            return response;
        }
        if (petInfo.getSpecies() == null || petInfo.getSpecies().isEmpty()) {
            InnerRespond<Pet> response = new InnerRespond<>(false, "Pet species is required.");
            return response;
        }
        if (petInfo.getBreed() == null || petInfo.getBreed().isEmpty()) {
            InnerRespond<Pet> response = new InnerRespond<>(false, "Pet breed is required.");
            return response;
        }

        try {
            Pet pet = new Pet();
            pet.setName(petInfo.getName().trim());
            pet.setSpecies(petInfo.getSpecies().trim());
            pet.setBreed(petInfo.getBreed().trim());

            // Address
            Address address = new Address();
            address.setStreet(petInfo.getStreet());
            address.setCity(petInfo.getCity());
            address.setState(petInfo.getState());
            address.setCountry(petInfo.getCountry());
            address.setZipCode(petInfo.getZipCode());
            pet.setAddress(address);

            // Pet profile
            PetProfile profile = new PetProfile();
            profile.setDateOfBirth(petInfo.getDateOfBirth());
            profile.setSex(petInfo.getSex());
            profile.setColor(petInfo.getColor());
            profile.setWeight(petInfo.getWeight());
            profile.setFriendlyLevel(petInfo.getFriendlyLevel());
            profile.setSize(petInfo.getSize());
            profile.setIsTrained(petInfo.getIsTrained());
            profile.setDescription(petInfo.getDescription());
            profile.setMicrochipNumber(petInfo.getMicrochipNumber());
            pet.setPetProfile(profile);
            // Medical record

            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setVaccination(petInfo.getVaccination());
            medicalRecord.setMedications(petInfo.getMedications());
            medicalRecord.setSpecialCare(petInfo.getSpecialCare());
            medicalRecord.setSurgeries(petInfo.getSurgeries());
            medicalRecord.setLabResults(petInfo.getLabResults());
            medicalRecord.setImagingResults(petInfo.getImagingResults());
            medicalRecord.setSpayedNeutered(petInfo.getSpayedNeutered());
            medicalRecord.setNote(petInfo.getNote());
            pet.setMedicalRecord(medicalRecord);
            pet = petDAO.addPet(pet);
            if (pet == null) {
                return new InnerRespond<>(false, "Pet adding faild.");
            }

            return new InnerRespond<>(true, "Pet added successfully.", pet);
        } catch (RuntimeException e) {
            log.error("error while adding pet.", e.getMessage());
            return new InnerRespond<>(false, "Pet adding faild.");
        }
    }
}
