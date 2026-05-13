# Banking Microservices Project

A Spring Boot project consisting of two co-dependent microservices for managing banking customers and their accounts.

---

## Project Overview

| Service           | Port | Responsibility                      | PostgreSQL Port |
|-------------------|------|-------------------------------------|-----------------|
| customer-service  | 8081 | Create and manage bank customers    | 5432            |
| account-service   | 8082 | Create and manage customer accounts | 5433            |

Each service has its own database, and they do not interact or call each other directly via HTTP. Instead, they communicate through RabbitMQ (Port: 5672) events, following the Event-Driven Architecture required by this project.

---

## Assumptions

- Each service should have its own database instead of a shared database.
- No direct HTTP calls between services.
- A savings account is automatically created when a customer is created.
- No API gateway needed at this level.
- Both services should run independently.
- Random number generation is acceptable for customer ID generation.
- Basic Auth is used instead of JWT for simplicity; this should be replaced with OAuth2/JWT tokens in production.
- Simple admin setup — all users share the same admin credentials. A more robust application would have proper user controls.

---

## Tech Stack

| Technology        | Purpose                         |
|-------------------|---------------------------------|
| Spring Boot 3.3   | Application framework           |
| Spring Data JPA   | Database access                 |
| Spring Security   | API authentication              |
| Spring AMQP       | RabbitMQ messaging              |
| PostgreSQL        | Relational database             |
| RabbitMQ          | Event-driven messaging broker   |
| Lombok            | Boilerplate reduction           |
| Springdoc OpenAPI | API documentation (Swagger UI)  |
| JaCoCo            | Test coverage reporting         |
| Docker            | Infrastructure containerization |

---

## Shortcomings

- Spring Boot was a new framework to me as I have primarily only worked on .NET projects.
- Missing features and functionality:
  - No soft delete
  - No pagination on list endpoints — as records increase, performance will be affected
  - No auto-closure of accounts when a customer is deleted
  - No audit trail
- Validation constraints are not tight enough in certain areas:
  - Civil ID has no format validation — any string is accepted
  - No cross-field validation, for example verifying a customer exists before creating an account for them
- Testing is shallow, only covering the service layer
- Security:
  - No role-based access control
  - Basic Auth instead of JWT or OAuth2
- Spring Profiles were not implemented

---

## Services

### Customer Service

**Base URL:** `http://localhost:8081/api/customers`

| Method | Endpoint        | Description           |
|--------|-----------------|-----------------------|
| POST   | `/`             | Create a new customer |
| GET    | `/`             | Get all customers     |
| GET    | `/{customerId}` | Get customer by ID    |
| PUT    | `/{customerId}` | Update a customer     |
| DELETE | `/{customerId}` | Delete a customer     |

**Customer fields:**
- `customerId` — **PK**: Randomly generated 7-digit number from 1,000,000 to 9,999,999
- `name` — required, 2–100 characters
- `civilId` — required, must be unique
- `type` — required: *RETAIL*, *CORPORATE*, or *INVESTMENT*
- `address` — required
- `email` — optional, must be a valid email format
- `mobile` — optional, 8–15 digits

**Events Published:**
- `customer.created` → published to RabbitMQ when a customer is successfully created

---

### Account Service

**Base URL:** `http://localhost:8082/api/accounts`

| Method | Endpoint                         | Description                     |
|--------|----------------------------------|---------------------------------|
| POST   | `/`                              | Create a new account            |
| GET    | `/{accountId}`                   | Get account by ID               |
| GET    | `/customer/{customerId}`         | Get all accounts for a customer |
| PATCH  | `/{accountId}/status?status=X`   | Update account status           |
| POST   | `/{accountId}/deposit`           | Deposit funds into an account   |
| POST   | `/{accountId}/withdraw`          | Withdraw funds from an account  |
| DELETE | `/{accountId}`                   | Delete an account               |

**Account fields:**
- `accountId` — **PK**: Customer ID (7 digits) + 3-digit random suffix = 10 digits total
- `customerId` — the customer this account belongs to
- `type` — required: *SALARY*, *SAVINGS*, or *INVESTMENT*
- `initialBalance` — required, cannot be negative

**Business rules:**
- Maximum 10 accounts per customer
- Only one *SALARY* account allowed per customer
- New accounts always start with *ACTIVE* status
- Deposits and withdrawals are only permitted on *ACTIVE* accounts
- Status values: *ACTIVE*, *DORMANT*, *FROZEN*, *CLOSED*

**Events Published:**
- `account.created` → published to RabbitMQ when an account is successfully created

**Events Consumed:**
- `customer.created` → received from customer-service when a new customer is created

---

## Running the Services

### Prerequisites

- Docker
- JDK 21
- Maven

### Steps

1. Start infrastructure:
```bash
docker-compose up -d
```

2. Run customer-service:
```bash
cd customer-service
./mvnw spring-boot:run
```

3. Run account-service:
```bash
cd account-service
./mvnw spring-boot:run
```

4. Access the Swagger UI:
   - Customer Service: `http://localhost:8081/swagger-ui.html` 
   - Account Service: `http://localhost:8082/swagger-ui.html`
   - Credentials — Username: `admin` | Password: `password`

### Infrastructure

| Service              | URL                        | Credentials          |
|----------------------|----------------------------|----------------------|
| Customer Service API | `http://localhost:8081`    | admin / password     |
| Account Service API  | `http://localhost:8082`    | admin / password     |
| RabbitMQ Management  | `http://localhost:15672`   | admin / secret       |

---

### Running Tests

```bash
cd customer-service
./mvnw clean verify

cd account-service
./mvnw clean verify
```

Coverage reports are generated at `target/site/jacoco/index.html` in each service directory.
