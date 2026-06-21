# Job Search Tracker

Simple Spring Boot 3 web application for tracking personal job applications.

## Tech stack

- Java 17+
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- File-based H2 database by default, PostgreSQL dependency included
- Flyway migrations
- Maven

## Run locally

```bash
export JOOBLE_API_KEY=your_jooble_api_key
mvn spring-boot:run
```

Open `http://localhost:8080`.

Default login:

- Username: `student@example.com`
- Password: `password123`

## External job API

The app integrates with the Jooble API when an API key is available. Copy `.env.example` for local setup if needed:

```bash
export JOOBLE_API_KEY=your_jooble_api_key
mvn spring-boot:run
```

Without `JOOBLE_API_KEY`, the search page stays usable but shows a setup message instead of fake vacancies.

## Main features

- Login and logout
- Simple user registration
- Search jobs by keyword and location
- Search filters for work mode, level, job type, and posting date
- Save vacancies to a personal tracker
- View dashboard totals, status counts, recent jobs, and action-needed summary
- Sort applications by status, company, or creation date
- Main applications page uses a Kanban board grouped by application status
- View, update, and delete saved applications
- Add application notes and delete individual notes
- Track priority, salary, and job type for each application
- Drag-and-drop or quick-select Kanban status updates
- DTO-based REST endpoints:
  - `GET /api/applications`
  - `GET /api/applications/{id}`
  - `PATCH /api/applications/{id}/status`
  - `POST /api/applications/{id}/notes`
  - `DELETE /api/applications/{id}`
