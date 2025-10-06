# Tasklist API

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive **Task Management API** built with Spring Boot 3, featuring PostgreSQL database, Docker containerization, comprehensive testing, and production-ready deployment capabilities.

## 🚀 Features

- ✅ **Complete CRUD Operations** - Create, read, update, and manage tasks
- ✅ **Advanced Filtering** - Filter tasks by completion status
- ✅ **RESTful API** - Clean, intuitive REST endpoints
- ✅ **Input Validation** - Comprehensive request validation
- ✅ **API Documentation** - Interactive Swagger/OpenAPI UI
- ✅ **Database Persistence** - PostgreSQL with JPA/Hibernate
- ✅ **Docker Ready** - Complete containerization with Docker Compose
- ✅ **Comprehensive Testing** - 35+ unit and integration tests
- ✅ **Production Logging** - Structured logging with multiple levels
- ✅ **CI/CD Ready** - GitHub Actions workflow included
- ✅ **VM Deployment** - Complete deployment scripts for Linux VMs

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [Troubleshooting](#troubleshooting)

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **Docker & Docker Compose** (recommended)
- **Maven 3.6+**

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repository-url>
cd TasklistAPI

# Start everything with a single command
docker compose up --build

# API available at: http://localhost:8080
# Swagger UI at: http://localhost:8080/swagger-ui/index.html
# Database accessible at: localhost:5432
```

### Option 2: Local Development

```bash
# Start PostgreSQL database
docker compose up -d db

# Build and run the application
mvn clean package
mvn spring-boot:run

# API available at: http://localhost:8080
```

## 📡 API Endpoints

### Base URL
```
http://localhost:8080/api/tasks
```

### Endpoints

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| `POST` | `/api/tasks` | Create a new task | `Task` | `Task` |
| `GET` | `/api/tasks` | List all tasks | - | `Task[]` |
| `GET` | `/api/tasks?completed=true` | List completed tasks | - | `Task[]` |
| `GET` | `/api/tasks?completed=false` | List pending tasks | - | `Task[]` |
| `PUT` | `/api/tasks/{id}/complete` | Mark task as completed | - | `Task` |

### Example Requests

#### Create Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Complete project documentation",
    "dueDate": "2025-10-15T17:00:00Z"
  }'
```

#### List All Tasks
```bash
curl http://localhost:8080/api/tasks
```

#### Filter by Status
```bash
# Completed tasks
curl "http://localhost:8080/api/tasks?completed=true"

# Pending tasks
curl "http://localhost:8080/api/tasks?completed=false"
```

#### Mark as Completed
```bash
curl -X PUT http://localhost:8080/api/tasks/1/complete
```

## 🛠️ Development

### Project Structure
```
TasklistAPI/
├── src/
│   ├── main/
│   │   ├── java/com/slmakomazi/tasklist/
│   │   │   ├── controller/     # REST Controllers
│   │   │   ├── model/         # JPA Entities
│   │   │   ├── repository/    # Data Access Layer
│   │   │   ├── service/       # Business Logic
│   │   │   └── config/        # Configuration Classes
│   │   └── resources/
│   │       ├── application.yml       # App Configuration
│   │       ├── application-prod.properties  # Production Config
│   │       └── logback-spring.xml    # Logging Config
│   └── test/                  # Comprehensive Test Suite
├── .github/workflows/         # CI/CD Pipeline
├── deployment/               # VM Deployment Scripts
├── postman/                  # API Testing Collection
├── Dockerfile               # Container Definition
├── docker-compose.yml       # Multi-container Setup
└── README.md
```

### Development Workflow

1. **Start Database**:
   ```bash
   docker compose up -d db
   ```

2. **Run Tests**:
   ```bash
   mvn test
   ```

3. **Start Application**:
   ```bash
   mvn spring-boot:run
   ```

4. **View Logs**:
   ```bash
   # Application logs
   tail -f logs/tasklist-api.log

   # Docker logs
   docker compose logs -f api
   ```

### Testing

The project includes **35 comprehensive tests**:

- **7 Model Tests** - Entity validation and behavior
- **8 Repository Tests** - Database operations and queries
- **8 Service Tests** - Business logic with mocked dependencies
- **8 Controller Tests** - REST API endpoints and HTTP responses
- **4 Integration Tests** - Full application stack testing

Run all tests:
```bash
mvn test
```

Run with coverage:
```bash
mvn test jacoco:report
```

## 🚢 Deployment

### Docker Deployment

#### Single Command Deployment
```bash
# Deploy everything
docker compose up --build -d

# View logs
docker compose logs -f

# Stop everything
docker compose down
```

#### Production Deployment
```bash
# Use production configuration
docker compose -f docker-compose.prod.yml up --build -d

# Scale API instances
docker compose up --build -d --scale api=3
```

### VM Deployment (Linux)

#### Automated Deployment Script
```bash
# Copy deployment files to VM
scp deployment/* user@vm-ip:/opt/tasklist/

# Run deployment script on VM
ssh user@vm-ip "cd /opt/tasklist && chmod +x deploy-to-vm.sh && ./deploy-to-vm.sh"
```

#### Manual Deployment
```bash
# Install dependencies
sudo apt update
sudo apt install -y openjdk-17-jdk postgresql docker.io

# Setup database
sudo -u postgres psql -c "CREATE DATABASE tasklist;"
sudo -u postgres psql -c "CREATE USER tasklist WITH PASSWORD 'secure-password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE tasklist TO tasklist;"

# Deploy application
docker compose up --build -d
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/tasklist` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring profile (dev/prod/test) |
| `SERVER_PORT` | `8080` | Application port |

## 📊 Monitoring & Health Checks

### Health Endpoints
- **Health Check**: `GET /actuator/health`
- **Application Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`

### Logging
- **Application Logs**: `/opt/tasklist/logs/` (in container)
- **Log Levels**: DEBUG for application code, INFO for root
- **Log Format**: Timestamp, thread, level, logger, message

## 🔧 Configuration

### Application Profiles
- **dev** (default): H2 in-memory database for development
- **prod**: PostgreSQL database for production
- **test**: H2 database for testing

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tasklist
    username: postgres
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run tests (`mvn test`)
4. Commit changes (`git commit -m 'Add amazing feature'`)
5. Push to branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

### Development Guidelines
- Write tests for new features
- Follow existing code style
- Update documentation for API changes
- Ensure all tests pass before submitting PR

## 🐛 Troubleshooting

### Common Issues

**Database Connection Issues**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# View database logs
docker compose logs db

# Test connection
psql -h localhost -p 5432 -U postgres -d tasklist
```

**Application Won't Start**
```bash
# Check application logs
docker compose logs api

# Verify environment variables
docker compose config

# Test with different port
SERVER_PORT=8081 mvn spring-boot:run
```

**Tests Failing**
```bash
# Run tests with detailed output
mvn test -X

# Run specific test class
mvn test -Dtest=TaskServiceTest

# Check test database
# Tests use H2 in-memory database automatically
```

### Getting Help

1. Check the [Issues](../../issues) page for known problems
2. Review the [deployment documentation](./deployment/README.md)
3. Check application logs for detailed error messages
4. Ensure all environment variables are properly set

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- Database: [PostgreSQL](https://postgresql.org/)
- Containerization: [Docker](https://docker.com/)
- API Documentation: [SpringDoc OpenAPI](https://springdoc.org/)

---

**⭐ If you found this project helpful, please give it a star!**
