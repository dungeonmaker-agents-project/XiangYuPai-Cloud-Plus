# XiangYuPai Trade Module - Complete Implementation

## Module Overview

This implementation creates TWO complete microservices for the trade domain:

### 1. **xypai-order** (Port: 9410)
- **Database**: `xypai_order`
- **6 HTTP APIs** for frontend
- **2 RPC interfaces** for other services

### 2. **xypai-payment** (Port: 9411)
- **Database**: `xypai_payment`
- **4 HTTP APIs** for frontend
- **5 RPC interfaces** for other services

---

## Project Structure

```
RuoYi-Cloud-Plus/
├── ruoyi-api/
│   ├── ruoyi-api-order/          # Order RPC API definitions
│   │   └── src/main/java/org/dromara/order/api/
│   │       ├── RemoteOrderService.java
│   │       ├── domain/
│   │       │   ├── OrderDTO.java
│   │       │   └── OrderStatusVO.java
│   │       └── ...
│   │
│   └── ruoyi-api-payment/        # Payment RPC API definitions
│       └── src/main/java/org/dromara/payment/api/
│           ├── RemotePaymentService.java
│           ├── domain/
│           │   ├── PaymentRequest.java
│           │   └── BalanceVO.java
│           └── ...
│
├── xypai-order/                  # Order Microservice
│   ├── src/main/java/org/dromara/order/
│   │   ├── XyPaiOrderApplication.java
│   │   ├── controller/           # HTTP Controllers
│   │   │   ├── OrderController.java
│   │   │   └── feign/
│   │   │       └── RemoteOrderServiceImpl.java
│   │   ├── service/
│   │   │   ├── IOrderService.java
│   │   │   └── impl/
│   │   │       └── OrderServiceImpl.java
│   │   ├── mapper/
│   │   │   └── OrderMapper.java
│   │   ├── domain/
│   │   │   ├── entity/
│   │   │   │   └── Order.java
│   │   │   ├── dto/
│   │   │   │   ├── CreateOrderDTO.java
│   │   │   │   └── OrderPreviewDTO.java
│   │   │   └── vo/
│   │   │       ├── OrderDetailVO.java
│   │   │       └── OrderStatusVO.java
│   │   └── config/
│   │       └── OrderConfiguration.java
│   ├── src/main/resources/
│   │   ├── bootstrap.yml
│   │   ├── application.yml
│   │   └── mapper/
│   │       └── OrderMapper.xml
│   └── pom.xml
│
└── xypai-payment/                # Payment Microservice
    ├── src/main/java/org/dromara/payment/
    │   ├── XyPaiPaymentApplication.java
    │   ├── controller/           # HTTP Controllers
    │   │   ├── PaymentController.java
    │   │   └── feign/
    │   │       └── RemotePaymentServiceImpl.java
    │   ├── service/
    │   │   ├── IPaymentService.java
    │   │   ├── IAccountService.java
    │   │   └── impl/
    │   │       ├── PaymentServiceImpl.java
    │   │       └── AccountServiceImpl.java
    │   ├── mapper/
    │   │   ├── PaymentRecordMapper.java
    │   │   ├── UserAccountMapper.java
    │   │   └── AccountTransactionMapper.java
    │   ├── domain/
    │   │   ├── entity/
    │   │   │   ├── PaymentRecord.java
    │   │   │   ├── UserAccount.java
    │   │   │   └── AccountTransaction.java
    │   │   ├── dto/
    │   │   │   ├── ExecutePaymentDTO.java
    │   │   │   └── VerifyPasswordDTO.java
    │   │   └── vo/
    │   │       ├── PaymentResultVO.java
    │   │       └── BalanceVO.java
    │   └── config/
    │       ├── PaymentConfiguration.java
    │       └── SecurityConfiguration.java
    ├── src/main/resources/
    │   ├── bootstrap.yml
    │   ├── application.yml
    │   └── mapper/
    │       ├── PaymentRecordMapper.xml
    │       ├── UserAccountMapper.xml
    │       └── AccountTransactionMapper.xml
    └── pom.xml
```

---

## Technical Stack (Aligned with Backend Guide)

- **Java**: 21 (LTS)
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.3
- **Nacos**: Service Discovery + Configuration
- **Dubbo**: 3.x for RPC
- **MySQL**: 8.0+
- **MyBatis Plus**: 3.5.7
- **Redis**: 7.0+ (Caching + Distributed Locks)
- **Sa-Token**: Authentication & Authorization

---

## Database Design

### Database: `xypai_order`

