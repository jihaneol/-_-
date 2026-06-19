CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    merchant_id VARCHAR(100) NOT NULL,
    order_id VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(150) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payments_idempotency_key UNIQUE (idempotency_key)
);
