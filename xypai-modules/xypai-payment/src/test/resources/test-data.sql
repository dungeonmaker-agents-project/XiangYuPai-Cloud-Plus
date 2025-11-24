-- ============================================
-- Test Data Setup for Payment Service
-- ============================================

-- Create User Account Table
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    frozen_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_income DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_expense DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_password_hash VARCHAR(255),
    password_error_count INT NOT NULL DEFAULT 0,
    password_locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0
);

-- Create Payment Record Table
CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT NOT NULL PRIMARY KEY,
    payment_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    reference_id VARCHAR(32),
    reference_type VARCHAR(20),
    amount DECIMAL(10,2) NOT NULL,
    service_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    payment_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0
);

-- Create Account Transaction Table
CREATE TABLE IF NOT EXISTS account_transaction (
    id BIGINT NOT NULL PRIMARY KEY,
    transaction_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    balance_before DECIMAL(10,2) NOT NULL,
    balance_after DECIMAL(10,2) NOT NULL,
    payment_no VARCHAR(32),
    reference_id VARCHAR(32),
    reference_type VARCHAR(20),
    remark VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0
);

-- Create indexes for payment queries
CREATE INDEX idx_user_account_user_id ON user_account(user_id);
CREATE INDEX idx_payment_record_user_id ON payment_record(user_id);
CREATE INDEX idx_payment_record_payment_no ON payment_record(payment_no);
CREATE INDEX idx_account_transaction_user_id ON account_transaction(user_id);
CREATE INDEX idx_account_transaction_transaction_no ON account_transaction(transaction_no);

-- Insert sample user accounts for testing
INSERT INTO user_account (id, user_id, balance, frozen_balance, total_income, total_expense, password_error_count)
VALUES
    (1001, 100001, 1000.00, 0.00, 1000.00, 0.00, 0),
    (1002, 10001, 500.00, 0.00, 500.00, 0.00, 0);

-- Insert sample payment records for testing
INSERT INTO payment_record (id, payment_no, user_id, payment_method, payment_type, reference_id, reference_type, amount, status, payment_time)
VALUES
    (2001, 'PAY20251114120000001', 100001, 'balance', 'order', '1002', 'order', 105.00, 'success', CURRENT_TIMESTAMP),
    (2002, 'PAY20251114120000002', 100001, 'balance', 'order', '1003', 'order', 157.50, 'success', CURRENT_TIMESTAMP);

-- Insert sample account transactions for testing
INSERT INTO account_transaction (id, transaction_no, user_id, transaction_type, amount, balance_before, balance_after, payment_no, reference_id, reference_type, remark)
VALUES
    (3001, 'TXN20251114120000001', 100001, 'income', 1000.00, 0.00, 1000.00, NULL, NULL, 'recharge', '充值'),
    (3002, 'TXN20251114120000002', 100001, 'expense', 105.00, 1000.00, 895.00, 'PAY20251114120000001', '1002', 'order', '订单支付'),
    (3003, 'TXN20251114120000003', 100001, 'expense', 157.50, 895.00, 737.50, 'PAY20251114120000002', '1003', 'order', '订单支付');

-- ============================================
-- Common Test Data (for both modules)
-- ============================================

-- Users table (simulated, for reference only)
-- In actual testing, users would come from the user service
-- User IDs used in tests:
-- - 100001: Test customer user (balance: 1000.00)
-- - 10001: Test provider user (balance: 500.00)
