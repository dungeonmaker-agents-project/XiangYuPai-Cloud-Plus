# Trade Module - Test Structure Summary

**Purpose:** Visual overview of planned test structure organized by frontend pages

---

## Test Organization Mapping

```
Frontend Pages → Backend Test Files

┌─────────────────────────────────────────────────────────────────┐
│  FRONTEND PAGE FLOWS (4 Pages → 5 Test Files)                  │
└─────────────────────────────────────────────────────────────────┘

📄 13-确认订单页面.md (Order Confirmation Page)
   ↓
   🧪 Page13_OrderConfirmationFlowTest.java
       • Load preview (GET /api/order/preview)
       • Adjust quantity (POST /api/order/preview/update)
       • Create order (POST /api/order/create)
       • Test data: serviceId=101, quantity=1, total=10.50


📄 14-支付页面.md (Payment Modal)
   ↓
   🧪 Page14_PaymentModalFlowTest.java
       • Execute payment (POST /api/payment/pay)
       • Verify password (POST /api/payment/verify)
       • Handle errors (wrong password, insufficient balance)
       • Test data: orderId, orderNo, password=123456


📄 15-支付成功页面.md (Payment Success Page)
   ↓
   🧪 Page15_PaymentSuccessFlowTest.java
       • Get order detail (GET /api/order/detail) [Optional]
       • Verify payment status
       • Test data: orderId, orderNo, amount=10.50


📄 16-订单详情页面.md (Order Detail Page)
   ↓
   🧪 Page16_OrderDetailFlowTest.java
       • Get order status (GET /api/order/status)
       • Simulate polling (every 5 seconds)
       • Cancel order (POST /api/order/cancel)
       • Test countdown timer, dynamic actions


🔄 Complete User Journey
   ↓
   🧪 CompleteUserJourneyTest.java
       • Page 13 → 14 → 15 → 16
       • End-to-end flow testing
       • All APIs in sequence
```

---

## Test File Count by Category

```
📁 xypai-order/src/test/java/org/dromara/order/
│
├── 📁 frontend/                    [3 files]
│   ├── Page13_OrderConfirmationFlowTest.java
│   ├── Page15_PaymentSuccessFlowTest.java
│   ├── Page16_OrderDetailFlowTest.java
│   └── CompleteUserJourneyTest.java (spans both modules)
│
├── 📁 integration/                 [2 files]
│   ├── OrderPaymentIntegrationTest.java
│   └── OrderCacheIntegrationTest.java
│
├── 📁 business/                    [3 files]
│   ├── PriceCalculationTest.java
│   ├── OrderStatusFlowTest.java
│   └── AutoCancelTimerTest.java
│
└── 📁 base/                        [1 file]
    └── OrderTestBase.java


📁 xypai-payment/src/test/java/org/dromara/payment/
│
├── 📁 frontend/                    [1 file]
│   └── Page14_PaymentModalFlowTest.java
│
├── 📁 security/                    [4 files]
│   ├── PasswordEncryptionTest.java
│   ├── AccountLockoutTest.java
│   ├── DistributedLockTest.java
│   └── OptimisticLockTest.java
│
├── 📁 integration/                 [2 files]
│   ├── PaymentOrderIntegrationTest.java
│   └── RefundIntegrationTest.java
│
├── 📁 business/                    [2 files]
│   ├── BalanceOperationsTest.java
│   └── TransactionAuditTest.java
│
└── 📁 base/                        [1 file]
    └── PaymentTestBase.java
```

**Total: 18 Test Files**

---

## API Coverage Map

### OrderService APIs (6 APIs → 3 Test Files)

| API | Frontend Page | Test File | Test Method |
|-----|---------------|-----------|-------------|
| GET /api/order/preview | Page 13 | Page13_OrderConfirmationFlowTest | testPage13_CompleteOrderConfirmationFlow() |
| POST /api/order/preview/update | Page 13 | Page13_OrderConfirmationFlowTest | testPage13_QuantityUpdateFlow() |
| POST /api/order/create | Page 13 | Page13_OrderConfirmationFlowTest | testPage13_CreateOrderFlow() |
| GET /api/order/detail | Page 15 | Page15_PaymentSuccessFlowTest | testPage15_GetOrderDetailAfterPayment() |
| GET /api/order/status | Page 16 | Page16_OrderDetailFlowTest | testPage16_PageLoadWithStatusQuery() |
| POST /api/order/cancel | Page 16 | Page16_OrderDetailFlowTest | testPage16_CancelOrderFlow() |

### PaymentService APIs (2 APIs → 1 Test File)

