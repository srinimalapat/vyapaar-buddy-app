# Vyapaar Buddy

A full-stack MVP for an India-market MSME WhatsApp Business Assistant.

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.x
- Maven
- REST APIs
- Spring Data JPA
- PostgreSQL for production/local Docker
- H2 for local dev/testing
- YAML configuration
- Bean validation
- Swagger/OpenAPI
- JWT authentication
- Layered architecture

### Frontend
- React
- Vite
- TypeScript
- Tailwind CSS
- Axios
- Responsive dashboard UI

## Project Structure

```
vyapaar-buddy/
├── backend/          # Spring Boot backend
├── frontend/         # React Vite frontend
├── docker-compose.yml # PostgreSQL configuration
├── README.md
├── .gitignore
└── docs/            # Documentation
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- Docker and Docker Compose (for PostgreSQL)

### Running PostgreSQL with Docker

```bash
# Start PostgreSQL
docker-compose up -d

# Stop PostgreSQL
docker-compose down

# View logs
docker-compose logs postgres
```

PostgreSQL will be available on port 5432 with:
- Database: vyapaar_buddy
- Username: vyapaar_user
- Password: vyapaar_password

### Running the Backend

```bash
cd backend

# Run with Maven
mvn spring-boot:run

# Or build and run
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

The backend will be available on http://localhost:8080

API Documentation (Swagger): http://localhost:8080/swagger-ui.html

### Running the Frontend

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

The frontend will be available on http://localhost:5173

### Quick Start (All at Once)

To run the entire stack (database, backend, and frontend) simultaneously:

```bash
# Terminal 1 - Start database
docker-compose up -d

# Terminal 2 - Start backend
cd backend
mvn spring-boot:run

# Terminal 3 - Start frontend
cd frontend
npm install
npm run dev
```

Access the application at http://localhost:5173

## Configuration

### Backend Environment Variables

Create `backend/.env` based on `backend/.env.example`:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vyapaar_buddy
SPRING_DATASOURCE_USERNAME=vyapaar_user
SPRING_DATASOURCE_PASSWORD=vyapaar_password
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000
```

### Frontend Environment Variables

Create `frontend/.env` based on `frontend/.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Development

### Backend Profiles
- `dev` - Uses H2 in-memory database (default for local development)
- `test` - Uses H2 for testing
- `prod` - Uses PostgreSQL

To switch profiles:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Database Migrations

Flyway migrations are located in `backend/src/main/resources/db/migration/`

## Features

- User authentication with JWT
- Business management
- Customer management
- Sales tracking
- Credit/udhaar management
- Payment reminders
- Inventory management
- Dashboard with analytics
- Reports
- Mock WhatsApp message parser
- WhatsApp webhook integration (future)

## Documentation

See the `docs/` folder for:
- API samples
- Database design
- Future WhatsApp Cloud API integration

## License

MIT
