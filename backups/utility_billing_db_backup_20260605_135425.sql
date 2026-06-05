-- Utility Billing System database export
-- Database: utility_billing_db
-- Generated at: 2026-06-05T13:54:26.233369

BEGIN;

SET session_replication_role = replica;

DROP TABLE IF EXISTS "users" CASCADE;
DROP TABLE IF EXISTS "tariffs" CASCADE;
DROP TABLE IF EXISTS "payments" CASCADE;
DROP TABLE IF EXISTS "notifications" CASCADE;
DROP TABLE IF EXISTS "meters" CASCADE;
DROP TABLE IF EXISTS "meter_readings" CASCADE;
DROP TABLE IF EXISTS "jwt_blacklist" CASCADE;
DROP TABLE IF EXISTS "customers" CASCADE;
DROP TABLE IF EXISTS "bills" CASCADE;
DROP TABLE IF EXISTS "audit_logs" CASCADE;

SET session_replication_role = DEFAULT;

CREATE TABLE "audit_logs" (
    "id" bigint NOT NULL,
    "action" character varying(255) NOT NULL,
    "actor_email" character varying(255) NOT NULL,
    "actor_role" character varying(255) NOT NULL,
    "created_at" timestamp(6) without time zone,
    "description" text NOT NULL,
    "entity_id" bigint,
    "entity_type" character varying(255) NOT NULL
);

CREATE TABLE "bills" (
    "id" bigint NOT NULL,
    "amount_before_tax" numeric(14,2) NOT NULL,
    "amount_paid" numeric(14,2) NOT NULL,
    "bill_number" character varying(255) NOT NULL,
    "billing_month" integer NOT NULL,
    "billing_year" integer NOT NULL,
    "due_date" date NOT NULL,
    "generated_at" timestamp(6) without time zone,
    "outstanding_balance" numeric(14,2) NOT NULL,
    "penalty_amount" numeric(14,2) NOT NULL,
    "status" character varying(255) NOT NULL,
    "tax_amount" numeric(14,2) NOT NULL,
    "total_amount" numeric(14,2) NOT NULL,
    "customer_id" bigint NOT NULL,
    "meter_id" bigint NOT NULL,
    "reading_id" bigint NOT NULL
);

CREATE TABLE "customers" (
    "id" bigint NOT NULL,
    "address" character varying(255) NOT NULL,
    "created_at" timestamp(6) without time zone,
    "email" character varying(255) NOT NULL,
    "full_names" character varying(255) NOT NULL,
    "national_id" character varying(255) NOT NULL,
    "phone_number" character varying(255) NOT NULL,
    "status" character varying(255) NOT NULL,
    "user_id" bigint
);

CREATE TABLE "jwt_blacklist" (
    "id" bigint NOT NULL,
    "expiry" timestamp(6) without time zone NOT NULL,
    "token" character varying(512) NOT NULL
);

CREATE TABLE "meter_readings" (
    "id" bigint NOT NULL,
    "billing_month" integer NOT NULL,
    "billing_year" integer NOT NULL,
    "created_at" timestamp(6) without time zone,
    "current_reading" numeric(12,2) NOT NULL,
    "previous_reading" numeric(12,2) NOT NULL,
    "reading_date" date NOT NULL,
    "units_consumed" numeric(12,2) NOT NULL,
    "created_by" bigint NOT NULL,
    "meter_id" bigint NOT NULL
);

CREATE TABLE "meters" (
    "id" bigint NOT NULL,
    "installation_date" date NOT NULL,
    "meter_number" character varying(255) NOT NULL,
    "meter_type" character varying(255) NOT NULL,
    "status" character varying(255) NOT NULL,
    "customer_id" bigint NOT NULL
);

CREATE TABLE "notifications" (
    "id" bigint NOT NULL,
    "message" text NOT NULL,
    "sent_at" timestamp(6) without time zone,
    "status" character varying(255) NOT NULL,
    "customer_id" bigint NOT NULL
);

CREATE TABLE "payments" (
    "id" bigint NOT NULL,
    "amount_paid" numeric(14,2) NOT NULL,
    "created_at" timestamp(6) without time zone,
    "payment_date" date NOT NULL,
    "payment_method" character varying(255) NOT NULL,
    "transaction_reference" character varying(255) NOT NULL,
    "bill_id" bigint NOT NULL,
    "received_by" bigint NOT NULL
);

