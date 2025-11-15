# Trade Module - Practical Test Suite Structure

**Organized Test Plan: 37 Tests → 5 Test Classes**

---

## Test Organization Strategy

Instead of 37 separate test files, we group related tests into **5 logical test classes**:

```
xypai-order/src/test/java/
└── org.dromara.order/
    ├── OrderServiceIntegrationTest.java     (10 tests) ← Order API tests
    └── OrderServiceBusinessTest.java        (5 tests)  ← Business logic tests

xypai-payment/src/test/java/
└── org.dromara.payment/
    ├── PaymentServiceIntegrationTest.java   (8 tests)  ← Payment API tests
    └── PaymentSecurityTest.java             (6 tests)  ← Security tests

Integration tests:
└── org.dromara.trade/
    └── TradeModuleE2ETest.java              (8 tests)  ← End-to-end flows
```

**Total: 5 Test Classes covering all 37 tests**

---

## Test Class Breakdown

### 1. **OrderServiceIntegrationTest** (10 tests)

**Purpose:** Test all Order Service HTTP APIs

**Tests:**
1. `testOrderPreview_Success()` - Order preview
2. `testOrderPreview_InvalidService()` - Invalid service ID
3. `testUpdateOrderPreview_Success()` - Update quantity
4. `testCreateOrder_Success()` - Create order
5. `testCreateOrder_InvalidAmount()` - Amount validation
6. `testGetOrderDetail_Success()` - Get order detail
7. `testGetOrderStatus_Success()` - Get order status
8. `testGetOrderStatus_WithActions()` - Verify actions array
9. `testCancelOrder_Success()` - Cancel pending order
10. `testCancelOrder_AlreadyAccepted()` - Cannot cancel accepted order

---

### 2. **OrderServiceBusinessTest** (5 tests)

**Purpose:** Test Order Service business logic

**Tests:**
1. `testServiceFeeCalculation()` - 5% service fee
2. `testAutoCancelTimer()` - 10-minute auto-cancel
3. `testOrderStatusFlow()` - Status transitions
4. `testRPCUpdateOrderStatus()` - RPC from PaymentService
5. `testCacheInvalidation()` - Cache cleared on update

---

### 3. **PaymentServiceIntegrationTest** (8 tests)

**Purpose:** Test all Payment Service HTTP APIs

**Tests:**
1. `testGetPaymentMethods_Success()` - List payment methods
2. `testGetBalance_Success()` - Query balance
3. `testGetBalance_AutoCreateAccount()` - Create account if not exists
4. `testExecutePayment_Success()` - Successful payment
5. `testExecutePayment_InsufficientBalance()` - Balance check
6. `testVerifyPassword_Success()` - Correct password
7. `testVerifyPassword_WrongPassword()` - Password error
8. `testVerifyPassword_Lockout()` - 5 attempts lockout

---

### 4. **PaymentSecurityTest** (6 tests)

**Purpose:** Test Payment Service security features

**Tests:**
1. `testPasswordEncryption()` - BCrypt hashing
2. `testPasswordErrorCounting()` - Error count in Redis
3. `testPasswordLockout()` - 30-minute lockout
4. `testDistributedLock()` - Prevent duplicate payment
5. `testOptimisticLocking()` - Version field check
6. `testAmountValidation()` - Prevent tampering

---

### 5. **TradeModuleE2ETest** (8 tests)

**Purpose:** Test complete business flows (End-to-End)

**Tests:**
1. `testCompleteOrderFlow_Success()` - Preview → Create → Pay → Success
2. `testOrderCancellation_WithRefund()` - Create → Pay → Cancel → Refund
3. `testInsufficientBalance_Flow()` - Create → Pay fails → Order remains pending
4. `testPasswordLockout_Flow()` - 5 wrong attempts → Account locked
5. `testConcurrentPayment_Prevention()` - Distributed lock test
6. `testRPCCommunication()` - OrderService ↔ PaymentService
7. `testCacheConsistency()` - Update clears cache
8. `testAutoCancelFlow()` - Order auto-cancelled after 10 min

---

## Test Execution Order

### **Phase 1: Unit Tests** (Quick, ~2 minutes)
```bash
# Run individual service tests
mvn test -Dtest=OrderServiceBusinessTest
mvn test -Dtest=PaymentSecurityTest
```

### **Phase 2: Integration Tests** (Moderate, ~5 minutes)
```bash
# Run API integration tests
mvn test -Dtest=OrderServiceIntegrationTest
mvn test -Dtest=PaymentServiceIntegrationTest
```

### **Phase 3: E2E Tests** (Comprehensive, ~10 minutes)
```bash
# Run end-to-end flow tests
mvn test -Dtest=TradeModuleE2ETest
```

### **Phase 4: All Tests** (Complete, ~15 minutes)
```bash
# Run entire test suite
mvn test
```

---

## Test Configuration

### Base Test Class

