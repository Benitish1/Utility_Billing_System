# Utility Billing System Backend

Spring Boot backend for a utility billing system used to manage water/electricity customers, meters, meter readings, bills, payments, notifications, and audit logs.

## Tech Stack

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Spring Security with JWT
- Spring Data JPA
- Bean Validation
- JavaMail / Gmail SMTP
- Swagger / OpenAPI

## Main Features

- User signup, login, OTP verification, refresh token, and logout
- JWT authentication and role-based authorization
- Admin user management
- Customer profile management
- Meter management for water and electricity
- Meter reading capture by operator
- Tariff management
- Bill generation from meter readings, including VAT and penalty fields
- Payment recording and bill status updates
- Email and in-app notifications
- Audit logs for important system actions
- PostgreSQL backup included in `backups/`

## User Roles

### Admin

The admin controls the system setup.

Admin can:

- Create and manage users
- Create and manage customers
- Create and manage meters
- Create and manage tariffs, including price per unit, fixed charge, VAT percentage, and penalty percentage
- View audit logs
- Supervise bills and payments

### Operator

The operator captures customer meter readings.

Operator can:

- View customers and meters
- Capture monthly meter readings
- View readings

### Finance

The finance user handles billing and payments.

Finance can:

- Generate bills from meter readings
- View bills
- Record payments
- View payment history
- View customer notifications

### Customer

The customer views their own billing information.

Customer can:

- View own bills
- View own payments
- View own notifications
- Receive bill/payment emails

## System Flow

```text
Admin creates users, customers, meters, and tariffs
        |
Operator captures meter readings
        |
Finance generates bills from readings
        |
System calculates total bill amount
        |
System includes VAT and penalty amount in the bill structure
        |
System sends email and creates notification
        |
Finance records payment
        |
Customer views bills, payments, and notifications
        |
Admin reviews audit logs
```

## Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE utility_billing_db;
```

Expected local database configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/utility_billing_db
spring.datasource.username=postgres
spring.datasource.password=12345
```

## Running The Application

Use JDK 21.

From the project root:

```powershell
.\scripts\compile-jdk21.ps1
.\scripts\run-jdk21.ps1
```

Or run the main Spring Boot class from IntelliJ using JDK 21.

Application URLs:

```text
API Base URL: http://localhost:8080
Swagger UI:   http://localhost:8080/swagger-ui.html
Swagger UI alternative: http://localhost:8080/swagger-ui/index.html
OpenAPI JSON: http://localhost:8080/api-docs
```

## Swagger API Documentation

The backend is documented with Swagger/OpenAPI. After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger groups the API by feature:

- `Authentication` - signup, OTP verification, login, refresh token, logout, and current user.
- `Users` - admin-only user management.
- `Customers` - customer profile registration and management.
- `Meters` - water/electricity meter registration and lookup.
- `Meter Readings` - operator reading capture.
- `Tariffs` - admin tariff, VAT, and penalty configuration.
- `Billing` - bill generation, bill lookup, finance approval, and penalty application.
- `Payments` - customer, finance, or admin payment recording and payment history.
- `Notifications` - customer notifications and staff customer-notification lookup.
- `Audit Logs` - admin audit trail review.

For protected endpoints:

1. Run `POST /api/auth/login`.
2. Copy `data.accessToken` from the response.
3. Click the **Authorize** button in Swagger.
4. Paste the token like this:

```text
Bearer <accessToken>
```

Each protected Swagger endpoint includes an access note in the description, for example `Access: ROLE_ADMIN only` or `Access: ROLE_CUSTOMER only`.

## Seeded Demo Users

All seeded users use this password:

```text
Admin@123
```

```text
admin@wasac.rw      ROLE_ADMIN
operator@wasac.rw   ROLE_OPERATOR
finance@wasac.rw    ROLE_FINANCE
customer@wasac.rw   ROLE_CUSTOMER
```

## Main API Endpoints And Access

Authentication:

```text
POST /api/auth/signup         Public - customer signup with OTP
POST /api/auth/verify-otp     Public - verify account OTP
POST /api/auth/login          Public - returns JWT access and refresh tokens
POST /api/auth/refresh-token  Public - returns a new access token
POST /api/auth/logout         Authenticated users - blacklists current JWT
GET  /api/auth/me             Authenticated users - current user profile
```

Administration:

```text
POST /api/users               ROLE_ADMIN
GET  /api/users               ROLE_ADMIN
PATCH /api/users/{id}/status  ROLE_ADMIN
DELETE /api/users/{id}        ROLE_ADMIN
GET  /api/audit-logs          ROLE_ADMIN
```

Customer and meter management:

```text
POST /api/customers           ROLE_ADMIN, ROLE_FINANCE
GET  /api/customers           ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR
POST /api/meters              ROLE_ADMIN, ROLE_FINANCE
GET  /api/meters              ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR
```

Billing operations:

```text
POST /api/tariffs                         ROLE_ADMIN
POST /api/readings                        ROLE_OPERATOR
POST /api/bills/generate                  ROLE_ADMIN, ROLE_FINANCE
GET  /api/bills                           ROLE_ADMIN, ROLE_FINANCE
GET  /api/bills/my                        ROLE_CUSTOMER
POST /api/payments                        ROLE_CUSTOMER for own bills, ROLE_ADMIN, ROLE_FINANCE
GET  /api/payments/my                     ROLE_CUSTOMER
POST /api/bills/{id}/approve              ROLE_FINANCE
POST /api/bills/{billId}/apply-penalty    ROLE_ADMIN, ROLE_FINANCE
GET  /api/notifications/my                ROLE_CUSTOMER
```

## Important Demo Files

Use these files when testing or explaining the system:

