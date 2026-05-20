# Architecture Documentation

## System Architecture

### Backend (Java Spring Boot)
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL (production), H2 (dev/test)
- **Authentication**: JWT
- **API Documentation**: Swagger/OpenAPI

### Frontend (React)
- **Framework**: React 18
- **Build Tool**: Vite
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: React Context
- **Routing**: React Router

## Layered Architecture

### Backend Layers
1. **Controller Layer**: REST API endpoints
2. **Service Layer**: Business logic
3. **Repository Layer**: Data access (Spring Data JPA)
4. **Entity Layer**: Database models
5. **DTO Layer**: Data transfer objects
6. **Mapper Layer**: Entity-DTO conversion

### Frontend Structure
1. **Pages**: Route components
2. **Components**: Reusable UI components
3. **Context**: State management
4. **Hooks**: Custom React hooks
5. **API**: API client functions
6. **Types**: TypeScript type definitions
7. **Utils**: Utility functions

## Database Schema
- users
- roles
- businesses
- customers
- sales
- sale_items
- credit_transactions
- reminders
- inventory_items

## Security
- JWT-based authentication
- Role-based access control (RBAC)
- CORS configuration
- Input validation
- SQL injection prevention (JPA)

## WhatsApp Integration
- Mock parser for development
- Future: WhatsApp Cloud API integration
- Message templates
- Webhook support
