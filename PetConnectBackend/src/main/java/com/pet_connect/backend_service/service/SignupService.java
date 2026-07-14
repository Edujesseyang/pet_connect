package com.pet_connect.backend_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pet_connect.backend_service.entity.InnerRespond;
import com.pet_connect.backend_service.entity.User;
import com.pet_connect.backend_service.repository.SignupDAO;

@Service
public class SignupService {
    private SignupDAO dao;

    public SignupService() {
        this.dao = new SignupDAO();
    }

    public InnerRespond signupUser(User user) {
        if (!ispasswordValid(user.getPassword())) {
            return new InnerRespond(false,
                    "Invalid password format: 2 digits, 2 uppercase, 2 lowercase, 1 special character from ~!@#$%^&*_, length greater or equal to 8.");
        }

        /*
         * User checkUser = dao.queryUser(user);
         * if (checkUser == null){
         *      return new InnerRespond(false, "user is exists");
         * } 
         * 
         * if (checkUser.email == user.email){
         *      return new InnerRespond(false, "Email is already in use")
         * }
         */

        user.setPassword(hashPassword(user.getPassword()));

        /*
         * try{
         * dao.insert(user)
         * }catch(Eeception e){
         * logger.log(e);
         * };
         * 
         */

        return new InnerRespond(true, "success");
    }

    private String hashPassword(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hash = passwordEncoder.encode(password);
        return hash;
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