CREATE TABLE "tariffs" (
    "id" bigint NOT NULL,
    "active" boolean NOT NULL,
    "effective_from" date NOT NULL,
    "fixed_charge" numeric(12,2) NOT NULL,
    "meter_type" character varying(255) NOT NULL,
    "penalty_percentage" numeric(5,2) NOT NULL,
    "price_per_unit" numeric(12,2) NOT NULL,
    "tariff_type" character varying(255) NOT NULL,
    "vat_percentage" numeric(5,2) NOT NULL,
    "version" integer NOT NULL
);

CREATE TABLE "users" (
    "id" bigint NOT NULL,
    "created_at" timestamp(6) without time zone,
    "email" character varying(255) NOT NULL,
    "email_verified" boolean NOT NULL,
    "full_name" character varying(255) NOT NULL,
    "otp_code" character varying(255),
    "otp_expiry" timestamp(6) without time zone,
    "password" character varying(255) NOT NULL,
    "phone_number" character varying(255) NOT NULL,
    "role" character varying(255) NOT NULL,
    "status" character varying(255) NOT NULL,
    "updated_at" timestamp(6) without time zone
);

INSERT INTO "audit_logs" ("id", "action", "actor_email", "actor_role", "created_at", "description", "entity_id", "entity_type") VALUES (1, 'CREATE_TARIFF', 'admin@wasac.rw', 'ROLE_ADMIN', '2026-06-05 13:00:34.664182', 'Created tariff WATER version 3', 4, 'Tariff');
INSERT INTO "audit_logs" ("id", "action", "actor_email", "actor_role", "created_at", "description", "entity_id", "entity_type") VALUES (2, 'CAPTURE_READING', 'operator@wasac.rw', 'ROLE_OPERATOR', '2026-06-05 13:09:51.916306', 'Captured reading for meter WTR-DEMO-001 for 6/2026', 2, 'MeterReading');
INSERT INTO "audit_logs" ("id", "action", "actor_email", "actor_role", "created_at", "description", "entity_id", "entity_type") VALUES (3, 'GENERATE_BILL', 'finance@wasac.rw', 'ROLE_FINANCE', '2026-06-05 13:22:52.167016', 'Generated bill BILL-202606-000001 for customer ibenitha36@gmail.com', 1, 'Bill');
INSERT INTO "audit_logs" ("id", "action", "actor_email", "actor_role", "created_at", "description", "entity_id", "entity_type") VALUES (4, 'RECORD_PAYMENT', 'finance@wasac.rw', 'ROLE_FINANCE', '2026-06-05 13:27:08.056255', 'Recorded payment 5000.00 for bill BILL-202606-000001. Bill status is PARTIALLY_PAID', 1, 'Payment');
INSERT INTO "audit_logs" ("id", "action", "actor_email", "actor_role", "created_at", "description", "entity_id", "entity_type") VALUES (5, 'RECORD_PAYMENT', 'finance@wasac.rw', 'ROLE_FINANCE', '2026-06-05 13:30:23.75981', 'Recorded payment 13644 for bill BILL-202606-000001. Bill status is PAID', 2, 'Payment');
INSERT INTO "audit_logs" ("id", "action", "actor_email", "actor_role", "created_at", "description", "entity_id", "entity_type") VALUES (6, 'GENERATE_BILL', 'finance@wasac.rw', 'ROLE_FINANCE', '2026-06-05 13:41:49.57532', 'Generated bill BILL-202605-000001 for customer ibenitha36@gmail.com', 2, 'Bill');

INSERT INTO "bills" ("id", "amount_before_tax", "amount_paid", "bill_number", "billing_month", "billing_year", "due_date", "generated_at", "outstanding_balance", "penalty_amount", "status", "tax_amount", "total_amount", "customer_id", "meter_id", "reading_id") VALUES (1, 15800.00, 18644.00, 'BILL-202606-000001', 6, 2026, '2026-07-05', '2026-06-05 13:22:52.065233', 0.00, 0.00, 'PAID', 2844.00, 18644.00, 2, 5, 2);
INSERT INTO "bills" ("id", "amount_before_tax", "amount_paid", "bill_number", "billing_month", "billing_year", "due_date", "generated_at", "outstanding_balance", "penalty_amount", "status", "tax_amount", "total_amount", "customer_id", "meter_id", "reading_id") VALUES (2, 20200.00, 0.00, 'BILL-202605-000001', 5, 2026, '2026-07-05', '2026-06-05 13:41:43.473196', 23836.00, 0.00, 'PENDING', 3636.00, 23836.00, 2, 5, 1);

