CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    idempotency_key VARCHAR(150) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payments_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_payments_merchant_id_id
    ON payments (merchant_id, id);

CREATE INDEX idx_payments_status_id
    ON payments (status, id);

CREATE TABLE payment_operation_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    operation_type VARCHAR(40) NOT NULL,
    order_id BIGINT NOT NULL,
    payment_ref_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    issued_coupon_count INT NOT NULL,
    voided_coupon_count INT NOT NULL,
    occurred_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payment_operation_records_operation_order UNIQUE (operation_type, order_id)
);

CREATE INDEX idx_payment_operation_records_occurred_at_id
    ON payment_operation_records (occurred_at, id);

CREATE INDEX idx_payment_operation_records_member_id_id
    ON payment_operation_records (member_id, id);
