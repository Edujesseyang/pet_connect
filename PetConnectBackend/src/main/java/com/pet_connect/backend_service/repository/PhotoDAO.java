package com.pet_connect.backend_service.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

@Repository
public class PhotoDAO {

    private final DataSource dataSource;

    public PhotoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insertPetPhoto(
            int petId,
            int uploaderId,
            String url,
            String description) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int photoId = insertPhoto(
                        conn,
                        uploaderId,
                        url,
                        description);

                linkPhotoToPet(
                        conn,
                        petId,
                        photoId);

                conn.commit();

                return photoId;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while uploading pet photo.",
                    e);
        }
    }

    private int insertPhoto(
            Connection conn,
            int uploaderId,
            String url,
            String description) throws SQLException {

        String sql = """
                INSERT INTO photos (
                    url,
                    description,
                    uploader_uid
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, url);
            stmt.setString(2, description);
            stmt.setInt(3, uploaderId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating photo failed.");
            }

            try (var generatedKeys = stmt.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Creating photo failed, no ID obtained.");
                }

                return generatedKeys.getInt(1);
            }
        }
    }

    private void linkPhotoToPet(
            Connection conn,
            int petId,
            int photoId) throws SQLException {

        String sql = """
                INSERT INTO photos_of_pet (
                    pet_id,
                    photo_id
                )
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, petId);
            stmt.setInt(2, photoId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Linking photo to pet failed.");
            }
        }
    }

    public void insertUserPhoto(int userId, String url) {
        String sql = """
                UPDATE user_profiles
                SET profile_pic_url = ?
                WHERE user_id = ?
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, url);
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new RuntimeException(
                        "Failed to update profile photo. User profile not found for user ID: "
                                + userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error occurred while updating user profile photo.",
                    e);
        }
    }
}