INSERT INTO "customers" ("id", "address", "created_at", "email", "full_names", "national_id", "phone_number", "status", "user_id") VALUES (1, 'KG 15 Ave, Kigali, Rwanda', '2026-06-05 11:21:04.343825', 'customer@wasac.rw', 'Jean Baptiste Uwimana', '1199080012345678', '+250788000004', 'ACTIVE', 4);
INSERT INTO "customers" ("id", "address", "created_at", "email", "full_names", "national_id", "phone_number", "status", "user_id") VALUES (2, 'KG 10 Ave, Kigali', '2026-06-05 12:30:32.33817', 'ibenitha36@gmail.com', 'Alice Demo Customer', '1199080012385678', '+250788111222', 'ACTIVE', 5);

INSERT INTO "jwt_blacklist" ("id", "expiry", "token") VALUES (3, '2026-06-05 13:23:58.0', 'eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiQUNDRVNTIiwic3ViIjoiYWRtaW5Ad2FzYWMucnciLCJpYXQiOjE3ODA2NTUwMzgsImV4cCI6MTc4MDY1ODYzOH0.W2cNWhLDZRloEArwD6tob56fkmA5__ZDCq1kCxdRuf5uii_574Cj18qySCF5i7MzNPxaeNlp9cWPMpbm58e2sQ');
INSERT INTO "jwt_blacklist" ("id", "expiry", "token") VALUES (4, '2026-06-05 13:42:31.0', 'eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiQUNDRVNTIiwic3ViIjoib3BlcmF0b3JAd2FzYWMucnciLCJpYXQiOjE3ODA2NTYxNTEsImV4cCI6MTc4MDY1OTc1MX0.mtrrUWUNcSOcxRIfiMau0diz7pxddc8h1TySFCQBMV5cQVDSE5PGINCz6YPT3ImLbA7uWIlioapt_s7-7kv5Zw');

INSERT INTO "meter_readings" ("id", "billing_month", "billing_year", "created_at", "current_reading", "previous_reading", "reading_date", "units_consumed", "created_by", "meter_id") VALUES (2, 6, 2026, '2026-06-05 13:09:51.912885', 180.00, 145.50, '2026-06-05', 34.50, 2, 5);
INSERT INTO "meter_readings" ("id", "billing_month", "billing_year", "created_at", "current_reading", "previous_reading", "reading_date", "units_consumed", "created_by", "meter_id") VALUES (1, 5, 2026, '2026-06-05 12:44:55.072866', 145.50, 100.00, '2026-06-05', 45.50, 2, 5);

INSERT INTO "meters" ("id", "installation_date", "meter_number", "meter_type", "status", "customer_id") VALUES (1, '2025-01-15', 'WTR-SEED-001', 'WATER', 'ACTIVE', 1);
INSERT INTO "meters" ("id", "installation_date", "meter_number", "meter_type", "status", "customer_id") VALUES (2, '2025-01-15', 'ELC-SEED-001', 'ELECTRICITY', 'ACTIVE', 1);
INSERT INTO "meters" ("id", "installation_date", "meter_number", "meter_type", "status", "customer_id") VALUES (3, '2026-06-05', 'MJ8GY9YVYXF', 'WATER', 'ACTIVE', 1);
INSERT INTO "meters" ("id", "installation_date", "meter_number", "meter_type", "status", "customer_id") VALUES (4, '2026-06-05', 'MJ8GY9YVYX3', 'WATER', 'ACTIVE', 1);
INSERT INTO "meters" ("id", "installation_date", "meter_number", "meter_type", "status", "customer_id") VALUES (5, '2026-06-05', 'WTR-DEMO-001', 'WATER', 'ACTIVE', 2);

