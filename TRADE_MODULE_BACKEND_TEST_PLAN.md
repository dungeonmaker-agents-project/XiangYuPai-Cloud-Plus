# Trade Module - Backend Test Plan Based on Frontend Flows

**Date:** 2025-11-14
**Purpose:** Plan backend testing organized by frontend page flows and user journeys
**Modules:** xypai-order, xypai-payment

---

## Test Strategy Overview

### Testing Approach

**Frontend-Driven Testing:** Organize backend tests to mirror actual frontend user flows, ensuring that:
1. Each frontend page's API calls are tested as a cohesive flow
2. Test data matches what frontend actually sends
3. Response validation matches what frontend expects
4. Error scenarios match frontend error handling

**Test Organization:**
- **Page Flow Tests:** One test file per frontend page flow
- **Integration Tests:** Cross-service communication (Order ↔ Payment)
- **Business Logic Tests:** Service fee calculation, status transitions, etc.
- **Security Tests:** Password validation, account lockout, distributed locks
- **Edge Case Tests:** Boundary conditions, race conditions, error recovery

---

## Module Analysis

### OrderService Implementation ✅

**Controller:** `OrderController.java`
- `/api/order/preview` (GET)
- `/api/order/preview/update` (POST)
- `/api/order/create` (POST)
- `/api/order/detail` (GET)
- `/api/order/status` (GET)
- `/api/order/cancel` (POST)

**DTOs (What Frontend Sends):**
- `OrderPreviewDTO` - serviceId, quantity
- `UpdateOrderPreviewDTO` - serviceId, quantity
- `CreateOrderDTO` - serviceId, quantity, totalAmount
- `CancelOrderDTO` - orderId, reason

**VOs (What Backend Returns):**
- `OrderPreviewVO` - provider info, service info, price info, preview calculation
- `OrderCreateResultVO` - orderId, orderNo, needPayment, paymentInfo
- `OrderDetailVO` - complete order details
- `OrderStatusVO` - status, actions, autoCancel info
- `OrderCancelResultVO` - refund info, balance

**Service Methods:**
- `preview()` - Validate service, calculate price, query user balance
- `updatePreview()` - Recalculate based on quantity change
- `createOrder()` - Create order, auto-cancel timer, return payment info
- `getOrderDetail()` - Query order with Redis cache
- `getOrderStatus()` - Query status, calculate remaining time, dynamic actions
- `cancelOrder()` - Cancel order, call PaymentService RPC for refund
- `updateOrderStatus()` - RPC method for PaymentService to update order
- `getOrderCount()` - RPC method for statistics

### PaymentService Implementation ✅

**Controller:** `PaymentController.java`
- `/api/payment/pay` (POST)
- `/api/payment/verify` (POST)
- `/api/payment/methods` (GET)
- `/api/payment/balance` (GET)

**DTOs (What Frontend Sends):**
- `ExecutePaymentDTO` - orderId, orderNo, paymentMethod, amount, paymentPassword (optional)
- `VerifyPasswordDTO` - orderId, orderNo, paymentPassword

**VOs (What Backend Returns):**
- `PaymentResultVO` - paymentStatus, requirePassword, balance, failureReason
- `PaymentMethodVO` - available payment methods
- `BalanceVO` - user balance info

**Service Methods:**
- `executePayment()` - Execute payment, check balance, BCrypt validation
- `verifyPassword()` - Verify password, deduct balance, update order via RPC
- `getPaymentMethods()` - Return available payment methods
- Account service: Balance queries, deduction, refunds

**Entities:**
- `UserAccount` - userId, balance, frozenBalance, passwordHash, errorCount, lockedUntil, version
- `PaymentRecord` - Payment transaction records
- `AccountTransaction` - Audit trail for all balance changes

---

## Test File Structure Plan

### Based on Frontend Pages (Primary Organization)

