# Utility Billing System - Clean Marking Demo Flow

Use this exact order when testing in Postman or Swagger. It avoids 400/403 errors by using the correct role at the correct time.

Base URL:

```text
http://localhost:8080
```

Seeded staff users:

```text
admin@wasac.rw      Admin@123
operator@wasac.rw   Admin@123
finance@wasac.rw    Admin@123
customer@wasac.rw   Admin@123
```

## Important Testing Rules

- In Postman, protected requests need `Authorization: Bearer <accessToken>`.
- In Swagger, click `Authorize` and paste `Bearer <accessToken>`.
- Do not reuse the same `meterNumber`, `transactionReference`, `email`, phone, or national ID.
- Do not capture two readings for the same meter in the same month/year.
- `POST /api/readings` is only for `ROLE_OPERATOR`.
- `GET /api/bills/my`, `GET /api/payments/my`, and `GET /api/notifications/my` are only for `ROLE_CUSTOMER`.

## What Is Better: Seeded Staff or Admin Creates Staff?

For demo/marking, seeded `ADMIN`, `OPERATOR`, and `FINANCE` users are better because the examiner can test every role immediately.

For a real production system, the admin should create or activate staff accounts. Customers can self-signup with OTP.

This project supports both testing styles:

- seeded staff accounts for fast demo
- public signup + OTP for customer account verification
- admin can view users and activate/deactivate them

## Phase 1 - Customer Self-Signup and OTP

### 1. Signup Customer

Public endpoint.

```http
POST /api/auth/signup
```

Use a new email and phone each time.

```json
{
  "fullName": "Alice Demo Customer",
  "email": "alice.demo@example.com",
  "phoneNumber": "+250788111222",
  "password": "Password@123",
  "role": "ROLE_CUSTOMER"
}
```

Expected: `200` or `201`, user created, OTP sent by email.

Copy:

```text
data.id = customerUserId
```

### 2. Verify OTP

Check the email inbox for OTP.

```http
POST /api/auth/verify-otp
```

```json
{
  "email": "alice.demo@example.com",
  "otpCode": "123456"
}
```

Replace `123456` with the actual OTP.

### 3. Resend OTP if Needed

```http
POST /api/auth/resend-otp
```

```json
{
  "email": "alice.demo@example.com"
}
```

### 4. Login New Customer

```http
POST /api/auth/login
```

```json
{
  "email": "alice.demo@example.com",
  "password": "Password@123"
}
```

This proves OTP verification works. This customer will not see bills yet until admin links a customer profile.

## Phase 2 - Admin Flow

### 5. Login Admin

```http
POST /api/auth/login
```

```json
{
  "email": "admin@wasac.rw",
  "password": "Admin@123"
}
```

Copy:

```text
data.accessToken = adminToken
```

Use `adminToken` for the next admin requests.

### 6. Check Current Admin

```http
GET /api/auth/me
```

Expected role:

```text
ROLE_ADMIN
```

### 6A. Admin Creates Staff or Another Admin

Admin can create `ROLE_OPERATOR`, `ROLE_FINANCE`, `ROLE_CUSTOMER`, or another `ROLE_ADMIN`.

```http
POST /api/users
```

Create operator:

```json
{
  "fullName": "Demo Operator",
  "email": "demo.operator@example.com",
  "phoneNumber": "+250788222333",
  "password": "Password@123",
  "role": "ROLE_OPERATOR"
}
```

Create finance:

```json
{
  "fullName": "Demo Finance",
  "email": "demo.finance@example.com",
  "phoneNumber": "+250788222334",
  "password": "Password@123",
  "role": "ROLE_FINANCE"
}
```

Create another admin:

```json
{
  "fullName": "Second Admin",
  "email": "second.admin@example.com",
  "phoneNumber": "+250788222335",
  "password": "Password@123",
  "role": "ROLE_ADMIN"
}
```

Create customer user directly:

```json
{
  "fullName": "Admin Created Customer",
  "email": "admin.customer@example.com",
  "phoneNumber": "+250788222336",
  "password": "Password@123",
  "role": "ROLE_CUSTOMER"
}
```

Users created by admin are immediately active and email-verified, so they can login right away.

### 7. List Users

```http
GET /api/users
```

Find the signed-up customer:

```text
alice.demo@example.com
```

Copy their user ID as:

```text
customerUserId
```

### 8. Create Customer Profile Linked to User

```http
POST /api/customers
```

Use the `customerUserId` from `GET /api/users`.

```json
{
  "fullNames": "Alice Demo Customer",
  "nationalId": "1199080011223344",
  "email": "alice.demo@example.com",
  "phoneNumber": "+250788111222",
  "address": "KG 10 Ave, Kigali",
  "status": "ACTIVE",
  "userId": 5
}
```

Replace `5` with the real `customerUserId`.

Copy:

```text
data.id = customerId
```

### 9. Create Water Meter for Customer

```http
POST /api/meters
```

Use the `customerId` from step 8.

```json
{
  "meterNumber": "WTR-DEMO-001",
  "meterType": "WATER",
  "installationDate": "2025-01-15",
  "status": "ACTIVE",
  "customerId": 1
}
```

Replace `1` with real `customerId`.

Copy:

```text
data.id = meterId
```

### 10. View Admin Data

```http
GET /api/customers
GET /api/customers/{customerId}
GET /api/meters
GET /api/meters/{meterId}
GET /api/meters/customer/{customerId}
GET /api/tariffs
GET /api/users/{customerUserId}
```

### 11. Optional Admin Creates New Tariff

Use a future/current date. Version must be new for the meter type.

```http
POST /api/tariffs
```

