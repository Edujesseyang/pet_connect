package com.pet_connect.backend_service.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.pet_connect.backend_service.entity.Address;
import com.pet_connect.backend_service.entity.MedicalRecord;
import com.pet_connect.backend_service.entity.Pet;
import com.pet_connect.backend_service.entity.PetProfile;
import com.pet_connect.backend_service.entity.Photo;
import com.pet_connect.backend_service.entity.Post;
import com.pet_connect.backend_service.entity.User;
import com.pet_connect.backend_service.entity.UserProfile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class UserDAO {
    private final DataSource dataSource;

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isEmailExist(String email) {
        String sql = """
                SELECT user_id
                FROM users
                WHERE users.email = ?
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while looking for email.", e);
        }
        return false;
    }

    public boolean isUserExist(String username) {
        String sql = """
                SELECT user_id
                FROM users
                WHERE username = ?
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while looking for user.", e);
        }
        return false;
    }

    public String getUserPasswordHash(String username) {
        String sql = """
                SELECT password_hash
                FROM users
                WHERE username = ?
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while looking for user.", e);
        }
        return "";
    }

    public User insertUser(User user) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Address address = insertAddress(conn, user.getAddress());
                user.setAddress(address);
                user = insertUserRecord(conn, user);
                UserProfile profile = user.getUserProfile();
                profile.setUserId(user.getUserId());
                insertUserProfile(conn, profile);
                conn.commit();
                return user;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while inserting user.", e);
        }
    }

    private Address insertAddress(Connection conn, Address address) throws SQLException {
        String addressSql = """
                INSERT INTO addresses (
                    country,
                    state,
                    city,
                    street,
                    zipcode
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(addressSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, address.getCountry());
            stmt.setString(2, address.getState());
            stmt.setString(3, address.getCity());
            stmt.setString(4, address.getStreet());
            stmt.setString(5, address.getZipCode());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating address failed, affected rows: " + affectedRows);
            }

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Creating address failed, no ID obtained.");
                }

                address.setAddressId(generatedKeys.getInt(1));
            }
        }
        return address;
    }

    private User insertUserRecord(Connection conn, User user) throws SQLException {
        String userSql = """
                INSERT INTO users (
                    username,
                    fullname,
                    password_hash,
                    email,
                    role,
                    address_id
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullname());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole());
            stmt.setInt(6, user.getAddress().getAddressId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating user failed, affected rows: " + affectedRows);
            }

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Creating user failed, no ID obtained.");
                }

                user.setUserId(generatedKeys.getInt(1));
            }
        }
        return user;
    }

    private void insertUserProfile(Connection conn, UserProfile userProfile) throws SQLException {
        String userProfileSql = """
                INSERT INTO user_profiles (
                    user_id,
                    household_type,
                    adoption_exp,
                    gender,
                    date_of_birth,
                    phone_number,
                    bio,
                    profile_pic_url,
                    social_media_link
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(userProfileSql)) {
            stmt.setInt(1, userProfile.getUserId());
            stmt.setString(2, userProfile.getHouseholdType());
            stmt.setString(3, userProfile.getAdoptionExp());
            stmt.setString(4, userProfile.getGender());

            if (userProfile.getDateOfBirth() != null) {
                stmt.setDate(
                        5,
                        new java.sql.Date(userProfile.getDateOfBirth().getTime()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }

            stmt.setString(6, userProfile.getPhoneNumber());
            stmt.setString(7, userProfile.getBio());
            stmt.setString(8, userProfile.getProfilePhotoUrl());
            stmt.setString(9, userProfile.getSocialMediaLinks());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating user profile failed, affected rows: "
                                + affectedRows);
            }
        }
    }

    public void setPasswordByUsername(String username, String hashedPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while updating user password.", e);
        }
    }

    public User getUserByEmail(String email) {
        String sql = """
                SELECT username
                FROM users
                WHERE users.email = ?
                """;
        String username;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                username = rs.getString("username");
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while getting user by email.", e);
        }
        return getUserByUsername(username);
    }

    public User getUserByUsername(String username) {
        String userSql = """
                SELECT
                    u.user_id,
                    u.username,
                    u.fullname,
                    u.password_hash,
                    u.email,
                    u.role,
                    a.address_id,
                    a.street,
                    a.city,
                    a.state,
                    a.country,
                    a.zipcode,
                    up.gender,
                    up.date_of_birth,
                    up.phone_number,
                    up.household_type,
                    up.adoption_exp,
                    up.bio,
                    up.profile_pic_url,
                    up.social_media_link
                FROM users u
                JOIN addresses a
                    ON u.address_id = a.address_id
                LEFT JOIN user_profiles up
                    ON u.user_id = up.user_id
                WHERE u.username = ?
                """;

        String ownedPetsSql = """
                SELECT pet_id
                FROM user_owns_pet
                WHERE user_id = ?
                """;

        String appliedPostsSql = """
                SELECT post_id
                FROM user_applies_post
                WHERE user_id = ?
                """;

        String savedPostSql = """
                SELECT post_id
                FROM user_saves_post
                WHERE user_id = ?
                """;

        try (Connection conn = dataSource.getConnection()) {
            User user;
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                stmt.setString(1, username);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullname(rs.getString("fullname"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setRole(rs.getString("role"));
                    Address address = new Address();
                    address.setAddressId(rs.getInt("address_id"));
                    address.setStreet(rs.getString("street"));
                    address.setCity(rs.getString("city"));
                    address.setState(rs.getString("state"));
                    address.setCountry(rs.getString("country"));
                    address.setZipCode(rs.getString("zipcode"));
                    user.setAddress(address);
                    UserProfile userProfile = new UserProfile();
                    userProfile.setGender(rs.getString("gender"));
                    userProfile.setDateOfBirth(rs.getDate("date_of_birth"));
                    userProfile.setPhoneNumber(rs.getString("phone_number"));
                    userProfile.setHouseholdType(rs.getString("household_type"));
                    userProfile.setAdoptionExp(rs.getString("adoption_exp"));
                    userProfile.setBio(rs.getString("bio"));
                    userProfile.setProfilePhotoUrl(rs.getString("profile_pic_url"));
                    userProfile.setSocialMediaLinks(rs.getString("social_media_link"));
                    user.setUserProfile(userProfile);

                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(ownedPetsSql)) {
                stmt.setInt(1, user.getUserId());
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        user.addOwnedPet(getPetByPetId(conn, rs.getInt("pet_id")));
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(appliedPostsSql)) {
                stmt.setInt(1, user.getUserId());
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        user.addAppliedPost(getPostByPostId(conn, rs.getInt("post_id")));
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(savedPostSql)) {
                stmt.setInt(1, user.getUserId());

                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        user.addSavedPost(getPostByPostId(conn, rs.getInt("post_id")));
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while fetching user by username.", e);
        }
    }

    public Post getPostByPostId(Connection conn, int postId) {
        String sql = """
                SELECT
                    p.user_id,
                    p.pet_id,
                    p.created_at,
                    p.title,
                    p.content,
                    p.type,
                    p.adoption_fee,
                    a.address_id,
                    a.street,
                    a.city,
                    a.state,
                    a.country,
                    a.zipcode
                FROM posts p
                JOIN addresses a
                    ON a.address_id = p.address_id
                WHERE p.post_id = ?
                ORDER BY p.created_at DESC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Post post = new Post();
                post.setPostId(postId);
                post.setUserId(rs.getInt("user_id"));
                post.setPetId(rs.getInt("pet_id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setType(rs.getString("type"));
                post.setAdoptionFee(rs.getBigDecimal("adoption_fee"));
                var createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    post.setCreatedAt(createdAt.toLocalDateTime());
                }
                Address address = new Address();
                address.setAddressId(rs.getInt("address_id"));
                address.setStreet(rs.getString("street"));
                address.setCity(rs.getString("city"));
                address.setState(rs.getString("state"));
                address.setCountry(rs.getString("country"));
                address.setZipCode(rs.getString("zipcode"));
                post.setPickupLocation(address);
                return post;
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while fetching posts by user ID: ", e);
        }
    }

    public Pet getPetByPetId(Connection conn, int petId) {
        String getPetSql = """
                SELECT
                    p.pet_id,
                    p.pet_name,
                    a.address_id,
                    a.street,
                    a.city,
                    a.state,
                    a.country,
                    a.zipcode,
                    bd.breed_id,
                    bd.breed_name,
                    sp.species_id,
                    sp.species_name,
                    pp.date_of_birth,
                    pp.sex,
                    pp.color,
                    pp.weight,
                    pp.friendly_level,
                    pp.size,
                    pp.is_trained,
                    pp.description,
                    pp.microchip_number,
                    mr.vaccination,
                    mr.allergies,
                    mr.medications,
                    mr.special_care,
                    mr.surgeries,
                    mr.lab_results,
                    mr.imaging_results,
                    mr.spayed_neutered,
                    mr.note
                FROM pets p
                JOIN addresses a
                    ON a.address_id = p.address_id
                JOIN breeds bd
                    ON p.breed_id = bd.breed_id
                JOIN species sp
                    ON bd.species_id = sp.species_id
                LEFT JOIN pet_profiles pp
                    ON pp.pet_id = p.pet_id
                LEFT JOIN medical_records mr
                    ON mr.pet_id = p.pet_id
                WHERE p.pet_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(getPetSql)) {
            stmt.setInt(1, petId);
            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Pet pet = new Pet();
                pet.setPetId(rs.getInt("pet_id"));
                pet.setName(rs.getString("pet_name"));
                // Address
                Address address = new Address();
                address.setAddressId(rs.getInt("address_id"));
                address.setStreet(rs.getString("street"));
                address.setCity(rs.getString("city"));
                address.setState(rs.getString("state"));
                address.setCountry(rs.getString("country"));
                address.setZipCode(rs.getString("zipcode"));
                pet.setAddress(address);

                // Breed and species
                pet.setBreed(rs.getString("breed_name"));
                pet.setSpecies(rs.getString("species_name"));

                // Pet profile
                PetProfile petProfile = new PetProfile();
                petProfile.setPetId(petId);
                petProfile.setDateOfBirth(rs.getDate("date_of_birth"));
                petProfile.setSex(rs.getString("sex"));
                petProfile.setColor(rs.getString("color"));
                petProfile.setWeight(rs.getBigDecimal("weight"));
                petProfile.setFriendlyLevel(rs.getInt("friendly_level"));
                petProfile.setSize(rs.getString("size"));
                petProfile.setIsTrained(rs.getBoolean("is_trained"));
                petProfile.setDescription(rs.getString("description"));
                petProfile.setMicrochipNumber(rs.getString("microchip_number"));
                pet.setPetProfile(petProfile);

                // medical record
                MedicalRecord medicalRecord = new MedicalRecord();
                medicalRecord.setPetId(petId);
                medicalRecord.setVaccination(rs.getString("vaccination"));
                medicalRecord.setAllergies(rs.getString("allergies"));
                medicalRecord.setMedications(rs.getString("medications"));
                medicalRecord.setSpecialCare(rs.getString("special_care"));
                medicalRecord.setSurgeries(rs.getString("surgeries"));
                medicalRecord.setLabResults(rs.getString("lab_results"));
                medicalRecord.setImagingResults(rs.getString("imaging_results"));
                medicalRecord.setSpayedNeutered(rs.getBoolean("spayed_neutered"));
                medicalRecord.setNote(rs.getString("note"));
                pet.setMedicalRecord(medicalRecord);

                pet.setPhotos(getPhotosByPetId(conn, petId));
                return pet;
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Database error occurred while fetching pet by ID: ", e);
        }
    }

    public List<Photo> getPhotosByPetId(Connection conn, int petId) {
        String sql = """
                SELECT photo_id
                FROM photos_of_pet
                WHERE pet_id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            List<Photo> photoList = new ArrayList<>();
            stmt.setInt(1, petId);
            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                while (rs.next()) {
                    Photo photo = getPhotoByPhotoId(conn, rs.getInt("photo_id"));
                    photoList.add(photo);
                }
            }
            return photoList;
        } catch (SQLException e) {
            log.error("Batabase connect error", e.getMessage());
            throw new RuntimeException("Database error occurred while fetching:", e);
        }
    }

    public Photo getPhotoByPhotoId(Connection conn, int photoId) {
        String sql = """
                SELECT
                    photo_id,
                    url,
                    description,
                    uploader_uid
                FROM photos
                WHERE photo_id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, photoId);
            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Photo photo = new Photo();
                photo.setPhototId(photoId);
                photo.setDescription(rs.getString("description"));
                photo.setUrl(rs.getString("url"));
                photo.setUploaderId(rs.getInt("uploader_uid"));
                return photo;
            }

        } catch (SQLException e) {
            log.error("Batabase connect error", e.getMessage());
            throw new RuntimeException("Database error occurred while fetching:", e);
        }
    }
}