INSERT INTO "notifications" ("id", "message", "sent_at", "status", "customer_id") VALUES (1, 'Dear Alice Demo Customer,
Your June/2026 utility bill of 18644.00 FRW has been successfully processed.', '2026-06-05 13:22:52.027081', 'SENT', 2);
INSERT INTO "notifications" ("id", "message", "sent_at", "status", "customer_id") VALUES (2, 'Dear Alice Demo Customer,
Your June/2026 utility bill of 18644.00 FRW has been successfully processed.', '2026-06-05 13:30:16.488711', 'SENT', 2);
INSERT INTO "notifications" ("id", "message", "sent_at", "status", "customer_id") VALUES (3, 'Dear Alice Demo Customer,
Your May/2026 utility bill of 23836.00 FRW has been successfully processed.', '2026-06-05 13:41:43.46392', 'SENT', 2);

INSERT INTO "payments" ("id", "amount_paid", "created_at", "payment_date", "payment_method", "transaction_reference", "bill_id", "received_by") VALUES (1, 5000.00, '2026-06-05 13:27:08.052254', '2026-06-05', 'CASH', 'MM-DEMO-001', 1, 3);
INSERT INTO "payments" ("id", "amount_paid", "created_at", "payment_date", "payment_method", "transaction_reference", "bill_id", "received_by") VALUES (2, 13644.00, '2026-06-05 13:30:16.494231', '2026-06-05', 'BANK_TRANSFER', 'BANK-DEMO-001', 1, 3);

INSERT INTO "tariffs" ("id", "active", "effective_from", "fixed_charge", "meter_type", "penalty_percentage", "price_per_unit", "tariff_type", "vat_percentage", "version") VALUES (2, TRUE, '2025-01-01', 1500.00, 'ELECTRICITY', 5.00, 180.00, 'FLAT', 18.00, 1);
INSERT INTO "tariffs" ("id", "active", "effective_from", "fixed_charge", "meter_type", "penalty_percentage", "price_per_unit", "tariff_type", "vat_percentage", "version") VALUES (1, FALSE, '2025-01-01', 2000.00, 'WATER', 5.00, 350.00, 'FLAT', 18.00, 1);
INSERT INTO "tariffs" ("id", "active", "effective_from", "fixed_charge", "meter_type", "penalty_percentage", "price_per_unit", "tariff_type", "vat_percentage", "version") VALUES (4, TRUE, '2026-06-07', 2000.00, 'WATER', 5.00, 400.00, 'FLAT', 18.00, 3);
INSERT INTO "tariffs" ("id", "active", "effective_from", "fixed_charge", "meter_type", "penalty_percentage", "price_per_unit", "tariff_type", "vat_percentage", "version") VALUES (3, FALSE, '2026-06-05', 2000.00, 'WATER', 5.00, 400.01, 'FLAT', 18.00, 2);
INSERT INTO "tariffs" ("id", "active", "effective_from", "fixed_charge", "meter_type", "penalty_percentage", "price_per_unit", "tariff_type", "vat_percentage", "version") VALUES (6, TRUE, '2026-05-01', 2000.00, 'WATER', 5.00, 400.00, 'FLAT', 18.00, 2);

INSERT INTO "users" ("id", "created_at", "email", "email_verified", "full_name", "otp_code", "otp_expiry", "password", "phone_number", "role", "status", "updated_at") VALUES (1, '2026-06-05 11:21:03.983178', 'admin@wasac.rw', TRUE, 'System Admin', NULL, NULL, '$2a$10$aOC9xAOxX740KPnWA9n4huWgh954yEcfjvi4m45d.EANlBgXG9xoe', '+250788000001', 'ROLE_ADMIN', 'ACTIVE', '2026-06-05 11:21:03.983178');
INSERT INTO "users" ("id", "created_at", "email", "email_verified", "full_name", "otp_code", "otp_expiry", "password", "phone_number", "role", "status", "updated_at") VALUES (2, '2026-06-05 11:21:04.067299', 'operator@wasac.rw', TRUE, 'Meter Operator', NULL, NULL, '$2a$10$Xg.Ib97yoq6yX.vFZDo63ObNI8MvA9he1id5wRUbIxeSjeyEZZRnC', '+250788000002', 'ROLE_OPERATOR', 'ACTIVE', '2026-06-05 11:21:04.067299');
INSERT INTO "users" ("id", "created_at", "email", "email_verified", "full_name", "otp_code", "otp_expiry", "password", "phone_number", "role", "status", "updated_at") VALUES (3, '2026-06-05 11:21:04.1296', 'finance@wasac.rw', TRUE, 'Finance Officer', NULL, NULL, '$2a$10$3mV4XTAUl.yiTYu7Mv/NxOLq.gUEIh4QSGhFPUN7K5uotrNIJ4I3S', '+250788000003', 'ROLE_FINANCE', 'ACTIVE', '2026-06-05 11:21:04.1296');
INSERT INTO "users" ("id", "created_at", "email", "email_verified", "full_name", "otp_code", "otp_expiry", "password", "phone_number", "role", "status", "updated_at") VALUES (4, '2026-06-05 11:21:04.192376', 'customer@wasac.rw', TRUE, 'Jean Customer', NULL, NULL, '$2a$10$ur//1YooAgeImd4xrLvU0.cn85kZm4vQGHcB8sx0bioThuPg3rx2S', '+250788000004', 'ROLE_CUSTOMER', 'ACTIVE', '2026-06-05 11:21:04.192376');
INSERT INTO "users" ("id", "created_at", "email", "email_verified", "full_name", "otp_code", "otp_expiry", "password", "phone_number", "role", "status", "updated_at") VALUES (5, '2026-06-05 12:16:01.794691', 'ibenitha36@gmail.com', TRUE, 'Alice Demo Customer', NULL, NULL, '$2a$10$lM2RQ42Olbn.QnYkbpjkIeegsTAks8XYmIZC838yulLpCHloy.WUu', '+250788123456', 'ROLE_CUSTOMER', 'ACTIVE', '2026-06-05 12:18:16.157304');
INSERT INTO "users" ("id", "created_at", "email", "email_verified", "full_name", "otp_code", "otp_expiry", "password", "phone_number", "role", "status", "updated_at") VALUES (6, '2026-06-05 12:25:57.083559', 'demo.operator@example.com', TRUE, 'Demo Operator', NULL, NULL, '$2a$10$X8KU1Kp7N3..GbUpAMyrnuISZbJJSEcbngcMBrcBtdAq6VMMqDrg.', '+250788123856', 'ROLE_OPERATOR', 'ACTIVE', '2026-06-05 12:25:57.083559');
INSERT INTO "users" ("id", "created_at", "email", "email_verified", "full_name", "otp_code", "otp_expiry", "password", "phone_number", "role", "status", "updated_at") VALUES (7, '2026-06-05 12:26:50.418829', 'demo.finance@example.com', TRUE, 'Demo Finance', NULL, NULL, '$2a$10$EwFPqIri5T9zgffCtT9RxelGTKumEEKKJ2Ft6PkA4zUNon9HQ5KWe', '+250788123806', 'ROLE_FINANCE', 'INACTIVE', '2026-06-05 12:36:05.603144');

ALTER TABLE "audit_logs" ADD CONSTRAINT "audit_logs_pkey" PRIMARY KEY (id);
ALTER TABLE "audit_logs" ADD CONSTRAINT "audit_logs_action_not_null" NOT NULL action;
ALTER TABLE "audit_logs" ADD CONSTRAINT "audit_logs_actor_email_not_null" NOT NULL actor_email;
ALTER TABLE "audit_logs" ADD CONSTRAINT "audit_logs_actor_role_not_null" NOT NULL actor_role;
ALTER TABLE "audit_logs" ADD CONSTRAINT "audit_logs_description_not_null" NOT NULL description;
ALTER TABLE "audit_logs" ADD CONSTRAINT "audit_logs_entity_type_not_null" NOT NULL entity_type;
ALTER TABLE "audit_logs" ADD CONSTRAINT "audit_logs_id_not_null" NOT NULL id;
ALTER TABLE "bills" ADD CONSTRAINT "bills_pkey" PRIMARY KEY (id);
ALTER TABLE "bills" ADD CONSTRAINT "idx_bills_bill_number" UNIQUE (bill_number);
ALTER TABLE "bills" ADD CONSTRAINT "ukr8xti75s6pn2w6ymbjabs1qkn" UNIQUE (reading_id);
ALTER TABLE "bills" ADD CONSTRAINT "bills_status_check" CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PARTIALLY_PAID'::character varying, 'PAID'::character varying, 'OVERDUE'::character varying])::text[])));
ALTER TABLE "bills" ADD CONSTRAINT "fk1rtnpgvhs9hqthvu89tl36fe7" FOREIGN KEY (reading_id) REFERENCES meter_readings(id);
ALTER TABLE "bills" ADD CONSTRAINT "fkfes5685l6y4urtsc0cq3cobo1" FOREIGN KEY (meter_id) REFERENCES meters(id);
ALTER TABLE "bills" ADD CONSTRAINT "fkoy9sc2dmxj2qwjeiiilf3yuxp" FOREIGN KEY (customer_id) REFERENCES customers(id);
ALTER TABLE "bills" ADD CONSTRAINT "bills_amount_before_tax_not_null" NOT NULL amount_before_tax;
ALTER TABLE "bills" ADD CONSTRAINT "bills_amount_paid_not_null" NOT NULL amount_paid;
ALTER TABLE "bills" ADD CONSTRAINT "bills_bill_number_not_null" NOT NULL bill_number;
ALTER TABLE "bills" ADD CONSTRAINT "bills_billing_month_not_null" NOT NULL billing_month;
ALTER TABLE "bills" ADD CONSTRAINT "bills_billing_year_not_null" NOT NULL billing_year;
ALTER TABLE "bills" ADD CONSTRAINT "bills_customer_id_not_null" NOT NULL customer_id;
ALTER TABLE "bills" ADD CONSTRAINT "bills_due_date_not_null" NOT NULL due_date;
ALTER TABLE "bills" ADD CONSTRAINT "bills_id_not_null" NOT NULL id;
ALTER TABLE "bills" ADD CONSTRAINT "bills_meter_id_not_null" NOT NULL meter_id;
ALTER TABLE "bills" ADD CONSTRAINT "bills_outstanding_balance_not_null" NOT NULL outstanding_balance;
ALTER TABLE "bills" ADD CONSTRAINT "bills_penalty_amount_not_null" NOT NULL penalty_amount;
ALTER TABLE "bills" ADD CONSTRAINT "bills_reading_id_not_null" NOT NULL reading_id;
ALTER TABLE "bills" ADD CONSTRAINT "bills_status_not_null" NOT NULL status;
ALTER TABLE "bills" ADD CONSTRAINT "bills_tax_amount_not_null" NOT NULL tax_amount;
ALTER TABLE "bills" ADD CONSTRAINT "bills_total_amount_not_null" NOT NULL total_amount;
ALTER TABLE "customers" ADD CONSTRAINT "customers_pkey" PRIMARY KEY (id);
ALTER TABLE "customers" ADD CONSTRAINT "idx_customers_email" UNIQUE (email);
ALTER TABLE "customers" ADD CONSTRAINT "idx_customers_national_id" UNIQUE (national_id);
ALTER TABLE "customers" ADD CONSTRAINT "idx_customers_phone" UNIQUE (phone_number);
ALTER TABLE "customers" ADD CONSTRAINT "ukeuat1oase6eqv195jvb71a93s" UNIQUE (user_id);
ALTER TABLE "customers" ADD CONSTRAINT "customers_status_check" CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])));
ALTER TABLE "customers" ADD CONSTRAINT "fkrh1g1a20omjmn6kurd35o3eit" FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE "customers" ADD CONSTRAINT "customers_address_not_null" NOT NULL address;
ALTER TABLE "customers" ADD CONSTRAINT "customers_email_not_null" NOT NULL email;
ALTER TABLE "customers" ADD CONSTRAINT "customers_full_names_not_null" NOT NULL full_names;
ALTER TABLE "customers" ADD CONSTRAINT "customers_id_not_null" NOT NULL id;
ALTER TABLE "customers" ADD CONSTRAINT "customers_national_id_not_null" NOT NULL national_id;
ALTER TABLE "customers" ADD CONSTRAINT "customers_phone_number_not_null" NOT NULL phone_number;
ALTER TABLE "customers" ADD CONSTRAINT "customers_status_not_null" NOT NULL status;
ALTER TABLE "jwt_blacklist" ADD CONSTRAINT "jwt_blacklist_pkey" PRIMARY KEY (id);
ALTER TABLE "jwt_blacklist" ADD CONSTRAINT "idx_jwt_blacklist_token" UNIQUE (token);
ALTER TABLE "jwt_blacklist" ADD CONSTRAINT "jwt_blacklist_expiry_not_null" NOT NULL expiry;
ALTER TABLE "jwt_blacklist" ADD CONSTRAINT "jwt_blacklist_id_not_null" NOT NULL id;
ALTER TABLE "jwt_blacklist" ADD CONSTRAINT "jwt_blacklist_token_not_null" NOT NULL token;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_pkey" PRIMARY KEY (id);
ALTER TABLE "meter_readings" ADD CONSTRAINT "uk_reading_meter_month_year" UNIQUE (meter_id, billing_month, billing_year);
ALTER TABLE "meter_readings" ADD CONSTRAINT "fkmf9shui9yiwblv5uh6bi0xuuw" FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE "meter_readings" ADD CONSTRAINT "fknalaulqjlf29g1dlukdeyg0g4" FOREIGN KEY (meter_id) REFERENCES meters(id);
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_billing_month_not_null" NOT NULL billing_month;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_billing_year_not_null" NOT NULL billing_year;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_created_by_not_null" NOT NULL created_by;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_current_reading_not_null" NOT NULL current_reading;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_id_not_null" NOT NULL id;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_meter_id_not_null" NOT NULL meter_id;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_previous_reading_not_null" NOT NULL previous_reading;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_reading_date_not_null" NOT NULL reading_date;
ALTER TABLE "meter_readings" ADD CONSTRAINT "meter_readings_units_consumed_not_null" NOT NULL units_consumed;
ALTER TABLE "meters" ADD CONSTRAINT "meters_pkey" PRIMARY KEY (id);
ALTER TABLE "meters" ADD CONSTRAINT "idx_meters_meter_number" UNIQUE (meter_number);
ALTER TABLE "meters" ADD CONSTRAINT "meters_meter_type_check" CHECK (((meter_type)::text = ANY ((ARRAY['WATER'::character varying, 'ELECTRICITY'::character varying])::text[])));
ALTER TABLE "meters" ADD CONSTRAINT "meters_status_check" CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])));
ALTER TABLE "meters" ADD CONSTRAINT "fkdgg79dhtsr0eumbce7ipw58lj" FOREIGN KEY (customer_id) REFERENCES customers(id);
ALTER TABLE "meters" ADD CONSTRAINT "meters_customer_id_not_null" NOT NULL customer_id;
ALTER TABLE "meters" ADD CONSTRAINT "meters_id_not_null" NOT NULL id;
ALTER TABLE "meters" ADD CONSTRAINT "meters_installation_date_not_null" NOT NULL installation_date;
ALTER TABLE "meters" ADD CONSTRAINT "meters_meter_number_not_null" NOT NULL meter_number;
ALTER TABLE "meters" ADD CONSTRAINT "meters_meter_type_not_null" NOT NULL meter_type;
ALTER TABLE "meters" ADD CONSTRAINT "meters_status_not_null" NOT NULL status;
ALTER TABLE "notifications" ADD CONSTRAINT "notifications_pkey" PRIMARY KEY (id);
ALTER TABLE "notifications" ADD CONSTRAINT "notifications_status_check" CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying])::text[])));
ALTER TABLE "notifications" ADD CONSTRAINT "fk30dp6ycner3dgso3scgc9vghy" FOREIGN KEY (customer_id) REFERENCES customers(id);
ALTER TABLE "notifications" ADD CONSTRAINT "notifications_customer_id_not_null" NOT NULL customer_id;
ALTER TABLE "notifications" ADD CONSTRAINT "notifications_id_not_null" NOT NULL id;
ALTER TABLE "notifications" ADD CONSTRAINT "notifications_message_not_null" NOT NULL message;
ALTER TABLE "notifications" ADD CONSTRAINT "notifications_status_not_null" NOT NULL status;
ALTER TABLE "payments" ADD CONSTRAINT "payments_pkey" PRIMARY KEY (id);
ALTER TABLE "payments" ADD CONSTRAINT "idx_payments_transaction_ref" UNIQUE (transaction_reference);
ALTER TABLE "payments" ADD CONSTRAINT "payments_payment_method_check" CHECK (((payment_method)::text = ANY ((ARRAY['CASH'::character varying, 'MOBILE_MONEY'::character varying, 'BANK_TRANSFER'::character varying, 'CARD'::character varying])::text[])));
ALTER TABLE "payments" ADD CONSTRAINT "fk5vwv4mwirmtu3lsjxlpc0dy02" FOREIGN KEY (received_by) REFERENCES users(id);
ALTER TABLE "payments" ADD CONSTRAINT "fk9565r6579khpdjxnyla0l2ycd" FOREIGN KEY (bill_id) REFERENCES bills(id);
ALTER TABLE "payments" ADD CONSTRAINT "payments_amount_paid_not_null" NOT NULL amount_paid;
ALTER TABLE "payments" ADD CONSTRAINT "payments_bill_id_not_null" NOT NULL bill_id;
ALTER TABLE "payments" ADD CONSTRAINT "payments_id_not_null" NOT NULL id;
ALTER TABLE "payments" ADD CONSTRAINT "payments_payment_date_not_null" NOT NULL payment_date;
ALTER TABLE "payments" ADD CONSTRAINT "payments_payment_method_not_null" NOT NULL payment_method;
ALTER TABLE "payments" ADD CONSTRAINT "payments_received_by_not_null" NOT NULL received_by;
ALTER TABLE "payments" ADD CONSTRAINT "payments_transaction_reference_not_null" NOT NULL transaction_reference;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_pkey" PRIMARY KEY (id);
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_meter_type_check" CHECK (((meter_type)::text = ANY ((ARRAY['WATER'::character varying, 'ELECTRICITY'::character varying])::text[])));
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_tariff_type_check" CHECK (((tariff_type)::text = ANY ((ARRAY['FLAT'::character varying, 'TIER'::character varying])::text[])));
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_active_not_null" NOT NULL active;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_effective_from_not_null" NOT NULL effective_from;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_fixed_charge_not_null" NOT NULL fixed_charge;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_id_not_null" NOT NULL id;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_meter_type_not_null" NOT NULL meter_type;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_penalty_percentage_not_null" NOT NULL penalty_percentage;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_price_per_unit_not_null" NOT NULL price_per_unit;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_tariff_type_not_null" NOT NULL tariff_type;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_vat_percentage_not_null" NOT NULL vat_percentage;
ALTER TABLE "tariffs" ADD CONSTRAINT "tariffs_version_not_null" NOT NULL version;
ALTER TABLE "users" ADD CONSTRAINT "users_pkey" PRIMARY KEY (id);
ALTER TABLE "users" ADD CONSTRAINT "idx_users_email" UNIQUE (email);
ALTER TABLE "users" ADD CONSTRAINT "idx_users_phone" UNIQUE (phone_number);
ALTER TABLE "users" ADD CONSTRAINT "users_role_check" CHECK (((role)::text = ANY ((ARRAY['ROLE_ADMIN'::character varying, 'ROLE_OPERATOR'::character varying, 'ROLE_FINANCE'::character varying, 'ROLE_CUSTOMER'::character varying])::text[])));
ALTER TABLE "users" ADD CONSTRAINT "users_status_check" CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])));
ALTER TABLE "users" ADD CONSTRAINT "users_email_not_null" NOT NULL email;
ALTER TABLE "users" ADD CONSTRAINT "users_email_verified_not_null" NOT NULL email_verified;
ALTER TABLE "users" ADD CONSTRAINT "users_full_name_not_null" NOT NULL full_name;
ALTER TABLE "users" ADD CONSTRAINT "users_id_not_null" NOT NULL id;
ALTER TABLE "users" ADD CONSTRAINT "users_password_not_null" NOT NULL password;
ALTER TABLE "users" ADD CONSTRAINT "users_phone_number_not_null" NOT NULL phone_number;
ALTER TABLE "users" ADD CONSTRAINT "users_role_not_null" NOT NULL role;
ALTER TABLE "users" ADD CONSTRAINT "users_status_not_null" NOT NULL status;

