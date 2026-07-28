package com.pet_connect.backend_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pet_connect.backend_service.dto.request.AddPetRequest;
import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Pet;
import com.pet_connect.backend_service.service.PetService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @Operation(summary = "Add a pet", description = "This endpoint allows a user to add a new pet", tags = {
            "Pet Management" })
    @PostMapping("/add_by_user/{user_id}")
    public ResponseEntity<Pet> addPet(@RequestBody AddPetRequest petInfo, @RequestParam int userId) {
        InnerRespond<Pet> response = petService.addPet(petInfo, userId);
        if (response.getState()) {
            log.info("Pet added successfully: {}", response.getData().getName());
            return ResponseEntity.ok(response.getData());
        } else {
            log.warn("Failed to add pet. Reason: {}", response.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete a pet", description = "This endpoint allows a user to delete a pet", tags = {
            "Pet Management" })
    @PostMapping("/delete_by_id/{pet_id}")
    public ResponseEntity<Pet> deletePet(@RequestParam int petId) {
        InnerRespond<Pet> response = petService.deletePet(petId);
        if (response.getState()) {
            log.info("Pet delete successfully: {}", response.getData().getName());
            return ResponseEntity.ok(response.getData());
        } else {
            log.warn("Failed to delete pet. Reason: {}", response.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "get a pet", description = "This endpoint allows a user to get a pet", tags = {
            "Pet Management" })
    @GetMapping("/get_by_id/{pet_id}")
    public ResponseEntity<Pet> getPet(@RequestParam int petId) {
        InnerRespond<Pet> response = petService.getPet(petId);
        if (response.getState()) {
            log.info("Qureying the pet successfully: {}", response.getData().getName());
            return ResponseEntity.ok(response.getData());
        } else {
            log.warn("Failed to query the pet. Reason: {}", response.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
