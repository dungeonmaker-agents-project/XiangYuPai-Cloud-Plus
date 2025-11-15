# Trade Module - API Testing Guide (cURL Examples)

**Quick Reference for Testing XiangYuPai Trade Module APIs**

---

## Setup

```bash
# Set environment variables
export GATEWAY="http://localhost:8080"
export TOKEN="Bearer your_token_here"

# Get auth token first
curl -X POST "$GATEWAY/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Extract token and set
export TOKEN="Bearer eyJhbGc..."
```

---

## Order Service APIs

### 1. Order Preview

```bash
curl -X GET "$GATEWAY/order/api/order/preview?serviceId=101&quantity=1" \
  -H "Authorization: $TOKEN" \
  | jq '.'
```

**Expected:** Order preview with price calculation

---

### 2. Update Order Preview

```bash
curl -X POST "$GATEWAY/order/api/order/preview/update" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 101,
    "quantity": 3
  }' | jq '.'
```

**Expected:** Recalculated prices for quantity=3

---

### 3. Create Order

```bash
curl -X POST "$GATEWAY/order/api/order/create" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 101,
    "quantity": 1,
    "totalAmount": 10.50
  }' | jq '.'
```

**Expected:** Order created with orderId

**Save orderId for next steps:**
```bash
export ORDER_ID="1234567890"
export ORDER_NO="20251114100001"
```

---

### 4. Get Order Detail

```bash
curl -X GET "$GATEWAY/order/api/order/detail?orderId=$ORDER_ID" \
  -H "Authorization: $TOKEN" \
  | jq '.'
```

**Expected:** Complete order details

---

### 5. Get Order Status

```bash
curl -X GET "$GATEWAY/order/api/order/status?orderId=$ORDER_ID" \
  -H "Authorization: $TOKEN" \
  | jq '.'
```

**Expected:** Order status with actions array

---

### 6. Cancel Order

```bash
curl -X POST "$GATEWAY/order/api/order/cancel" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "'$ORDER_ID'",
    "reason": "Changed my mind"
  }' | jq '.'
```

**Expected:** Order cancelled with refund info

---

## Payment Service APIs

### 1. Get Payment Methods

```bash
curl -X GET "$GATEWAY/payment/api/payment/methods" \
  -H "Authorization: $TOKEN" \
  | jq '.'
```

**Expected:** List of payment methods with balance

---

### 2. Get Balance

```bash
curl -X GET "$GATEWAY/payment/api/payment/balance" \
  -H "Authorization: $TOKEN" \
  | jq '.'
```

**Expected:** User balance information

---

### 3. Execute Payment

```bash
curl -X POST "$GATEWAY/payment/api/payment/pay" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "'$ORDER_ID'",
    "orderNo": "'$ORDER_NO'",
    "paymentMethod": "balance",
    "amount": 10.50,
    "paymentPassword": "123456"
  }' | jq '.'
```

**Expected:** Payment success with new balance

---

### 4. Verify Payment Password

```bash
curl -X POST "$GATEWAY/payment/api/payment/verify" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "'$ORDER_ID'",
    "orderNo": "'$ORDER_NO'",
    "paymentPassword": "123456"
  }' | jq '.'
```

**Expected:** Password verified and payment processed

---

## Complete Order Flow Script

```bash
#!/bin/bash

# Complete order flow test script

GATEWAY="http://localhost:8080"
TOKEN="Bearer your_token_here"

echo "=== Step 1: Order Preview ==="
PREVIEW=$(curl -s -X GET "$GATEWAY/order/api/order/preview?serviceId=101&quantity=1" \
  -H "Authorization: $TOKEN")
echo $PREVIEW | jq '.'

TOTAL=$(echo $PREVIEW | jq -r '.data.preview.total')
echo "Order Total: $TOTAL"

echo -e "\n=== Step 2: Create Order ==="
ORDER=$(curl -s -X POST "$GATEWAY/order/api/order/create" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"serviceId\":101,\"quantity\":1,\"totalAmount\":$TOTAL}")
echo $ORDER | jq '.'

ORDER_ID=$(echo $ORDER | jq -r '.data.orderId')
ORDER_NO=$(echo $ORDER | jq -r '.data.orderNo')
echo "Order ID: $ORDER_ID"
echo "Order No: $ORDER_NO"

echo -e "\n=== Step 3: Get Payment Methods ==="
curl -s -X GET "$GATEWAY/payment/api/payment/methods" \
  -H "Authorization: $TOKEN" | jq '.'

echo -e "\n=== Step 4: Execute Payment ==="
PAYMENT=$(curl -s -X POST "$GATEWAY/payment/api/payment/pay" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"orderId\":\"$ORDER_ID\",\"orderNo\":\"$ORDER_NO\",\"paymentMethod\":\"balance\",\"amount\":$TOTAL,\"paymentPassword\":\"123456\"}")
echo $PAYMENT | jq '.'

PAYMENT_STATUS=$(echo $PAYMENT | jq -r '.data.paymentStatus')
echo "Payment Status: $PAYMENT_STATUS"

echo -e "\n=== Step 5: Verify Order Status ==="
curl -s -X GET "$GATEWAY/order/api/order/status?orderId=$ORDER_ID" \
  -H "Authorization: $TOKEN" | jq '.'

echo -e "\n=== Test Complete ==="
```

**Save as:** `test_order_flow.sh`
**Run:** `chmod +x test_order_flow.sh && ./test_order_flow.sh`

---

## Error Testing

### Test Insufficient Balance

```bash
# User with low balance
curl -X POST "$GATEWAY/payment/api/payment/pay" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "'$ORDER_ID'",
    "orderNo": "'$ORDER_NO'",
    "paymentMethod": "balance",
    "amount": 999999,
    "paymentPassword": "123456"
  }' | jq '.'
```

