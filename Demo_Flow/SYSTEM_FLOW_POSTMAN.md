# Utility Billing System - Postman System Flow

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

For protected requests, add:

```text
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

Use new values each time for `email`, `phoneNumber`, `nationalId`, `meterNumber`, and `transactionReference`.

## 1. Customer Signup and OTP Verification

Public signup sends an OTP and creates an inactive/unverified account until OTP verification succeeds.

```http
POST {{baseUrl}}/api/auth/signup
```

```json
{
  "fullName": "Alice Demo Customer",
  "email": "alice.demo@example.com",
  "phoneNumber": "+250788111222",
  "password": "Password@123",
  "role": "ROLE_CUSTOMER"
}
```

Verify the OTP sent to email:

```http
POST {{baseUrl}}/api/auth/verify-otp
```

```json
{
  "email": "alice.demo@example.com",
  "otpCode": "123456"
}
```

Login and save `data.accessToken` as `customerToken`:

```http
POST {{baseUrl}}/api/auth/login
```

```json
{
  "email": "alice.demo@example.com",
  "password": "Password@123"
}
```

## 2. Admin Sets Tariffs

Login as admin and save token as `adminToken`.

```http
POST {{baseUrl}}/api/auth/login
```

```json
{
  "email": "admin@wasac.rw",
  "password": "Admin@123"
}
```

Create an active tariff. Repeat for `ELECTRICITY` if needed.

```http
POST {{baseUrl}}/api/tariffs
Authorization: Bearer {{adminToken}}
```

```json
{
  "meterType": "WATER",
  "tariffType": "FLAT",
  "pricePerUnit": 500,
  "fixedCharge": 1000,
  "vatPercentage": 18,
  "penaltyPercentage": 10,
  "version": 2,
  "effectiveFrom": "2026-06-05",
  "active": true
}
```

Important:
- `version` is required. Use `2` if the app already seeded version `1` on startup.
- `effectiveFrom` must be **today or a future date**, not a past date like `2026-01-01`.

If the app already started once, tariffs may already exist from the seeder. You can skip this step and continue from step 3, or verify with:

```http
GET {{baseUrl}}/api/tariffs
Authorization: Bearer {{adminToken}}
```

## 3. Admin Registers Customer Profile

Link the verified signup user by passing the returned `userId`.

```http
POST {{baseUrl}}/api/customers
Authorization: Bearer {{adminToken}}
```

```json
{
  "fullNames": "Alice Demo Customer",
  "nationalId": "1199080012345678",
  "email": "alice.demo@example.com",
  "phoneNumber": "+250788111222",
  "address": "Kigali, Rwanda",
  "status": "ACTIVE",
  "userId": 1
}
```

Save `data.id` as `customerId`.

## 4. Admin Registers Meter

```http
POST {{baseUrl}}/api/meters
Authorization: Bearer {{adminToken}}
```

```json
{
  "customerId": {{customerId}},
  "meterNumber": "WTR-2026-0001",
  "meterType": "WATER",
  "installationDate": "2026-01-15",
  "status": "ACTIVE"
}
```

Save `data.id` as `meterId`.

## 5. Operator Captures Meter Reading

Login as operator and save token as `operatorToken`.

```http
POST {{baseUrl}}/api/readings
Authorization: Bearer {{operatorToken}}
```

```json
{
  "meterId": {{meterId}},
  "previousReading": 100,
  "currentReading": 145,
  "readingDate": "2026-06-05",
  "billingMonth": 6,
  "billingYear": 2026
}
```

Save `data.id` as `readingId`.

## 6. Finance Generates Bill

Login as finance and save token as `financeToken`.

```http
POST {{baseUrl}}/api/bills/generate
Authorization: Bearer {{financeToken}}
```

```json
{
  "readingId": {{readingId}}
}
```

Expected result: bill status is `PENDING`, outstanding balance is calculated, and a notification is created for the customer.

Save `data.id` as `billId`.

If the email failed because SMTP was not configured yet, fix `application.properties`, restart the app, then resend the existing bill email:

```http
POST {{baseUrl}}/api/bills/{{billId}}/resend-email
Authorization: Bearer {{financeToken}}
```

## 7. Customer Views Bill and Notifications

```http
GETcc
Authorization: Bearer {{customerToken}}
```

```http
GET {{baseUrl}}/api/notifications/my
Authorization: Bearer {{customerToken}}
```

## 8. Customer Pays Bill

Customers can pay only their own bills. Partial payment changes bill status to `PARTIALLY_PAID`; full payment changes it to `PAID` and sends a payment notification.

```http
POST {{baseUrl}}/api/payments
Authorization: Bearer {{customerToken}}
```

```json
{
  "billId": {{billId}},
  "amountPaid": 54100,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-06-05",
  "transactionReference": "MTN-2026-0001"
}
```

## 9. Finance Approves Fully Paid Bill

Finance approval is allowed only when outstanding balance is zero and status is `PAID`.

```http
POST {{baseUrl}}/api/bills/{{billId}}/approve
Authorization: Bearer {{financeToken}}
```

Expected result: bill status becomes `APPROVED`, and the customer receives an approval notification.

## 10. Late Payment Penalty Flow

Use this on a different unpaid bill after the due date. For demos only, add `?force=true` to apply before the due date.

```http
POST {{baseUrl}}/api/bills/{{billId}}/apply-penalty?force=true
Authorization: Bearer {{financeToken}}
```

Expected result: one penalty is applied, status becomes `OVERDUE`, outstanding balance increases, and a notification is saved for the customer.

## 11. Sign Out

```http
POST {{baseUrl}}/api/auth/logout
Authorization: Bearer {{accessToken}}
```

After logout, the same JWT should no longer work for protected endpoints.
