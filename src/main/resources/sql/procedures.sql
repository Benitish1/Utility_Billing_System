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
        || E'\nYour ' || TRIM(v_month_name) || '/' || v_billing_year
        || ' utility bill of ' || v_total_amount || ' FRW has been successfully processed.';

    INSERT INTO notifications (customer_id, message, sent_at, status)
    VALUES (v_customer_id, v_message, NOW(), 'SENT');
END;
$$;
