# Vyapaar Buddy — API Samples

> Updated with Phase 6: Inventory Management and Payment Reminders

All protected endpoints require a JWT token obtained from login/register.

```
Authorization: Bearer <token>
```

---

## Authentication

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Ramesh Sharma",
  "email": "ramesh@shop.com",
  "password": "secret123",
  "phone": "9876543210"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "ramesh@shop.com",
  "password": "secret123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCI...",
    "type": "Bearer",
    "userId": 1,
    "email": "ramesh@shop.com",
    "name": "Ramesh Sharma"
  }
}
```

---

## Business APIs

### Create Business Profile

```http
POST /api/v1/business
Authorization: Bearer <token>
Content-Type: application/json

{
  "ownerName": "Ramesh Sharma",
  "mobileNumber": "9876543210",
  "businessName": "Ramesh General Store",
  "businessType": "GROCERY",
  "city": "Mumbai",
  "state": "Maharashtra",
  "preferredLanguage": "HINDI",
  "address": "Shop No 5, Andheri West",
  "pinCode": "400053",
  "gstNumber": "27AABCU9603R1ZX"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Business profile created successfully",
  "data": {
    "id": 1,
    "ownerName": "Ramesh Sharma",
    "mobileNumber": "9876543210",
    "businessName": "Ramesh General Store",
    "businessType": "GROCERY",
    "city": "Mumbai",
    "state": "Maharashtra",
    "preferredLanguage": "HINDI",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### Get My Business Profile

```http
GET /api/v1/business/me
Authorization: Bearer <token>
```

### Update My Business Profile

```http
PUT /api/v1/business/me
Authorization: Bearer <token>
Content-Type: application/json

{
  "ownerName": "Ramesh Sharma",
  "mobileNumber": "9876543210",
  "businessName": "Ramesh Super Store",
  "businessType": "GROCERY",
  "city": "Mumbai",
  "state": "Maharashtra",
  "preferredLanguage": "HINDI"
}
```

---

## Customer APIs

### Create Customer

```http
POST /api/v1/customers
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerName": "Suresh Patel",
  "mobileNumber": "9123456780",
  "address": "12, MG Road, Pune",
  "notes": "Regular customer, pays on time",
  "status": "ACTIVE"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": 1,
    "customerName": "Suresh Patel",
    "mobileNumber": "9123456780",
    "address": "12, MG Road, Pune",
    "notes": "Regular customer, pays on time",
    "totalCreditAmount": 0,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:35:00"
  }
}
```

### Get Customer by ID

```http
GET /api/v1/customers/1
Authorization: Bearer <token>
```

### List All Customers (Active by default)

```http
GET /api/v1/customers
Authorization: Bearer <token>
```

### List Customers by Status

```http
GET /api/v1/customers?status=INACTIVE
Authorization: Bearer <token>
```

### Search Customers

```http
GET /api/v1/customers/search?query=ramesh
Authorization: Bearer <token>
```

Searches by customer name or mobile number within the logged-in user's business.

### Update Customer

```http
PUT /api/v1/customers/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerName": "Suresh Patel",
  "mobileNumber": "9123456780",
  "address": "15, MG Road, Pune",
  "notes": "Updated address",
  "status": "ACTIVE"
}
```

### Deactivate Customer (Soft Delete)

```http
DELETE /api/v1/customers/1
Authorization: Bearer <token>
```

**Response (200):**
```json
{
  "success": true,
  "message": "Customer deactivated successfully",
  "data": null
}
```

The customer record is **not deleted** from the database. Status is set to `INACTIVE`.

---

## Error Responses

### 400 Bad Request — Validation failure

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Validation failed",
  "validationErrors": {
    "mobileNumber": "Invalid Indian mobile number (must be 10 digits starting with 6-9)",
    "businessName": "Business name is required"
  }
}
```

### 400 Bad Request — Duplicate business

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Business profile already exists. Use PUT /api/v1/business/me to update it."
}
```

### 401 Unauthorized

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

### 404 Not Found

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id: 5"
}
```

---

## Sales APIs

### Create Cash Sale

```http
POST /api/v1/sales
Authorization: Bearer <token>
Content-Type: application/json

