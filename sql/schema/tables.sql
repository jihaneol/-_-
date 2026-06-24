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

CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id)
);

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    price BIGINT NOT NULL,
    sale_status VARCHAR(30) NOT NULL,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id)
);

CREATE TABLE inventories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_inventories_product_id UNIQUE (product_id)
);

CREATE TABLE commerce_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    total_amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_ref_id BIGINT NULL,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id)
);

CREATE TABLE order_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    unit_price BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    line_amount BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE coupons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    payment_ref_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE coupon_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_id BIGINT NULL,
    member_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    payment_ref_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE payment_operational_projections (
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
    CONSTRAINT uk_payment_operational_projection_operation_order UNIQUE (operation_type, order_id)
);

CREATE TABLE outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_key VARCHAR(120) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    aggregate_type VARCHAR(60) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempt_count INT NOT NULL,
    last_error TEXT NULL,
    created_at DATETIME NOT NULL,
    published_at DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_outbox_events_event_key UNIQUE (event_key)
);

CREATE TABLE processed_outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_key VARCHAR(120) NOT NULL,
    processed_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_processed_outbox_events_event_key UNIQUE (event_key)
);
