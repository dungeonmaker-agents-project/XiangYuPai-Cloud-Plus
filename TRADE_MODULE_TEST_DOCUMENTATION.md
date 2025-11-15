# Trade Module - Comprehensive Test Documentation

**Project:** XiangYuPai Trade Module
**Version:** 1.0
**Date:** 2025-11-14
**Test Environment:** Development

---

## Table of Contents

1. [Test Overview](#test-overview)
2. [Test Environment Setup](#test-environment-setup)
3. [API Test Cases](#api-test-cases)
4. [Integration Test Scenarios](#integration-test-scenarios)
5. [Security Test Cases](#security-test-cases)
6. [Performance Test Cases](#performance-test-cases)
7. [Edge Case Testing](#edge-case-testing)
8. [Test Data](#test-data)
9. [Postman Collection](#postman-collection)

---

## Test Overview

### Test Scope

**Services Under Test:**
- xypai-order (Port 9410)
- xypai-payment (Port 9411)

**Test Types:**
- ✅ Unit Tests
- ✅ API Tests
- ✅ Integration Tests
- ✅ Security Tests
- ✅ Performance Tests
- ✅ Edge Case Tests

### Test Statistics

| Category | Total Cases | Passed | Failed | Pending |
|----------|-------------|--------|--------|---------|
| API Tests | 10 | 0 | 0 | 10 |
| Integration | 8 | 0 | 0 | 8 |
| Security | 6 | 0 | 0 | 6 |
| Performance | 5 | 0 | 0 | 5 |
| Edge Cases | 8 | 0 | 0 | 8 |
| **TOTAL** | **37** | **0** | **0** | **37** |

---

## Test Environment Setup

### Prerequisites

```bash
# 1. Start infrastructure
docker-compose up -d mysql redis nacos

# 2. Create databases
mysql -u root -p < script/sql/trade_module_setup.sql

# 3. Start services
cd xypai-payment && mvn spring-boot:run &
cd xypai-order && mvn spring-boot:run &

# 4. Verify services
curl http://localhost:9410/actuator/health
curl http://localhost:9411/actuator/health
```

### Test Data

**Test Users:**
```sql
-- User 1: Buyer with 100 coins
INSERT INTO xypai_payment.user_account (user_id, balance, payment_password_hash)
VALUES (1, 100.00, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM4JZnmMfwHqXpq8.LvO');
-- Password: 123456

-- User 2: Service Provider with 50 coins
INSERT INTO xypai_payment.user_account (user_id, balance)
VALUES (2, 50.00);

-- User 3: Low balance (10 coins)
INSERT INTO xypai_payment.user_account (user_id, balance)
VALUES (3, 10.00);
```

### Environment Variables

```bash
export GATEWAY_URL="http://localhost:8080"
export ORDER_URL="http://localhost:9410"
export PAYMENT_URL="http://localhost:9411"
export TEST_TOKEN="Bearer eyJhbGc..." # Get from /auth/login
```

---

## API Test Cases

### Order Service APIs (6 Tests)

#### TEST-ORDER-001: Order Preview - Success

**Objective:** Verify order preview returns correct information

**Prerequisites:**
- User logged in (User ID: 1)
- Service exists (Service ID: 101)

**Test Steps:**
```bash
curl -X GET "$GATEWAY_URL/order/api/order/preview?serviceId=101&quantity=1" \
  -H "Authorization: $TEST_TOKEN"
```

**Expected Response:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "provider": {
      "userId": 2,
      "nickname": "TestProvider",
      "avatar": "...",
      "gender": "male"
    },
    "service": {
      "serviceId": 101,
      "name": "王者荣耀陪玩"
    },
    "price": {
      "unitPrice": 10.00,
      "unit": "局",
      "displayText": "10金币/局"
    },
    "preview": {
      "quantity": 1,
      "subtotal": 10.00,
      "serviceFee": 0.50,
      "total": 10.50
    },
    "userBalance": 100.00
  }
}
```

**Validation:**
- ✅ Status code: 200
- ✅ Response has all required fields
- ✅ Service fee = subtotal * 0.05
- ✅ Total = subtotal + serviceFee
- ✅ User balance matches database

**Test Result:** ⏳ Pending

---

#### TEST-ORDER-002: Order Preview Update - Quantity Change

**Objective:** Verify price recalculation when quantity changes

**Test Steps:**
```bash
curl -X POST "$GATEWAY_URL/order/api/order/preview/update" \
  -H "Authorization: $TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 101,
    "quantity": 3
  }'
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "quantity": 3,
    "subtotal": 30.00,
    "serviceFee": 1.50,
    "total": 31.50
  }
}
```

**Validation:**
- ✅ Subtotal = unitPrice * quantity
- ✅ Service fee recalculated correctly
- ✅ Response time < 200ms

**Test Result:** ⏳ Pending

---

#### TEST-ORDER-003: Create Order - Success

**Objective:** Verify order creation with valid data

**Test Steps:**
```bash
curl -X POST "$GATEWAY_URL/order/api/order/create" \
  -H "Authorization: $TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 101,
    "quantity": 1,
    "totalAmount": 10.50
  }'
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "20251114...",
    "amount": 10.50,
    "needPayment": true,
    "paymentInfo": {
      "amount": 10.50,
      "currency": "coin",
      "userBalance": 100.00,
      "sufficientBalance": true
    }
  }
}
```

**Validation:**
- ✅ Order created in database
- ✅ Status = 'pending'
- ✅ Order number format correct (yyyyMMddHHmmss+4digits)
- ✅ Auto-cancel timer set (10 minutes)
- ✅ needPayment = true
- ✅ sufficientBalance calculated correctly

**Database Verification:**
```sql
SELECT * FROM xypai_order.order WHERE order_no = '20251114...';
-- Should have: status='pending', auto_cancel_time=NOW()+10min
```

**Test Result:** ⏳ Pending

---

#### TEST-ORDER-004: Get Order Detail

**Objective:** Verify order detail retrieval

**Prerequisites:** Order created from TEST-ORDER-003

**Test Steps:**
```bash
curl -X GET "$GATEWAY_URL/order/api/order/detail?orderId=1234567890" \
  -H "Authorization: $TEST_TOKEN"
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "20251114...",
    "status": "pending",
    "amount": 10.50,
    "createdAt": "2025-11-14 10:30:00",
    "autoCancelTime": "2025-11-14 10:40:00",
    "provider": { ... },
    "service": { ... }
  }
}
```

**Validation:**
- ✅ All order details returned
- ✅ Data from Redis cache (first call populates cache)
- ✅ Second call served from cache (< 50ms)

**Test Result:** ⏳ Pending

---

#### TEST-ORDER-005: Get Order Status

**Objective:** Verify order status query with actions

**Test Steps:**
```bash
curl -X GET "$GATEWAY_URL/order/api/order/status?orderId=1234567890" \
  -H "Authorization: $TEST_TOKEN"
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "status": "pending",
    "statusLabel": "等待接单",
    "autoCancel": {
      "enabled": true,
      "cancelAt": "2025-11-14 10:40:00",
      "remainingSeconds": 598
    },
    "actions": [
      { "action": "cancel", "label": "取消订单", "enabled": true },
      { "action": "contact", "label": "联系服务者", "enabled": false }
    ]
  }
}
```

**Validation:**
- ✅ Status matches database
- ✅ Auto-cancel countdown accurate
- ✅ Actions array correct for 'pending' status
- ✅ cancel action enabled
- ✅ contact action disabled (not yet accepted)

**Test Result:** ⏳ Pending

---

#### TEST-ORDER-006: Cancel Order - Success

**Objective:** Verify order cancellation and refund

**Prerequisites:**
- Order from TEST-ORDER-003
- Order status = 'pending' (not yet accepted)

**Test Steps:**
```bash
curl -X POST "$GATEWAY_URL/order/api/order/cancel" \
  -H "Authorization: $TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "1234567890",
    "reason": "Changed my mind"
  }'
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "status": "cancelled",
    "refundAmount": 10.50,
    "refundTime": "2025-11-14 10:35:00",
    "balance": 100.00
  }
}
```

**Validation:**
- ✅ Order status updated to 'cancelled'
- ✅ Refund amount = order total
- ✅ User balance restored
- ✅ PaymentService.refundBalance() RPC called
- ✅ Cache invalidated

**Database Verification:**
```sql
-- Order status updated
SELECT status, cancelled_time, refund_amount
FROM xypai_order.order
WHERE order_id = 1234567890;
-- Expected: status='cancelled', refund_amount=10.50