```
xypai-order/src/test/java/org/dromara/order/
├── frontend/                                    # Frontend flow tests
│   ├── Page13_OrderConfirmationFlowTest.java   # 13-确认订单页面
│   ├── Page15_PaymentSuccessFlowTest.java      # 15-支付成功页面
│   └── Page16_OrderDetailFlowTest.java         # 16-订单详情页面
│
├── integration/                                 # Integration tests
│   ├── OrderPaymentIntegrationTest.java        # Order ↔ Payment RPC
│   └── OrderCacheIntegrationTest.java          # Redis cache testing
│
├── business/                                    # Business logic tests
│   ├── PriceCalculationTest.java               # Service fee (5%)
│   ├── OrderStatusFlowTest.java                # Status transitions
│   └── AutoCancelTimerTest.java                # 10-minute auto-cancel
│
└── base/
    └── OrderTestBase.java                       # Common test setup

xypai-payment/src/test/java/org/dromara/payment/
├── frontend/                                    # Frontend flow tests
│   └── Page14_PaymentModalFlowTest.java        # 14-支付页面
│
├── security/                                    # Security tests
│   ├── PasswordEncryptionTest.java             # BCrypt testing
│   ├── AccountLockoutTest.java                 # 5 attempts, 30-min lockout
│   ├── DistributedLockTest.java                # Prevent duplicate payments
│   └── OptimisticLockTest.java                 # Balance update concurrency
│
├── integration/                                 # Integration tests
│   ├── PaymentOrderIntegrationTest.java        # Payment → Order RPC
│   └── RefundIntegrationTest.java              # Refund flow testing
│
├── business/                                    # Business logic tests
│   ├── BalanceOperationsTest.java              # Deduct/add/query balance
│   └── TransactionAuditTest.java               # Audit trail verification
│
└── base/
    └── PaymentTestBase.java                     # Common test setup
```

**Total Test Files:** 17 files

---

## Test File Details

### Frontend Flow Tests (5 Files) - PRIMARY FOCUS

These tests simulate exact frontend user flows, using the same data format frontend sends.

#### 1. Page13_OrderConfirmationFlowTest.java
**Location:** `xypai-order/src/test/java/org/dromara/order/frontend/`

**Purpose:** Test complete "确认订单页面" (Order Confirmation Page) flow

**Frontend Flow:**
```
User enters page → GET /api/order/preview
User adjusts quantity → POST /api/order/preview/update
User clicks "立即支付" → POST /api/order/create
→ Show payment modal (next page)
```

**Test Cases:**

**Test 1: Complete Page Load Flow**
```java
@Test
void testPage13_CompleteOrderConfirmationFlow() {
    // GIVEN: Frontend enters page with serviceId=101
    Long serviceId = 101L;
    Integer quantity = 1;

    // WHEN: Step 1 - Page loads, call preview API
    OrderPreviewDTO previewRequest = OrderPreviewDTO.builder()
        .serviceId(serviceId)
        .quantity(quantity)
        .build();
    OrderPreviewVO previewResponse = orderService.preview(previewRequest);

    // THEN: Verify response matches frontend expectation
    assertThat(previewResponse).isNotNull();
    assertThat(previewResponse.getProvider()).isNotNull();
    assertThat(previewResponse.getService().getServiceId()).isEqualTo(101L);
    assertThat(previewResponse.getPrice().getUnitPrice()).isEqualTo(new BigDecimal("10.00"));
    assertThat(previewResponse.getPreview().getServiceFee())
        .isEqualTo(new BigDecimal("0.50")); // 5% of 10.00
    assertThat(previewResponse.getPreview().getTotal())
        .isEqualTo(new BigDecimal("10.50"));
    assertThat(previewResponse.getUserBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
}
```

**Test 2: Quantity Update Flow**
```java
@Test
void testPage13_QuantityUpdateFlow() {
    // GIVEN: User adjusts quantity from 1 to 3
    UpdateOrderPreviewDTO updateRequest = UpdateOrderPreviewDTO.builder()
        .serviceId(101L)
        .quantity(3)
        .build();

    // WHEN: Frontend calls /api/order/preview/update
    OrderPreviewVO.PreviewInfo updatedPreview = orderService.updatePreview(updateRequest);

    // THEN: Verify real-time calculation
    assertThat(updatedPreview.getQuantity()).isEqualTo(3);
    assertThat(updatedPreview.getSubtotal()).isEqualTo(new BigDecimal("30.00")); // 10*3
    assertThat(updatedPreview.getServiceFee()).isEqualTo(new BigDecimal("1.50")); // 5%
    assertThat(updatedPreview.getTotal()).isEqualTo(new BigDecimal("31.50"));
}
```

