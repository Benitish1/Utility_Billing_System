# Utility Billing System - Explanation Guide for Marking

Use this file when explaining the project to a marker. It focuses on the system flow, what each role does, and how the parts are connected.

## 1. System Overview

This project is a backend system for managing utility billing for water and electricity customers. It supports the full flow from user authentication, customer registration, meter management, meter reading capture, bill generation, payment recording, notifications, and audit logging.

The system uses role-based access control, so each user can only perform actions allowed for their role.

Main roles:

- `ADMIN`
- `OPERATOR`
- `FINANCE`
- `CUSTOMER`

Main business objects:

- `User` - login account used for authentication
- `Customer` - customer profile connected to a user
- `Meter` - physical water or electricity meter assigned to a customer
- `MeterReading` - monthly reading captured from a meter
- `Tariff` - price rules used to calculate bills
- `Bill` - generated charge for a customer based on reading and tariff
- `Payment` - money paid against a bill
- `Notification` - message sent or stored for the customer
- `AuditLog` - record of important actions done in the system

## 2. Authentication and Security Flow

Every user logs in using email and password.

For protected endpoints, the user must send a JWT token:

```text
Authorization: Bearer <accessToken>
```

Customers can sign up publicly, but they must verify their account using an OTP sent to email.

Staff users such as admin, operator, and finance are created by the admin or seeded for demo purposes.

The system uses:

- JWT authentication
- Role-based access control
- OTP email verification
- Logout with token blacklist
- Validation on request data

## 3. Admin Role

The admin manages the setup and control of the system.

The admin can:

- Create staff users such as operator, finance, admin, or customer accounts
- View all users
- Activate or deactivate users
- Create and manage customers
- Create and manage meters
- Create and manage tariffs
- Generate bills if needed
- Record payments if needed
- View audit logs

How admin fits in the flow:

1. Admin logs in.
2. Admin creates or verifies users.
3. Admin creates a customer profile.
4. Admin assigns a meter to the customer.
5. Admin creates tariffs for water or electricity.
6. Other roles then continue the operational flow.

Important explanation:

The admin is responsible for system configuration and supervision. Without customers, meters, and tariffs, the operator and finance roles cannot complete their work.

## 4. Operator Role

The operator is responsible for capturing meter readings.

The operator can:

- View customers and meters
- Capture meter readings
- View readings

The operator cannot:

- Generate bills
- Record payments
- Manage users
- Manage tariffs

How operator fits in the flow:

1. Admin creates a customer.
2. Admin assigns a meter to that customer.
3. Operator visits or checks the meter.
4. Operator records the previous reading, current reading, reading date, billing month, and billing year.
5. The system calculates units consumed.

Example:

```text
Current reading - Previous reading = Units consumed
180 - 145.5 = 34.5 units
```

Important explanation:

The operator provides the consumption data. Finance cannot generate a correct bill until a meter reading exists.

## 5. Finance Role

The finance user handles billing and payments.

The finance user can:

- View customers and meters
- Generate bills from readings
- View bills
- Record customer payments
- View payments
- View customer notifications

The finance user cannot:

- Capture readings
- Manage users
- Manage tariffs

How finance fits in the flow:

1. Finance logs in.
2. Finance selects a meter reading.
3. Finance generates a bill using the reading.
4. The system finds the active tariff for that meter type.
5. The system calculates the bill amount.
6. The system sends an email notification to the customer.
7. The system stores an in-app notification for the customer.
8. When the customer pays, finance records the payment.
9. The bill status changes depending on the amount paid.

Bill calculation uses:

```text
amountBeforeTax = unitsConsumed * pricePerUnit + fixedCharge
taxAmount = amountBeforeTax * VAT percentage
totalAmount = amountBeforeTax + taxAmount + penaltyAmount
```

Bill statuses:

- `PENDING` - no payment yet
- `PARTIALLY_PAID` - some amount has been paid
- `PAID` - full amount has been paid

Important explanation:

Finance converts meter readings into official bills and records money received from customers.

## 6. Customer Role

The customer is the final user of the system.

The customer can:

- Log in after OTP verification
- View their own bills
- View their own payments
- View their own notifications
- View their account details

The customer cannot:

