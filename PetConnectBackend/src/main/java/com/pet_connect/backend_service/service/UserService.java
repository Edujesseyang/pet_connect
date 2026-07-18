package com.pet_connect.backend_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.User;
import com.pet_connect.backend_service.repository.UserDAO;
 
@Service
public class UserService {
    private final UserDAO dao;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDAO dao) {
        this.dao = dao;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public InnerRespond<User> signupUser(User user) {
        if (!ispasswordValid(user.getPasswordHash())) {
            return new InnerRespond<>(false,
                    "Invalid password format: 2 digits, 2 uppercase, 2 lowercase, 1 special character from ~!@#$%^&*_, length greater or equal to 8.",
                    null);
        }

        if (dao.getUserByUsername(user.getUsername()) != null) {
            return new InnerRespond<>(false, "Username is already in use", null);
        }

        String hashPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(hashPassword);

        dao.insertUser(user);
        user.setPasswordHash("*****");
        return new InnerRespond<>(true, "success", user);
    }

    @Transactional
    public InnerRespond<User> changePassword(String username, String oldPassword, String newPassword) {
        User verfiedUser = verifyUser(username, oldPassword);
        if (verfiedUser == null) {
            return new InnerRespond<>(false, "Old password is incorrect", null);
        }

        String hashNewPassword = passwordEncoder.encode(newPassword);
        try {
            dao.setPasswordByUsername(username, hashNewPassword);
        } catch (RuntimeException e) {
            return new InnerRespond<>(false, e.getMessage(), null);
        }
        verfiedUser.setPasswordHash("*****");
        return new InnerRespond<>(true, "Password changed successfully", verfiedUser);
    }

    @Transactional
    public InnerRespond<User> login(String username, String password) {
        User user = verifyUser(username, password);
        if (user == null) {
            return new InnerRespond<>(false, "Username or password is incorrect", null);
        }
        user.setPasswordHash("*****");
        return new InnerRespond<>(true, "Login successful", user);
    }

    @Transactional
    public InnerRespond<User> getUserByUsername(String username) {
        User user = dao.getUserByUsername(username);
        if (user == null) {
            return new InnerRespond<>(false, "User not found", null);
        }
        user.setPasswordHash("*****");
        return new InnerRespond<>(true, "User found", user);
    }

    @Transactional
    public InnerRespond<User> getUserByEmail(String email) {
        User user = dao.getUserByEmail(email);
        if (user == null) {
            return new InnerRespond<>(false, "User not found", null);
        }
        user.setPasswordHash("*****");
        return new InnerRespond<>(true, "User found", user);
    }

    // ================ Private Helper Methods =================
    private User verifyUser(String username, String rawPassword) {
        User user = dao.getUserByUsername(username);
        if (user == null) {
            return null;
        }

        if (passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    private boolean ispasswordValid(String password) {
        if (password.length() < 8) {
            return false;
        }
        int uppercaseCount = 0;
        int lowercaseCount = 0;
        int numCount = 0;
        int specialCharCount = 0;

        for (char c : password.toCharArray()) {
            if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^' || c == '&' || c == '*'
                    || c == '_') {
                specialCharCount++;
            }
            if (Character.isUpperCase(c)) {
                uppercaseCount++;
            }
            if (Character.isLowerCase(c)) {
                lowercaseCount++;
            }
            if (Character.isDigit(c)) {
                numCount++;
            }
        }
        return uppercaseCount >= 2 && lowercaseCount >= 2 && numCount >= 2 && specialCharCount >= 1;
    }
}
