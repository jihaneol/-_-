CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(200) NULL,
    role VARCHAR(30) NOT NULL,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_members_username UNIQUE (username)
);

CREATE INDEX idx_members_deleted_at_id
    ON members (deleted_at, id);

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    price BIGINT NOT NULL,
    sale_status VARCHAR(30) NOT NULL,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_products_deleted_at_id
    ON products (deleted_at, id);

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

CREATE INDEX idx_commerce_orders_deleted_at_id
    ON commerce_orders (deleted_at, id);

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

CREATE INDEX idx_order_lines_order_id
    ON order_lines (order_id);