**Test 3: Create Order Flow**
```java
@Test
void testPage13_CreateOrderFlow() {
    // GIVEN: User clicks "立即支付"
    CreateOrderDTO createRequest = CreateOrderDTO.builder()
        .serviceId(101L)
        .quantity(1)
        .totalAmount(new BigDecimal("10.50"))
        .build();

    // WHEN: Frontend calls /api/order/create
    OrderCreateResultVO createResponse = orderService.createOrder(createRequest);

    // THEN: Verify order created and payment info returned
    assertThat(createResponse.getOrderId()).isNotNull();
    assertThat(createResponse.getOrderNo()).matches("\\d{14}\\d{4}"); // yyyyMMddHHmmss + 4 digits
    assertThat(createResponse.getNeedPayment()).isTrue();
    assertThat(createResponse.getPaymentInfo()).isNotNull();
    assertThat(createResponse.getPaymentInfo().getAmount()).isEqualTo(new BigDecimal("10.50"));
    assertThat(createResponse.getPaymentInfo().getCurrency()).isEqualTo("coin");

    // Verify auto-cancel timer set (10 minutes)
    Order order = orderMapper.selectById(createResponse.getOrderId());
    assertThat(order.getAutoCancelTime()).isAfter(LocalDateTime.now().plusMinutes(9));
}
```

**Test 4: Error Scenarios**
```java
@Test
void testPage13_ServiceNotAvailable() {
    // Frontend sends invalid serviceId
    OrderPreviewDTO request = OrderPreviewDTO.builder()
        .serviceId(999L) // Non-existent
        .build();

    // Expect exception matching frontend error handling
    assertThatThrownBy(() -> orderService.preview(request))
        .hasMessageContaining("服务不存在");
}

@Test
void testPage13_AmountTampering() {
    // Frontend sends tampered amount (10.50 should be 10.50, but sends 5.00)
    CreateOrderDTO request = CreateOrderDTO.builder()
        .serviceId(101L)
        .quantity(1)
        .totalAmount(new BigDecimal("5.00")) // Wrong!
        .build();

    assertThatThrownBy(() -> orderService.createOrder(request))
        .hasMessageContaining("订单金额不匹配");
}
```

**Frontend Data to Test With:**
```javascript
// From 13-确认订单页面.md
Request: {
  serviceId: 101,
  quantity: 1
}

Expected Response: {
  provider: { userId, avatar, nickname, gender, age, tags, skillInfo },
  service: { serviceId, name, icon },
  price: { unitPrice: 10.00, unit: "局", displayText: "10金币/局" },
  quantityOptions: { min: 1, max: 10, default: 1 },
  preview: { quantity: 1, subtotal: 10.00, serviceFee: 0.50, total: 10.50 },
  userBalance: 100.00
}
```

---

#### 2. Page14_PaymentModalFlowTest.java
**Location:** `xypai-payment/src/test/java/org/dromara/payment/frontend/`

**Purpose:** Test complete "支付页面" (Payment Modal) flow

**Frontend Flow:**
```
Modal appears after order created
User clicks "立即支付" → POST /api/payment/pay
If requirePassword → Show password input
User enters 6-digit password → POST /api/payment/verify
If success → Navigate to success page
```

**Test Cases:**

**Test 1: Payment Requires Password**
```java
@Test
void testPage14_PaymentRequiresPassword() {
    // GIVEN: Frontend shows payment modal after order created
    String orderId = "1234567890";
    String orderNo = "20251114123456001";

    // WHEN: Step 1 - User clicks "立即支付" WITHOUT password
    ExecutePaymentDTO paymentRequest = ExecutePaymentDTO.builder()
        .orderId(orderId)
        .orderNo(orderNo)
        .paymentMethod("balance")
        .amount(new BigDecimal("10.50"))
        .paymentPassword(null) // Frontend doesn't send password initially
        .build();

    PaymentResultVO result = paymentService.executePayment(paymentRequest);

    // THEN: Backend should return requirePassword=true
    assertThat(result.getPaymentStatus()).isEqualTo("require_password");
    assertThat(result.getRequirePassword()).isTrue();

    // Frontend now shows password input modal
}
```

