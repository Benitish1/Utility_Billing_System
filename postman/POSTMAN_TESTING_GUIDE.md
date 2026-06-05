# Utility Billing System - Postman Testing Guide

Base URL:

```text
http://localhost:8080
```

Create Postman variables:

```text
baseUrl = http://localhost:8080
accessToken = <paste token here after login>
```

For protected endpoints, add headers:

```text
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

Seeded users use this password:

```text
Admin@123
```

## 1. Login as Admin

```http
POST {{baseUrl}}/api/auth/login
```

```json
{
  "email": "admin@wasac.rw",
  "password": "Admin@123"
}
```

Copy `data.accessToken` into `accessToken`.

## 2. Check Current User

```http
GET {{baseUrl}}/api/auth/me
```

## 3. List Users

Access: `ROLE_ADMIN`

```http
GET {{baseUrl}}/api/users
```

## 4. Create Customer

Access: `ROLE_ADMIN` or `ROLE_FINANCE`

```http
POST {{baseUrl}}/api/customers
```

```json
{
  "fullNames": "Marie Uwase",
  "nationalId": "1199080098765432",
  "email": "marie.uwase@example.com",
  "phoneNumber": "+250788654321",
  "address": "KN 5 St, Kigali",
  "status": "ACTIVE"
}
```

Save returned `data.id` as `customerId`.

## 5. Create Meter

Access: `ROLE_ADMIN` or `ROLE_FINANCE`

```http
POST {{baseUrl}}/api/meters
```

```json
{
  "meterNumber": "WTR-TEST-001",
  "meterType": "WATER",
  "installationDate": "2025-01-15",
  "status": "ACTIVE",
  "customerId": 1
}
```

If seeded data is present, you can also use existing meter:

```text
meterId = 1
meterNumber = WTR-SEED-001
```

## 6. Login as Operator

```http
POST {{baseUrl}}/api/auth/login
```

```json
{
  "email": "operator@wasac.rw",
  "password": "Admin@123"
}
```

Copy `data.accessToken` into `accessToken`.

## 7. Capture Meter Reading

Access: `ROLE_OPERATOR`

```http
POST {{baseUrl}}/api/readings
```

Use a billing month/year that is not in the future.

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

Save returned `data.id` as `readingId`.

## 8. Login as Finance

```http
POST {{baseUrl}}/api/auth/login
```

```json
{
  "email": "finance@wasac.rw",
  "password": "Admin@123"
}
```

Copy `data.accessToken` into `accessToken`.

## 9. Generate Bill

Access: `ROLE_ADMIN` or `ROLE_FINANCE`

```http
POST {{baseUrl}}/api/bills/generate
```

```json
{
  "readingId": 1
}
```

Save returned:

```text
billId = data.id
billNumber = data.billNumber
outstandingBalance = data.outstandingBalance
```

## 10. Get Bill

```http
GET {{baseUrl}}/api/bills/1
```

or:

```http
GET {{baseUrl}}/api/bills/number/BILL-202606-000001
```

## 11. Record Partial Payment

Access: `ROLE_ADMIN` or `ROLE_FINANCE`

```http
POST {{baseUrl}}/api/payments
```

```json
{
  "billId": 1,
  "amountPaid": 5000.00,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-06-05",
  "transactionReference": "MM-TEST-001"
}
```

## 12. Record Full Remaining Payment

Use the exact current `outstandingBalance` from the bill response.

```http
POST {{baseUrl}}/api/payments
```

```json
{
  "billId": 1,
  "amountPaid": 16151.50,
  "paymentMethod": "BANK_TRANSFER",
  "paymentDate": "2026-06-05",
  "transactionReference": "BANK-TEST-001"
}
```

When the bill is fully paid:

- bill status becomes `PAID`
- outstanding balance becomes `0`
- notification is inserted
- confirmation email is sent

## 13. Login as Customer

```http
POST {{baseUrl}}/api/auth/login
```

```json
{
  "email": "customer@wasac.rw",
  "password": "Admin@123"
}
```

Copy `data.accessToken` into `accessToken`.

## 14. Customer Views Own Data

```http
GET {{baseUrl}}/api/bills/my
```

```http
GET {{baseUrl}}/api/payments/my
```

```http
GET {{baseUrl}}/api/notifications/my
```

## 15. Logout

```http
POST {{baseUrl}}/api/auth/logout
```

After logout, reuse the same token on `GET /api/auth/me`; it should no longer authenticate.

## Validation Tests

Invalid uppercase email:

```json
{
  "email": "Admin@wasac.rw",
  "password": "Admin@123"
}
```

Invalid phone:

```json
{
  "phoneNumber": "12345"
}
```

Invalid national ID:

```json
{
  "nationalId": "1111111111111111"
}
```

Overpayment:

```json
{
  "billId": 1,
  "amountPaid": 999999999,
  "paymentMethod": "CASH",
  "paymentDate": "2026-06-05"
}
```
