package com.pet_connect.backend_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Pet;
import com.pet_connect.backend_service.service.PetService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping("/add")
    public ResponseEntity<Pet> addPet(@RequestBody Pet pet) {
        InnerRespond<Pet> response = petService.addPet(pet);
        if (response.getState()) {
            log.info("Pet added successfully: {}", response.getData().getName());
            return ResponseEntity.ok(response.getData());
        } else {
            log.warn("Failed to add pet. Reason: {}", response.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    

}