**Test 2: Password Verification Success**
```java
@Test
void testPage14_PasswordVerificationSuccess() {
    // GIVEN: User has entered 6-digit password "123456"
    VerifyPasswordDTO verifyRequest = VerifyPasswordDTO.builder()
        .orderId(orderId)
        .orderNo(orderNo)
        .paymentPassword("123456")
        .build();

    // WHEN: Frontend auto-submits after 6 digits
    PaymentResultVO result = paymentService.verifyPassword(verifyRequest);

    // THEN: Payment should succeed
    assertThat(result.getPaymentStatus()).isEqualTo("success");
    assertThat(result.getBalance()).isEqualTo(new BigDecimal("89.50")); // 100 - 10.50

    // Verify balance deducted in database
    UserAccount account = accountMapper.selectByUserId(testUserId);
    assertThat(account.getBalance()).isEqualTo(new BigDecimal("89.50"));

    // Verify order status updated via RPC
    Order order = orderMapper.selectById(orderId);
    assertThat(order.getPaymentStatus()).isEqualTo("paid");
}
```

**Test 3: Password Error Handling**
```java
@Test
void testPage14_PasswordErrorWithRetry() {
    // GIVEN: User enters wrong password
    VerifyPasswordDTO wrongPassword = VerifyPasswordDTO.builder()
        .orderId(orderId)
        .orderNo(orderNo)
        .paymentPassword("wrong123")
        .build();

    // WHEN: Attempt 1 - Wrong password
    assertThatThrownBy(() -> paymentService.verifyPassword(wrongPassword))
        .hasMessageContaining("支付密码错误");

    // THEN: Error count should increment
    UserAccount account = accountMapper.selectByUserId(testUserId);
    assertThat(account.getPasswordErrorCount()).isEqualTo(1);

    // Frontend shows: "密码错误，还剩4次机会"
}
```

**Test 4: Account Lockout After 5 Attempts**
```java
@Test
void testPage14_AccountLockoutAfter5Attempts() {
    // GIVEN: User has already failed 4 times
    UserAccount account = accountMapper.selectByUserId(testUserId);
    account.setPasswordErrorCount(4);
    accountMapper.updateById(account);

    // WHEN: 5th attempt fails
    VerifyPasswordDTO wrongPassword = VerifyPasswordDTO.builder()
        .orderId(orderId)
        .orderNo(orderNo)
        .paymentPassword("wrong123")
        .build();

    assertThatThrownBy(() -> paymentService.verifyPassword(wrongPassword))
        .hasMessageContaining("账户已锁定");

    // THEN: Account should be locked for 30 minutes
    account = accountMapper.selectByUserId(testUserId);
    assertThat(account.getPasswordErrorCount()).isEqualTo(5);
    assertThat(account.getLockedUntil()).isAfter(LocalDateTime.now().plusMinutes(29));

    // Frontend shows: "密码错误次数过多，请30分钟后再试"
}
```

**Test 5: Insufficient Balance**
```java
@Test
void testPage14_InsufficientBalance() {
    // GIVEN: User balance is only 5 coins, order costs 10.50
    UserAccount account = accountMapper.selectByUserId(testUserId);
    account.setBalance(new BigDecimal("5.00"));
    accountMapper.updateById(account);

    // WHEN: User attempts payment
    VerifyPasswordDTO request = VerifyPasswordDTO.builder()
        .orderId(orderId)
        .orderNo(orderNo)
        .paymentPassword("123456")
        .build();

    // THEN: Payment should fail
    assertThatThrownBy(() -> paymentService.verifyPassword(request))
        .hasMessageContaining("余额不足");

    // Frontend shows: "余额不足，请先充值"
}
```

**Frontend Data to Test With:**
```javascript
// From 14-支付页面.md
Request (Step 1): {
  orderId: "1234567890",
  orderNo: "20251114123456001",
  paymentMethod: "balance",
  amount: 10.50,
  paymentPassword: undefined
}

Expected Response: {
  paymentStatus: "require_password",
  requirePassword: true
}

Request (Step 2): {
  orderId: "1234567890",
  orderNo: "20251114123456001",
  paymentPassword: "123456"
}

Expected Response: {
  paymentStatus: "success",
  balance: 89.50
}
```

---

#### 3. Page15_PaymentSuccessFlowTest.java
**Location:** `xypai-order/src/test/java/org/dromara/order/frontend/`

**Purpose:** Test "支付成功页面" (Payment Success Page) optional API

**Frontend Flow:**
```
Navigate to /payment/success with orderId, orderNo, amount
Optional: Call GET /api/order/detail for more info
Show success animation
After 3-5 seconds → Auto-navigate to order detail
Or user clicks "完成" → Navigate to order detail
```

**Test Cases:**

