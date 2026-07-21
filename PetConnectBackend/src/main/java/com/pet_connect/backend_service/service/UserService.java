package com.pet_connect.backend_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pet_connect.backend_service.dto.request.SignupRequest;
import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Address;
import com.pet_connect.backend_service.entity.User;
import com.pet_connect.backend_service.entity.UserProfile;
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
    public InnerRespond<User> signupUser(SignupRequest userInfo) {
        if (!ispasswordValid(userInfo.getPasswordHash())) {
            return new InnerRespond<>(false,
                    "Invalid password format: 2 digits, 2 uppercase, 2 lowercase, 1 special character from ~!@#$%^&*_, length greater or equal to 8.",
                    null);
        }

        if (dao.getUserByUsername(userInfo.getUsername()) != null) {
            return new InnerRespond<>(false, "Username is already in use", null);
        }

        if (dao.isEmailExist(userInfo.getEmail())) {
            return new InnerRespond<>(false, "Email is already in use", null);
        }

        String hashPassword = passwordEncoder.encode(userInfo.getPasswordHash());
        userInfo.setPasswordHash(hashPassword);

        User user = new User();
        user.setUsername(userInfo.getUsername());
        user.setFullname(userInfo.getFullname());
        user.setEmail(userInfo.getEmail());
        user.setRole("regular");
        user.setPasswordHash(userInfo.getPasswordHash());
        Address address = new Address();
        address.setCountry(userInfo.getCountry());
        address.setState(userInfo.getState());
        address.setCity(userInfo.getCity());
        address.setStreet(userInfo.getStreet());
        address.setZipCode(userInfo.getZipCode());
        user.setAddress(address);
        UserProfile userProfile = new UserProfile();
        userProfile.setGender(userInfo.getGender());
        userProfile.setDateOfBirth(userInfo.getDateOfBirth());
        userProfile.setPhoneNumber(userInfo.getPhoneNumber());
        userProfile.setBio(userInfo.getBio());
        userProfile.setProfilePhotoUrl(userInfo.getProfilePhotoUrl());
        userProfile.setSocialMediaLinks(userInfo.getSocialMediaLinks());
        userProfile.setHouseholdType(userInfo.getHouseholdType());
        userProfile.setAdoptionExp(userInfo.getAdoptionExp());
        user.setUserProfile(userProfile);

        user = dao.insertUser(user);
        user.setPasswordHash("*****");
        return new InnerRespond<>(true, "success", user);
    }

    @Transactional
    public InnerRespond<User> changePassword(String username, String oldPassword, String newPassword) {
        if (!verifyUser(username, oldPassword)) {
            return new InnerRespond<>(false, "Old password is incorrect", null);
        }

        String hashNewPassword = passwordEncoder.encode(newPassword);
        dao.setPasswordByUsername(username, hashNewPassword);

        User updatedUser = dao.getUserByUsername(username);
        updatedUser.setPasswordHash("*****");
        return new InnerRespond<>(true, "Password changed successfully", updatedUser);
    }

    @Transactional
    public InnerRespond<User> login(String username, String password) {
        if (!verifyUser(username, password)) {
            return new InnerRespond<>(false, "Username or password is incorrect", null);
        }
        User user = dao.getUserByUsername(username);
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
    private boolean verifyUser(String username, String rawPassword) {
        if (!dao.isUserExist(username)) {
            return false;
        }

        if (!passwordEncoder.matches(rawPassword, dao.getUserPasswordHash(username))) {
            return false;
        }
        return true;
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
