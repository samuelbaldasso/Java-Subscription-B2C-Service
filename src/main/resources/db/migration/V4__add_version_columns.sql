-- Add version columns for Optimistic Locking
ALTER TABLE users ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE subscriptions ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE invoices ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE plans ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN users.version IS 'Optimistic locking version';
COMMENT ON COLUMN subscriptions.version IS 'Optimistic locking version';
COMMENT ON COLUMN invoices.version IS 'Optimistic locking version';
COMMENT ON COLUMN plans.version IS 'Optimistic locking version';
