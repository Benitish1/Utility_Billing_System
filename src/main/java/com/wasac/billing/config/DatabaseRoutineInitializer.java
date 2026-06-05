package com.wasac.billing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseRoutineInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void installDatabaseRoutines() {
        try {
            installBillNotificationTrigger();
            installFullPaymentProcedure();
            log.info("PostgreSQL trigger and stored procedure installed successfully");
        } catch (Exception ex) {
            log.warn("Could not install PostgreSQL routines automatically: {}", ex.getMessage());
        }
    }

    private void installBillNotificationTrigger() {
        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION notify_on_bill_insert()
                RETURNS TRIGGER AS $$
                DECLARE
                    v_customer_name VARCHAR(120);
                    v_month_name VARCHAR(20);
                    v_message TEXT;
                BEGIN
                    SELECT full_names INTO v_customer_name FROM customers WHERE id = NEW.customer_id;
                    v_month_name := TO_CHAR(TO_DATE(NEW.billing_month::TEXT, 'MM'), 'Month');
                    v_message := 'Dear ' || v_customer_name || ','
                        || E'\\nYour ' || TRIM(v_month_name) || '/' || NEW.billing_year
                        || ' utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.';
                    INSERT INTO notifications (customer_id, message, sent_at, status)
                    VALUES (NEW.customer_id, v_message, NOW(), 'SENT');
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql
                """);
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_bill_notification ON bills");
        jdbcTemplate.execute("""
                CREATE TRIGGER trg_bill_notification
                    AFTER INSERT ON bills
                    FOR EACH ROW
                    EXECUTE FUNCTION notify_on_bill_insert()
                """);
    }

    private void installFullPaymentProcedure() {
        jdbcTemplate.execute("""
                CREATE OR REPLACE PROCEDURE process_full_payment(p_bill_id BIGINT)
                LANGUAGE plpgsql
                AS $$
                DECLARE
                    v_customer_id BIGINT;
                    v_customer_name VARCHAR(120);
                    v_billing_month INT;
                    v_billing_year INT;
                    v_total_amount NUMERIC(14,2);
                    v_month_name VARCHAR(20);
                    v_message TEXT;
                BEGIN
                    SELECT b.customer_id, c.full_names, b.billing_month, b.billing_year, b.total_amount
                    INTO v_customer_id, v_customer_name, v_billing_month, v_billing_year, v_total_amount
                    FROM bills b
                    JOIN customers c ON c.id = b.customer_id
                    WHERE b.id = p_bill_id;
                    IF NOT FOUND THEN
                        RAISE EXCEPTION 'Bill not found with id %', p_bill_id;
                    END IF;
                    UPDATE bills
                    SET status = 'PAID',
                        outstanding_balance = 0
                    WHERE id = p_bill_id;
                    v_month_name := TO_CHAR(TO_DATE(v_billing_month::TEXT, 'MM'), 'Month');
                    v_message := 'Dear ' || v_customer_name || ','
                        || E'\\nYour ' || TRIM(v_month_name) || '/' || v_billing_year
                        || ' utility bill of ' || v_total_amount || ' FRW has been successfully processed.';
                    INSERT INTO notifications (customer_id, message, sent_at, status)
                    VALUES (v_customer_id, v_message, NOW(), 'SENT');
                END;
                $$
                """);
    }
}
