# API Documentation

## Base URL
- Development: `http://localhost:8080/api`
- Production: `https://api.vyapaarbuddy.com/api`

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

## Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/register` - User registration

### Business
- `GET /businesses` - Get all businesses
- `GET /businesses/{id}` - Get business by ID
- `POST /businesses` - Create business
- `PUT /businesses/{id}` - Update business
- `DELETE /businesses/{id}` - Delete business

### Customers
- `GET /customers` - Get all customers
- `GET /customers/{id}` - Get customer by ID
- `POST /customers` - Create customer
- `PUT /customers/{id}` - Update customer
- `DELETE /customers/{id}` - Delete customer

### Sales
- `GET /sales` - Get all sales
- `GET /sales/{id}` - Get sale by ID
- `POST /sales` - Create sale
- `PUT /sales/{id}` - Update sale
- `DELETE /sales/{id}` - Delete sale

### Credits
- `GET /credits` - Get all credit transactions
- `GET /credits/{id}` - Get credit transaction by ID
- `POST /credits` - Create credit transaction
- `PUT /credits/{id}/settle` - Settle credit

### Reminders
- `GET /reminders` - Get all reminders
- `GET /reminders/{id}` - Get reminder by ID
- `POST /reminders` - Create reminder
- `PUT /reminders/{id}` - Update reminder
- `POST /reminders/{id}/send` - Send reminder

### Inventory
- `GET /inventory` - Get all inventory items
- `GET /inventory/low-stock` - Get low stock items
- `GET /inventory/{id}` - Get inventory item by ID
- `POST /inventory` - Create inventory item
- `PUT /inventory/{id}` - Update inventory item
- `DELETE /inventory/{id}` - Delete inventory item

### Dashboard
- `GET /dashboard` - Get dashboard statistics
- `GET /dashboard/sales-trend` - Get sales trend

### Reports
- `GET /reports/sales` - Get sales report
- `GET /reports/credit` - Get credit report
- `GET /reports/inventory` - Get inventory report

### Mock WhatsApp
- `POST /mock-whatsapp/parse` - Parse WhatsApp message

## Swagger UI
Access interactive API documentation at: `http://localhost:8080/swagger-ui.html`
