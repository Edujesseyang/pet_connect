DROP DATABASE IF EXISTS pet_connect;
CREATE DATABASE pet_connect;
USE pet_connect;

CREATE TABLE addresses (
	address_id INT PRIMARY KEY AUTO_INCREMENT,
	street VARCHAR(100),
	city VARCHAR(100),
	state VARCHAR(30),
	country VARCHAR(30),
	zipcode VARCHAR(30)
);

CREATE TABLE users (
	user_id INT PRIMARY KEY AUTO_INCREMENT,
	username VARCHAR(100) NOT NULL UNIQUE,
	fullname VARCHAR(100) NOT NULL,
	email VARCHAR(100) NOT NULL UNIQUE,
	password_hash VARCHAR(300) NOT NULL,
	role VARCHAR(30) NOT NULL,
	address_id INT NOT NULL,
	CONSTRAINT fk_users_addresses
		FOREIGN KEY (address_id)
		REFERENCES addresses(address_id)
		ON UPDATE CASCADE 
		ON DELETE RESTRICT,
	CONSTRAINT check_role
		CHECK (
			role IN (
				'admin',
				'regular'
			)
		)
);

CREATE TABLE user_profiles (
	user_id INT  NOT NULL UNIQUE,
	gender VARCHAR(30),
	date_of_birth DATE,
	phone_number VARCHAR(30),
	household_type VARCHAR(30),
	adoption_exp VARCHAR(30),
	bio VARCHAR(500),
	profile_pic_url VARCHAR(500),
	social_media_link VARCHAR(500),
	CONSTRAINT pk_user_profiles
		PRIMARY KEY (user_id),
	CONSTRAINT fk_user_profiles_users
		FOREIGN KEY (user_id)
		REFERENCES users(user_id)
		ON DELETE CASCADE 
		ON UPDATE CASCADE 
);