{
  "saleType": "CASH",
  "totalAmount": 450.00,
  "paidAmount": 450.00,
  "saleDate": "2026-05-19",
  "notes": "Regular morning sale",
  "items": [
    { "itemName": "Basmati Rice 5kg", "quantity": 2, "unitPrice": 150.00 },
    { "itemName": "Cooking Oil 1L",   "quantity": 1, "unitPrice": 150.00 }
  ]
}
```

### Create Credit Sale (Udhaar)

```http
POST /api/v1/sales
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerId": 1,
  "saleType": "CREDIT",
  "totalAmount": 800.00,
  "paidAmount": 300.00,
  "saleDate": "2026-05-19",
  "notes": "Suresh udhaar",
  "items": [
    { "itemName": "Dal 2kg",   "quantity": 2, "unitPrice": 200.00 },
    { "itemName": "Sugar 1kg", "quantity": 4, "unitPrice": 100.00 }
  ]
}
```

Remaining balance (500.00) is automatically added to customer's credit balance and a `CREDIT_GIVEN` transaction is created.

### Get Sale by ID

```http
GET /api/v1/sales/1
Authorization: Bearer <token>
```

### List All Sales

```http
GET /api/v1/sales
Authorization: Bearer <token>
```

### Filter Sales by Date Range

```http
GET /api/v1/sales?fromDate=2026-05-01&toDate=2026-05-19
Authorization: Bearer <token>
```

### Filter Sales by Customer

```http
GET /api/v1/sales?customerId=1
Authorization: Bearer <token>
```

### Filter Sales by Type

```http
GET /api/v1/sales?saleType=CREDIT
Authorization: Bearer <token>
```

### Daily Sales Summary

```http
GET /api/v1/sales/summary/daily?date=2026-05-19
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "date": "2026-05-19",
    "totalSales": 1250.00,
    "cashSales": 450.00,
    "creditSales": 800.00,
    "upiSales": 0.00,
    "cardSales": 0.00,
    "totalPaid": 750.00,
    "totalBalance": 500.00,
    "saleCount": 2
  }
}
```

### Monthly Sales Summary

```http
GET /api/v1/sales/summary/monthly?year=2026&month=5
Authorization: Bearer <token>
```

---

## Credit / Udhaar APIs

### Add Credit Transaction (CREDIT_GIVEN)

```http
POST /api/v1/credits
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerId": 1,
  "transactionType": "CREDIT_GIVEN",
  "amount": 1000.00,
  "description": "Grocery items on credit",
  "transactionDate": "2026-05-19"
}
```

### Record Payment Received

```http
POST /api/v1/credits/payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerId": 1,
  "amount": 500.00,
  "description": "Partial payment received",
  "transactionDate": "2026-05-19",
  "allowOverPayment": false
}
```

### Allow Over-Payment (clears negative balance)

```http
POST /api/v1/credits/payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerId": 1,
  "amount": 2000.00,
  "description": "Full advance payment",
  "allowOverPayment": true
}
```

### Customer Credit History

```http
GET /api/v1/credits/customers/1/history
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "customerId": 1,
      "customerName": "Suresh Patel",
      "transactionType": "PAYMENT_RECEIVED",
      "amount": 500.00,
      "description": "Partial payment",
      "transactionDate": "2026-05-19",
      "createdAt": "2026-05-19T14:30:00"
    },
    {
      "id": 1,
      "customerId": 1,
      "customerName": "Suresh Patel",
      "transactionType": "CREDIT_GIVEN",
      "amount": 800.00,
      "description": "Credit from Sale #2",
      "transactionDate": "2026-05-19",
      "createdAt": "2026-05-19T10:30:00"
    }
  ]
}
```

### Pending Credit Customers

```http
GET /api/v1/credits/pending-customers
Authorization: Bearer <token>
```

Returns all active customers with `totalCreditAmount > 0`.

### Total Outstanding Credit

```http
GET /api/v1/credits/total-outstanding
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalOutstandingCredit": 2500.00,
    "customersWithPendingCredit": 4
  }
}
```

---

## Phase 6 — Inventory Management

### Add Inventory Item

```http
POST /api/v1/inventory
Authorization: Bearer <token>
Content-Type: application/json

