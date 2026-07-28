package com.pet_connect.backend_service.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.pet_connect.backend_service.entity.Address;
import com.pet_connect.backend_service.entity.MedicalRecord;
import com.pet_connect.backend_service.entity.Pet;
import com.pet_connect.backend_service.entity.PetProfile;
import com.pet_connect.backend_service.entity.Photo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PetDAO {

    private final DataSource dataSource;

    public PetDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void linkPetToOwner(int petId, int userId) {
        String sql = """
                INSERT INTO user_owns_pet (
                    user_id,
                    pet_id
                )
                VALUES (?, ?)
                """;

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, petId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new SQLException("Creating user ownership to pet failed, affected rows: " + affectedRows);
            }
        } catch (SQLException e) {
            log.error("Database error while linking pet to owner.", e);
            throw new RuntimeException("Database error occurred while linking pet to owner.", e);
        }
    };

    public Pet addPet(Pet pet) {
        validatePetForInsert(pet);

        try (Connection conn = dataSource.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                int petId = addPetRecord(conn, pet);
                pet.setPetId(petId);

                if (pet.getMedicalRecord() != null) {
                    pet.getMedicalRecord().setPetId(petId);
                    addMedicalRecord(conn, pet.getMedicalRecord());
                }

                if (pet.getPetProfile() != null) {
                    pet.getPetProfile().setPetId(petId);
                    addPetProfile(conn, pet.getPetProfile());
                }

                conn.commit();
                return pet;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        } catch (SQLException e) {
            log.error("Database error while inserting pet.", e);
            throw new RuntimeException(
                    "Database error occurred while inserting pet.",
                    e);
        }
    }

    public void addMedicalRecord(
            Connection conn,
            MedicalRecord medicalRecord) throws SQLException {

        String sql = """
                INSERT INTO medical_records (
                    pet_id,
                    vaccination,
                    allergies,
                    medications,
                    special_care,
                    surgeries,
                    lab_results,
                    imaging_results,
                    spayed_neutered,
                    note
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medicalRecord.getPetId());
            stmt.setString(2, medicalRecord.getVaccination());
            stmt.setString(3, medicalRecord.getAllergies());
            stmt.setString(4, medicalRecord.getMedications());
            stmt.setString(5, medicalRecord.getSpecialCare());
            stmt.setString(6, medicalRecord.getSurgeries());
            stmt.setString(7, medicalRecord.getLabResults());
            stmt.setString(8, medicalRecord.getImagingResults());

            if (medicalRecord.getSpayedNeutered() == null) {
                stmt.setNull(9, Types.BOOLEAN);
            } else {
                stmt.setBoolean(9, medicalRecord.getSpayedNeutered());
            }

            stmt.setString(10, medicalRecord.getNote());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating medical record failed, affected rows: "
                                + affectedRows);
            }
        }
    }

    public void addPetProfile(
            Connection conn,
            PetProfile petProfile) throws SQLException {

        String sql = """
                INSERT INTO pet_profiles (
                    pet_id,
                    date_of_birth,
                    sex,
                    color,
                    weight,
                    friendly_level,
                    size,
                    is_trained,
                    description,
                    microchip_number
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petProfile.getPetId());

            if (petProfile.getDateOfBirth() == null) {
                stmt.setNull(2, Types.DATE);
            } else {
                stmt.setDate(2, petProfile.getDateOfBirth());
            }

            stmt.setString(3, petProfile.getSex());
            stmt.setString(4, petProfile.getColor());
            stmt.setBigDecimal(5, petProfile.getWeight());

            if (petProfile.getFriendlyLevel() == null) {
                stmt.setNull(6, Types.INTEGER);
            } else {
                stmt.setInt(6, petProfile.getFriendlyLevel());
            }

            stmt.setString(7, petProfile.getSize());

            if (petProfile.getIsTrained() == null) {
                stmt.setNull(8, Types.BOOLEAN);
            } else {
                stmt.setBoolean(8, petProfile.getIsTrained());
            }

            stmt.setString(9, petProfile.getDescription());
            stmt.setString(10, petProfile.getMicrochipNumber());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating pet profile failed, affected rows: "
                                + affectedRows);
            }
        }
    }

    public int addPetRecord(
            Connection conn,
            Pet pet) throws SQLException {

        String sql = """
                INSERT INTO pets (
                    pet_name,
                    breed_id,
                    address_id
                )
                VALUES (?, ?, ?)
                """;

        int breedId = getBreedId(
                conn,
                pet.getBreed(),
                pet.getSpecies());

        int addressId = insertAddress(
                conn,
                pet.getAddress());

        try (PreparedStatement stmt = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pet.getName());
            stmt.setInt(2, breedId);
            stmt.setInt(3, addressId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating pet failed, affected rows: "
                                + affectedRows);
            }

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Creating pet failed, no ID obtained.");
                }

                return generatedKeys.getInt(1);
            }
        }
    }

    public int getBreedId(
            Connection conn,
            String breed,
            String species) throws SQLException {

        String sql = """
                SELECT bd.breed_id
                FROM breeds bd
                JOIN species sp
                    ON sp.species_id = bd.species_id
                WHERE bd.breed_name = ?
                  AND sp.species_name = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, breed);
            stmt.setString(2, species);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("breed_id");
                }
            }
        }

        int speciesId = getSpeciesId(conn, species);
        return insertBreed(conn, breed, speciesId);
    }

    public int insertBreed(
            Connection conn,
            String breed,
            int speciesId) throws SQLException {

        String sql = """
                INSERT INTO breeds (
                    breed_name,
                    species_id
                )
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, breed);
            stmt.setInt(2, speciesId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException("Creating breed failed, affected rows: " + affectedRows);
            }

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Creating breed failed, no ID obtained.");
                }

                return generatedKeys.getInt(1);
            }
        }
    }

    public int getSpeciesId(
            Connection conn,
            String species) throws SQLException {

        String selectSql = """
                SELECT species_id
                FROM species
                WHERE species_name = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, species);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("species_id");
                }
            }
        }

        String insertSql = """
                INSERT INTO species (
                    species_name
                )
                VALUES (?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(
                insertSql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, species);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating species failed, affected rows: "
                                + affectedRows);
            }

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Creating species failed, no ID obtained.");
                }

                return generatedKeys.getInt(1);
            }
        }
    }

    public int insertAddress(
            Connection conn,
            Address address) throws SQLException {

        String sql = """
                INSERT INTO addresses (
                    country,
                    state,
                    city,
                    street,
                    zipcode
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, address.getCountry());
            stmt.setString(2, address.getState());
            stmt.setString(3, address.getCity());
            stmt.setString(4, address.getStreet());
            stmt.setString(5, address.getZipCode());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Creating address failed, affected rows: "
                                + affectedRows);
            }

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Creating address failed, no ID obtained.");
                }

                return generatedKeys.getInt(1);
            }
        }
    }

    public Pet getPetByPetId(int petId) {
        String sql = """
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
                    ON bd.breed_id = p.breed_id
                JOIN species sp
                    ON sp.species_id = bd.species_id
                LEFT JOIN pet_profiles pp
                    ON pp.pet_id = p.pet_id
                LEFT JOIN medical_records mr
                    ON mr.pet_id = p.pet_id
                WHERE p.pet_id = ?
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petId);

            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Pet pet = new Pet();
                pet.setPetId(rs.getInt("pet_id"));
                pet.setName(rs.getString("pet_name"));
                pet.setBreed(rs.getString("breed_name"));
                pet.setSpecies(rs.getString("species_name"));

                Address address = new Address();
                address.setAddressId(rs.getInt("address_id"));
                address.setStreet(rs.getString("street"));
                address.setCity(rs.getString("city"));
                address.setState(rs.getString("state"));
                address.setCountry(rs.getString("country"));
                address.setZipCode(rs.getString("zipcode"));
                pet.setAddress(address);

                PetProfile petProfile = buildPetProfile(rs, petId);

                if (petProfile != null) {
                    pet.setPetProfile(petProfile);
                }

                MedicalRecord medicalRecord = buildMedicalRecord(
                        rs,
                        petId);

                if (medicalRecord != null) {
                    pet.setMedicalRecord(medicalRecord);
                }

                pet.setPhotos(getPhotosByPetId(conn, petId));

                return pet;
            }
        } catch (SQLException e) {
            log.error(
                    "Database error while fetching pet with ID {}.",
                    petId,
                    e);

            throw new RuntimeException(
                    "Database error occurred while fetching pet by ID.",
                    e);
        }
    }

    private PetProfile buildPetProfile(
            java.sql.ResultSet rs,
            int petId) throws SQLException {

        Object profilePetId = rs.getObject(
                "date_of_birth");

        boolean profileExists = profilePetId != null
                || rs.getString("sex") != null
                || rs.getString("color") != null
                || rs.getBigDecimal("weight") != null
                || rs.getObject("friendly_level") != null
                || rs.getString("size") != null
                || rs.getObject("is_trained") != null
                || rs.getString("description") != null
                || rs.getString("microchip_number") != null;

        if (!profileExists) {
            return null;
        }

        PetProfile petProfile = new PetProfile();
        petProfile.setPetId(petId);
        petProfile.setDateOfBirth(rs.getDate("date_of_birth"));
        petProfile.setSex(rs.getString("sex"));
        petProfile.setColor(rs.getString("color"));
        petProfile.setWeight(rs.getBigDecimal("weight"));

        Integer friendlyLevel = (Integer) rs.getObject(
                "friendly_level");

        petProfile.setFriendlyLevel(friendlyLevel);
        petProfile.setSize(rs.getString("size"));

        Boolean isTrained = (Boolean) rs.getObject(
                "is_trained");

        petProfile.setIsTrained(isTrained);
        petProfile.setDescription(rs.getString("description"));
        petProfile.setMicrochipNumber(
                rs.getString("microchip_number"));

        return petProfile;
    }

    private MedicalRecord buildMedicalRecord(
            java.sql.ResultSet rs,
            int petId) throws SQLException {

        boolean medicalRecordExists = rs.getString("vaccination") != null
                || rs.getString("allergies") != null
                || rs.getString("medications") != null
                || rs.getString("special_care") != null
                || rs.getString("surgeries") != null
                || rs.getString("lab_results") != null
                || rs.getString("imaging_results") != null
                || rs.getObject("spayed_neutered") != null
                || rs.getString("note") != null;

        if (!medicalRecordExists) {
            return null;
        }

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPetId(petId);
        medicalRecord.setVaccination(
                rs.getString("vaccination"));
        medicalRecord.setAllergies(
                rs.getString("allergies"));
        medicalRecord.setMedications(
                rs.getString("medications"));
        medicalRecord.setSpecialCare(
                rs.getString("special_care"));
        medicalRecord.setSurgeries(
                rs.getString("surgeries"));
        medicalRecord.setLabResults(
                rs.getString("lab_results"));
        medicalRecord.setImagingResults(
                rs.getString("imaging_results"));

        Boolean spayedNeutered = (Boolean) rs.getObject(
                "spayed_neutered");

        medicalRecord.setSpayedNeutered(spayedNeutered);
        medicalRecord.setNote(rs.getString("note"));

        return medicalRecord;
    }

    public List<Photo> getPhotosByPetId(
            Connection conn,
            int petId) throws SQLException {

        String sql = """
                SELECT photo_id
                FROM photos_of_pet
                WHERE pet_id = ?
                """;

        List<Photo> photos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petId);

            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Photo photo = getPhotoByPhotoId(
                            conn,
                            rs.getInt("photo_id"));

                    if (photo != null) {
                        photos.add(photo);
                    }
                }
            }
        }

        return photos;
    }

    public Photo getPhotoByPhotoId(
            Connection conn,
            int photoId) throws SQLException {

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
                photo.setPhototId(rs.getInt("photo_id"));
                photo.setDescription(rs.getString("description"));
                photo.setUrl(rs.getString("url"));
                photo.setUploaderId(rs.getInt("uploader_uid"));

                return photo;
            }
        }
    }

    private void validatePetForInsert(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException(
                    "Pet cannot be null.");
        }

        if (pet.getName() == null || pet.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Pet name cannot be empty.");
        }

        if (pet.getBreed() == null || pet.getBreed().isBlank()) {
            throw new IllegalArgumentException(
                    "Pet breed cannot be empty.");
        }

        if (pet.getSpecies() == null || pet.getSpecies().isBlank()) {
            throw new IllegalArgumentException(
                    "Pet species cannot be empty.");
        }

        if (pet.getAddress() == null) {
            throw new IllegalArgumentException(
                    "Pet address cannot be null.");
        }
    }

    private void rollback(
            Connection conn,
            Exception originalException) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(
                    rollbackException);

            log.error(
                    "Database transaction rollback failed.",
                    rollbackException);
        }
    }

    private void restoreAutoCommit(
            Connection conn,
            boolean originalAutoCommit) {
        try {
            conn.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            log.warn(
                    "Failed to restore database auto-commit state.",
                    e);
        }
    }

    public boolean deletePetById(int petId) {
        String sql = """
                DELETE FROM pets
                WHERE pet_id = ?
                """;
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            log.error("Database error while deleting pet with ID {}.", petId, e);
            throw new RuntimeException("Database error occurred while deleting pet.", e);
        }
    }

    public boolean isPetExist(int petId) {
        String sql = """
                SELECT *
                FROM pets
                WHERE pet_id = ?
                """;
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, petId);
            try (var rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Database error while deleting pet with ID {}.", petId, e);

            throw new RuntimeException("Database error occurred while deleting pet.", e);
        }
    }

}