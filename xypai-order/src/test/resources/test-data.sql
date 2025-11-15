-- ============================================
-- Test Data Setup for Order Service
-- ============================================

-- Create Order Table
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT NOT NULL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    order_type VARCHAR(20) NOT NULL DEFAULT 'service',
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    service_fee DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'pending',
    payment_method VARCHAR(20),
    payment_time TIMESTAMP,
    accepted_time TIMESTAMP,
    completed_time TIMESTAMP,
    cancelled_time TIMESTAMP,
    auto_cancel_time TIMESTAMP,
    cancel_reason VARCHAR(255),
    refund_amount DECIMAL(10,2),
    refund_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0
);

-- Create index for order queries
CREATE INDEX idx_order_user_id ON `order`(user_id);
CREATE INDEX idx_order_provider_id ON `order`(provider_id);
CREATE INDEX idx_order_status ON `order`(status);
CREATE INDEX idx_order_no ON `order`(order_no);

-- Insert sample orders for testing
INSERT INTO `order` (id, order_no, user_id, provider_id, service_id, order_type, quantity, unit_price, subtotal, service_fee, total_amount, status, payment_status)
VALUES
    (1001, '20251114120000001', 100001, 10001, 101, 'service', 1, 50.00, 50.00, 2.50, 52.50, 'pending', 'pending'),
    (1002, '20251114120000002', 100001, 10001, 101, 'service', 2, 50.00, 100.00, 5.00, 105.00, 'accepted', 'success'),
    (1003, '20251114120000003', 100001, 10001, 101, 'service', 3, 50.00, 150.00, 7.50, 157.50, 'completed', 'success'),
    (1004, '20251114120000004', 100001, 10001, 101, 'service', 1, 50.00, 50.00, 2.50, 52.50, 'cancelled', 'pending');

-- ============================================
-- Common Test Data (for both modules)
-- ============================================

-- Users table (simulated, for reference only)
-- In actual testing, users would come from the user service
-- User IDs used in tests:
-- - 100001: Test customer user
-- - 10001: Test provider user