{
  "itemName": "Basmati Rice 5kg",
  "category": "Grains",
  "quantityAvailable": 100,
  "lowStockThreshold": 20,
  "unitPrice": 450.00
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Inventory item added",
  "data": {
    "id": 1,
    "itemName": "Basmati Rice 5kg",
    "category": "Grains",
    "quantityAvailable": 100,
    "lowStockThreshold": 20,
    "unitPrice": 450.00,
    "status": "ACTIVE",
    "lowStock": false
  }
}
```

### List Inventory Items

```http
GET /api/v1/inventory?search=rice&status=ACTIVE
Authorization: Bearer <token>
```

### Manually Update Stock

```http
PATCH /api/v1/inventory/1/stock
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantityAvailable": 75
}
```

### Get Low-Stock Items

```http
GET /api/v1/inventory/low-stock
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "itemName": "Toor Dal 1kg",
      "quantityAvailable": 8,
      "lowStockThreshold": 15,
      "lowStock": true,
      "status": "ACTIVE"
    }
  ]
}
```

### Deactivate Inventory Item (Soft Delete)

```http
DELETE /api/v1/inventory/1
Authorization: Bearer <token>
```

---

## Phase 6 — Payment Reminders

### Generate Reminder for a Customer

Body is optional — all fields default from customer data and business name.

```http
POST /api/v1/reminders/customer/10
Authorization: Bearer <token>
Content-Type: application/json

{
  "amountDue": 500.00,
  "reminderDate": "2026-05-25",
  "channel": "WHATSAPP_MANUAL"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Reminder generated",
  "data": {
    "id": 1,
    "customerId": 10,
    "customerName": "Ramesh Kumar",
    "customerMobileNumber": "9876543210",
    "amountDue": 500.00,
    "reminderDate": "2026-05-25",
    "message": "Namaste Ramesh Kumar ji,\n\nAapka Test Shop mein Rs 500.00 baaki hai. Kripya jald se jald payment karein.\n\nDhanyawad,\nTest Shop",
    "status": "SENT",
    "channel": "WHATSAPP_MANUAL"
  }
}
```

### Bulk Generate Reminders

Generates one PENDING reminder for every active customer with `creditBalance > 0`.

```http
POST /api/v1/reminders/bulk
Authorization: Bearer <token>
```

### List Reminders

```http
GET /api/v1/reminders?customerId=10&status=PENDING
Authorization: Bearer <token>
```

### Mark Reminder as Sent

```http
PATCH /api/v1/reminders/1/sent
Authorization: Bearer <token>
```

### Cancel a Reminder

```http
PATCH /api/v1/reminders/1/cancel
Authorization: Bearer <token>
```

---

## Auto Inventory Reduction on Sale

When a sale item name exactly matches (case-insensitive) an ACTIVE inventory item,
stock is automatically reduced. If stock is insufficient, the sale is rejected.

```http
POST /api/v1/sales
Authorization: Bearer <token>
Content-Type: application/json

{
  "saleType": "CASH",
  "totalAmount": 900.00,
  "paidAmount": 900.00,
  "items": [
    {
      "itemName": "Basmati Rice 5kg",
      "quantity": 2,
      "unitPrice": 450.00
    }
  ]
}
```

If `Basmati Rice 5kg` exists in inventory with qty ≥ 2, stock is reduced by 2 automatically.
If qty < 2, response is `400 Bad Request`:
```json
{
  "success": false,
  "message": "Insufficient stock for item: Basmati Rice 5kg (available: 1, requested: 2)"
}
```

---

## Phase 7 — Dashboard

### Get Dashboard Stats

```http
GET /api/v1/dashboard
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "todayTotalSales": 900.00,
    "todayCashSales": 900.00,
    "todayCreditSales": 0.00,
    "todayUpiSales": 0.00,
    "todayCardSales": 0.00,
    "totalPendingUdhaar": 360.00,
    "customersWithPendingCredit": 1,
    "lowStockCount": 1,
    "monthlySalesTotal": 1460.00,
    "recentSales": [ { "id": 4, "saleType": "CASH", "totalAmount": 900.00 } ],
    "recentCreditPayments": []
  }
}
```

---

## Phase 7 — Reports

### Daily Sales Report

```http
GET /api/v1/reports/daily-sales?date=2026-05-20
Authorization: Bearer <token>
```

### Monthly Sales Report

```http
GET /api/v1/reports/monthly-sales?year=2026&month=5
Authorization: Bearer <token>
```

### Customer Credit Report

```http
GET /api/v1/reports/customer-credit
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "reportType": "CUSTOMER_CREDIT",
    "totalOutstandingCredit": 360.00,
    "customersWithPendingCredit": 1,
    "customers": [
      { "id": 1, "customerName": "Suresh Patil", "totalCreditAmount": 360.00 }
    ]
  }
}
```

### Inventory Low-Stock Report

```http
GET /api/v1/reports/inventory-low-stock
Authorization: Bearer <token>
```

---

## Phase 7 — Mock WhatsApp Parser

All examples require `Authorization: Bearer <token>`.

### Parse Only (no side-effects)

```http
POST /api/v1/mock-whatsapp/parse
Content-Type: application/json

