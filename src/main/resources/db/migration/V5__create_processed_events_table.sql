-- Create processed_events table for Idempotency Pattern
CREATE TABLE processed_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_processed_event_id ON processed_events(event_id);
CREATE INDEX idx_processed_event_type ON processed_events(event_type);
CREATE INDEX idx_processed_at ON processed_events(processed_at);

COMMENT ON TABLE processed_events IS 'Tracks processed Kafka events to ensure idempotency';
COMMENT ON COLUMN processed_events.event_id IS 'Unique message ID from Kafka headers or event payload';
