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

CREATE INDEX idx_outbox_events_status_id
    ON outbox_events (status, id);

CREATE INDEX idx_outbox_events_aggregate_id_id
    ON outbox_events (aggregate_id, id);

CREATE TABLE processed_outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_key VARCHAR(120) NOT NULL,
    processed_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_processed_outbox_events_event_key UNIQUE (event_key)
);