{ "message": "Sale Ramesh rice 2kg 120 cash" }
```

**Response:**
```json
{
  "data": {
    "commandType": "CREATE_SALE",
    "customerName": "Ramesh",
    "itemName": "rice",
    "quantity": 2,
    "amount": 120.00,
    "paymentType": "cash",
    "confidenceScore": 0.90,
    "validationErrors": [],
    "executable": true,
    "executed": false
  }
}
```

### Parse + Execute

```http
POST /api/v1/mock-whatsapp/execute
Content-Type: application/json

{ "message": "Sale Ramesh rice 2kg 120 cash" }
```

```http
POST /api/v1/mock-whatsapp/execute
Content-Type: application/json

{ "message": "Udhaar Suresh 500" }
```

```http
POST /api/v1/mock-whatsapp/execute
Content-Type: application/json

{ "message": "Payment Ramesh 300" }
```

```http
POST /api/v1/mock-whatsapp/execute
Content-Type: application/json

{ "message": "Stock add sugar 10kg 45" }
```

```http
POST /api/v1/mock-whatsapp/execute
Content-Type: application/json

{ "message": "Report today" }
```

**Supported command formats:**

| Command | Example |
|---------|---------|
| Cash sale | `Sale <customer> <item> <qty> <amount> cash` |
| UPI sale | `Sale Suresh sugar 1kg 50 upi` |
| Credit sale (udhaar) | `Sale Amit tea 2 40 credit` |
| Add udhaar | `Udhaar Suresh 500` or `Credit Ramesh 850` |
| Record payment | `Payment Ramesh 300` or `Paid Suresh 500` |
| Add/update stock | `Stock add sugar 10kg 45` |
| Daily report | `Report today` or `Today report` |

---

## Photo Stock Entry (Phase 10)

> Upload supplier bills, stock sheets, or handwritten lists to auto-extract inventory items.
> Inventory is **only updated after owner confirmation** — never at upload time.

### 1. Upload Image & Extract Items

```http
POST /api/v1/photo-stock/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form fields:
  file     = stock-list.jpg        (required)
  mockText = Rice 25kg 60          (optional — for local testing without real OCR)
             Sugar 10kg 45
             Oil 5L 140
             Tea powder 12 pcs 90
             Dal 20kg ₹120
             Milk 10 packets 30
```

Response (201):
```json
{
  "success": true,
  "message": "Image uploaded and text extracted",
  "data": {
    "id": 1,
    "sourceType": "LOCAL_UPLOAD",
    "originalFileName": "stock-list.jpg",
    "extractedText": "Rice 25kg 60\nSugar 10kg 45\nOil 5L 140",
    "status": "PENDING_REVIEW",
    "items": [
      { "id": 1, "itemName": "Rice",  "quantity": 25, "unit": "kg", "unitPrice": 60,  "category": "General", "confidenceScore": 0.90, "validationErrors": null },
      { "id": 2, "itemName": "Sugar", "quantity": 10, "unit": "kg", "unitPrice": 45,  "category": "General", "confidenceScore": 0.90, "validationErrors": null },
      { "id": 3, "itemName": "Oil",   "quantity": 5,  "unit": "l",  "unitPrice": 140, "category": "General", "confidenceScore": 0.90, "validationErrors": null }
    ]
  }
}
```

### 2. Get Entry by ID

```http
GET /api/v1/photo-stock/1
Authorization: Bearer <token>
```

### 3. List Entries

```http
GET /api/v1/photo-stock
GET /api/v1/photo-stock?status=PENDING_REVIEW
Authorization: Bearer <token>
```

### 4. Confirm — Update Inventory

Pass edited items to override extracted values.
If `items` is omitted, original extracted items are used.

```http
POST /api/v1/photo-stock/1/confirm
Authorization: Bearer <token>
Content-Type: application/json

