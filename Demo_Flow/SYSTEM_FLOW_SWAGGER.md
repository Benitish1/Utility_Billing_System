# Utility Billing System - Swagger System Flow

Open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

When using protected endpoints, click `Authorize` and paste:

```text
Bearer <accessToken>
```

Seeded staff users:

```text
admin@wasac.rw      Admin@123
operator@wasac.rw   Admin@123
finance@wasac.rw    Admin@123
customer@wasac.rw   Admin@123
```

## 1. Public Authentication

In `Authentication`, run `POST /api/auth/signup` with a customer email in lowercase and a strong password:

```json
{
  "fullName": "Alice Swagger Customer",
  "email": "alice.swagger@example.com",
  "phoneNumber": "+250788222333",
  "password": "Password@123",
  "role": "ROLE_CUSTOMER"
}
```

Run `POST /api/auth/verify-otp` using the OTP received by email:

```json
{
  "email": "alice.swagger@example.com",
  "otpCode": "123456"
}
```

Run `POST /api/auth/login`, copy `data.accessToken`, click `Authorize`, and paste the token as `Bearer <token>`.

## 2. Admin Configuration

Login as `admin@wasac.rw`, authorize with the admin token, then use `Tariffs`.

Run `POST /api/tariffs`.

Access shown in Swagger: `ROLE_ADMIN only`.

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

Use `version: 2` if version `1` was already seeded. `effectiveFrom` must be today or later.

## 3. Admin Customer and Meter Setup

Use `Customers` then run `POST /api/customers`.

Access shown in Swagger: `ROLE_ADMIN, ROLE_FINANCE`.

```json
{
  "fullNames": "Alice Swagger Customer",
  "nationalId": "1199080012345679",
  "email": "alice.swagger@example.com",
  "phoneNumber": "+250788222333",
  "address": "Kigali, Rwanda",
  "status": "ACTIVE",
  "userId": 1
}
```

Copy the returned `data.id` as `customerId`.

Use `Meters` then run `POST /api/meters`.

Access shown in Swagger: `ROLE_ADMIN, ROLE_FINANCE`.

```json
{
  "customerId": 1,
  "meterNumber": "WTR-2026-0002",
  "meterType": "WATER",
  "installationDate": "2026-01-15",
  "status": "ACTIVE"
}
```

Copy the returned `data.id` as `meterId`.

## 4. Operator Reading Capture

Login as `operator@wasac.rw`, click `Authorize`, and paste the operator token.

Use `Meter Readings` then run `POST /api/readings`.

Access shown in Swagger: `ROLE_OPERATOR only`.

```json
{
  "meterId": 1,
  "previousReading": 100,
  "currentReading": 145,
  "readingDate": "2026-06-05",
  "billingMonth": 6,
  "billingYear": 2026
}
```

Copy the returned `data.id` as `readingId`.

## 5. Finance Bill Generation

Login as `finance@wasac.rw`, click `Authorize`, and paste the finance token.

Use `Billing` then run `POST /api/bills/generate`.

Access shown in Swagger: `ROLE_ADMIN, ROLE_FINANCE`.

```json
{
  "readingId": 1
}
```

Expected result: a `PENDING` bill is created and a customer notification is inserted.

If the bill was created before SMTP email worked, use `Billing` then run `POST /api/bills/{id}/resend-email` with the finance or admin token. This resends the bill email without creating another bill.

## 6. Customer Bill, Payment, and Notifications

Login as the customer, click `Authorize`, and paste the customer token.

Use `Billing` then run `GET /api/bills/my`.

Access shown in Swagger: `ROLE_CUSTOMER only`.

Use `Notifications` then run `GET /api/notifications/my` to confirm the generated bill message.

Use `Payments` then run `POST /api/payments`.

Access shown in Swagger: `ROLE_CUSTOMER for own bills, ROLE_ADMIN and ROLE_FINANCE for any bill`.

```json
{
  "billId": 1,
  "amountPaid": 54100,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-06-05",
  "transactionReference": "SWAGGER-MTN-2026-0001"
}
```

Expected result: full payment sets bill status to `PAID` and sends a payment notification.

## 7. Finance Approval

Login as finance again and authorize with the finance token.

Use `Billing` then run `POST /api/bills/{id}/approve`.

Access shown in Swagger: `ROLE_FINANCE only`.

Expected result: only a fully paid bill can be approved. Status becomes `APPROVED`, and the customer receives an approval notification.

## 8. Late Penalty

Use a different unpaid bill for this test. Finance or admin can apply a penalty after the due date.

For a demo before due date, run:

```http
POST /api/bills/{billId}/apply-penalty?force=true
```

Access shown in Swagger: `ROLE_ADMIN, ROLE_FINANCE`.

Expected result: status becomes `OVERDUE`, outstanding balance increases, and the customer can see the penalty in `GET /api/notifications/my`.

## 9. Logout

Use `Authentication` then run `POST /api/auth/logout` while authorized.

Access shown in Swagger: authenticated users only.

Expected result: the token is blacklisted and cannot be used again.
