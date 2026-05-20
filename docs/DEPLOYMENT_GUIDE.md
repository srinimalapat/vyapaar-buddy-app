# Deployment Guide

## Prerequisites
- Docker and Docker Compose
- Java 17
- Node.js 18+
- PostgreSQL 15 (or use Docker)

## Backend Deployment

### Using Docker Compose (Recommended)
```bash
docker-compose up -d postgres
```

### Manual PostgreSQL Setup
```bash
# Create database
createdb vyapaar_buddy

# Run migrations
cd backend
./mvnw flyway:migrate
```

### Build and Run Backend
```bash
cd backend
./mvnw clean package
java -jar target/vyapaar-buddy-0.0.1.jar
```

## Frontend Deployment

### Development
```bash
cd frontend
npm install
npm run dev
```

### Production Build
```bash
cd frontend
npm install
npm run build
```

Serve the `dist` folder using nginx or any static file server.

## Environment Variables

### Backend (.env)
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vyapaar_buddy
SPRING_DATASOURCE_USERNAME=vyapaar_user
SPRING_DATASOURCE_PASSWORD=vyapaar_password
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
SERVER_PORT=8080
```

### Frontend (.env)
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## Production Considerations
- Change default JWT secret
- Use strong database passwords
- Enable HTTPS
- Configure CORS properly
- Set up monitoring and logging
