package com.pet_connect.backend_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pet_connect.backend_service.dto.request.ChangePasswordRequest;
import com.pet_connect.backend_service.dto.request.SignupRequest;
import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.User;
import com.pet_connect.backend_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Sign up a new user", description = "This endpoint allows a new user to sign up by providing their details.", tags = {
            "User Management" })
    @PostMapping("/signup")
    public ResponseEntity<InnerRespond<User>> signup(@RequestBody SignupRequest userInfo) {
        InnerRespond<User> result = userService.signupUser(userInfo);
        if (result.getState()) {
            log.info("User signed up successfully: {}", userInfo.getUsername());
            return ResponseEntity.ok(result);
        } else {
            log.warn("User signup failed for username: {}. Reason: {}", userInfo.getUsername(), result.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @Operation(summary = "Change user password", description = "This endpoint allows a user to change their password by providing their username and both old and new passwords.", tags = {
            "User Management" })
    @PostMapping("/changePassword")
    public ResponseEntity<InnerRespond<User>> changePassword(@RequestBody ChangePasswordRequest request) {
        InnerRespond<User> result = userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        if(result.getState()) {
            log.info("Password changed successfully for user: {}", request.getUsername());
            return ResponseEntity.ok(result);
        } else {
            log.warn("Password change failed for user: {}. Reason: {}", request.getUsername(), result.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<InnerRespond<User>> login(@RequestParam String username,
            @RequestParam String password) {
        InnerRespond<User> result = userService.login(username, password);
        if (result.getState()) {
            log.info("User logged in successfully: {}", username);
            return ResponseEntity.ok(result);
        } else {
            log.warn("Login failed for user: {}. Reason: {}", username, result.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/getUserByUsername/{username}")
    public ResponseEntity<InnerRespond<User>> getUserByUsername(@PathVariable String username) {
        InnerRespond<User> result = userService.getUserByUsername(username);
        if (result.getState()) {
            log.info("User retrieved successfully: {}", username);
            return ResponseEntity.ok(result);
        } else {
            log.warn("Failed to retrieve user: {}. Reason: {}", username, result.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getUserByEmail/{email}")
    public ResponseEntity<InnerRespond<User>> getUserByEmail(@PathVariable String email) {
        InnerRespond<User> result = userService.getUserByEmail(email);
        if (result.getState()) {
            log.info("User retrieved successfully by email: {}", email);
            return ResponseEntity.ok(result);
        } else {
            log.warn("Failed to retrieve user by email: {}. Reason: {}", email, result.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
