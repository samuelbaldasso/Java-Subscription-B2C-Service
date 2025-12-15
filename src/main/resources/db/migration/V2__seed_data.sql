-- Insert default plans
INSERT INTO plans (code, name, description, price, billing_cycle_in_days, trial_period_in_days, active)
VALUES 
    ('FREE', 'Free Plan', 'Basic features for free', 0.00, 30, 0, true),
    ('BASIC', 'Basic Plan', 'Essential features for individuals', 9.99, 30, 7, true),
    ('PREMIUM', 'Premium Plan', 'Advanced features for professionals', 29.99, 30, 7, true),
    ('ENTERPRISE', 'Enterprise Plan', 'Full features for teams', 99.99, 30, 14, true);

-- Insert admin user (password: admin123)
INSERT INTO users (email, name, password, role, active)
VALUES ('admin@subscription.com', 'Admin User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true);