{
  "updateExistingItems": true,
  "items": [
    {
      "itemName": "Rice",
      "quantity": 25,
      "unit": "kg",
      "unitPrice": 60,
      "category": "Grocery",
      "lowStockThreshold": 5
    },
    {
      "itemName": "Sugar",
      "quantity": 10,
      "unit": "kg",
      "unitPrice": 45,
      "category": "Grocery"
    }
  ]
}
```

Behavior on confirm:
- If item exists (case-insensitive name match, ACTIVE status): add quantity, optionally update price.
- If item does not exist: create new ACTIVE inventory item (category default "General", threshold default 5).

### 5. Cancel Entry

```http
POST /api/v1/photo-stock/1/cancel
Authorization: Bearer <token>
Content-Type: application/json

{ "reason": "Duplicate upload" }
```

### Local Test Steps (Postman)

1. `POST /api/auth/login` → get token
2. `POST /api/v1/photo-stock/upload` (multipart) with file + mockText
3. Note the entry `id` and review `items` in response
4. Optionally edit items in request body
5. `POST /api/v1/photo-stock/{id}/confirm`
6. Check `GET /api/v1/inventory` to see updated stock

### Supported Mock Text Formats

| Format | Example |
|--------|---------|
| Name + qty + unit + price | `Rice 25kg 60` |
| Name + qty + unit (space) + price | `Sugar 10 kg 45` |
| Name + qty + litre unit | `Oil 5L 140` |
| Name + qty + pcs | `Tea powder 12 pcs 90` |
| Price with ₹ symbol | `Dal 20kg ₹120` |
| Packets unit | `Milk 10 packets 30` |

---

## File Stock Entry (Phase 10 Update)

> Supports image upload, PDF, Excel, CSV, Word, and TXT files.
> Text/CSV/Excel/PDF/Word are extracted automatically (no paid APIs).
> Image OCR requires mockText for local MVP.
> Inventory is **only updated after owner confirmation**.

### Upload image (mockText required for local MVP)

```http
POST /api/v1/file-stock/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file     = bill-photo.jpg
mockText = Rice 25kg 60
           Sugar 10kg 45
           Oil 5L 140
```

### Upload TXT / CSV (extracted automatically)

```
file = stock-list.txt
```

TXT content:
```
Rice 25kg 60
Sugar 10 kg 45
Oil 5L 140
```

CSV content (also auto-extracted):
```
Rice,25,kg,60
Sugar,10,kg,45
Oil,5,l,140
```

### Upload Excel (.xlsx — extracted via Apache POI)

```
file = stock-sheet.xlsx
```

First sheet rows are converted to `Name Qty Unit Price` lines automatically.

### Upload PDF (digital text PDFs extracted via PDFBox)

```
file = supplier-bill.pdf
```

For scanned PDFs (no text layer), provide mockText or integrate Tesseract OCR.

### Upload Word document (.docx — extracted via Apache POI)

```
file = item-list.docx
```

### Supported line formats for parsing

| Format | Example |
|--------|---------|
| Name qty unit price | `Rice 25kg 60` |
| Name qty unit (space) price | `Sugar 10 kg 45` |
| Name qty unit ₹price | `Dal 20kg ₹120` |
| CSV columns | `Rice,25,kg,60` |
| Pipe-delimited | `Rice \| 25 \| kg \| 60` |
| No unit (fallback) | `Rice 25 60` |
| Packets unit | `Milk 10 packets 30` |

### Get entry

```http
GET /api/v1/file-stock/{id}
Authorization: Bearer <token>
```

### List entries

```http
GET /api/v1/file-stock
GET /api/v1/file-stock?status=PENDING_REVIEW
Authorization: Bearer <token>
```

### Confirm — update inventory

```http
POST /api/v1/file-stock/{id}/confirm
Authorization: Bearer <token>
Content-Type: application/json

{
  "updateExistingItems": true,
  "items": [
    { "itemName": "Rice",  "quantity": 25, "unit": "kg", "unitPrice": 60, "category": "Grocery" },
    { "itemName": "Sugar", "quantity": 10, "unit": "kg", "unitPrice": 45, "category": "Grocery" }
  ]
}
```

Omit `items` to use extracted items as-is.

### Cancel entry

```http
POST /api/v1/file-stock/{id}/cancel
Authorization: Bearer <token>

{ "reason": "Wrong file uploaded" }
```
