CREATE TABLE coupons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    payment_ref_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_coupons_member_id_id
    ON coupons (member_id, id);

CREATE INDEX idx_coupons_order_id_id
    ON coupons (order_id, id);

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

CREATE INDEX idx_coupon_histories_member_id_id
    ON coupon_histories (member_id, id);

CREATE INDEX idx_coupon_histories_order_id_id
    ON coupon_histories (order_id, id);
