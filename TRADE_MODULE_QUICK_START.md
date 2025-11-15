# Trade Module - Quick Start Guide

## Overview

This guide helps you quickly start the XiangYuPai Trade Module (Order + Payment services).

---

## Prerequisites

✅ MySQL 8.0+ running
✅ Redis 7.0+ running
✅ Nacos 2.x running (on localhost:8848)
✅ RuoYi-Cloud-Plus gateway running (generates Same-Token)

---

## Step 1: Database Setup (2 minutes)

```bash
# Run the SQL setup script
mysql -u root -p < script/sql/trade_module_setup.sql

# Verify databases created
mysql -u root -p -e "SHOW DATABASES LIKE 'xypai_%';"
```

**Expected Output:**
```
xypai_order
xypai_payment
```

---

## Step 2: Nacos Configuration (3 minutes)

### 2.1 Create Common Configurations

Login to Nacos console: http://localhost:8848/nacos (nacos/nacos)

**Create `common-mysql.yml`** (if not exists):
```yaml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      max-lifetime: 1800000
```

**Create `common-redis.yml`** (if not exists):
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

**Create `common-dubbo.yml`** (if not exists):
```yaml
dubbo:
  protocol:
    name: dubbo
    port: -1
    serialization: hessian2
  consumer:
    timeout: 10000
    retries: 0
    check: false
  provider:
    timeout: 10000
    retries: 0
```

### 2.2 Create Service-Specific Configurations

**Create `xypai-order-dev.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_order?useSSL=false&serverTimezone=GMT%2B8&characterEncoding=utf-8
    username: root
    password: your_password_here

xypai:
  order:
    service-fee-rate: 0.05  # 5% service fee
    auto-cancel-minutes: 10  # Auto-cancel after 10 minutes
```

**Create `xypai-payment-dev.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_payment?useSSL=false&serverTimezone=GMT%2B8&characterEncoding=utf-8
    username: root
    password: your_password_here

xypai:
  payment:
    password-max-errors: 5
    password-lockout-minutes: 30
```

---

## Step 3: Build Projects (1 minute)

```bash
cd e:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus

# Build API modules first
cd ruoyi-api
mvn clean install -DskipTests

# Return to root
cd ..
```

---

## Step 4: Start Services (in order!)

### 4.1 Start Payment Service (Port 9411)

```bash
# Terminal 1
cd xypai-payment
mvn spring-boot:run

# Wait for: "Started XyPaiPaymentApplication in X seconds"
```

### 4.2 Start Order Service (Port 9410)

```bash
# Terminal 2
cd xypai-order
mvn spring-boot:run

# Wait for: "Started XyPaiOrderApplication in X seconds"
```

---

## Step 5: Verify Services

### 5.1 Check Nacos Registry

Visit: http://localhost:8848/nacos

**Services List** should show:
- ✅ xypai-order (1 instance)
- ✅ xypai-payment (1 instance)

### 5.2 Check Health Endpoints

```bash
# Payment service
curl http://localhost:9411/actuator/health

# Order service
curl http://localhost:9410/actuator/health
```

**Expected:** `{"status":"UP"}`

### 5.3 Check Swagger API Documentation

- **Order Service**: http://localhost:9410/doc.html
- **Payment Service**: http://localhost:9411/doc.html

---

## Step 6: Test Complete Flow

### 6.1 Get JWT Token (via Gateway)

```bash
# Login via gateway
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Copy the token from response
```

### 6.2 Test Order Preview

