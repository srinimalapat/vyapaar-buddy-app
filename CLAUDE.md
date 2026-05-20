# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vyapaar Buddy is a full-stack MSME business management web app targeting Indian small business owners. It handles sales, customers, credit (udhaar), inventory, and payment reminders, with a planned WhatsApp Cloud API integration.

---

## Commands

### Database
```bash
docker-compose up -d          # Start PostgreSQL (port 5432)
docker-compose down           # Stop PostgreSQL
```

### Backend (from `backend/`)
```bash
mvn spring-boot:run                                        # Run with H2 (dev default)
mvn spring-boot:run -Dspring-boot.run.profiles=prod        # Run with PostgreSQL
mvn clean package                                          # Build JAR
mvn test                                                   # Run all tests
mvn test -Dtest=ClassName                                  # Run a single test class
mvn test -Dtest=ClassName#methodName                       # Run a single test method
```

### Frontend (from `frontend/`)
```bash
npm install       # Install dependencies
npm run dev       # Dev server at http://localhost:5173
npm run build     # Production build
npm run lint      # ESLint
npm run preview   # Preview production build
```

### Access Points
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html

---

## Environment Setup

**Backend** — create `backend/.env` from `backend/.env.example`:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vyapaar_buddy
SPRING_DATASOURCE_USERNAME=vyapaar_user
SPRING_DATASOURCE_PASSWORD=vyapaar_password
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000
```

**Frontend** — create `frontend/.env` from `frontend/.env.example`:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

Backend profiles: `dev` (H2 in-memory, default), `prod` (PostgreSQL + Flyway).

---

## Architecture

### Backend — Layered Spring Boot

```
Controller  →  Service (interface + impl)  →  Repository (JPA)  →  Entity
                      ↕
                   Mapper (Entity ↔ DTO)
```

- **Entities** (`model/entity/`) — JPA models for: `User`, `Business`, `Customer`, `Sale`, `SaleItem`, `CreditTransaction`, `Reminder`, `InventoryItem`
- **DTOs** (`model/dto/`) — `request/` and `response/` subpackages; controllers only accept/return DTOs, never entities
- **Repositories** (`repository/`) — Spring Data JPA interfaces
- **Services** (`service/`) — interfaces in `service/`, implementations in `service/impl/`
- **Controllers** (`controller/`) — REST endpoints; most are currently **stubbed** (TODO bodies)
- **Security** (`security/`) — JWT filter chain; `JwtUtil`, `CustomUserDetailsService`, `SecurityConfig`
- **Exception handling** (`exception/`) — `GlobalExceptionHandler` with custom exception types
- **Mappers** (`mapper/`) — convert between entity and DTO

**Spring profiles:** `application.yml` (base) → `application-dev.yml` (H2) or `application-prod.yml` (PostgreSQL + Flyway). Flyway migrations live in `src/main/resources/db/migration/`.

**Current implementation status:** Entities, DTOs, repositories, security, and controller skeletons exist. Most **service implementations are empty** — the primary pending work is filling in the service layer business logic and wiring controller responses.

### Frontend — React SPA

```
src/
├── api/          # Axios client instances and per-resource API functions
├── context/      # AuthContext (currently mock — no real token storage yet)
├── pages/        # One file per route (Dashboard, Customers, Sales, Credits, etc.)
├── components/   # Shared UI components
├── routes/       # Route definitions and ProtectedRoute wrapper
├── types/        # TypeScript interfaces mirroring backend DTOs
├── hooks/        # Custom React hooks
└── utils/        # Formatting helpers, date utils, currency (INR)
```

Vite proxies `/api/*` to `http://localhost:8080` in dev, so frontend code calls `/api/...` without hardcoding the backend origin.

Auth state lives in `AuthContext` — currently a mock implementation. Real JWT integration is pending.

### Domain Model Relationships

```
User ──< Business ──< Customer ──< CreditTransaction ──< Reminder
                  └──< Sale ──< SaleItem
                  └──< InventoryItem
```

`Business` is the top-level tenant boundary — all resources (customers, sales, inventory, reminders) belong to a business, which belongs to a user.

### WhatsApp Integration

`MockWhatsAppService` provides a dev-mode message parser (recognizes commands: `ADD_SALE`, `ADD_CREDIT`, `CHECK_BALANCE`, `CUSTOMER_INFO`, `INVENTORY_CHECK`). Real WhatsApp Cloud API webhook integration is planned but not yet implemented.

---

## Key Enums

| Enum | Values |
|------|--------|
| `UserRole` | `ADMIN`, `USER`, `VIEWER` |
| `SaleType` | `CASH`, `CREDIT` |
| `CreditTransactionType` | `DEBIT`, `CREDIT`, `PAYMENT` |
| `ReminderChannel` | `WHATSAPP`, `EMAIL`, `SMS` |
| `ReminderStatus` | `PENDING`, `SENT`, `FAILED`, `DELIVERED` |
| `BusinessType` | `RETAIL`, `WHOLESALE`, `RESTAURANT`, `GROCERY`, etc. |
