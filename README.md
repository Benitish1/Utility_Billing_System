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
```

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

## Important Demo Files

Use these files when testing or explaining the system:

- `Demo_Flow/SYSTEM_FLOW_EXPLANATION_FOR_MARKING.md`
- `Demo_Flow/MARKING_DEMO_FLOW.md`
- `Demo_Flow/POSTMAN_TESTING_GUIDE.md`

## Database Backup

The exported database backup is stored here:

```text
backups/utility_billing_db_backup_20260605_135425.sql
```

This file contains the database tables, data inserts, constraints, and sequence resets.

## Common API Flow

1. Login as admin.
2. Create or view customers.
3. Create or view meters.
4. Create or view tariffs.
5. Login as operator.
6. Capture a meter reading.
7. Login as finance.
8. Generate a bill from the reading.
9. Record a payment.
10. Login as customer.
11. View own bills, payments, and notifications.
12. Login as admin.
13. View audit logs.

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