**Test 1: Get Order Detail After Payment**
```java
@Test
void testPage15_GetOrderDetailAfterPayment() {
    // GIVEN: Payment succeeded, frontend navigated to success page
    String orderId = createPaidOrder(); // Helper method

    // WHEN: Frontend optionally calls /api/order/detail
    OrderDetailVO detail = orderService.getOrderDetail(orderId);

    // THEN: Verify detail matches frontend display
    assertThat(detail.getOrderId()).isEqualTo(orderId);
    assertThat(detail.getOrderNo()).matches("\\d{14}\\d{4}");
    assertThat(detail.getStatus()).isEqualTo("pending");
    assertThat(detail.getAmount()).isEqualTo(new BigDecimal("10.50"));
    assertThat(detail.getCreatedAt()).isNotNull();
    assertThat(detail.getAutoCancelTime()).isAfter(LocalDateTime.now());

    // Frontend displays:
    // - Success icon
    // - Amount: 10.50 coins
    // - Order number: 20251114123456001
    // - Time: 2025-11-14 12:34:56
    // - Notice: "10分钟后未接单自动取消"
}
```

**Frontend Data to Test With:**
```javascript
// From 15-支付成功页面.md
Route Params: {
  orderId: "1234567890",
  orderNo: "20251114123456001",
  amount: 10.50
}

Optional API Call: GET /api/order/detail?orderId=1234567890
```

---

#### 4. Page16_OrderDetailFlowTest.java
**Location:** `xypai-order/src/test/java/org/dromara/order/frontend/`

**Purpose:** Test "订单详情页面" (Order Detail Page) flow with status polling

**Frontend Flow:**
```
Enter page → GET /api/order/status
If status='pending' → Start polling every 5 seconds
Show countdown timer (10 minutes)
Show "取消订单" button
User clicks cancel → POST /api/order/cancel
Stop polling when status changes
```

**Test Cases:**

**Test 1: Page Load with Status Query**
```java
@Test
void testPage16_PageLoadWithStatusQuery() {
    // GIVEN: User enters order detail page
    String orderId = createPendingOrder();

    // WHEN: Frontend calls /api/order/status
    OrderStatusVO status = orderService.getOrderStatus(orderId);

    // THEN: Verify complete status info
    assertThat(status.getOrderId()).isEqualTo(orderId);
    assertThat(status.getStatus()).isEqualTo("pending");
    assertThat(status.getStatusLabel()).isEqualTo("等待服务者接单");

    // Provider info
    assertThat(status.getProvider()).isNotNull();
    assertThat(status.getProvider().getNickname()).isNotEmpty();
    assertThat(status.getProvider().getIsOnline()).isNotNull();

    // Service info
    assertThat(status.getService().getName()).isEqualTo("王者荣耀陪玩");
    assertThat(status.getService().getQuantity()).isEqualTo(1);
    assertThat(status.getService().getUnitPrice()).isEqualTo(new BigDecimal("10.00"));

    // Auto-cancel info
    assertThat(status.getAutoCancel().getEnabled()).isTrue();
    assertThat(status.getAutoCancel().getCancelAt()).isAfter(LocalDateTime.now());
    assertThat(status.getAutoCancel().getRemainingSeconds()).isGreaterThan(0);
    assertThat(status.getAutoCancel().getRemainingSeconds()).isLessThanOrEqualTo(600); // 10 min

    // Available actions
    assertThat(status.getActions()).hasSize(1);
    assertThat(status.getActions().get(0).getAction()).isEqualTo("cancel");
    assertThat(status.getActions().get(0).getLabel()).isEqualTo("取消订单");
    assertThat(status.getActions().get(0).getEnabled()).isTrue();
}
```

**Test 2: Status Polling Simulation**
```java
@Test
void testPage16_StatusPollingUntilAccepted() {
    // GIVEN: Order is pending
    String orderId = createPendingOrder();

    // WHEN: Frontend polls every 5 seconds
    // Poll 1: Still pending
    OrderStatusVO poll1 = orderService.getOrderStatus(orderId);
    assertThat(poll1.getStatus()).isEqualTo("pending");

    // Simulate provider accepts order
    updateOrderStatus(orderId, "accepted");

    // Poll 2: Status changed
    OrderStatusVO poll2 = orderService.getOrderStatus(orderId);
    assertThat(poll2.getStatus()).isEqualTo("accepted");
    assertThat(poll2.getStatusLabel()).isEqualTo("服务者已接单");

    // Actions changed
    assertThat(poll2.getActions()).hasSize(1);
    assertThat(poll2.getActions().get(0).getAction()).isEqualTo("contact");
    assertThat(poll2.getActions().get(0).getLabel()).isEqualTo("联系服务者");

    // Frontend stops polling
}
```