- Create meters
- Capture readings
- Generate bills
- Record payments
- View other customers' data

How customer fits in the flow:

1. Customer signs up or is created by admin.
2. Customer verifies email using OTP.
3. Admin links the customer account to a customer profile.
4. Admin assigns a meter to the customer.
5. Operator records readings for the meter.
6. Finance generates a bill.
7. Customer receives email and notification.
8. Customer can view the bill and payment history.

Important explanation:

The customer only sees their own information. This proves data privacy and role-based security.

## 7. Full System Flow

This is the complete business process:

```text
Admin creates users
        |
Admin creates customer profile
        |
Admin assigns meter to customer
        |
Admin creates active tariff
        |
Operator captures monthly meter reading
        |
System calculates units consumed
        |
Finance generates bill from reading
        |
System calculates total amount using tariff
        |
System sends bill email and notification to customer
        |
Finance records payment
        |
System updates bill status
        |
Customer views bills, payments, and notifications
        |
Admin can review audit logs
```

## 8. How Main Entities Are Related

The relationship between the main data models is:

```text
User 1 ---- 1 Customer
Customer 1 ---- many Meters
Meter 1 ---- many MeterReadings
MeterReading 1 ---- 1 Bill
Customer 1 ---- many Bills
Bill 1 ---- many Payments
Customer 1 ---- many Notifications
User 1 ---- many AuditLogs
```

Explanation:

- A user account is used to log in.
- A customer profile stores customer billing details.
- A customer can have one or more meters.
- A meter can have many monthly readings.
- Each reading can generate one bill only.
- A bill can have one or more payments.
- Notifications belong to the customer.
- Audit logs track important actions.

## 9. Notifications and Email

The system supports two types of customer notification:

- Email notification
- In-app notification stored in the database

When a bill is generated:

- An email is sent to the customer with subject `Utility Bill Generated`.
- A notification is stored for the customer.

When a bill is fully paid:

- A payment confirmation email is sent.
- A payment notification is stored.

Important explanation:

Notifications prove that customers are informed when billing or payment events happen.

## 10. Audit Logging

Audit logs record important system actions.

Examples:

- Admin creates a user
- Admin creates a customer
- Admin creates a meter
- Admin creates a tariff
- Operator captures a reading
- Finance generates a bill
- Finance records a payment

Important explanation:

Audit logs help administrators trace who did what and when. This is important for accountability in a billing system.

## 11. What To Say During Marking

You can explain the system like this:

```text
My system is a utility billing backend for water and electricity.
It has four roles: admin, operator, finance, and customer.

The admin prepares the system by creating users, customers, meters, and tariffs.
The operator captures monthly meter readings.
The finance user generates bills from readings and records payments.
The customer logs in to view their own bills, payments, and notifications.

The system calculates units consumed from meter readings, applies the active tariff,
adds VAT, generates a bill, sends an email notification, and stores an in-app notification.

Every important action is saved in audit logs, and access is protected using JWT and role-based authorization.
```

## 12. Demo Order To Follow

Use this order during marking:

1. Login as admin.
2. Show users, customers, meters, and tariffs.
3. Login as operator.
4. Capture or show a meter reading.
5. Login as finance.
6. Generate a bill from a reading.
7. Show that email or notification is created.
8. Record payment for the bill.
9. Login as customer.
10. Show customer bills, payments, and notifications.
11. Login as admin again.
12. Show audit logs.

## 13. Common Errors To Explain

If you get `403 Forbidden`:

```text
The logged-in user does not have permission for that endpoint.
Use the correct role.
```

If you get `Bill already generated for this reading`:

```text
Each meter reading can only generate one bill.
Use another reading.
```

If you get `A reading already exists for this meter in the specified billing month/year`:

```text
The system allows only one reading per meter per month and year.
Use a different month/year or another meter.
```

If you get `No active tariff found`:

```text
The system needs an active tariff for the meter type and billing period before generating a bill.
Create or activate a tariff first.
```

## 14. Short Final Summary

The system works like this:

```text
Admin sets up the system.
Operator captures consumption.
Finance bills and records payments.
Customer views their own billing information.
Notifications inform the customer.
Audit logs track all important actions.
JWT and roles protect the system.
```