CREATE TABLE photos (
	photo_id INT PRIMARY KEY AUTO_INCREMENT,
	url VARCHAR(500) NOT NULL,
	description VARCHAR(500),
	uploader_uid INT NOT NULL,
	CONSTRAINT fk_photos_users
		FOREIGN KEY (uploader_uid)
		REFERENCES users(user_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);

CREATE TABLE species (
	species_id INT PRIMARY KEY AUTO_INCREMENT,
	species_name VARCHAR(300) NOT NULL 
);

CREATE TABLE breeds (
	breed_id INT PRIMARY KEY AUTO_INCREMENT,
	breed_name VARCHAR(300) NOT NULL,
	species_id INT NOT NULL,
	CONSTRAINT uq_breeds_name_species
    	UNIQUE (breed_name, species_id),
	CONSTRAINT fk_breeds_species
		FOREIGN KEY (species_id)
		REFERENCES species(species_id)
		ON UPDATE CASCADE 
		ON DELETE NO ACTION 
);

CREATE TABLE pets (
	pet_id INT PRIMARY KEY AUTO_INCREMENT,
	pet_name VARCHAR(100) NOT NULL,
	breed_id INT NOT NULL,
	address_id INT NOT NULL,
	CONSTRAINT fk_pets_breeds
		FOREIGN KEY (breed_id)
		REFERENCES breeds(breed_id)
		ON UPDATE CASCADE 
		ON DELETE NO ACTION,
	CONSTRAINT fk_pets_addresses
		FOREIGN KEY (address_id)
		REFERENCES addresses(address_id)
		ON UPDATE CASCADE 
		ON DELETE RESTRICT
);

CREATE TABLE photos_of_pet (
	pet_id INT NOT NULL,
	photo_id INT NOT NULL,
	CONSTRAINT fk_photos_of_pet_pets
		FOREIGN KEY (pet_id)
		REFERENCES pets(pet_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_photos_of_pet_photots
		FOREIGN KEY (photo_id)
		REFERENCES photos(photo_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);

CREATE TABLE pet_profiles (
	pet_id INT NOT NULL UNIQUE,
	date_of_birth DATE,
	sex VARCHAR(30),
	color VARCHAR(30),
	weight DECIMAL(6,2),
	friendly_level TINYINT,
	size VARCHAR(30),
	is_trained BOOLEAN,
	description VARCHAR(500),
	microchip_number VARCHAR(100),
	CONSTRAINT pk_pet_profiles
		PRIMARY KEY (pet_id),
	CONSTRAINT chk_pet_profiles_friendly_level
    CHECK (
        friendly_level IS NULL
        OR friendly_level BETWEEN 1 AND 5
    ),
	CONSTRAINT fk_pet_profiles_pets
		FOREIGN KEY (pet_id)
		REFERENCES pets(pet_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);

CREATE TABLE medical_records (
	pet_id INT NOT NULL UNIQUE,
	allergies VARCHAR(1000),
	vaccination VARCHAR(1000),
	medications VARCHAR(1000),
	special_care VARCHAR(1000),
	surgeries VARCHAR(1000),
	lab_results VARCHAR(1000),
	imaging_results VARCHAR(1000),
	spayed_neutered BOOLEAN,
	note VARCHAR(5000),
	CONSTRAINT pk_medical_records
		PRIMARY KEY (pet_id),
	CONSTRAINT fk_medical_records_pets
		FOREIGN KEY (pet_id)
		REFERENCES pets(pet_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);

CREATE TABLE posts (
	post_id INT PRIMARY KEY AUTO_INCREMENT,
	user_id INT,
	pet_id INT,
	created_at TIMESTAMP,
	title VARCHAR(500),
	content VARCHAR(5000),
	type VARCHAR(100),
	CONSTRAINT check_type 
		CHECK (type IN (
			'adoption',
			'rehoming',
			'breeder'
		)
	),
	adoption_fee DECIMAL(9,2),
	address_id INT NOT NULL,
	CONSTRAINT fk_posts_pets
		FOREIGN KEY (pet_id)
		REFERENCES pets(pet_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_posts_users
		FOREIGN KEY (user_id)
		REFERENCES users(user_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_posts_addresses
		FOREIGN KEY (address_id)
		REFERENCES addresses(address_id)
		ON UPDATE CASCADE 
		ON DELETE RESTRICT
);

CREATE TABLE user_applies_post (
	user_id INT,
	post_id INT,
	CONSTRAINT pk_user_applies_post
		PRIMARY KEY (user_id, post_id),
	CONSTRAINT fk_user_applies_post_posts
		FOREIGN KEY (post_id)
		REFERENCES posts(post_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_user_applies_post_users
		FOREIGN KEY (user_id)
		REFERENCES users(user_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);

CREATE TABLE user_owns_pet (
	user_id INT,
	pet_id INT,
	CONSTRAINT pk_user_owns_pet
		PRIMARY KEY (user_id, pet_id),
	CONSTRAINT fk_user_owns_pet_pets
		FOREIGN KEY (pet_id)
		REFERENCES pets(pet_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_user_owns_pet_users
		FOREIGN KEY (user_id)
		REFERENCES users(user_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);

CREATE TABLE user_saves_post (
	user_id INT,
	post_id INT,
	CONSTRAINT pk_user_saves_post
		PRIMARY KEY (user_id, post_id),
	CONSTRAINT fk_user_saves_post_posts
		FOREIGN KEY (post_id)
		REFERENCES posts(post_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_user_saves_post_users
		FOREIGN KEY (user_id)
		REFERENCES users(user_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);

CREATE TABLE conversations (
	conversation_id INT PRIMARY KEY AUTO_INCREMENT,
	from_uid INT NOT NULL,
	to_uid INT NOT NULL, 
	pet_id INT,
	started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT fk_conversation_from_uid
		FOREIGN KEY (from_uid)
		REFERENCES users(user_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_conversation_to_uid
		FOREIGN KEY (to_uid)
		REFERENCES users(user_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_conversation_pet
		FOREIGN KEY (pet_id)
		REFERENCES pets(pet_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);

CREATE TABLE messages (
	message_id INT PRIMARY KEY AUTO_INCREMENT,
	content VARCHAR(1000),
	sender_id INT NOT NULL,
	sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	conversation_id INT,
	CONSTRAINT fk_messages_conversations
		FOREIGN KEY (conversation_id)
		REFERENCES conversations(conversation_id)
		ON UPDATE CASCADE 
		ON DELETE CASCADE,
	CONSTRAINT fk_messages_sender
	    FOREIGN KEY (sender_id)
	    REFERENCES users(user_id)
	    ON DELETE CASCADE
	    ON UPDATE CASCADE
);

























