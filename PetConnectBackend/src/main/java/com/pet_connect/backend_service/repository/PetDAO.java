package com.pet_connect.backend_service.repository;

import org.springframework.stereotype.Repository;

import com.pet_connect.backend_service.entity.Pet;

import jakarta.activation.DataSource;

@Repository
public class PetDAO {
    private final DataSource dataSource;

    public PetDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void  addPet(Pet pet) {
        // Implement the logic to add a pet to the database using the dataSource
        
    }

}
