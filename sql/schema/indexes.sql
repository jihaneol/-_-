CREATE INDEX idx_payments_merchant_id_id
    ON payments (merchant_id, id);

CREATE INDEX idx_payments_status_id
    ON payments (status, id);
