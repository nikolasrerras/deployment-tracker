# Deployment Tracker API

A backend service for tracking application deployments across multiple environments.

Built with **Java 21, Spring Boot, PostgreSQL, Docker and Jenkins**, the project demonstrates REST API development, automated testing, database migrations, containerization and a complete local CI/CD workflow.

## Features

- Register and retrieve applications
- Record deployments with version, environment and status
- View deployment history per application
- Retrieve the latest successful deployment for an environment
- Request validation and centralized error handling
- PostgreSQL persistence with Spring Data JPA
- Versioned database migrations with Flyway
- Unit and integration testing with Testcontainers
- Dockerized application and database
- Jenkins CI/CD with automated deployment and smoke tests

## Tech Stack

- **Java 21**
- **Spring Boot 4.1**
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway
- Maven
- JUnit 5 / Mockito / MockMvc
- Testcontainers
- Docker / Docker Compose
- Jenkins
- Git

## Architecture

```text
Client
  |
  v
REST Controllers
  |
  v
Services
  |
  v
Repositories
  |
  v
PostgreSQL
```

The application uses a layered architecture with separate controllers, services, repositories, DTOs and entities.

Flyway manages database schema changes while Hibernate validates the resulting schema.

## REST API

### Applications

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/applications` | Create an application |
| `GET` | `/api/applications` | List all applications |
| `GET` | `/api/applications/{id}` | Get application by ID |
| `GET` | `/api/applications/{id}/deployments` | Get deployment history |
| `GET` | `/api/applications/{id}/environments/{environment}/latest` | Get latest successful deployment |

### Deployments

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/deployments` | Record a deployment |
| `GET` | `/api/deployments` | List deployments |

### Health

```http
GET /api/health
```

Response:

```json
{
  "status": "UP"
}
```

## Example

Create an application:

```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
    "name": "payment-service"
  }'
```

Record a deployment:

```bash
curl -X POST http://localhost:8080/api/deployments \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": 1,
    "environment": "PRODUCTION",
    "version": "1.4.2",
    "status": "SUCCESS"
  }'
```

Supported environments:

```text
DEV
STAGING
PRODUCTION
```

Supported statuses:

```text
SUCCESS
FAILED
```

## Running Locally

### Requirements

- Docker
- Docker Compose

Create a `.env` file in the project root:

```dotenv
POSTGRES_PASSWORD=your_password
```

Start the application:

```bash
docker compose up -d --build
```

Verify that the API is running:

```bash
curl http://localhost:8080/api/health
```

Stop the services:

```bash
docker compose down
```

Database data is stored in a persistent Docker volume.

## Testing

The project currently contains **24 automated tests**, including:

- Service unit tests
- Controller integration tests
- Repository integration tests
- Application context tests

PostgreSQL integration tests run against isolated databases using **Testcontainers**.

Run the full test suite:

```bash
./mvnw clean verify
```

## Database Migrations

Database schema changes are managed with **Flyway**.

Migrations are stored in:

```text
src/main/resources/db/migration/
```

The initial schema is defined in:

```text
V1__create_initial_schema.sql
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

so schema changes are handled explicitly through migrations instead of automatic Hibernate updates.

## Docker

The application uses a multi-stage Docker build:

```text
Maven + Java 21
       |
       v
 Application JAR
       |
       v
 Java 21 JRE
```

The final container runs the application as a non-root user.

Build manually with:

```bash
docker build -t deployment-tracker:local .
```

## CI/CD

The repository contains a Jenkins pipeline defined in `Jenkinsfile`.

The pipeline automatically performs:

```text
Git Commit
    |
    v
Checkout
    |
    v
Build & Test
    |
    v
Docker Build
    |
    v
Deploy
    |
    v
Smoke Test
    |
    v
Image Cleanup
```

Each successful build creates a versioned Docker image such as:

```text
deployment-tracker:ci-5
```

The deployment stage recreates the API container while preserving the PostgreSQL database.

After deployment, Jenkins verifies:

```text
GET /api/health
GET /api/applications
```

Old CI image tags are automatically cleaned up while recent versions are kept for rollback.

## Project Structure

```text
deployment-tracker/
├── src/
│   ├── main/
│   │   ├── java/com/nikolas/deploymenttracker/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
├── pom.xml
└── README.md
```
