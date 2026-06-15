# Job Search Tracker

Simple Spring Boot 3 web application for tracking personal job applications.

## Tech stack

- Java 17+
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- H2 database by default, PostgreSQL dependency included
- Maven

## Run locally

```bash
mvn spring-boot:run
```

Open `http://localhost:8080`.

Default login:

- Username: `student@example.com`
- Password: `password123`

## External job API

The app integrates with the Jooble API when an API key is available:

```bash
export JOOBLE_API_KEY=5372d3a6-1d37-4118-8a72-0a34d791e72f
mvn spring-boot:run
```

Without `JOOBLE_API_KEY`, the search page returns small demo results so the application remains easy to run for lab work.

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
- DTO-based REST endpoints at `/api/applications`
