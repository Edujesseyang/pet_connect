# PetConnect: A Pet Adoption and Rehoming Web App

## Detailed Final Project Outline

**Team Members:** Jesse, Sean, and Ayesha  
**Course:** CS157A

This outline is based on the project proposal, but it adds more detail for the database design, application screens, SQL work, implementation plan, report, and demo.

## Project Overview

PetConnect is a web application for pet adoption and rehoming. The application is meant to give pet owners and adopters one organized place to post pets, browse available pets, save favorites, and contact each other.

The main idea is to make the adoption and rehoming process easier to follow than using scattered social media posts or classified ads. The project will also show database design, SQL queries, backend APIs, authentication, and frontend integration.

## Problem Being Addressed

Pet adoption information is often spread across different websites, posts, and informal messages. This makes it harder for adopters to find current information and harder for pet owners to reach people who are actually interested.

PetConnect will organize pet listings in a searchable database and give users a simple way to communicate about pets through the application.

## Main Users

- **Pet Owner:** creates and manages pet listings, uploads photos, updates pet status, and responds to messages.
- **Adopter:** browses pets, searches by filters, views details, saves favorite pets, and contacts owners.
- **Registered User:** has an account, profile, saved favorites, and message history. A user can act as both an owner and an adopter.
- **Visitor:** can browse available pets before creating an account, but cannot create listings, save favorites, or message owners.

## Project Objectives

- Create a centralized pet adoption and rehoming platform.
- Allow users to create accounts and manage basic profile information.
- Allow pet owners to create, edit, and delete pet listings.
- Allow users to upload and view pet images.
- Support search and filtering by species, breed, age, location, and status.
- Allow users to save favorite pets and view them later.
- Allow registered users to contact pet owners through the application.
- Build a responsive interface that works on desktop and mobile screens.
- Design a normalized MySQL database with meaningful relationships and constraints.

## Core Functional Requirements

### Account and Profile Features

- Users can register with their name, email, and password.
- Users can log in and log out securely.
- Users can update basic profile details such as phone number, location, bio, and adoption experience.
- Only logged-in users can create listings, save favorites, or send messages.

### Pet Listing Features

- Pet owners can add a new pet listing with name, species, breed, age, gender, location, description, and adoption status.
- Pet owners can edit or remove their own pet listings.
- Each pet listing can have one or more uploaded photos.
- Pet listings can include optional medical notes such as vaccination status, spayed or neutered status, and special care needs.

### Search and Browse Features

- Visitors and users can browse available pets.
- Users can filter pets by species, breed, age range, location, and adoption status.
- Users can open a pet details page to see photos, owner information, description, and medical notes.
- Search results should be clear and easy to scan.

### Favorites and Messaging

- Registered users can save pets to a favorites list.
- Users can remove pets from their favorites list.
- Registered users can send messages to pet owners about a specific listing.
- Users can view their message history for pet-related conversations.

## Non-Functional Requirements

- Passwords should be stored securely and not as plain text.
- The backend should validate user input before inserting or updating database records.
- Only the owner of a pet listing should be able to edit or delete that listing.
- Frequently searched fields should be indexed where it makes sense.
- Foreign key constraints should be used to keep related data valid.
- The application should handle missing records, invalid searches, and failed requests without crashing.
- The UI should be simple and responsive rather than overdesigned.


## Database Outline

The database will store users, profiles, pet listings, photos, favorites, messages, and lookup data for species, breeds, and listing status. This gives the project enough database complexity while still matching the main proposal.

### Core Tables

- **User:** stores login and account information such as user ID, name, email, password hash, phone, role, and created date.
- **UserProfile:** stores extra profile details such as bio, household type, adoption experience, and address reference.
- **Address:** stores city, state, ZIP code, and optional street information so location data is not repeated everywhere.
- **Pet:** stores the main pet listing information, including owner ID, name, species, breed, age, gender, description, status, and created date.
- **Species:** stores pet species such as dog, cat, rabbit, or bird.
- **Breed:** stores breed names and connects each breed to a species.
- **ListingStatus:** stores status values such as available, pending, adopted, or removed.
- **PetPhoto:** stores image URLs or file paths for photos connected to each pet.
- **MedicalRecord:** stores optional health information such as vaccination status, spayed or neutered status, and medical notes.
- **Favorite:** connects users to the pets they saved.
- **Conversation:** groups messages between two users about one pet listing.
- **Message:** stores the actual message text, sender, conversation, and sent date.

