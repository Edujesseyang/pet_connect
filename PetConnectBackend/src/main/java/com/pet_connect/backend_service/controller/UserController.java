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
import com.pet_connect.backend_service.entity.User;
import com.pet_connect.backend_service.service.UserService;
import com.pet_connect.backend_service.utility.InnerRespond;

import io.swagger.v3.oas.annotations.Operation;

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
    public ResponseEntity<InnerRespond<User>> signup(@RequestBody User user) {
        InnerRespond<User> result = userService.signupUser(user);
        return result.getState() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @Operation(summary = "Change user password", description = "This endpoint allows a user to change their password by providing their username and both old and new passwords.", tags = {
            "User Management" })
    @PostMapping("/changePassword")
    public ResponseEntity<InnerRespond<User>> changePassword(@RequestBody ChangePasswordRequest request) {
        InnerRespond<User> result = userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return result.getState() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<InnerRespond<User>> login(@RequestParam String username,
            @RequestParam String password) {
        InnerRespond<User> result = userService.login(username, password);
        return result.getState() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/getUser/{username}")
    public ResponseEntity<InnerRespond<User>> getUserByUsername(@PathVariable String username) {
        InnerRespond<User> result = userService.getUserByUsername(username);
        return result.getState() ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/getUser/{email}")
    public ResponseEntity<InnerRespond<User>> getUserByEmail(@PathVariable String email) {
        InnerRespond<User> result = userService.getUserByEmail(email);
        return result.getState() ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

}