All test classes extend `BaseIntegrationTest`:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional  // Rollback after each test
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected RemotePaymentService remotePaymentService;  // For OrderService tests

    @MockBean
    protected RemoteOrderService remoteOrderService;  // For PaymentService tests

    protected String testToken;
    protected Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // Get test token
        testToken = "Bearer test_token";

        // Clear Redis cache
        // Reset test data
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
    }
}
```

---

## Quick Test Examples

### Example 1: OrderServiceIntegrationTest

```java
@DisplayName("Order Service API Integration Tests")
public class OrderServiceIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Test 1: Order Preview - Success")
    void testOrderPreview_Success() throws Exception {
        // When: Request order preview
        mockMvc.perform(get("/api/order/preview")
                .param("serviceId", "101")
                .param("quantity", "1")
                .header("Authorization", testToken))

            // Then: Should return preview data
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.preview.total").value(10.50))
            .andExpect(jsonPath("$.data.preview.serviceFee").value(0.50));
    }

    @Test
    @DisplayName("Test 2: Create Order - Success")
    void testCreateOrder_Success() throws Exception {
        // Given: Valid order data
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setServiceId(101L);
        dto.setQuantity(1);
        dto.setTotalAmount(new BigDecimal("10.50"));

        // When: Create order
        MvcResult result = mockMvc.perform(post("/api/order/create")
                .header("Authorization", testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))

            // Then: Should create order
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderId").exists())
            .andReturn();

        // And: Order should be in database
        String orderId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.orderId");
        assertThat(orderId).isNotNull();
    }
}
```

---

### Example 2: TradeModuleE2ETest

```java
@DisplayName("Trade Module End-to-End Tests")
public class TradeModuleE2ETest extends BaseIntegrationTest {

    @Test
    @DisplayName("E2E 1: Complete Order Flow - Success")
    void testCompleteOrderFlow_Success() throws Exception {
        // Step 1: Preview order
        MvcResult previewResult = mockMvc.perform(get("/api/order/preview")
                .param("serviceId", "101")
                .header("Authorization", testToken))
            .andExpect(status().isOk())
            .andReturn();

        BigDecimal total = new BigDecimal(JsonPath.read(
            previewResult.getResponse().getContentAsString(),
            "$.data.preview.total").toString());

        // Step 2: Create order
        CreateOrderDTO createDto = new CreateOrderDTO();
        createDto.setServiceId(101L);
        createDto.setQuantity(1);
        createDto.setTotalAmount(total);

        MvcResult createResult = mockMvc.perform(post("/api/order/create")
                .header("Authorization", testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isOk())
            .andReturn();

        String orderId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.orderId");
        String orderNo = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.orderNo");

        // Step 3: Execute payment
        ExecutePaymentDTO paymentDto = new ExecutePaymentDTO();
        paymentDto.setOrderId(orderId);
        paymentDto.setOrderNo(orderNo);
        paymentDto.setPaymentMethod("balance");
        paymentDto.setAmount(total);
        paymentDto.setPaymentPassword("123456");

        mockMvc.perform(post("/api/payment/pay")
                .header("Authorization", testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentStatus").value("success"));

        // Step 4: Verify order status
        mockMvc.perform(get("/api/order/status")
                .param("orderId", orderId)
                .header("Authorization", testToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("accepted"))
            .andExpect(jsonPath("$.data.payment_status").value("success"));

        // Step 5: Verify balance deducted
        mockMvc.perform(get("/api/payment/balance")
                .header("Authorization", testToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.balance").value(89.50));
    }
}
```

---

## Test Data Management

### Test Data Setup (SQL)

```sql
-- Insert test data before running tests
-- File: src/test/resources/test-data.sql

-- Test user account
INSERT INTO xypai_payment.user_account (user_id, balance, payment_password_hash)
VALUES (1, 100.00, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM4JZnmMfwHqXpq8.LvO');

-- Test service provider
INSERT INTO xypai_payment.user_account (user_id, balance)
VALUES (2, 50.00);

-- Clean up after tests
-- File: src/test/resources/test-cleanup.sql
TRUNCATE TABLE xypai_order.order;
UPDATE xypai_payment.user_account SET balance = 100.00 WHERE user_id = 1;
```

---

## Test Execution Summary

### When to Run Each Test Class:

| Test Class | When to Run | Duration | Purpose |
|------------|-------------|----------|---------|
| OrderServiceBusinessTest | After code change | ~30s | Quick business logic check |
| PaymentSecurityTest | After security changes | ~1min | Security validation |
| OrderServiceIntegrationTest | Before commit | ~2min | API contract verification |
| PaymentServiceIntegrationTest | Before commit | ~2min | API contract verification |
| TradeModuleE2ETest | Before deployment | ~10min | Complete flow validation |

---

## Benefits of This Structure

✅ **Easy to Review** - 5 files instead of 37
✅ **Logical Grouping** - Related tests together
✅ **Fast Execution** - Run specific test classes as needed
✅ **Clear Purpose** - Each class has a specific focus
✅ **Maintainable** - Easy to add/modify tests
✅ **Comprehensive** - All 37 test cases covered

---

## Next Steps

1. ✅ Create 5 test class files
2. ✅ Implement all test methods
3. ✅ Add test configuration
4. ✅ Create test data scripts
5. ⏳ Run and verify all tests pass

---

**This structure makes testing practical and manageable!** 🎯
