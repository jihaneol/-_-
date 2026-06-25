CREATE INDEX idx_payments_merchant_id_id
    ON payments (merchant_id, id);

CREATE INDEX idx_payments_status_id
    ON payments (status, id);

CREATE INDEX idx_members_deleted_at_id
    ON members (deleted_at, id);

CREATE INDEX idx_products_deleted_at_id
    ON products (deleted_at, id);

CREATE INDEX idx_order_lines_order_id
    ON order_lines (order_id);

CREATE INDEX idx_commerce_orders_deleted_at_id
    ON commerce_orders (deleted_at, id);

CREATE INDEX idx_coupons_member_id_id
    ON coupons (member_id, id);

CREATE INDEX idx_coupons_order_id_id
    ON coupons (order_id, id);

CREATE INDEX idx_coupon_histories_member_id_id
    ON coupon_histories (member_id, id);

CREATE INDEX idx_coupon_histories_order_id_id
    ON coupon_histories (order_id, id);

CREATE INDEX idx_payment_operation_records_occurred_at_id
    ON payment_operation_records (occurred_at, id);

CREATE INDEX idx_payment_operation_records_member_id_id
    ON payment_operation_records (member_id, id);

CREATE INDEX idx_outbox_events_status_id
    ON outbox_events (status, id);

CREATE INDEX idx_outbox_events_aggregate_id_id
    ON outbox_events (aggregate_id, id);
