# Reservation System

REST API for managing room reservations, built with Java 21 and Spring Boot.

The project focuses on backend fundamentals used in real applications: database-backed authentication, session-based security, ownership authorization, persistence with PostgreSQL, reservation conflict checks, validation, and automated service tests.

> **Project status:** USER authorization is implemented. ADMIN-specific authorization is the next development stage.

## Features

- User registration and login
- Password encoding with Spring Security
- Session-based authentication using `SecurityContext` and `JSESSIONID`
- Create reservations for the authenticated user
- View all reservations owned by the authenticated user
- View a specific reservation only when it belongs to the authenticated user
- Update only owned reservations with `PENDING` status
- Cancel only owned reservations
- Ownership violations return `403 Forbidden`
- Reservation availability and date-conflict checks
- Search and pagination support for reservations
- Centralized exception handling
- Unit tests with JUnit 5 and Mockito

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- JUnit 5
- Mockito
- Docker for local PostgreSQL development

## Architecture

The application follows a conventional layered backend structure:

```text
HTTP Request
    ↓
Spring Security
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Authentication and reservation ownership are kept separate from client input. The client does not choose the reservation owner or initial status.

```text
Authenticated request
        ↓
Authentication.getName()
        ↓
username
        ↓
UserRepository
        ↓
current userId
        ↓
reservation.userId == current userId ?
        ↓
      yes / no
       ↓     ↓
    allow   403 Forbidden
```

When a reservation is created, the server assigns the authenticated user's ID and sets the reservation status to `PENDING`.

## API

### Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/user/register` | Public | Register a new user |
| `POST` | `/user/login` | Public | Authenticate and create an HTTP session |

### Reservations

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/reservation` | Authenticated | Create a reservation for the current user |
| `GET` | `/reservation/all` | Authenticated | Get all reservations owned by the current user |
| `GET` | `/reservation/{id}` | Owner | Get one owned reservation |
| `PUT` | `/reservation/{id}` | Owner | Update an owned `PENDING` reservation |
| `DELETE` | `/reservation/{id}/cancel` | Owner | Cancel an owned reservation |
| `POST` | `/reservation/availability/check` | Authenticated | Check whether a room is available for a date range |
| `GET` | `/reservation` | Admin endpoint in progress | Search reservations with filters and pagination |
| `POST` | `/reservation/{id}/approve` | Admin endpoint in progress | Approve a pending reservation |

The last two endpoints already contain service logic, but role-based access restriction for `ADMIN` is still being implemented.

## Authentication

Login uses Spring Security's `AuthenticationManager`. After successful authentication, the application stores the authenticated `Authentication` object in a `SecurityContext` and persists it through `HttpSessionSecurityContextRepository`.

Subsequent requests are associated with the authenticated user through the session cookie (`JSESSIONID`).

Passwords are stored using Spring Security's `DelegatingPasswordEncoder`.

## Running Locally

### Requirements

- Java 21
- Docker or a local PostgreSQL installation

Start PostgreSQL with Docker, for example:

```bash
docker run --name reservation-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres
```

Set the database credentials expected by `application.properties`:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

Run the application:

```bash
./mvnw spring-boot:run
```

The API is available at:

```text
http://localhost:8080
```


## API Usage Example

Register a user:

```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"artem","password":"password"}'
```

Login and save the session cookie:

```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"username":"artem","password":"password"}'
```

Create a reservation using that session:

```bash
curl -X POST http://localhost:8080/reservation \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "roomId": 5,
    "startDate": "2026-09-20",
    "endDate": "2026-09-22"
  }'
```

The client does not send `userId` or `status`; both are controlled by the server.

## Testing

Run the test suite with:

```bash
./mvnw test
```

The current service tests cover successful flows and failure cases including:

- reservation creation and date validation
- ownership checks for reading, updating, and cancelling reservations
- missing users and reservations
- invalid reservation status transitions
- search pagination behavior
- reservation approval and availability conflicts

## Error Handling

The API uses a centralized `GlobalExceptionHandler` for consistent HTTP responses, including:

- `400 Bad Request` — invalid input or invalid reservation state
- `401 Unauthorized` — invalid credentials
- `403 Forbidden` — authenticated user attempts to access another user's reservation
- `404 Not Found` — user or reservation does not exist
- `500 Internal Server Error` — unexpected server errors

## Roadmap

- Enforce `ADMIN` role authorization for administrative endpoints
- Allow administrators to view and manage reservations across users
- Restrict reservation approval to administrators
- Add controller/security integration tests
- Add OpenAPI / Swagger documentation
- Review CSRF strategy before production-style browser usage