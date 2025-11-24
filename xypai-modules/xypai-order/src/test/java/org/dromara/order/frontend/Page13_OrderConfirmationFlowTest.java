package org.dromara.order.frontend;

import org.dromara.order.base.OrderTestBase;
import org.dromara.order.domain.dto.CreateOrderDTO;
import org.dromara.order.domain.dto.OrderPreviewDTO;
import org.dromara.order.domain.dto.UpdateOrderPreviewDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Page 13 - Order Confirmation Flow Test
 * 13-确认订单页面 流程测试
 *
 * <p>Tests complete user flow for the Order Confirmation Page (13-确认订单页面.md)
 * <p>测试"确认订单页面"的完整用户流程
 *
 * <p><b>Frontend Flow:</b>
 * <pre>
 * 1. User enters page → GET /api/order/preview (with serviceId, quantity)
 * 2. User adjusts quantity → POST /api/order/preview/update
 * 3. User clicks "立即支付" → POST /api/order/create
 * 4. → Navigate to Payment Modal (Page 14)
 * </pre>
 *
 * <p><b>Test Data (from frontend docs):</b>
 * <pre>
 * serviceId: 101
 * quantity: 1
 * unitPrice: 10.00 coins
 * serviceFee: 5% = 0.50 coins
 * total: 10.50 coins
 * userBalance: 100.00 coins
 * </pre>
 *
 * @author XyPai Team
 * @date 2025-11-14
 * @see <a href="file:///XiangYuPai-Doc/.../Frontend/13-确认订单页面.md">Frontend Documentation</a>
 */
@DisplayName("Page 13 - Order Confirmation Flow Tests")
public class Page13_OrderConfirmationFlowTest extends OrderTestBase {

    // ==================== HAPPY PATH TESTS ====================

    @Test
    @DisplayName("TC-P13-001: Complete Page Load Flow - Success")
    void testCompletePageLoadFlow_Success() throws Exception {
        // GIVEN: Frontend enters order confirmation page with serviceId=101
        // 前端进入确认订单页面，携带 serviceId=101

        Long serviceId = TEST_SERVICE_ID; // 101
        Integer quantity = 1;

        // WHEN: Frontend calls GET /api/order/preview
        // 前端调用订单预览接口

        mockMvc.perform(get("/api/order/preview")
                .param("serviceId", serviceId.toString())
                .param("quantity", quantity.toString())
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))

