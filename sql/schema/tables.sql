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