SELECT setval('public.audit_logs_id_seq', COALESCE((SELECT MAX("id") FROM "audit_logs"), 1), (SELECT COUNT(*) > 0 FROM "audit_logs"));
SELECT setval('public.bills_id_seq', COALESCE((SELECT MAX("id") FROM "bills"), 1), (SELECT COUNT(*) > 0 FROM "bills"));
SELECT setval('public.customers_id_seq', COALESCE((SELECT MAX("id") FROM "customers"), 1), (SELECT COUNT(*) > 0 FROM "customers"));
SELECT setval('public.jwt_blacklist_id_seq', COALESCE((SELECT MAX("id") FROM "jwt_blacklist"), 1), (SELECT COUNT(*) > 0 FROM "jwt_blacklist"));
SELECT setval('public.meter_readings_id_seq', COALESCE((SELECT MAX("id") FROM "meter_readings"), 1), (SELECT COUNT(*) > 0 FROM "meter_readings"));
SELECT setval('public.meters_id_seq', COALESCE((SELECT MAX("id") FROM "meters"), 1), (SELECT COUNT(*) > 0 FROM "meters"));
SELECT setval('public.notifications_id_seq', COALESCE((SELECT MAX("id") FROM "notifications"), 1), (SELECT COUNT(*) > 0 FROM "notifications"));
SELECT setval('public.payments_id_seq', COALESCE((SELECT MAX("id") FROM "payments"), 1), (SELECT COUNT(*) > 0 FROM "payments"));
SELECT setval('public.tariffs_id_seq', COALESCE((SELECT MAX("id") FROM "tariffs"), 1), (SELECT COUNT(*) > 0 FROM "tariffs"));
SELECT setval('public.users_id_seq', COALESCE((SELECT MAX("id") FROM "users"), 1), (SELECT COUNT(*) > 0 FROM "users"));

COMMIT;