-- Balance restored
SELECT balance FROM xypai_payment.user_account WHERE user_id = 1;
-- Expected: balance=100.00 (back to original)

-- Transaction logged
SELECT * FROM xypai_payment.account_transaction
WHERE user_id = 1 AND transaction_type = 'income'
ORDER BY created_at DESC LIMIT 1;
-- Expected: amount=10.50, remark contains 'refund'
```

**Test Result:** ⏳ Pending

---

### Payment Service APIs (4 Tests)

#### TEST-PAYMENT-001: Get Payment Methods

**Objective:** Verify available payment methods list

**Test Steps:**
```bash
curl -X GET "$GATEWAY_URL/payment/api/payment/methods" \
  -H "Authorization: $TEST_TOKEN"
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "methods": [
      {
        "type": "balance",
        "name": "金币余额",
        "icon": "coin-icon.png",
        "enabled": true,
        "requirePassword": true,
        "balance": 100.00
      },
      {
        "type": "alipay",
        "name": "支付宝",
        "icon": "alipay-icon.png",
        "enabled": true,
        "requirePassword": false
      },
      {
        "type": "wechat",
        "name": "微信支付",
        "icon": "wechat-icon.png",
        "enabled": true,
        "requirePassword": false
      }
    ]
  }
}
```

**Validation:**
- ✅ All 3 payment methods listed
- ✅ Balance method shows user's current balance
- ✅ Password requirement indicated correctly

**Test Result:** ⏳ Pending

---

#### TEST-PAYMENT-002: Get Balance

**Objective:** Verify balance query

**Test Steps:**
```bash
curl -X GET "$GATEWAY_URL/payment/api/payment/balance" \
  -H "Authorization: $TEST_TOKEN"
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "balance": 100.00,
    "frozenBalance": 0.00,
    "availableBalance": 100.00
  }
}
```

**Validation:**
- ✅ Balance matches database
- ✅ availableBalance = balance - frozenBalance
- ✅ Response from Redis cache (< 50ms)
- ✅ Auto-create account if not exists

**Test Result:** ⏳ Pending

---

#### TEST-PAYMENT-003: Execute Payment - Success

**Objective:** Verify successful balance payment

**Prerequisites:**
- Order created (from TEST-ORDER-003)
- User has sufficient balance
- Correct payment password

**Test Steps:**
```bash
curl -X POST "$GATEWAY_URL/payment/api/payment/pay" \
  -H "Authorization: $TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "1234567890",
    "orderNo": "20251114...",
    "paymentMethod": "balance",
    "amount": 10.50,
    "paymentPassword": "123456"
  }'
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "orderNo": "20251114...",
    "paymentStatus": "success",
    "balance": 89.50
  }
}
```

**Validation:**
- ✅ Payment status = 'success'
- ✅ Balance deducted: 100.00 - 10.50 = 89.50
- ✅ Payment record created
- ✅ Account transaction logged
- ✅ OrderService.updateOrderStatus() RPC called
- ✅ Distributed lock acquired and released
- ✅ Cache invalidated

**Database Verification:**
```sql
-- User balance updated
SELECT balance, total_expense FROM xypai_payment.user_account WHERE user_id = 1;
-- Expected: balance=89.50, total_expense=10.50