| API | Frontend Page | Test File | Test Method |
|-----|---------------|-----------|-------------|
| POST /api/payment/pay | Page 14 | Page14_PaymentModalFlowTest | testPage14_PaymentRequiresPassword() |
| POST /api/payment/verify | Page 14 | Page14_PaymentModalFlowTest | testPage14_PasswordVerificationSuccess() |

**Coverage:** 8/8 APIs = 100% ✅

---

## Test Data Mapping

### What Frontend Sends vs What Backend Tests

```java
// PAGE 13: Order Confirmation
Frontend Sends:
{
  serviceId: 101,
  quantity: 1
}

Backend Test Data:
OrderPreviewDTO dto = OrderPreviewDTO.builder()
    .serviceId(101L)
    .quantity(1)
    .build();

Expected Response:
{
  preview: {
    subtotal: 10.00,
    serviceFee: 0.50,  // 5% calculation
    total: 10.50
  },
  userBalance: 100.00
}
```

```java
// PAGE 14: Payment Modal
Frontend Sends (Step 1):
{
  orderId: "1234567890",
  orderNo: "20251114123456001",
  paymentMethod: "balance",
  amount: 10.50
}

Backend Test Data:
ExecutePaymentDTO dto = ExecutePaymentDTO.builder()
    .orderId("1234567890")
    .orderNo("20251114123456001")
    .paymentMethod("balance")
    .amount(new BigDecimal("10.50"))
    .build();

Expected Response:
{
  paymentStatus: "require_password",
  requirePassword: true
}
```

```java
// PAGE 14: Password Verification
Frontend Sends (Step 2):
{
  orderId: "1234567890",
  orderNo: "20251114123456001",
  paymentPassword: "123456"
}

Backend Test Data:
VerifyPasswordDTO dto = VerifyPasswordDTO.builder()
    .orderId("1234567890")
    .orderNo("20251114123456001")
    .paymentPassword("123456")
    .build();

Expected Response:
{
  paymentStatus: "success",
  balance: 89.50  // 100 - 10.50
}
```

```java
// PAGE 16: Order Status
Frontend Sends:
{
  orderId: "1234567890"
}

Backend Test Data:
String orderId = "1234567890";
OrderStatusVO status = orderService.getOrderStatus(orderId);

Expected Response:
{
  status: "pending",
  statusLabel: "等待服务者接单",
  autoCancel: {
    enabled: true,
    remainingSeconds: 580
  },
  actions: [
    { action: "cancel", label: "取消订单", enabled: true }
  ]
}
```

---

## Key Test Scenarios

### Happy Path (5 Tests)
```
✅ Complete Order Flow
   Page 13 → Preview → Update → Create
   Page 14 → Pay → Verify Password
   Page 15 → Show Success
   Page 16 → View Detail → Poll Status

✅ Payment Success Flow
   Sufficient balance → Password correct → Deduct balance → Update order

✅ Order Cancellation Flow
   Pending order → Cancel request → Refund → Update status
```

### Error Scenarios (8 Tests)
```
❌ Service not available (Page 13)
❌ Amount tampering (Page 13)
❌ Insufficient balance (Page 14)
❌ Wrong password - 1st attempt (Page 14)
❌ Wrong password - 5th attempt = lockout (Page 14)
❌ Account locked (Page 14)
❌ Cannot cancel accepted order (Page 16)
❌ Order not found (All pages)
```

### Edge Cases (5 Tests)
```
⚠️  Concurrent payment prevention (distributed lock)
⚠️  Concurrent balance update (optimistic lock)
⚠️  Auto-cancel after 10 minutes
⚠️  Status polling simulation
⚠️  Cache consistency across updates
```

---

## Implementation Priority

### Phase 1: Frontend Flow Tests (PRIORITY 1)
```
Week 1:
✅ Page13_OrderConfirmationFlowTest.java
✅ Page14_PaymentModalFlowTest.java

Week 2:
✅ Page15_PaymentSuccessFlowTest.java
✅ Page16_OrderDetailFlowTest.java
✅ CompleteUserJourneyTest.java
```

### Phase 2: Business Logic Tests (PRIORITY 2)
```
Week 2-3:
✅ PriceCalculationTest.java
✅ OrderStatusFlowTest.java
✅ BalanceOperationsTest.java
```

### Phase 3: Security Tests (PRIORITY 1 - Parallel)
```
Week 2:
✅ PasswordEncryptionTest.java
✅ AccountLockoutTest.java
✅ DistributedLockTest.java
✅ OptimisticLockTest.java
```