**Expected:** `400 Bad Request - "余额不足"`

---

### Test Wrong Password

```bash
curl -X POST "$GATEWAY/payment/api/payment/verify" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "'$ORDER_ID'",
    "orderNo": "'$ORDER_NO'",
    "paymentPassword": "wrongpass"
  }' | jq '.'
```

**Expected:** `400 Bad Request - "支付密码错误"`

---

### Test Unauthorized Access

```bash
curl -X GET "$GATEWAY/order/api/order/preview?serviceId=101" \
  | jq '.'
# No Authorization header
```

**Expected:** `401 Unauthorized`

---

### Test Amount Tampering

```bash
curl -X POST "$GATEWAY/order/api/order/create" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 101,
    "quantity": 1,
    "totalAmount": 1.00
  }' | jq '.'
# Tampered amount (should be 10.50)
```

**Expected:** `400 Bad Request - "订单金额不匹配"`

---

## Database Verification Queries

### After Create Order

```sql
-- Check order created
SELECT order_no, status, total_amount, auto_cancel_time
FROM xypai_order.order
WHERE order_no = '20251114100001';
```

### After Payment

```sql
-- Check balance deducted
SELECT user_id, balance, total_expense
FROM xypai_payment.user_account
WHERE user_id = 1;

-- Check payment record
SELECT payment_no, status, amount, payment_time
FROM xypai_payment.payment_record
WHERE reference_id = '1234567890'
AND payment_type = 'order';

-- Check transaction logged
SELECT transaction_no, transaction_type, amount, balance_after
FROM xypai_payment.account_transaction
WHERE user_id = 1
ORDER BY created_at DESC
LIMIT 5;
```

### After Cancel Order

```sql
-- Check refund processed
SELECT status, refund_amount, refund_time
FROM xypai_order.order
WHERE order_no = '20251114100001';

-- Check balance restored
SELECT balance FROM xypai_payment.user_account WHERE user_id = 1;
```

---

## Redis Verification

```bash
# Check order cache
redis-cli KEYS "order:detail:*"

# Check balance cache
redis-cli GET "payment:balance:1"

# Check password errors
redis-cli GET "payment:pwd:error:1"

# Check distributed locks
redis-cli KEYS "payment:lock:*"

# Clear cache for testing
redis-cli FLUSHDB
```

---

## Performance Testing

### Apache Bench (ab)

```bash
# Test order preview (200 requests, 10 concurrent)
ab -n 200 -c 10 -H "Authorization: $TOKEN" \
  "$GATEWAY/order/api/order/preview?serviceId=101&quantity=1"

# Test balance query (500 requests, 20 concurrent)
ab -n 500 -c 20 -H "Authorization: $TOKEN" \
  "$GATEWAY/payment/api/payment/balance"
```

### wrk (Advanced)

```bash
# Install wrk: https://github.com/wg/wrk

# Create script: test.lua
cat > test.lua << 'EOF'
wrk.method = "GET"
wrk.headers["Authorization"] = "Bearer your_token"
EOF

# Run test (10 threads, 100 connections, 30 seconds)
wrk -t10 -c100 -d30s -s test.lua \
  "$GATEWAY/order/api/order/preview?serviceId=101"
```

---

## Monitoring Commands

### Check Service Health

```bash
# Order Service
curl http://localhost:9410/actuator/health | jq '.'

# Payment Service
curl http://localhost:9411/actuator/health | jq '.'
```

### Check Service Metrics

```bash
# Order Service metrics
curl http://localhost:9410/actuator/metrics | jq '.'

# Payment Service metrics
curl http://localhost:9411/actuator/metrics | jq '.'
```

### Check Nacos Registration

```bash
# List services
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=xypai-order" | jq '.'
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=xypai-payment" | jq '.'
```

---

## Troubleshooting

### Clear All Test Data

```sql
-- Clear order data
TRUNCATE TABLE xypai_order.order;

-- Reset user balance
UPDATE xypai_payment.user_account
SET balance = 100.00, total_expense = 0, total_income = 0
WHERE user_id = 1;

-- Clear payment records
TRUNCATE TABLE xypai_payment.payment_record;
TRUNCATE TABLE xypai_payment.account_transaction;
```

### Clear Redis

```bash
redis-cli FLUSHDB
```

### Restart Services

```bash
# Kill services
ps aux | grep xypai-order | awk '{print $2}' | xargs kill
ps aux | grep xypai-payment | awk '{print $2}' | xargs kill

# Restart
cd xypai-payment && mvn spring-boot:run &
cd xypai-order && mvn spring-boot:run &
```

---

## Quick Reference

| API | Method | Path |
|-----|--------|------|
| Order Preview | GET | `/order/api/order/preview` |
| Create Order | POST | `/order/api/order/create` |
| Order Status | GET | `/order/api/order/status` |
| Cancel Order | POST | `/order/api/order/cancel` |
| Payment Methods | GET | `/payment/api/payment/methods` |
| Execute Payment | POST | `/payment/api/payment/pay` |
| Get Balance | GET | `/payment/api/payment/balance` |

---

**Pro Tip:** Use `jq` for JSON formatting and `watch` for monitoring:

```bash
# Monitor balance changes
watch -n 2 "curl -s -H 'Authorization: $TOKEN' \
  '$GATEWAY/payment/api/payment/balance' | jq '.data.balance'"

# Monitor order status
watch -n 5 "curl -s -H 'Authorization: $TOKEN' \
  '$GATEWAY/order/api/order/status?orderId=$ORDER_ID' | jq '.data.status'"
```

---

**Document Version:** 1.0
**Last Updated:** 2025-11-14