-- Payment record created
SELECT * FROM xypai_payment.payment_record
WHERE reference_id = '1234567890' AND payment_type = 'order';
-- Expected: status='success', amount=10.50

-- Transaction logged
SELECT * FROM xypai_payment.account_transaction
WHERE user_id = 1 AND transaction_type = 'expense'
ORDER BY created_at DESC LIMIT 1;
-- Expected: amount=10.50, balance_after=89.50

-- Order status updated (RPC call)
SELECT status, payment_status, payment_time
FROM xypai_order.order
WHERE order_id = 1234567890;
-- Expected: status='accepted', payment_status='success'
```

**Test Result:** ⏳ Pending

---

#### TEST-PAYMENT-004: Verify Payment Password - Wrong Password

**Objective:** Verify password error handling

**Test Steps:**
```bash
curl -X POST "$GATEWAY_URL/payment/api/payment/verify" \
  -H "Authorization: $TEST_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "1234567890",
    "orderNo": "20251114...",
    "paymentPassword": "wrongpass"
  }'
```

**Expected Response:**
```json
{
  "code": 400,
  "msg": "支付密码错误",
  "data": {
    "paymentStatus": "failed",
    "failureReason": "密码错误,剩余尝试次数: 4"
  }
}
```

**Validation:**
- ✅ Error message clear
- ✅ Remaining attempts shown (5 - 1 = 4)
- ✅ Error count incremented in Redis
- ✅ No balance deduction

**Redis Verification:**
```bash
redis-cli
> GET payment:pwd:error:1
# Expected: "1"
> TTL payment:pwd:error:1
# Expected: ~1800 (30 minutes)
```

**Test Result:** ⏳ Pending

---

## Integration Test Scenarios

### INTEGRATION-001: Complete Order Flow - Success Path

**Scenario:** User successfully completes an order from preview to payment

**Test Steps:**

1. **Preview Order**
   ```bash
   GET /api/order/preview?serviceId=101&quantity=1
   # Expected: 200 OK, preview data with price
   ```

2. **Create Order**
   ```bash
   POST /api/order/create
   { serviceId: 101, quantity: 1, totalAmount: 10.50 }
   # Expected: 200 OK, orderId returned
   ```

3. **Get Payment Methods**
   ```bash
   GET /api/payment/methods
   # Expected: 200 OK, balance=100.00, sufficientBalance=true
   ```

4. **Execute Payment**
   ```bash
   POST /api/payment/pay
   { orderId, orderNo, paymentMethod: "balance", amount: 10.50, paymentPassword: "123456" }
   # Expected: 200 OK, paymentStatus='success', balance=89.50
   ```

5. **Verify Order Status**
   ```bash
   GET /api/order/status?orderId=...
   # Expected: 200 OK, status='accepted', payment_status='success'
   ```

**Success Criteria:**
- ✅ All 5 steps complete without errors
- ✅ Order created and paid
- ✅ Balance deducted correctly
- ✅ Transaction logged
- ✅ Order status updated
- ✅ Total time < 2 seconds

**Test Result:** ⏳ Pending

---

### INTEGRATION-002: Order Cancellation Flow

**Scenario:** User cancels unpaid order and receives refund

**Test Steps:**

1. Create order (status='pending')
2. Pay for order (balance deducted)
3. Cancel order
4. Verify refund processed
5. Check balance restored

**Expected Database State:**
- Order status = 'cancelled'
- Refund amount = order total
- User balance = original balance
- 2 transactions logged (payment + refund)

**Test Result:** ⏳ Pending

---

### INTEGRATION-003: Insufficient Balance Flow

**Scenario:** User attempts payment with insufficient balance

**Prerequisites:** User balance = 5 coins, Order amount = 10.50 coins

**Test Steps:**

1. Create order (success)
2. Attempt payment
   ```bash
   POST /api/payment/pay
   # Expected: 400 Bad Request, "余额不足"
   ```

3. Verify no balance deduction
4. Verify order remains 'pending'

**Test Result:** ⏳ Pending

---

### INTEGRATION-004: Password Lockout Flow

**Scenario:** User locked out after 5 failed password attempts

**Test Steps:**

1. Attempt payment with wrong password (5 times)
2. Check error count increases
3. 5th attempt triggers lockout
4. Attempt payment again
   ```bash
   # Expected: 403 Forbidden, "账户已锁定,请30分钟后重试"
   ```

5. Wait 30 minutes (or clear Redis)
6. Verify can pay again

**Test Result:** ⏳ Pending

---

### INTEGRATION-005: Concurrent Payment Prevention

**Scenario:** Prevent duplicate payment for same order

**Test Steps:**

1. Create order
2. Send 2 simultaneous payment requests
3. Verify only 1 succeeds
4. Check distributed lock behavior

**Expected:**
- First request acquires lock, processes payment
- Second request waits, then fails with "订单正在支付中"

**Test Result:** ⏳ Pending

---

### INTEGRATION-006: Auto-Cancel Timer

**Scenario:** Order automatically cancelled after 10 minutes

**Test Steps:**

1. Create order at T=0
2. Check status at T=5min (should be 'pending')
3. Check status at T=11min (should be 'cancelled')
4. Verify refund processed if paid

**Note:** Use scheduled job or manual timer trigger

**Test Result:** ⏳ Pending

---

### INTEGRATION-007: Cache Consistency

**Scenario:** Verify cache invalidation on updates

**Test Steps:**

1. Query order detail (populates cache)
2. Update order status via payment
3. Query order detail again
4. Verify updated data returned (not cached old data)

**Validation:**
- ✅ Cache cleared on update
- ✅ Fresh data fetched from DB

**Test Result:** ⏳ Pending

---

### INTEGRATION-008: RPC Communication

**Scenario:** Verify inter-service RPC calls

**Test Steps:**

1. **OrderService calls PaymentService.createPayment()**
   - Create order with payment
   - Verify payment record created

2. **PaymentService calls OrderService.updateOrderStatus()**
   - Execute payment
   - Verify order status updated

**Validation:**
- ✅ RPC calls succeed
- ✅ Dubbo service discovery works
- ✅ Request/response data correct

**Test Result:** ⏳ Pending

---

## Security Test Cases

### SECURITY-001: SQL Injection Prevention

**Objective:** Verify protection against SQL injection

**Test Steps:**
```bash
curl -X GET "$GATEWAY_URL/order/api/order/detail?orderId=123' OR '1'='1" \
  -H "Authorization: $TEST_TOKEN"
