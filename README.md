# PetConnect

PetConnect is our CS157A project for pet adoption and rehoming. The app lets users sign up, log in, make pet profiles, create posts for pets, upload photos, search/browse community posts, save/apply to posts, and message other users.

Repo link: https://github.com/Edujesseyang/pet_connect.git

## Tech Used

| Part | Tech |
| --- | --- |
| Frontend | Vue 3, HTML, CSS, TypeScript, Vite |
| Backend | Java 21, Spring Boot, JDBC, Maven |
| Database | MySQL |
| API | REST/JSON, Swagger |
| Version Control | Git and GitHub |
| Docs | Markdown, SQL files, Draw.io / Google Drawings diagrams |

## Folder Structure

```text
pet_connect/
+-- PetConnectBackend/      # Spring Boot backend
+-- PetConnectFrontend/     # Vue frontend
+-- database/               # database setup and sample data
+-- docs/                   # project docs and diagrams
+-- README.md
```

## Requirements

Before running the project, install:

- Java 21
- MySQL Server
- Node.js 22.18.x or Node.js 24.12.0+
- npm
- Git

The backend uses the Maven wrapper, so Maven does not have to be installed separately.

## Database Setup

Start MySQL first.

From the main project folder, run:

```powershell
mysql -u root -p < database\database_init.sql
```

Then add the sample data:

```powershell
mysql -u root -p < database\pet_connect_sample_data.sql
```

To check that the database loaded:

```powershell
mysql -u root -p -e "USE petconnect; SHOW TABLES;"
```

Also update the MySQL username and password in [application.properties](PetConnectBackend/src/main/resources/application.properties) for your own computer.

## Backend Setup

Open a terminal and go into the backend folder:

```powershell
cd PetConnectBackend
```

Run the backend tests:

```powershell
.\mvnw.cmd test
```

Start the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend should run at:

```text
http://localhost:8080
```

Swagger can be checked at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Frontend Setup

Open another terminal and go into the frontend folder:

```powershell
cd PetConnectFrontend
```

Install packages:

```powershell
npm install
```

Start the frontend:

```powershell
npm run dev
```

Open the app here:

```text
http://localhost:5173
```

## How to Test the System

### Backend Test

```powershell
cd PetConnectBackend
.\mvnw.cmd test
```

### Frontend Test

```powershell
cd PetConnectFrontend
npm run type-check
npm run build
```

### Database Test

These commands check that the database, users table, and posts table exist:

```powershell
mysql -u root -p -e "USE petconnect; SHOW TABLES;"
mysql -u root -p -e "USE petconnect; SELECT COUNT(*) FROM users;"
mysql -u root -p -e "USE petconnect; SELECT COUNT(*) FROM posts;"
```

### Manual Test

After MySQL, the backend, and the frontend are all running, test the main app flow:

1. Go to `http://localhost:5173`.
2. Sign up for a new account.
3. Log in.
4. Go to the dashboard.
5. Add a pet.
6. Upload a pet photo.
7. Create a post for the pet.
8. View posts on the home page.
9. Save or apply to a post.
10. Try sending a message about a pet.

If those steps work and the data shows up in MySQL, then the main system is working.

## Division of Work

| Team Member | Main Area | Work Done |
| --- | --- | --- |
| Sean Tadina | Docs and database | Worked on the project documents, database schema, sample data, setup instructions, testing instructions, and README. Also helped connect the database design to what the backend needed. |
| Jesse Yang | Backend | Built the Spring Boot backend, API routes, services, repositories, MySQL connection, and backend logic for users, pets, posts, photos, and messages. |
| Ayesha Asim | Frontend | Built the Vue frontend pages and components, including login/signup, home page, dashboard sections, pet pages, forms, and frontend API connections. |

## Collaboration

We split the project by frontend, backend, and database/docs, but the parts still had to work together. Sean worked on the database and documentation, Jesse worked on the backend that uses the database, and Ayesha Asim worked on the frontend that connects to the backend. We checked the app flow together so the database tables, API responses, and frontend pages matched what the project needed.