```sql
CREATE DATABASE IF NOT EXISTS `xypai_order` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `xypai_order`;

CREATE TABLE `order` (
    -- Primary Key
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '订单编号',

    -- Order Info
    `user_id` BIGINT(20) NOT NULL COMMENT '下单用户ID',
    `provider_id` BIGINT(20) NOT NULL COMMENT '服务提供者ID',
    `service_id` BIGINT(20) NOT NULL COMMENT '服务ID',
    `order_type` VARCHAR(20) NOT NULL DEFAULT 'service' COMMENT '订单类型: service/activity',

    -- Quantity & Pricing
    `quantity` INT(11) NOT NULL COMMENT '数量',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
    `service_fee` DECIMAL(10,2) NOT NULL COMMENT '服务费',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',

    -- Status
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '订单状态: pending/accepted/in_progress/completed/cancelled/refunded',
    `payment_status` VARCHAR(20) COMMENT '支付状态: pending/success/failed',
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
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_provider_id` (`provider_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

### Database: `xypai_payment`

```sql
CREATE DATABASE IF NOT EXISTS `xypai_payment` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `xypai_payment`;

-- User Account Table
CREATE TABLE `user_account` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT(20) UNIQUE NOT NULL COMMENT '用户ID',

    -- Balance
    `balance` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
    `frozen_balance` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    `total_income` DECIMAL(15,2) DEFAULT 0.00 COMMENT '累计收入',
    `total_expense` DECIMAL(15,2) DEFAULT 0.00 COMMENT '累计支出',

    -- Payment Password
    `payment_password_hash` VARCHAR(255) COMMENT '支付密码哈希(BCrypt)',
    `password_error_count` INT(11) DEFAULT 0 COMMENT '密码错误次数',
    `password_locked_until` DATETIME COMMENT '密码锁定至',

    -- Audit Fields
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账户表';

-- Payment Record Table
CREATE TABLE `payment_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `payment_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '支付流水号',

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
    `service_fee` DECIMAL(15,2) DEFAULT 0.00 COMMENT '服务费',

    -- Status
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/success/failed/refunded',
    `payment_time` DATETIME COMMENT '支付成功时间',

    -- Refund
    `refund_amount` DECIMAL(15,2) COMMENT '退款金额',
    `refund_time` DATETIME COMMENT '退款时间',

    `remark` VARCHAR(255),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_payee_id` (`payee_id`),
    KEY `idx_reference` (`reference_id`, `reference_type`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- Account Transaction Table
CREATE TABLE `account_transaction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `transaction_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '交易流水号',

    -- User & Type
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `transaction_type` VARCHAR(50) NOT NULL COMMENT '交易类型: income/expense/freeze/unfreeze',

    -- Amount
    `amount` DECIMAL(15,2) NOT NULL COMMENT '交易金额',
    `balance_before` DECIMAL(15,2) NOT NULL COMMENT '交易前余额',
    `balance_after` DECIMAL(15,2) NOT NULL COMMENT '交易后余额',

    -- Reference
    `payment_no` VARCHAR(50) COMMENT '关联支付流水号',
    `reference_id` VARCHAR(50),
    `reference_type` VARCHAR(50),
    `remark` VARCHAR(255),

    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_no` (`transaction_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户流水表';
```

---

## API Implementation Summary

### xypai-order APIs (6 HTTP + 2 RPC)

#### HTTP APIs:
1. `GET /api/order/preview` - Order preview
2. `POST /api/order/preview/update` - Update preview
3. `POST /api/order/create` - Create order
4. `GET /api/order/detail` - Get order detail
5. `GET /api/order/status` - Get order status
6. `POST /api/order/cancel` - Cancel order

#### RPC APIs:
1. `updateOrderStatus()` - Update order status (called by payment service)
2. `getOrderCount()` - Get order count (called by other services)

### xypai-payment APIs (4 HTTP + 5 RPC)

#### HTTP APIs:
1. `POST /api/payment/pay` - Execute payment
2. `POST /api/payment/verify` - Verify payment password
3. `GET /api/payment/methods` - Get payment methods
4. `GET /api/payment/balance` - Get balance

#### RPC APIs:
1. `createPayment()` - Create payment
2. `deductBalance()` - Deduct balance
3. `addBalance()` - Add balance
4. `refundBalance()` - Refund
5. `getBalance()` - Get balance (RPC)

---

## Implementation Status

✅ **Completed:**
- Project structure created
- POM files configured
- Database schemas designed
- Architecture documented

⏳ **In Progress:**
- Creating all Java source files
- Implementing business logic
- Configuring YAML files

---

**Next Steps:**
Creating all source code files systematically...
