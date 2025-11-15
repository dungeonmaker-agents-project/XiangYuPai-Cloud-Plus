-- =====================================================
-- XiangYuPai Trade Module - Database Setup Script
-- =====================================================
-- Date: 2025-11-14
-- Description: Creates databases and tables for Order and Payment services
-- =====================================================

-- =====================================================
-- Part 1: Order Service Database (xypai_order)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `xypai_order`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `xypai_order`;

-- Drop existing tables (for clean reinstall)
DROP TABLE IF EXISTS `order`;

-- Order Table
CREATE TABLE `order` (
    -- Primary Key
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',

    -- Order Info
    `user_id` BIGINT(20) NOT NULL COMMENT '下单用户ID',
    `provider_id` BIGINT(20) NOT NULL COMMENT '服务提供者ID',
    `service_id` BIGINT(20) NOT NULL COMMENT '服务ID',
    `order_type` VARCHAR(20) NOT NULL DEFAULT 'service' COMMENT '订单类型: service/activity',

    -- Quantity & Pricing
    `quantity` INT(11) NOT NULL DEFAULT 1 COMMENT '数量',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价(金币)',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
    `service_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务费',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',

    -- Status
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '订单状态: pending/accepted/in_progress/completed/cancelled/refunded',
    `payment_status` VARCHAR(20) DEFAULT 'pending' COMMENT '支付状态: pending/success/failed',
    `payment_method` VARCHAR(20) COMMENT '支付方式: balance/alipay/wechat',

    -- Timestamps
    `payment_time` DATETIME COMMENT '支付时间',
    `accepted_time` DATETIME COMMENT '接单时间',
    `completed_time` DATETIME COMMENT '完成时间',
    `cancelled_time` DATETIME COMMENT '取消时间',
    `auto_cancel_time` DATETIME COMMENT '自动取消时间',

    -- Refund
    `cancel_reason` VARCHAR(255) COMMENT '取消原因',
    `refund_amount` DECIMAL(10,2) COMMENT '退款金额',
    `refund_time` DATETIME COMMENT '退款时间',

    -- Audit Fields
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除: 0=正常, 1=已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_provider_id` (`provider_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_status` (`status`),
    KEY `idx_payment_status` (`payment_status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- Insert sample data for testing
INSERT INTO `order` (
    `order_no`, `user_id`, `provider_id`, `service_id`, `order_type`,
    `quantity`, `unit_price`, `subtotal`, `service_fee`, `total_amount`,
    `status`, `payment_status`
) VALUES
(
    '20251114100001', 1, 2, 101, 'service',
    1, 10.00, 10.00, 0.50, 10.50,
    'pending', 'pending'
);


-- =====================================================
-- Part 2: Payment Service Database (xypai_payment)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `xypai_payment`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `xypai_payment`;

-- Drop existing tables (for clean reinstall)
DROP TABLE IF EXISTS `account_transaction`;
DROP TABLE IF EXISTS `payment_record`;
DROP TABLE IF EXISTS `user_account`;

-- User Account Table
CREATE TABLE `user_account` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',

    -- Balance
    `balance` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '余额(金币)',
    `frozen_balance` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    `total_income` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '累计收入',
    `total_expense` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '累计支出',

    -- Payment Password
    `payment_password_hash` VARCHAR(255) COMMENT '支付密码哈希(BCrypt)',
    `password_error_count` INT(11) NOT NULL DEFAULT 0 COMMENT '密码错误次数',
    `password_locked_until` DATETIME COMMENT '密码锁定至',

    -- Audit Fields
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除: 0=正常, 1=已删除',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账户表';

-- Payment Record Table
CREATE TABLE `payment_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `payment_no` VARCHAR(50) NOT NULL COMMENT '支付流水号',

    -- User Info
    `user_id` BIGINT(20) NOT NULL COMMENT '付款用户ID',
    `payee_id` BIGINT(20) COMMENT '收款用户ID',

    -- Payment Info
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式: balance/alipay/wechat',
    `payment_type` VARCHAR(50) NOT NULL COMMENT '支付类型: order/activity_publish/activity_register',
    `reference_id` VARCHAR(50) COMMENT '关联ID(订单ID/活动ID)',
    `reference_type` VARCHAR(50) COMMENT '关联类型',

    -- Amount
    `amount` DECIMAL(15,2) NOT NULL COMMENT '支付金额',
    `service_fee` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '服务费',

    -- Status
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/success/failed/refunded',
    `payment_time` DATETIME COMMENT '支付成功时间',

    -- Refund
    `refund_amount` DECIMAL(15,2) COMMENT '退款金额',
    `refund_time` DATETIME COMMENT '退款时间',

    `remark` VARCHAR(255) COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_payee_id` (`payee_id`),
    KEY `idx_reference` (`reference_id`, `reference_type`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- Account Transaction Table
CREATE TABLE `account_transaction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `transaction_no` VARCHAR(50) NOT NULL COMMENT '交易流水号',

    -- User & Type
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `transaction_type` VARCHAR(50) NOT NULL COMMENT '交易类型: income/expense/freeze/unfreeze',

    -- Amount
    `amount` DECIMAL(15,2) NOT NULL COMMENT '交易金额',
    `balance_before` DECIMAL(15,2) NOT NULL COMMENT '交易前余额',
    `balance_after` DECIMAL(15,2) NOT NULL COMMENT '交易后余额',

    -- Reference
    `payment_no` VARCHAR(50) COMMENT '关联支付流水号',
    `reference_id` VARCHAR(50) COMMENT '关联业务ID',
    `reference_type` VARCHAR(50) COMMENT '关联业务类型',
    `remark` VARCHAR(255) COMMENT '备注',

    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_no` (`transaction_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户流水表';

-- Insert sample data for testing
-- User 1 with 100 coins balance
INSERT INTO `user_account` (`user_id`, `balance`, `payment_password_hash`) VALUES
(1, 100.00, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM4JZnmMfwHqXpq8.LvO'); -- password: 123456

-- User 2 with 50 coins balance (service provider)
INSERT INTO `user_account` (`user_id`, `balance`) VALUES
(2, 50.00);

-- =====================================================
-- Verification Queries
-- =====================================================

-- Check order database
USE `xypai_order`;
SELECT 'Order Database Tables:' AS '';
SHOW TABLES;
SELECT 'Sample Order:' AS '';
SELECT * FROM `order` LIMIT 1;

-- Check payment database
USE `xypai_payment`;
SELECT 'Payment Database Tables:' AS '';
SHOW TABLES;
SELECT 'Sample User Accounts:' AS '';
SELECT user_id, balance, frozen_balance, total_income, total_expense FROM `user_account`;

-- =====================================================
-- Setup Complete!
-- =====================================================
SELECT '
========================================
✅ Trade Module Database Setup Complete
========================================

Databases Created:
  - xypai_order (1 table)
  - xypai_payment (3 tables)

Sample Data:
  - 1 pending order (Order#20251114100001)
  - 2 user accounts with balances

Next Steps:
  1. Configure Nacos with database credentials
  2. Start xypai-payment service (port 9411)
  3. Start xypai-order service (port 9410)
  4. Test APIs via Swagger: http://localhost:9410/doc.html

========================================
' AS 'Setup Status';