**Test 3: Cancel Order Flow**
```java
@Test
void testPage16_CancelOrderFlow() {
    // GIVEN: User clicks "取消订单" button
    String orderId = createPendingOrder();

    // WHEN: Frontend shows confirmation dialog, user confirms
    CancelOrderDTO cancelRequest = CancelOrderDTO.builder()
        .orderId(orderId)
        .reason("Changed my mind")
        .build();

    OrderCancelResultVO result = orderService.cancelOrder(cancelRequest);

    // THEN: Verify refund processed
    assertThat(result.getOrderId()).isEqualTo(orderId);
    assertThat(result.getStatus()).isEqualTo("cancelled");
    assertThat(result.getRefundAmount()).isEqualTo(new BigDecimal("10.50"));
    assertThat(result.getRefundTime()).isNotNull();
    assertThat(result.getBalance()).isEqualTo(new BigDecimal("100.00")); // Refunded

    // Verify order updated in database
    Order order = orderMapper.selectById(orderId);
    assertThat(order.getStatus()).isEqualTo("cancelled");
    assertThat(order.getCancelledTime()).isNotNull();

    // Verify balance refunded via PaymentService RPC
    UserAccount account = accountMapper.selectByUserId(testUserId);
    assertThat(account.getBalance()).isEqualTo(new BigDecimal("100.00"));

    // Frontend shows: "订单已取消，退款10.50金币"
    // Frontend stops polling
}
```

**Test 4: Cannot Cancel Accepted Order**
```java
@Test
void testPage16_CannotCancelAcceptedOrder() {
    // GIVEN: Order has been accepted by provider
    String orderId = createAcceptedOrder();

    // WHEN: User tries to cancel
    CancelOrderDTO cancelRequest = CancelOrderDTO.builder()
        .orderId(orderId)
        .reason("Want to cancel")
        .build();

    // THEN: Should fail
    assertThatThrownBy(() -> orderService.cancelOrder(cancelRequest))
        .hasMessageContaining("订单状态不允许取消");

    // Frontend should hide "取消订单" button for accepted orders
    OrderStatusVO status = orderService.getOrderStatus(orderId);
    assertThat(status.getActions())
        .noneMatch(action -> action.getAction().equals("cancel"));
}
```

**Frontend Data to Test With:**
```javascript
// From 16-订单详情页面.md
Request: {
  orderId: "1234567890"
}

Expected Response: {
  orderId: "1234567890",
  orderNo: "20251114123456001",
  status: "pending",
  statusLabel: "等待服务者接单",
  provider: { userId, nickname, avatar, isOnline },
  service: { name, quantity, unitPrice },
  amount: 10.50,
  createdAt: "2025-11-14 12:34:56",
  autoCancel: {
    enabled: true,
    cancelAt: "2025-11-14 12:44:56",
    remainingSeconds: 580
  },
  actions: [
    { action: "cancel", label: "取消订单", enabled: true }
  ]
}
```

---

#### 5. CompleteUserJourneyTest.java
**Location:** `xypai-order/src/test/java/org/dromara/order/frontend/`

**Purpose:** Test complete end-to-end user journey across all 4 pages

**Complete User Flow:**
```
Page 13: Order Confirmation
→ Page 14: Payment Modal
→ Page 15: Payment Success
→ Page 16: Order Detail
→ Cancel Order (optional path)
```