```bash
curl -X GET "http://localhost:8080/order/api/order/preview?serviceId=101&quantity=1" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Expected Response:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "provider": { ... },
    "service": { ... },
    "price": {
      "unitPrice": 10.00,
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

### 6.3 Test Create Order

```bash
curl -X POST "http://localhost:8080/order/api/order/create" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
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
  "msg": "success",
  "data": {
    "orderId": "1234567890",
    "orderNo": "20251114100002",
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

### 6.4 Test Payment

```bash
curl -X POST "http://localhost:8080/payment/api/payment/pay" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "1234567890",
    "orderNo": "20251114100002",
    "paymentMethod": "balance",
    "amount": 10.50,
    "paymentPassword": "123456"
  }'
```

**Expected Response:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "orderId": "1234567890",
    "orderNo": "20251114100002",
    "paymentStatus": "success",
    "balance": 89.50
  }
}
```

### 6.5 Verify Order Status

```bash
curl -X GET "http://localhost:8080/order/api/order/status?orderId=1234567890" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Expected:** Status should be `accepted` or `in_progress`

---

## Troubleshooting

### Issue: "No provider available for RemoteXxxService"

**Cause:** Dubbo RPC not registered

**Solution:**
1. Check both services are running
2. Check Nacos service list shows both services
3. Restart services in order: payment → order

### Issue: "Same-Token验证失败"

**Cause:** Gateway not running or Redis not shared

**Solution:**
1. Start gateway first: `cd ruoyi-gateway && mvn spring-boot:run`
2. Ensure all services use same Redis instance
3. Check `check-same-token: true` in gateway config

### Issue: Database connection failed

**Cause:** Wrong credentials or database not created

**Solution:**
1. Verify MySQL is running: `mysql -u root -p -e "SELECT 1"`
2. Check databases exist: `SHOW DATABASES LIKE 'xypai_%'`
3. Update Nacos config with correct password

### Issue: Port already in use

**Cause:** Service already running or port conflict

**Solution:**
```bash
# Check what's using the port
netstat -ano | findstr :9410
netstat -ano | findstr :9411

# Kill the process or change port in application.yml
```

---

## Monitoring

### Check Redis Cache

```bash
redis-cli

# Check order cache
KEYS order:detail:*

# Check balance cache
KEYS payment:balance:*

# Check password errors
KEYS payment:pwd:error:*

# Check distributed locks
KEYS payment:lock:*
```

### Check Logs

```bash
# Order service logs
tail -f xypai-order/logs/xypai-order.log

# Payment service logs
tail -f xypai-payment/logs/xypai-payment.log
```

---

## Service Ports Reference

| Service | Port | Swagger UI | Database |
|---------|------|------------|----------|
| Gateway | 8080 | - | - |
| xypai-order | 9410 | http://localhost:9410/doc.html | xypai_order |
| xypai-payment | 9411 | http://localhost:9411/doc.html | xypai_payment |

---

## API Endpoints Summary

### Order Service (via Gateway: /order/*)

- `GET /api/order/preview` - Preview order
- `POST /api/order/preview/update` - Update preview
- `POST /api/order/create` - Create order
- `GET /api/order/detail` - Get order detail
- `GET /api/order/status` - Get order status
- `POST /api/order/cancel` - Cancel order

### Payment Service (via Gateway: /payment/*)

- `POST /api/payment/pay` - Execute payment
- `POST /api/payment/verify` - Verify password
- `GET /api/payment/methods` - Get payment methods
- `GET /api/payment/balance` - Get balance

---

## Default Test Accounts

| User ID | Balance | Password | Notes |
|---------|---------|----------|-------|
| 1 | 100.00 | 123456 | Test buyer |
| 2 | 50.00 | - | Test provider |

---

## Next Steps

1. ✅ Services running
2. ✅ APIs tested via Swagger
3. ⏳ Connect frontend application
4. ⏳ Add more test scenarios
5. ⏳ Deploy to production environment

---

**Need Help?**
- Check logs in `xypai-order/logs/` and `xypai-payment/logs/`
- Review [TRADE_MODULE_IMPLEMENTATION.md](./TRADE_MODULE_IMPLEMENTATION.md) for architecture details
- Consult [BACKEND_TECH_STACK_GUIDE.md](../XiangYuPai-Doc/启动/BACKEND_TECH_STACK_GUIDE.md) for patterns

---

**Status:** ✅ Ready for Development
**Version:** 1.0.0
**Last Updated:** 2025-11-14