### Possible Future Tables

- **AdoptionApplication:** could be added later if the project includes formal adoption forms.
- **ShelterOrganization:** could be added later if shelters need organization accounts.
- **Notification:** could be added later for email or in-app notifications.
- **Review:** could be added later if completed adoptions include feedback.

## Important Relationships

- One user can have one user profile.
- One address can be connected to many user profiles or pet listings if needed.
- One user can create many pet listings.
- One pet listing belongs to one owner.
- One species can have many breeds.
- One breed can be used by many pet listings.
- One listing status can be assigned to many pets.
- One pet can have many photos.
- One pet can have zero or more medical records.
- One user can save many pets as favorites.
- One pet can be saved by many users through the Favorite table.
- One conversation is connected to one pet listing and two users.
- One conversation can contain many messages.

## ER Diagram Plan

The ER diagram will show the entities, attributes, primary keys, foreign keys, and cardinalities. The diagram should make it clear how users, pets, photos, favorites, and messages connect to each other.

### Main Items to Show

- Primary keys for every table, such as `user_id`, `pet_id`, `species_id`, and `message_id`.
- Foreign keys such as `Pet.owner_id`, `Pet.species_id`, `Pet.breed_id`, `Favorite.user_id`, `Favorite.pet_id`, and `Message.conversation_id`.
- Cardinality labels such as `1:N` and `M:N` through bridge tables.
- `Favorite` as the bridge table between `User` and `Pet`.
- `Conversation` and `Message` as the communication structure between users.
- `Species` and `Breed` as lookup tables to avoid repeated text values.

## Normalization and Constraints

The schema will be designed to avoid duplicate and inconsistent data. The goal is to keep related information in separate tables and connect them using keys.

- User account details are separate from profile details.
- Address data is kept separate from user and pet records.
- Species and Breed are separate tables so names are not repeated in every pet row.
- PetPhoto is separate because one pet can have multiple photos.
- Favorite is a bridge table for the many-to-many relationship between users and pets.
- Conversation and Message are separate so message history can be stored clearly.
- Foreign keys will help prevent orphaned records.
- Unique constraints can be used for user email and favorite pairs so the same user does not save the same pet twice.

## SQL Script Plan

### Schema Script

- Create all database tables.
- Define primary keys and foreign keys.
- Add `NOT NULL` constraints for required fields.
- Add `UNIQUE` constraints where needed, such as email addresses.
- Add `CHECK` constraints for fields like age or status if supported.
- Create indexes for commonly searched fields.

### Seed Script

- Add sample users and profiles.
- Add sample addresses and locations.
- Add sample species and breeds.
- Add sample pet listings with different statuses.
- Add sample pet photos and medical records.
- Add sample favorites, conversations, and messages.

### Query Script

- Search pets by species and city.
- Search pets by breed and adoption status.
- View pet details with owner, breed, species, and photo information.
- List all pets created by one owner.
- Show all favorite pets for one user.
- Show conversation messages for a pet listing.
- Count how many pets each owner has listed.
- Find the most commonly favorited pets.
- Show available pets grouped by species.
- Use at least one join query, one aggregate query, one filtering query, and one subquery.

## Indexing Plan

- `Pet(species_id)` for filtering by species.
- `Pet(breed_id)` for filtering by breed.
- `Pet(status_id)` for filtering available or adopted pets.
- `Pet(owner_id)` for listing pets created by one user.
- `Favorite(user_id, pet_id)` for saved pet lookups and duplicate prevention.
- `Message(conversation_id)` for loading message history.
- `Address(city, state)` for location-based search.

## Web Pages and Screens

- Home page: short app description, browse pets button, and login/register links.
- Register page: account creation form.
- Login page: user authentication form.
- Browse pets page: searchable pet cards with filters.
- Pet details page: photos, pet information, owner details, and contact option.
- Create listing page: form for adding a pet listing and uploading photos.
- My listings page: owner view for editing, deleting, or updating status.
- Favorites page: saved pets for the logged-in user.
- Messages page: conversations between users about pet listings.
- Profile page: user profile and account information.

## CRUD Coverage

- **Create:** register a user, add a profile, create a pet listing, upload a photo, save a favorite, and send a message.
- **Read:** browse pets, view pet details, search and filter listings, view favorites, view messages, and view profiles.
- **Update:** edit profile information, update a pet listing, update pet status, and edit optional pet details.
- **Delete:** remove a pet listing, delete a photo, remove a favorite, or delete a message if needed.

