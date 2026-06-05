-- Trigger: Auto-insert notification when a bill is generated
-- Message format: Dear <CustomerName>, Your <Month/Year> utility bill of <Amount> FRW has been successfully processed.

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
        || E'\nYour ' || TRIM(v_month_name) || '/' || NEW.billing_year
        || ' utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.';

    INSERT INTO notifications (customer_id, message, sent_at, status)
    VALUES (NEW.customer_id, v_message, NOW(), 'SENT');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bill_notification ON bills;
CREATE TRIGGER trg_bill_notification
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE FUNCTION notify_on_bill_insert();
