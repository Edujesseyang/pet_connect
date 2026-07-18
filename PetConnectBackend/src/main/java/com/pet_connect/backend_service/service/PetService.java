package com.pet_connect.backend_service.service;

import org.springframework.stereotype.Service;

import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Pet;
import com.pet_connect.backend_service.repository.PetDAO;


@Service
public class PetService {
    private final PetDAO petDAO;

    public PetService(PetDAO petDAO) {
        this.petDAO = petDAO;
    }

    public InnerRespond<Pet> addPet(Pet pet) {
        if (pet == null) {
            InnerRespond<Pet> response = new InnerRespond<>(false, "Pet information is missing.");
            return response;
        }
        if (pet.getName() == null || pet.getName().isEmpty()) {
            InnerRespond<Pet> response = new InnerRespond<>(false, "Pet name is required.");
            return response;
        }
        if (pet.getSpecies() == null || pet.getSpecies().isEmpty()) {
            InnerRespond<Pet> response = new InnerRespond<>(false, "Pet species is required.");
            return response;
        }   
        if (pet.getBreed() == null || pet.getBreed().isEmpty()) {
            InnerRespond<Pet> response = new InnerRespond<>(false, "Pet breed is required.");
            return response;
        }   

        petDAO.addPet(pet);
        return new InnerRespond<>(true, "Pet added successfully.", pet);
    }
    
    
}