## Backend API Plan

The backend will expose REST endpoints that the Vue frontend can call. The exact endpoint names may change during development, but the main groups will be organized around users, pets, favorites, and messages.

### Possible API Groups

- Auth endpoints for register, login, and current user information.
- User/profile endpoints for viewing and updating profile data.
- Pet endpoints for creating, reading, updating, deleting, searching, and filtering listings.
- Photo endpoints for uploading and retrieving pet images.
- Favorite endpoints for saving and removing pets from a user favorites list.
- Message endpoints for starting conversations and viewing message history.

## Frontend Plan

- Use Vue views for main pages such as browse pets, pet details, create listing, favorites, messages, login, and register.
- Use reusable components for pet cards, search filters, forms, buttons, and navigation.
- Use frontend services to call backend APIs.
- Show loading and error states for searches, form submissions, and API failures.
- Keep the UI simple and focused on the core workflow instead of trying to make it overly fancy.

## Testing Plan

- Test user registration and login.
- Test creating, editing, deleting, and viewing pet listings.
- Test search filters for species, breed, location, and status.
- Test saving and removing favorites.
- Test sending and viewing messages.
- Test database constraints with sample invalid data.
- Test important SQL queries using the seed data.
- Test the app on at least one desktop and one mobile-sized screen.

## Repository Structure

- `docs` folder for proposal, ER diagram, schema notes, and final report files.
- `database` folder for schema script, seed script, indexes, and query examples.
- `PetConnectBackend` folder for the Spring Boot project.
- `PetConnectFrontend` folder for the Vue project.
- `screenshots` folder for images used in the report and presentation.
- `README.md` file with setup instructions and project overview.

## Final Report Outline

- Cover page with project title, team members, course, and contact information.
- Project description and problem statement.
- Functional and non-functional requirements.
- Technology stack and system architecture.
- ER diagram with explanation of entities and relationships.
- Relational schema, keys, constraints, normalization, and indexing choices.
- SQL scripts and example query outputs.
- Implementation summary with screenshots of the running application.
- Testing summary and known limitations.
- Conclusion, challenges, and future improvements.

## Presentation and Demo Plan

- Introduce PetConnect and the problem it solves.
- Explain the main users and core features.
- Show the ER diagram and explain the most important relationships.
- Show the database schema and important SQL queries.
- Demo login, browsing pets, creating a listing, saving a favorite, and sending a message.
- Explain challenges, what was learned, and possible future improvements.


## Folder and Repo Structure

The repository will be organized so the database scripts, backend code, frontend code, documents, screenshots, and presentation files are easy to find. Some files will be added later as the project develops.

### Planned Structure

```text
pet_connect/
|-- README.md
|-- .gitignore
|-- docs/
|   |-- project_proposal.md
|   |-- functional_spec.md
|   |-- er_diagram.png
|   |-- database_schema.md
|   |-- api_notes.md
|   `-- final_report.pdf
|-- database/
|   |-- schema.sql
|   |-- seed.sql
|   |-- indexes.sql
|   |-- queries.sql
|   `-- reset_database.sql
|-- PetConnectBackend/
|   |-- pom.xml
|   |-- src/main/java/com/pet_connect/backend_service/
|   |   |-- controller/
|   |   |-- service/
|   |   |-- repository/
|   |   |-- entity/
|   |   |-- dto/
|   |   |-- config/
|   |   `-- exception/
|   `-- src/main/resources/application.properties
|-- PetConnectFrontend/
|   |-- package.json
|   |-- src/
|   |   |-- components/
|   |   |-- views/
|   |   |-- router/
|   |   |-- services/
|   |   `-- assets/
|   `-- vite.config.ts
|-- screenshots/
|   |-- home.png
|   |-- browse_pets.png
|   |-- pet_details.png
|   `-- messages.png
`-- presentation/
    |-- slides.pdf
    `-- demo_link.md
```

### Folder Purpose

- `docs` will store the proposal, ER diagram, database notes, API notes, and final report.
- `database` will store the SQL scripts for creating tables, inserting sample data, adding indexes, and running example queries.
- `PetConnectBackend` will store the Spring Boot backend, including controllers, services, repositories, entities, DTOs, configuration, and exception handling.
- `PetConnectFrontend` will store the Vue frontend, including pages, components, routes, services, and assets.
- `screenshots` will store images used in the final report and presentation.
- `presentation` will store the final slides and demo link.
