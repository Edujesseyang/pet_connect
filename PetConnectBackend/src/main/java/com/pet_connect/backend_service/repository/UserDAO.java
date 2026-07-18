package com.pet_connect.backend_service.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.pet_connect.backend_service.entity.User;

@Repository
public class UserDAO {
    private final DataSource dataSource;

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insertUser(User user) {
        // String sql = """
        //         INSERT INTO users (
        //             username,
        //             first_name,
        //             last_name,
        //             gender,
        //             date_of_birth,
        //             password_hash,
        //             email,
        //             country,
        //             state,
        //             city,
        //             street,
        //             zip_code,
        //             phone_number,
        //             bio,
        //             role,
        //             active,
        //             profile_photo_url
        //         )
        //         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        //         """;

        // try (
        //         Connection conn = dataSource.getConnection();
        //         PreparedStatement stmt = conn.prepareStatement(sql)) {
        //     stmt.setString(1, user.getUsername());
        //     stmt.setString(2, user.getFirstName());
        //     stmt.setString(3, user.getLastName());
        //     stmt.setString(4, user.getGender());
        //     stmt.setDate(5, new java.sql.Date(user.getDateOfBirth().getTime()));
        //     stmt.setString(6, user.getPasswordHash());
        //     stmt.setString(7, user.getEmail());
        //     stmt.setString(8, user.getCountry());
        //     stmt.setString(9, user.getState());
        //     stmt.setString(10, user.getCity());
        //     stmt.setString(11, user.getStreet());
        //     stmt.setString(12, user.getZipCode());
        //     stmt.setString(13, user.getPhoneNumber());
        //     stmt.setString(14, user.getBio());
        //     stmt.setString(15, user.getRole());
        //     stmt.setBoolean(16, user.isActived());
        //     stmt.setString(17, user.getProfilePhotoUrl());
        // } catch (SQLException e) {
        //     throw new RuntimeException("Database error occurred while inserting user.", e);
        // }
    }

    public void setPasswordByUsername(String username, String hashedPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred while updating user password.", e);
        }
    }

    public User getUserByUsername(String username) {
        // String sql = "SELECT * FROM users WHERE username = ?";
        // try (
        //         Connection conn = dataSource.getConnection();
        //         PreparedStatement stmt = conn.prepareStatement(sql)) {
        //     stmt.setString(1, username);
        //     var rs = stmt.executeQuery();
        //     if (rs.next()) {
        //         User user = new User();
        //         user.setUserId(rs.getInt("user_id"));
        //         user.setUsername(rs.getString("username"));
        //         user.setFirstName(rs.getString("first_name"));
        //         user.setLastName(rs.getString("last_name"));
        //         user.setGender(rs.getString("gender"));
        //         user.setDateOfBirth(rs.getDate("date_of_birth"));
        //         user.setPasswordHash(rs.getString("password_hash"));
        //         user.setEmail(rs.getString("email"));
        //         user.setCountry(rs.getString("country"));
        //         user.setState(rs.getString("state"));
        //         user.setCity(rs.getString("city"));
        //         user.setStreet(rs.getString("street"));
        //         user.setZipCode(rs.getString("zip_code"));
        //         user.setPhoneNumber(rs.getString("phone_number"));
        //         user.setBio(rs.getString("bio"));
        //         user.setRole(rs.getString("role"));
        //         user.setActived(rs.getBoolean("active"));
        //         user.setProfilePhotoUrl(rs.getString("profile_photo_url"));
        //         return user;
        //     }
        // } catch (SQLException e) {
        //     throw new RuntimeException("Database error occurred while fetching user by username.", e);
        // }
        return null;
    }

    public User getUserByEmail(String email) {
        // String sql = "SELECT * FROM users WHERE email = ?";
        // try (
        //         Connection conn = dataSource.getConnection();
        //         PreparedStatement stmt = conn.prepareStatement(sql)) {
        //     stmt.setString(1, email);
        //     var rs = stmt.executeQuery();
        //     if (rs.next()) {
        //         User user = new User();
        //         user.setUserId(rs.getInt("user_id"));
        //         user.setUsername(rs.getString("username"));
        //         user.setFirstName(rs.getString("first_name"));
        //         user.setLastName(rs.getString("last_name"));
        //         user.setGender(rs.getString("gender"));
        //         user.setDateOfBirth(rs.getDate("date_of_birth"));
        //         user.setPasswordHash(rs.getString("password_hash"));
        //         user.setEmail(rs.getString("email"));
        //         user.setCountry(rs.getString("country"));
        //         user.setState(rs.getString("state"));
        //         user.setCity(rs.getString("city"));
        //         user.setStreet(rs.getString("street"));
        //         user.setZipCode(rs.getString("zip_code"));
        //         user.setPhoneNumber(rs.getString("phone_number"));
        //         user.setBio(rs.getString("bio"));
        //         user.setRole(rs.getString("role"));
        //         user.setActived(rs.getBoolean("active"));
        //         user.setProfilePhotoUrl(rs.getString("profile_photo_url"));
        //         return user;
        //     }
        // } catch (SQLException e) {
        //     throw new RuntimeException("Database error occurred while fetching user by email.", e);
        // }
        return null;
    }
}