package com.pet_connect.backend_service.repository;

import org.springframework.stereotype.Repository;

import com.pet_connect.backend_service.entity.Address;
import com.pet_connect.backend_service.entity.MedicalRecord;
import com.pet_connect.backend_service.entity.Pet;
import com.pet_connect.backend_service.entity.PetProfile;
import com.pet_connect.backend_service.entity.Photo;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

@Slf4j
@Repository
public class PetDAO {
    private final DataSource dataSource;

    public PetDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Pet addPet(Pet pet) {

        return null;
    }

    public Pet getPetByPetId(int petId) {
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

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(getPetSql)) {
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
