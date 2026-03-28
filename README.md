# HR System API

A REST API for employee management built with Spring Boot, demonstrating enterprise Java patterns.

## Tech Stack
- **Java 21** + Spring Boot 3.2
- **Spring Security** with JWT authentication
- **Spring Data JPA** + Hibernate + PostgreSQL
- **Docker** + Docker Compose
- **Maven**

## Quick Start

```bash
# Start Postgres + app
docker-compose up

# Or run locally against a local Postgres instance
./mvnw spring-boot:run
```

## Endpoints

### Auth (public)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Create a new user account |
| POST | `/api/v1/auth/login` | Get a JWT token |

### Employees (requires Bearer token)
| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/api/v1/employees` | ALL | List employees (paginated) |
| GET | `/api/v1/employees?search=jane` | ALL | Search by name/email |
| GET | `/api/v1/employees?departmentId=1` | ALL | Filter by department |
| GET | `/api/v1/employees/{id}` | ALL | Get employee details |
| POST | `/api/v1/employees` | ADMIN | Create employee |
| PATCH | `/api/v1/employees/{id}` | ADMIN, MANAGER | Update employee |
| DELETE | `/api/v1/employees/{id}` | ADMIN | Delete employee |

### Example: Login then create an employee

```bash
# 1. Register an admin
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secret","role":"ROLE_ADMIN"}'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secret"}' | jq -r .token)

# 3. Create employee
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane@example.com",
    "jobTitle": "Software Engineer",
    "salary": 95000,
    "hireDate": "2025-01-15",
    "departmentId": 1
  }'
```

## Architecture

```
controller/     HTTP layer — maps routes, delegates to service
service/        Business logic, validation, mapping
repository/     Spring Data JPA — DB access
model/          JPA entities (Employee, Department, AppUser)
dto/            Request/response objects (keeps API decoupled from DB schema)
security/       JWT filter, UserDetailsService
config/         Spring Security config
exception/      Custom exceptions + global error handler
```

## Design Notes

- **Stateless auth** — JWT Bearer tokens, no sessions (same approach as OnyxChat's Go server)
- **Role-based access** — ADMIN / MANAGER / EMPLOYEE via `@PreAuthorize`
- **Paginated list endpoints** — all list endpoints return `Page<T>` with size/sort params
- **DTO pattern** — API contracts are separate from DB entities; internal fields (like `passwordHash`) never leak to responses
- **Global exception handler** — consistent JSON error shape across all endpoints
- **Transactional boundaries** — read-only transactions on queries for performance

## Running Tests

```bash
./mvnw test
```

Tests use H2 in-memory database — no Postgres needed for the test suite.
