package com.pet_connect.backend_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pet_connect.backend_service.entity.InnerRespond;
import com.pet_connect.backend_service.entity.User;
import com.pet_connect.backend_service.service.SignupService;

@RestController
public class SignupController {

    @PostMapping("/signup")
    public ResponseEntity<?> postMethodName(@RequestBody User user) {
        SignupService service = new SignupService();
        InnerRespond result = service.signupUser(user);

        return result.getState() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

}
