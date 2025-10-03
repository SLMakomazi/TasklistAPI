# Tasklist API

Java Spring Boot Tasklist API with PostgreSQL, Swagger/OpenAPI, Docker, and logging.

## Tech Stack
- Java 17, Spring Boot 3
- Spring Web, Spring Data JPA, Validation
- PostgreSQL
- Springdoc OpenAPI (Swagger UI)
- Docker, Docker Compose

## Local Development

1. Ensure PostgreSQL is running locally, or use Docker Compose (recommended):

```bash
# From repo root
docker compose up -d db
```

2. Start the API from your IDE or via Maven:

```bash
# Build
mvn clean package
# Run
mvn spring-boot:run
```

API will run at `http://localhost:8080`.

### Swagger UI
Visit `http://localhost:8080/swagger-ui/index.html`.

## Docker Compose (Full Stack)

```bash
# Build and start API + DB
docker compose up --build
```

## cURL Examples

- Create Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Write documentation",
    "dueDate": "2025-10-15T17:00:00Z"
  }'
```

- List All Tasks
```bash
curl http://localhost:8080/api/tasks
```

- List by Completed Status
```bash
curl "http://localhost:8080/api/tasks?completed=true"
```

- Mark as Completed
```bash
curl -X PUT http://localhost:8080/api/tasks/1/complete
```

## Linux VM (Hyper-V) Deployment Notes

- Install Docker Engine or Docker Desktop on the VM.
- Clone repo and run `docker compose up --build -d`.
- Ensure ports 8080 (API) and 5432 (DB) are open in firewall if remote access is needed.

## Configuration
- Adjust DB connection via environment variables:
  - `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5432/tasklist`)
  - `SPRING_DATASOURCE_USERNAME` (default `postgres`)
  - `SPRING_DATASOURCE_PASSWORD` (default `postgres`)

## Logging
- Logback configured in `src/main/resources/logback-spring.xml`.
- Package `com.slmakomazi.tasklist` logs at DEBUG; root at INFO.