```

**Expected:**
- ✅ No SQL injection
- ✅ Either 404 (order not found) or parameter validation error

**Test Result:** ⏳ Pending

---

### SECURITY-002: Authentication Required

**Objective:** Verify all APIs require valid token

**Test Steps:**
```bash
curl -X GET "$GATEWAY_URL/order/api/order/preview?serviceId=101"
# No Authorization header
```

**Expected:**
- ✅ 401 Unauthorized
- ✅ Error message: "未登录"

**Test Result:** ⏳ Pending

---

### SECURITY-003: Password Encryption

**Objective:** Verify payment password stored encrypted

**Test Steps:**

1. Create user with password "123456"
2. Check database:
   ```sql
   SELECT payment_password_hash FROM user_account WHERE user_id = 1;
   ```

**Expected:**
- ✅ Password hashed with BCrypt
- ✅ Hash starts with "$2a$10$"
- ✅ Cannot reverse hash to plaintext

**Test Result:** ⏳ Pending

---

### SECURITY-004: Amount Tampering Prevention

**Objective:** Verify order amount cannot be manipulated

**Test Steps:**

1. Get order preview (amount = 10.50)
2. Create order with tampered amount:
   ```json
   { "serviceId": 101, "quantity": 1, "totalAmount": 1.00 }
   ```

**Expected:**
- ✅ 400 Bad Request
- ✅ Error: "订单金额不匹配"

**Test Result:** ⏳ Pending

---

### SECURITY-005: Permission Check

**Objective:** Verify users can only access own orders

**Test Steps:**

1. User 1 creates order
2. User 2 attempts to view User 1's order

**Expected:**
- ✅ 403 Forbidden
- ✅ Error: "无权访问此订单"

**Test Result:** ⏳ Pending

---

### SECURITY-006: Rate Limiting

**Objective:** Verify rate limiting prevents abuse

**Test Steps:**

1. Send 1000 requests in 1 second to same endpoint

**Expected:**
- ✅ After limit reached, 429 Too Many Requests
- ✅ Error: "请求过于频繁"
- ✅ `@RateLimiter` annotation working

**Test Result:** ⏳ Pending

---

## Performance Test Cases

### PERF-001: Order Preview Response Time

**Objective:** P95 < 200ms

**Load:**
- 200 concurrent users
- Duration: 5 minutes

**Expected:**
- ✅ P50 < 100ms
- ✅ P95 < 200ms
- ✅ P99 < 300ms
- ✅ No errors

**Test Result:** ⏳ Pending

---

### PERF-002: Create Order Throughput

**Objective:** Support 100 QPS

**Load:**
- 100 requests/second
- Duration: 5 minutes

**Expected:**
- ✅ All requests succeed
- ✅ P95 response time < 300ms
- ✅ No database deadlocks

**Test Result:** ⏳ Pending

---

### PERF-003: Payment Execution Under Load

**Objective:** Support 200 QPS concurrent payments

**Load:**
- 200 concurrent payment requests
- Each with different orderId

**Expected:**
- ✅ No duplicate payments
- ✅ All locks acquired/released correctly
- ✅ P95 < 300ms

**Test Result:** ⏳ Pending

---

### PERF-004: Cache Hit Rate

**Objective:** Cache hit rate > 80% for order details

**Test:**
- Query same orders repeatedly
- Measure cache hits vs misses

**Expected:**
- ✅ First query: cache miss (from DB)
- ✅ Subsequent queries: cache hit (from Redis)
- ✅ Cache hit rate > 80%

**Test Result:** ⏳ Pending

---

### PERF-005: Database Connection Pool

**Objective:** No connection pool exhaustion

**Load:**
- 500 concurrent requests
- Monitor connection pool

**Expected:**
- ✅ Max connections < pool size (20)
- ✅ No "connection timeout" errors
- ✅ Connections released promptly

**Test Result:** ⏳ Pending

---

## Edge Case Testing

### EDGE-001: Zero Balance Payment

**Scenario:** User with 0 balance attempts payment

**Expected:**
- ✅ 400 Bad Request
- ✅ "余额不足"

**Test Result:** ⏳ Pending

---

### EDGE-002: Negative Quantity

**Scenario:** Create order with quantity = -1

**Expected:**
- ✅ 400 Bad Request
- ✅ Validation error: "数量必须大于0"

**Test Result:** ⏳ Pending

---

### EDGE-003: Extremely Large Amount

**Scenario:** Order amount > 10000 coins

**Expected:**
- ✅ Warning logged (potential fraud)
- ✅ Manual review triggered

**Test Result:** ⏳ Pending

---

### EDGE-004: Non-existent Order

**Scenario:** Query order that doesn't exist

**Expected:**
- ✅ 404 Not Found
- ✅ "订单不存在"

**Test Result:** ⏳ Pending

---

### EDGE-005: Duplicate Order Number

**Scenario:** System generates duplicate order number (unlikely)

**Expected:**
- ✅ Database unique constraint prevents insertion
- ✅ System retries with new number

**Test Result:** ⏳ Pending

---

### EDGE-006: Service Unavailable

**Scenario:** PaymentService down during order creation

**Expected:**
- ✅ Circuit breaker opens
- ✅ Graceful degradation
- ✅ Error message to user

**Test Result:** ⏳ Pending

---

### EDGE-007: Redis Failure

**Scenario:** Redis server stops

**Expected:**
- ✅ APIs still work (slower, direct DB queries)
- ✅ Locks fail-safe (allow payment to proceed)
- ✅ No data loss

**Test Result:** ⏳ Pending

---

### EDGE-008: Database Deadlock

**Scenario:** Concurrent updates cause deadlock

**Expected:**
- ✅ Deadlock detected by MySQL
- ✅ One transaction rolled back
- ✅ Application retries

**Test Result:** ⏳ Pending

---

## Test Data

### Sample Test Orders

```sql
-- Order 1: Pending payment
INSERT INTO xypai_order.order (order_no, user_id, provider_id, service_id, quantity, unit_price, subtotal, service_fee, total_amount, status)
VALUES ('20251114100001', 1, 2, 101, 1, 10.00, 10.00, 0.50, 10.50, 'pending');

