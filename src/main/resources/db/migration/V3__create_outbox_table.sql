-- Create outbox_events table for Transactional Outbox Pattern
CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_status ON outbox_events(status);
CREATE INDEX idx_outbox_created ON outbox_events(created_at);

COMMENT ON TABLE outbox_events IS 'Transactional Outbox Pattern: stores domain events atomically with business data';
COMMENT ON COLUMN outbox_events.status IS 'PENDING, PUBLISHED, or FAILED';
COMMENT ON COLUMN outbox_events.retry_count IS 'Number of retry attempts (max 3)';