### Phase 4: Integration Tests (PRIORITY 3)
```
Week 3:
✅ OrderPaymentIntegrationTest.java
✅ PaymentOrderIntegrationTest.java
✅ RefundIntegrationTest.java
✅ OrderCacheIntegrationTest.java
```

---

## Test Execution Commands

### Run by Frontend Page
```bash
# Test Page 13 - Order Confirmation
mvn test -Dtest=Page13_OrderConfirmationFlowTest

# Test Page 14 - Payment Modal
mvn test -Dtest=Page14_PaymentModalFlowTest

# Test Page 16 - Order Detail
mvn test -Dtest=Page16_OrderDetailFlowTest

# Test Complete Journey
mvn test -Dtest=CompleteUserJourneyTest
```

### Run by Category
```bash
# All frontend flow tests
mvn test -Dtest=*FlowTest

# All security tests
cd xypai-payment
mvn test -Dtest=*SecurityTest,*LockTest

# All integration tests
mvn test -Dtest=*IntegrationTest
```

### Run All Tests
```bash
# Order module
cd xypai-order
mvn test

# Payment module
cd xypai-payment
mvn test
```

---

## Expected Test Output

### Page13_OrderConfirmationFlowTest
```
✅ testPage13_CompleteOrderConfirmationFlow
   → Preview loaded successfully
   → Provider info verified
   → Service fee calculated: 5%
   → Total amount: 10.50

✅ testPage13_QuantityUpdateFlow
   → Quantity updated: 1 → 3
   → Subtotal recalculated: 30.00
   → Service fee: 1.50
   → Total: 31.50

✅ testPage13_CreateOrderFlow
   → Order created
   → Order number format valid
   → Auto-cancel timer set: 10 minutes
   → Payment info returned

✅ testPage13_ServiceNotAvailable
   → Exception thrown: "服务不存在"

✅ testPage13_AmountTampering
   → Exception thrown: "订单金额不匹配"
```

### Page14_PaymentModalFlowTest
```
✅ testPage14_PaymentRequiresPassword
   → Payment status: require_password
   → requirePassword: true

✅ testPage14_PasswordVerificationSuccess
   → Password verified
   → Balance deducted: 100 → 89.50
   → Order status updated
   → Payment status: success

✅ testPage14_PasswordErrorWithRetry
   → Password error count: 1
   → Remaining attempts: 4

✅ testPage14_AccountLockoutAfter5Attempts
   → Account locked
   → Lockout duration: 30 minutes
   → Exception: "账户已锁定"

✅ testPage14_InsufficientBalance
   → Exception: "余额不足"
```

### Page16_OrderDetailFlowTest
```
✅ testPage16_PageLoadWithStatusQuery
   → Status: pending
   → Status label: "等待服务者接单"
   → Auto-cancel enabled
   → Remaining seconds: 580
   → Actions: [cancel]

✅ testPage16_StatusPollingSimulation
   → Poll 1: pending
   → Status changed: accepted
   → Poll 2: accepted
   → Actions updated: [contact]

✅ testPage16_CancelOrderFlow
   → Order cancelled
   → Refund amount: 10.50
   → Balance restored: 100.00
   → Status updated: cancelled

✅ testPage16_CannotCancelAcceptedOrder
   → Exception: "订单状态不允许取消"
   → Cancel action not in actions list
```

---

## Documentation References

| Document | Purpose | Location |
|----------|---------|----------|
| **Test Plan (Detailed)** | Complete test specifications | `TRADE_MODULE_BACKEND_TEST_PLAN.md` |
| **Test Structure (This Doc)** | Visual overview | `TRADE_MODULE_TEST_STRUCTURE_SUMMARY.md` |
| **Frontend Verification** | API specifications | `XiangYuPai-Doc/.../FRONTEND_INTERFACE_VERIFICATION.md` |
| **Frontend Page Docs** | Expected data formats | `XiangYuPai-Doc/.../Frontend/13-*.md` etc. |
| **Backend API Docs** | API implementation specs | `XiangYuPai-Doc/.../Backend/*服务接口文档.md` |

---

## Next Steps

1. ✅ **Review** this test structure
2. ⏳ **Create** test base classes (`OrderTestBase`, `PaymentTestBase`)
3. ⏳ **Implement** Page 13 tests first (simplest flow)
4. ⏳ **Implement** Page 14 tests (payment security)
5. ⏳ **Implement** Page 16 tests (status polling)
6. ⏳ **Implement** complete user journey test
7. ⏳ **Run** all tests and verify 100% pass

---

**Status:** ✅ Test Plan Ready for Implementation
**Total Test Files:** 18 files
**Estimated Test Methods:** ~80 test methods
**Expected Coverage:** 100% of frontend flows