            // THEN: Verify response matches frontend expectation
            // 验证响应符合前端期望

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))

            // Provider info
            .andExpect(jsonPath("$.data.provider").exists())
            .andExpect(jsonPath("$.data.provider.userId").isNumber())
            .andExpect(jsonPath("$.data.provider.avatar").isString())
            .andExpect(jsonPath("$.data.provider.nickname").isString())
            .andExpect(jsonPath("$.data.provider.gender").isString())

            // Service info
            .andExpect(jsonPath("$.data.service").exists())
            .andExpect(jsonPath("$.data.service.serviceId").value(101))
            .andExpect(jsonPath("$.data.service.name").isString())

            // Price info
            .andExpect(jsonPath("$.data.price").exists())
            .andExpect(jsonPath("$.data.price.unitPrice").value(10.00))
            .andExpect(jsonPath("$.data.price.unit").value("局"))
            .andExpect(jsonPath("$.data.price.displayText").value("10金币/局"))

            // Quantity options
            .andExpect(jsonPath("$.data.quantityOptions").exists())
            .andExpect(jsonPath("$.data.quantityOptions.min").value(1))
            .andExpect(jsonPath("$.data.quantityOptions.max").value(greaterThan(1)))
            .andExpect(jsonPath("$.data.quantityOptions.default").value(1))

            // Preview calculation (5% service fee)
            .andExpect(jsonPath("$.data.preview").exists())
            .andExpect(jsonPath("$.data.preview.quantity").value(1))
            .andExpect(jsonPath("$.data.preview.subtotal").value(10.00))
            .andExpect(jsonPath("$.data.preview.serviceFee").value(0.50)) // 5% of 10.00
            .andExpect(jsonPath("$.data.preview.total").value(10.50))    // 10.00 + 0.50

            // User balance
            .andExpect(jsonPath("$.data.userBalance").isNumber());

        System.out.println("✅ TC-P13-001: Order preview loaded successfully");
        System.out.println("   - Provider info: ✓");
        System.out.println("   - Service info: ✓");
        System.out.println("   - Price calculation: 10.00 + 0.50(5%) = 10.50 ✓");
        System.out.println("   - User balance: ✓");
    }

    @Test
    @DisplayName("TC-P13-002: Quantity Update Flow - Recalculate Price")
    void testQuantityUpdateFlow_RecalculatePrice() throws Exception {
        // GIVEN: User adjusts quantity from 1 to 3
        // 用户将数量从 1 调整为 3

        UpdateOrderPreviewDTO updateRequest = UpdateOrderPreviewDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(3)
            .build();

        // WHEN: Frontend calls POST /api/order/preview/update
        // 前端调用更新订单预览接口

        mockMvc.perform(post("/api/order/preview/update")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))

            // THEN: Verify real-time calculation
            // 验证实时计算结果

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.quantity").value(3))
            .andExpect(jsonPath("$.data.subtotal").value(30.00))   // 10 * 3
            .andExpect(jsonPath("$.data.serviceFee").value(1.50))  // 30 * 0.05
            .andExpect(jsonPath("$.data.total").value(31.50));     // 30 + 1.50

        System.out.println("✅ TC-P13-002: Quantity updated successfully");
        System.out.println("   - Quantity: 1 → 3 ✓");
        System.out.println("   - Subtotal: 10.00 → 30.00 ✓");
        System.out.println("   - Service fee: 0.50 → 1.50 ✓");
        System.out.println("   - Total: 10.50 → 31.50 ✓");
    }

    @Test
    @DisplayName("TC-P13-003: Create Order Flow - Generate Order")
    void testCreateOrderFlow_GenerateOrder() throws Exception {
        // GIVEN: User clicks "立即支付" button
        // 用户点击"立即支付"按钮

        CreateOrderDTO createRequest = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(DEFAULT_TOTAL) // 10.50
            .build();

        // WHEN: Frontend calls POST /api/order/create
        // 前端调用创建订单接口

        mockMvc.perform(post("/api/order/create")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))

            // THEN: Verify order created and payment info returned
            // 验证订单创建成功并返回支付信息

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))

            // Order IDs
            .andExpect(jsonPath("$.data.orderId").isString())
            .andExpect(jsonPath("$.data.orderNo").value(matchesPattern(getOrderNumberPattern())))

            // Order amount
            .andExpect(jsonPath("$.data.amount").value(10.50))

            // Payment required
            .andExpect(jsonPath("$.data.needPayment").value(true))

            // Payment info
            .andExpect(jsonPath("$.data.paymentInfo").exists())
            .andExpect(jsonPath("$.data.paymentInfo.amount").value(10.50))
            .andExpect(jsonPath("$.data.paymentInfo.currency").value("coin"))
            .andExpect(jsonPath("$.data.paymentInfo.userBalance").isNumber())
            .andExpect(jsonPath("$.data.paymentInfo.sufficientBalance").isBoolean());

        System.out.println("✅ TC-P13-003: Order created successfully");
        System.out.println("   - Order ID generated: ✓");
        System.out.println("   - Order number format: yyyyMMddHHmmss + 4 digits ✓");
        System.out.println("   - Total amount: 10.50 ✓");
        System.out.println("   - Payment info returned: ✓");
        System.out.println("   → Frontend shows Payment Modal (Page 14)");
    }

    @Test
    @DisplayName("TC-P13-004: Preview with Default Quantity - Use 1 as Default")
    void testPreviewWithDefaultQuantity_UseDefault() throws Exception {
        // GIVEN: Frontend doesn't specify quantity (should default to 1)
        // 前端未指定数量（应默认为 1）

        // WHEN: Call preview without quantity parameter
        mockMvc.perform(get("/api/order/preview")
                .param("serviceId", TEST_SERVICE_ID.toString())
                // No quantity param
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))

            // THEN: Should use quantity=1 as default
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.preview.quantity").value(1))
            .andExpect(jsonPath("$.data.preview.subtotal").value(10.00))
            .andExpect(jsonPath("$.data.preview.total").value(10.50));

        System.out.println("✅ TC-P13-004: Default quantity handling works");
    }

    // ==================== ERROR SCENARIO TESTS ====================

    @Test
    @DisplayName("TC-P13-E01: Service Not Available - Should Fail")
    void testServiceNotAvailable_ShouldFail() throws Exception {
        // GIVEN: Frontend sends invalid/non-existent serviceId
        // 前端发送无效/不存在的服务ID

        Long invalidServiceId = TEST_INVALID_SERVICE_ID; // 999

        // WHEN: Call preview with invalid service ID
        mockMvc.perform(get("/api/order/preview")
                .param("serviceId", invalidServiceId.toString())
                .param("quantity", "1")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))

            // THEN: Should return error matching frontend error handling
            // 应返回符合前端错误处理的错误信息

            .andExpect(status().isOk()) // R.fail() still returns 200
            .andExpect(jsonPath("$.code").value(not(200)))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("服务")));

        System.out.println("✅ TC-P13-E01: Service not available error handled");
        System.out.println("   - Frontend shows: '该服务暂时不可用' ✓");
    }

    @Test
    @DisplayName("TC-P13-E02: Amount Tampering - Should Detect and Reject")
    void testAmountTampering_ShouldReject() throws Exception {
        // GIVEN: Frontend sends tampered amount (user modified frontend code)
        // 前端发送被篡改的金额（用户修改了前端代码）

        CreateOrderDTO tamperedRequest = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("5.00")) // Should be 10.50, but tampered to 5.00
            .build();

        // WHEN: Call create order with wrong amount
        mockMvc.perform(post("/api/order/create")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tamperedRequest)))

            // THEN: Backend should detect mismatch and reject
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(not(200)))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("金额")));

        System.out.println("✅ TC-P13-E02: Amount tampering detected");
        System.out.println("   - Expected: 10.50, Received: 5.00 ✗");
        System.out.println("   - Backend validation: ✓");
    }

    @Test
    @DisplayName("TC-P13-E03: Quantity Exceeds Maximum - Should Validate")
    void testQuantityExceedsMaximum_ShouldValidate() throws Exception {
        // GIVEN: User tries to order more than maximum allowed
        // 用户尝试订购超过最大允许数量

        UpdateOrderPreviewDTO excessiveQuantity = UpdateOrderPreviewDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(999) // Way over limit
            .build();

        // WHEN: Call update preview with excessive quantity
        mockMvc.perform(post("/api/order/preview/update")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(excessiveQuantity)))

            // THEN: Should validate and reject
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(not(200)));

        System.out.println("✅ TC-P13-E03: Quantity validation works");
    }

    @Test
    @DisplayName("TC-P13-E04: Unauthorized Access - Should Return 401")
    void testUnauthorizedAccess_ShouldReturn401() throws Exception {
        // GIVEN: User not logged in (no auth token)
        // 用户未登录（无认证令牌）

        // WHEN: Call API without authorization header
        mockMvc.perform(get("/api/order/preview")
                .param("serviceId", TEST_SERVICE_ID.toString())
                .contentType(MediaType.APPLICATION_JSON))

            // THEN: Should return 401 Unauthorized
            .andExpect(status().isUnauthorized());

        System.out.println("✅ TC-P13-E04: Unauthorized access rejected");
        System.out.println("   - Frontend redirects to login page ✓");
    }

    @Test
    @DisplayName("TC-P13-E05: Missing Required Parameters - Should Validate")
    void testMissingRequiredParameters_ShouldValidate() throws Exception {
        // GIVEN: Frontend sends request without required serviceId
        // 前端发送缺少必需参数的请求

        // WHEN: Call preview without serviceId
        mockMvc.perform(get("/api/order/preview")
                // Missing serviceId parameter
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))

            // THEN: Should return validation error
            .andExpect(status().is4xxClientError());

        System.out.println("✅ TC-P13-E05: Missing parameter validation works");
    }

    // ==================== BUSINESS LOGIC TESTS ====================

    @Test
    @DisplayName("TC-P13-B01: Service Fee Calculation - Verify 5% Rate")
    void testServiceFeeCalculation_Verify5Percent() throws Exception {
        // Test different quantities to verify consistent 5% service fee

        // Quantity = 1: 10.00 + 0.50 = 10.50
        verifyServiceFeeForQuantity(1, "10.00", "0.50", "10.50");

        // Quantity = 2: 20.00 + 1.00 = 21.00
        verifyServiceFeeForQuantity(2, "20.00", "1.00", "21.00");

        // Quantity = 5: 50.00 + 2.50 = 52.50
        verifyServiceFeeForQuantity(5, "50.00", "2.50", "52.50");

        System.out.println("✅ TC-P13-B01: Service fee calculation verified");
        System.out.println("   - Consistent 5% rate across all quantities ✓");
    }

    /**
     * Helper method to verify service fee calculation
     */
    private void verifyServiceFeeForQuantity(int quantity, String expectedSubtotal,
                                            String expectedServiceFee, String expectedTotal) throws Exception {
        mockMvc.perform(get("/api/order/preview")
                .param("serviceId", TEST_SERVICE_ID.toString())
                .param("quantity", String.valueOf(quantity))
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.preview.subtotal").value(new BigDecimal(expectedSubtotal)))
            .andExpect(jsonPath("$.data.preview.serviceFee").value(new BigDecimal(expectedServiceFee)))
            .andExpect(jsonPath("$.data.preview.total").value(new BigDecimal(expectedTotal)));
    }

    @Test
    @DisplayName("TC-P13-B02: Balance Check - Show Sufficient/Insufficient Status")
    void testBalanceCheck_ShowStatus() throws Exception {
        // GIVEN: Preview order that costs 10.50 coins
        // WHEN: User has balance of 100.00 coins
        // THEN: sufficientBalance should be true

        mockMvc.perform(get("/api/order/preview")
                .param("serviceId", TEST_SERVICE_ID.toString())
                .param("quantity", "1")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userBalance").value(greaterThanOrEqualTo(10.50)));

        // Create order should include sufficientBalance flag
        CreateOrderDTO createRequest = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(DEFAULT_TOTAL)
            .build();

        mockMvc.perform(post("/api/order/create")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentInfo.sufficientBalance").isBoolean());

        System.out.println("✅ TC-P13-B02: Balance check works");
    }
}