- `postman/SYSTEM_FLOW_POSTMAN.md` - full testing flow for Postman.
- `postman/SYSTEM_FLOW_SWAGGER.md` - full testing flow for Swagger UI.
- `postman/MARKING_DEMO_FLOW.md` - detailed marking/demo sequence.
- `postman/POSTMAN_TESTING_GUIDE.md` - extra Postman request guide.

## Database Backup

The exported database backup is stored here:

```text
backups/utility_billing_db_backup_20260605_135425.sql
```

This file contains the database tables, data inserts, constraints, and sequence resets.

## Common API Flow

1. Admin configures tariffs for water/electricity.
2. Customer signs up and verifies the OTP sent by email.
3. Admin or finance creates the customer profile and links it to the verified user.
4. Admin or finance registers a meter for the customer.
5. Operator captures a monthly meter reading.
6. Finance generates a bill from the meter reading.
7. The system calculates consumption, VAT, total amount, due date, and sends a notification.
8. Customer views the bill using `GET /api/bills/my`.
9. Customer pays the bill using `POST /api/payments`.
10. Full payment changes the bill status to `PAID` and creates a payment notification.
11. Finance approves the fully paid bill using `POST /api/bills/{id}/approve`.
12. If an unpaid bill passes the due date, finance/admin applies a late penalty.
13. Customer views bills, payments, and notifications.
14. Admin reviews audit logs.

## Billing, VAT, And Penalty Logic

The bill is generated from a meter reading and the active tariff for that meter type.

The tariff contains:

- `pricePerUnit` - amount charged for each consumed unit
- `fixedCharge` - fixed service charge added to the bill
- `vatPercentage` - VAT rate applied to the bill
- `penaltyPercentage` - configured late-payment penalty rate (stored for future use)

The bill contains:

- `amountBeforeTax`
- `taxAmount`
- `penaltyAmount`
- `totalAmount`
- `amountPaid`
- `outstandingBalance`

Normal bill generation formula:

```text
unitsConsumed = currentReading - previousReading
amountBeforeTax = (unitsConsumed * pricePerUnit) + fixedCharge
taxAmount = amountBeforeTax * vatPercentage / 100
penaltyAmount = 0.00
totalAmount = amountBeforeTax + taxAmount + penaltyAmount
```

When the bill is first generated, `penaltyAmount` is `0.00` because the bill is not overdue yet.

## Validation And Business Rules

The API uses Bean Validation plus service-level business checks. Invalid requests return clear `400`, `401`, `403`, `404`, or `409` responses with a message explaining what went wrong.

Important validation rules:

- Emails must be lowercase and use a valid email format.
- Phone numbers must follow the configured Rwanda phone validation.
- Passwords must be strong: at least 8 characters with uppercase, lowercase, number, and symbol.
- Required request fields cannot be empty.
- Duplicate user/customer emails, phone numbers, national IDs, meter numbers, and payment transaction references are rejected.
- Customer national ID must be unique.
- Meter readings require `currentReading > previousReading`.
- Only one reading is allowed for the same meter in the same month/year.
- Inactive customers cannot receive bills.
- Inactive meters cannot be billed.
- Payment amount cannot exceed the bill outstanding balance.
- Customer users can pay only their own bills.
- Finance can approve only fully paid bills.

Tariff rules:

- `version` is required and must be unique per meter type.
- `effectiveFrom` must be today or a future date when creating a new tariff.
- The seeded app may already contain version `1`, so demos can use `version: 2`.
- Only one active tariff is kept per meter type; activating a new tariff deactivates the previous active one.

## Penalty Backend

Penalty is applied by **ADMIN** or **FINANCE** after the bill due date.

Endpoint:

```http
POST /api/bills/{billId}/apply-penalty
```

Access:

```text
ROLE_ADMIN
ROLE_FINANCE
```

Penalty calculation:

```text
penaltyAmount = outstandingBalance * penaltyPercentage / 100
newTotalAmount = oldTotalAmount + penaltyAmount
newOutstandingBalance = oldOutstandingBalance + penaltyAmount
status = OVERDUE
```

For demo/testing before the due date, use:

```http
POST /api/bills/{billId}/apply-penalty?force=true
```

What the backend does:

- Finds the bill by ID.
- Rejects fully paid bills.
- Rejects bills that already have a penalty.
- Uses the active tariff for the bill meter type.
- Reads `penaltyPercentage` from the tariff.
- Calculates and saves `penaltyAmount`.
- Updates `totalAmount` and `outstandingBalance`.
- Changes bill status to `OVERDUE`.
- Sends an email to the customer.
- Writes an audit log entry.

During marking, explain it like this:

```text
The admin configures penalty percentage on the tariff.
Finance or admin applies penalty to an overdue unpaid bill.
The backend calculates the penalty from the outstanding balance and tariff penalty percentage.
Then it updates penalty amount, total amount, outstanding balance, and bill status to OVERDUE.
```

## Common Errors

`403 Forbidden`

The logged-in user does not have the correct role for that endpoint.

`Bill already generated for this reading`

Each meter reading can generate only one bill.

`A reading already exists for this meter in the specified billing month/year`

Only one reading is allowed per meter per month and year.

`No active tariff found`

Create or activate a tariff for the meter type and billing period before generating a bill.

## Short Marking Explanation

This system manages the full utility billing process. Admin prepares users, customers, meters, and tariffs including VAT and penalty percentage configuration. Operator captures consumption readings. Finance generates bills with base amount, VAT, and total amount, then records payments. Customer views their own bills, payments, and notifications. The system uses JWT and role-based authorization for security, sends email notifications, stores in-app notifications, and records important actions in audit logs.