-- Order 2: Paid and accepted
INSERT INTO xypai_order.order (order_no, user_id, provider_id, service_id, quantity, unit_price, subtotal, service_fee, total_amount, status, payment_status, payment_time)
VALUES ('20251114100002', 1, 2, 101, 2, 10.00, 20.00, 1.00, 21.00, 'accepted', 'success', NOW());

-- Order 3: Cancelled
INSERT INTO xypai_order.order (order_no, user_id, provider_id, service_id, quantity, unit_price, subtotal, service_fee, total_amount, status, cancelled_time, refund_amount)
VALUES ('20251114100003', 1, 2, 101, 1, 10.00, 10.00, 0.50, 10.50, 'cancelled', NOW(), 10.50);
```

---

## Postman Collection

### Collection Structure

```
XiangYuPai Trade Module Tests/
├── Order Service/
│   ├── 01-Order Preview
│   ├── 02-Update Preview
│   ├── 03-Create Order
│   ├── 04-Get Order Detail
│   ├── 05-Get Order Status
│   └── 06-Cancel Order
│
├── Payment Service/
│   ├── 01-Get Payment Methods
│   ├── 02-Get Balance
│   ├── 03-Execute Payment
│   └── 04-Verify Password
│
└── Integration Tests/
    ├── Complete Flow - Success
    ├── Complete Flow - Insufficient Balance
    ├── Complete Flow - Cancel Order
    └── Complete Flow - Password Lockout