**Test Case: Happy Path**
```java
@Test
void testCompleteUserJourney_OrderToPaymentToDetail() {
    // ===== PAGE 13: 确认订单页面 =====

    // Step 1: Load order preview
    OrderPreviewVO preview = orderService.preview(
        OrderPreviewDTO.builder()
            .serviceId(101L)
            .quantity(1)
            .build()
    );
    assertThat(preview.getPreview().getTotal()).isEqualTo(new BigDecimal("10.50"));

    // Step 2: Create order
    OrderCreateResultVO createResult = orderService.createOrder(
        CreateOrderDTO.builder()
            .serviceId(101L)
            .quantity(1)
            .totalAmount(new BigDecimal("10.50"))
            .build()
    );
    String orderId = createResult.getOrderId();
    String orderNo = createResult.getOrderNo();

    // ===== PAGE 14: 支付页面 =====

    // Step 3: Execute payment (requires password)
    PaymentResultVO paymentResult = paymentService.executePayment(
        ExecutePaymentDTO.builder()
            .orderId(orderId)
            .orderNo(orderNo)
            .paymentMethod("balance")
            .amount(new BigDecimal("10.50"))
            .build()
    );
    assertThat(paymentResult.getPaymentStatus()).isEqualTo("require_password");

    // Step 4: Verify password
    PaymentResultVO verifyResult = paymentService.verifyPassword(
        VerifyPasswordDTO.builder()
            .orderId(orderId)
            .orderNo(orderNo)
            .paymentPassword("123456")
            .build()
    );
    assertThat(verifyResult.getPaymentStatus()).isEqualTo("success");

    // ===== PAGE 15: 支付成功页面 =====

    // Step 5: Get order detail (optional)
    OrderDetailVO detail = orderService.getOrderDetail(orderId);
    assertThat(detail.getStatus()).isEqualTo("pending");

    // ===== PAGE 16: 订单详情页面 =====

    // Step 6: Get order status
    OrderStatusVO status = orderService.getOrderStatus(orderId);
    assertThat(status.getStatus()).isEqualTo("pending");
    assertThat(status.getActions()).anyMatch(a -> a.getAction().equals("cancel"));

    // Complete flow verified!
}
```

---

### Integration Tests (4 Files)

#### 6. OrderPaymentIntegrationTest.java
**Purpose:** Test Order ↔ Payment RPC communication

**Test Cases:**
- Order calls Payment.deductBalance() after payment verification
- Payment calls Order.updateOrderStatus() after successful payment
- Payment calls Order.refundBalance() during order cancellation
- RPC timeout and retry handling

#### 7. PaymentOrderIntegrationTest.java
**Purpose:** Test Payment → Order status updates

**Test Cases:**
- Payment success triggers order status update
- Payment failure handling
- Duplicate payment prevention

#### 8. OrderCacheIntegrationTest.java
**Purpose:** Test Redis cache for order operations

**Test Cases:**
- Order detail cached (10-min TTL)
- Cache invalidation on order update
- Cache hit/miss ratio

#### 9. RefundIntegrationTest.java
**Purpose:** Test complete refund flow

**Test Cases:**
- Order cancellation triggers refund RPC
- Balance restored correctly
- Transaction audit trail created

---

### Business Logic Tests (6 Files)

#### 10. PriceCalculationTest.java
**Purpose:** Test service fee calculation (5%)

**Test Cases:**
```java
@Test
void testServiceFeeCalculation() {
    // 10 coins × 1 = 10.00 subtotal
    // Service fee = 10.00 × 0.05 = 0.50
    // Total = 10.50

    BigDecimal subtotal = new BigDecimal("10.00");
    BigDecimal serviceFee = calculateServiceFee(subtotal);
    assertThat(serviceFee).isEqualTo(new BigDecimal("0.50"));
}

@Test
void testServiceFeeRounding() {
    // Test edge cases with decimal precision
    BigDecimal subtotal = new BigDecimal("10.01");
    BigDecimal serviceFee = calculateServiceFee(subtotal); // 0.5005
    assertThat(serviceFee).isEqualTo(new BigDecimal("0.50")); // Rounded
}
```

#### 11. OrderStatusFlowTest.java
**Purpose:** Test order status transitions

**Test Cases:**
- pending → accepted → in_progress → completed
- pending → cancelled
- Invalid state transitions rejected

#### 12. AutoCancelTimerTest.java
**Purpose:** Test 10-minute auto-cancel mechanism

**Test Cases:**
- Auto-cancel time set correctly on order creation
- Scheduled task cancels orders after 10 minutes
- Refund triggered automatically

#### 13. BalanceOperationsTest.java
**Purpose:** Test balance deduction, addition, queries

**Test Cases:**
- Deduct balance with optimistic locking
- Add balance (refunds, income)
- Query balance with Redis cache

#### 14. TransactionAuditTest.java
**Purpose:** Test account transaction audit trail

**Test Cases:**
- Every balance change creates transaction record
- Transaction history queryable
- Balance consistency with transactions

---

### Security Tests (4 Files)

#### 15. PasswordEncryptionTest.java
**Purpose:** Test BCrypt password encryption

**Test Cases:**
- Password hashed on first set
- BCrypt matches() verification
- Different salt for each password

