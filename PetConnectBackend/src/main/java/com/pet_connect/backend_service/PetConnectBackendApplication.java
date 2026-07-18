package com.pet_connect.backend_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class PetConnectBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetConnectBackendApplication.class, args);
        log.info("PetConnect Backend Application started successfully.");
    }

}