```

### Environment Variables

```json
{
  "gateway_url": "http://localhost:8080",
  "order_url": "http://localhost:9410",
  "payment_url": "http://localhost:9411",
  "test_token": "Bearer {{access_token}}",
  "test_user_id": "1",
  "test_service_id": "101"
}
```

### Pre-request Scripts

```javascript
// Get fresh access token
if (!pm.environment.get("access_token") || tokenExpired()) {
    pm.sendRequest({
        url: pm.environment.get("gateway_url") + "/auth/login",
        method: "POST",
        header: "Content-Type: application/json",
        body: {
            mode: "raw",
            raw: JSON.stringify({
                username: "testuser",
                password: "password123"
            })
        }
    }, function(err, response) {
        const token = response.json().data.access_token;
        pm.environment.set("access_token", token);
    });
}
```

---

## Test Execution Checklist

### Before Testing

- [ ] All services running and healthy
- [ ] Databases created and populated with test data
- [ ] Redis running
- [ ] Nacos running
- [ ] Gateway running
- [ ] Test user accounts created
- [ ] Postman collection imported

### During Testing

- [ ] Run unit tests first
- [ ] Run API tests individually
- [ ] Run integration tests
- [ ] Run security tests
- [ ] Run performance tests
- [ ] Document all failures

### After Testing

- [ ] Generate test report
- [ ] Review failed tests
- [ ] Create bug tickets for failures
- [ ] Update test cases based on findings
- [ ] Archive test results

---

## Test Report Template

```markdown
# Test Execution Report

**Date:** 2025-11-14
**Tester:** [Name]
**Environment:** Development

## Summary
- Total Cases: 37
- Passed: X
- Failed: Y
- Skipped: Z

## Failed Tests
1. TEST-XXX: [Description]
   - Reason: [Error message]
   - Screenshots: [Link]
   - Bug Ticket: [JIRA-123]

## Performance Metrics
- Average Response Time: X ms
- P95 Response Time: Y ms
- Cache Hit Rate: Z%

## Recommendations
- [Action items]
```

---

**Document Version:** 1.0
**Last Updated:** 2025-11-14
**Next Review:** After first test cycle