#### 16. AccountLockoutTest.java
**Purpose:** Test 5-attempt lockout mechanism

**Test Cases:**
- Error count increments on wrong password
- Account locked after 5 attempts
- Lockout duration: 30 minutes
- Error count resets after successful login

#### 17. DistributedLockTest.java
**Purpose:** Test Redisson distributed lock for payments

**Test Cases:**
- Concurrent payment attempts blocked
- Lock released after payment completes
- Lock timeout handling

#### 18. OptimisticLockTest.java
**Purpose:** Test MyBatis Plus version field for balance updates

**Test Cases:**
- Concurrent balance updates handled
- Version conflict detection
- Retry mechanism

---

## Test Data Structure

### Based on Frontend Documentation

#### From 13-确认订单页面.md
```java
// Test data structure
class Page13TestData {
    // Request
    Long serviceId = 101L;
    Integer quantity = 1;

    // Expected response
    BigDecimal unitPrice = new BigDecimal("10.00");
    BigDecimal subtotal = new BigDecimal("10.00");
    BigDecimal serviceFee = new BigDecimal("0.50");
    BigDecimal total = new BigDecimal("10.50");
    BigDecimal userBalance = new BigDecimal("100.00");
}
```

#### From 14-支付页面.md
```java
class Page14TestData {
    // Request
    String orderId = "1234567890";
    String orderNo = "20251114123456001";
    String paymentMethod = "balance";
    BigDecimal amount = new BigDecimal("10.50");
    String paymentPassword = "123456";

    // Expected response
    String paymentStatus = "success"; // or "require_password", "failed"
    Boolean requirePassword = true;
    BigDecimal balanceAfter = new BigDecimal("89.50");
}
```

#### From 16-订单详情页面.md
```java
class Page16TestData {
    // Request
    String orderId = "1234567890";

    // Expected response
    String status = "pending";
    String statusLabel = "等待服务者接单";
    Boolean autoCancelEnabled = true;
    Integer remainingSeconds = 580; // Less than 600
    List<Action> actions = List.of(
        new Action("cancel", "取消订单", true)
    );
}
```

---

## Test Execution Order

### 1. Unit Tests First (Business Logic)
```bash
# Run price calculation tests
mvn test -Dtest=PriceCalculationTest

# Run status flow tests
mvn test -Dtest=OrderStatusFlowTest
```

### 2. Security Tests
```bash
mvn test -Dtest=PasswordEncryptionTest
mvn test -Dtest=AccountLockoutTest
```

### 3. Frontend Flow Tests (Main Focus)
```bash
# Page by page
mvn test -Dtest=Page13_OrderConfirmationFlowTest
mvn test -Dtest=Page14_PaymentModalFlowTest
mvn test -Dtest=Page16_OrderDetailFlowTest

# Complete journey
mvn test -Dtest=CompleteUserJourneyTest
```

### 4. Integration Tests Last
```bash
mvn test -Dtest=OrderPaymentIntegrationTest
mvn test -Dtest=RefundIntegrationTest
```

---

## Test Coverage Goals

| Category | Goal | Priority |
|----------|------|----------|
| Frontend Flow Tests | 100% of user journeys | P0 - Critical |
| API Endpoint Tests | 100% of HTTP APIs | P0 - Critical |
| Business Logic Tests | 100% of calculations | P0 - Critical |
| Security Tests | 100% of security features | P0 - Critical |
| Integration Tests | Key RPC flows | P1 - High |
| Edge Case Tests | Common edge cases | P2 - Medium |

---

## Summary

**Total Test Files Planned:** 18 files

**Distribution:**
- Frontend Flow Tests: 5 files (PRIMARY FOCUS)
- Integration Tests: 4 files
- Business Logic Tests: 3 files
- Security Tests: 4 files
- Base/Util Files: 2 files

**Testing Strategy:**
- ✅ Organize tests by frontend pages and user flows
- ✅ Use exact data formats frontend sends
- ✅ Validate responses match frontend expectations
- ✅ Cover all error scenarios frontend handles
- ✅ Test complete user journeys end-to-end

**Next Steps:**
1. Review this test plan
2. Confirm test file organization
3. Create test base classes with common setup
4. Implement frontend flow tests first (Pages 13, 14, 15, 16)
5. Then security and business logic tests
6. Finally integration tests

---

**Document Status:** ✅ READY FOR REVIEW
**Test Implementation:** ⏳ PENDING APPROVAL