```json
{
  "meterType": "WATER",
  "tariffType": "FLAT",
  "pricePerUnit": 400.00,
  "fixedCharge": 2000.00,
  "vatPercentage": 18.00,
  "penaltyPercentage": 5.00,
  "version": 2,
  "effectiveFrom": "2026-06-05",
  "active": true
}
```

If version `2` already exists, change it to `3`.

## Phase 3 - Operator Flow

### 12. Login Operator

```http
POST /api/auth/login
```

```json
{
  "email": "operator@wasac.rw",
  "password": "Admin@123"
}
```

Copy:

```text
data.accessToken = operatorToken
```

### 13. Operator Views Meters and Customers

```http
GET /api/customers
GET /api/meters
GET /api/meters/{meterId}
```

### 14. Capture Reading

```http
POST /api/readings
```

Use the `meterId` created by admin.

```json
{
  "meterId": 1,
  "previousReading": 100.00,
  "currentReading": 145.50,
  "readingDate": "2026-06-05",
  "billingMonth": 6,
  "billingYear": 2026
}
```

Replace `1` with the real `meterId`.

Copy:

```text
data.id = readingId
```

If you get duplicate reading error, change `billingMonth` or create another meter.

### 15. Operator Views Readings

```http
GET /api/readings
GET /api/readings/{readingId}
```

## Phase 4 - Finance Flow

### 16. Login Finance

```http
POST /api/auth/login
```

```json
{
  "email": "finance@wasac.rw",
  "password": "Admin@123"
}
```

Copy:

```text
data.accessToken = financeToken
```

### 17. Finance Reviews Data

```http
GET /api/customers
GET /api/meters
GET /api/readings
GET /api/tariffs
```

### 18. Generate Bill

```http
POST /api/bills/generate
```

Use the `readingId` from step 14.

```json
{
  "readingId": 1
}
```

Replace `1` with the real `readingId`.

Copy:

```text
data.id = billId
data.billNumber = billNumber
data.outstandingBalance = outstandingBalance
```

Expected notification behavior:

```text
notification inserted in database
bill email sent to the customer email address
```

### 19. View Bills

```http
GET /api/bills
GET /api/bills/{billId}
GET /api/bills/number/{billNumber}
```

### 20. Record Partial Payment

```http
POST /api/payments
```

```json
{
  "billId": 1,
  "amountPaid": 5000.00,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-06-05",
  "transactionReference": "MM-DEMO-001"
}
```

Replace `1` with the real `billId`.

### 21. Get Bill Again

```http
GET /api/bills/{billId}
```

Copy the new:

```text
data.outstandingBalance
```

### 22. Record Full Remaining Payment

Use exactly the outstanding balance from step 21.

```http
POST /api/payments
```

```json
{
  "billId": 1,
  "amountPaid": 16151.50,
  "paymentMethod": "BANK_TRANSFER",
  "paymentDate": "2026-06-05",
  "transactionReference": "BANK-DEMO-001"
}
```

Replace:

- `billId`
- `amountPaid`
- transaction reference if already used

Expected:

```text
bill status = PAID
outstandingBalance = 0
notification inserted
payment confirmation email sent to the customer email address
```

### 23. View Payments and Notifications

```http
GET /api/payments
GET /api/payments/bill/{billId}
GET /api/notifications/customer/{customerId}
```

### 23A. Admin Views Audit Logs

Switch back to the admin token.

```http
GET /api/audit-logs
```

Filter logs by actor email:

```http
GET /api/audit-logs/actor/finance@wasac.rw
GET /api/audit-logs/actor/operator@wasac.rw
GET /api/audit-logs/actor/admin@wasac.rw
```

Filter logs by entity:

```http
GET /api/audit-logs/entity/Customer/{customerId}
GET /api/audit-logs/entity/Meter/{meterId}
GET /api/audit-logs/entity/MeterReading/{readingId}
GET /api/audit-logs/entity/Bill/{billId}
```

Expected audit actions include:

```text
CREATE_USER
CREATE_CUSTOMER
CREATE_METER
CREATE_TARIFF
CAPTURE_READING
GENERATE_BILL
RECORD_PAYMENT
```

## Phase 5 - Customer Flow

### 24. Login Customer

Use the customer created in phase 1.

```http
POST /api/auth/login
```

```json
{
  "email": "alice.demo@example.com",
  "password": "Password@123"
}
```

Copy:

```text
data.accessToken = customerToken
```

### 25. Customer Views Own Data

```http
GET /api/auth/me
GET /api/bills/my
GET /api/payments/my
GET /api/notifications/my
```

Customer should only see their own bills, payments, and notifications.

## Phase 6 - Logout and Refresh Token

### 26. Refresh Token

Use any login response `data.refreshToken`.

```http
POST /api/auth/refresh-token
```

```json
{
  "refreshToken": "PASTE_REFRESH_TOKEN_HERE"
}
```

### 27. Logout

Use the current access token in the Authorization header.

```http
POST /api/auth/logout
```

No body.

After logout, the same access token should no longer work.

## Common Errors and Fixes

### 400 Duplicate email/phone/national ID

Use a new value.

### 400 Duplicate meter number

Change `meterNumber`, for example:

```text
WTR-DEMO-002
```

### 400 Reading already exists

Change `billingMonth`, `billingYear`, or use another meter.

### 400 Overpayment

Use the exact `outstandingBalance`.

### 403 Forbidden

You are using the wrong role token. Switch token:

- readings: operator
- bill generation/payment: finance or admin
- own bills/payments/notifications: customer

### 401 Unauthorized

Token missing, expired, malformed, or already logged out.
