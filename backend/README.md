# Vyapaar Buddy Backend

Spring Boot backend for Vyapaar Buddy - India-market MSME WhatsApp Business Assistant.

## Tech Stack
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security with JWT
- PostgreSQL (production) / H2 (dev)
- Flyway migrations
- Swagger/OpenAPI

## Running the Application

### Development (H2 Database)
```bash
mvn spring-boot:run
```

### Production (PostgreSQL)
```bash
# Ensure PostgreSQL is running via docker-compose
docker-compose up -d

# Set environment variables or use application-prod.yml
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## API Documentation
Swagger UI: http://localhost:8080/swagger-ui.html

## Configuration
See `src/main/resources/application.yml` for configuration options.

Environment variables can be set in `.env` file or system environment.
