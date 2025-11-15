package org.dromara.payment;

import org.dromara.common.BaseIntegrationTest;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.payment.domain.dto.ExecutePaymentDTO;
import org.dromara.payment.domain.dto.VerifyPasswordDTO;
import org.dromara.payment.domain.entity.UserAccount;
import org.dromara.payment.mapper.UserAccountMapper;
import org.dromara.payment.service.IAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Payment Service Integration Test
 * Tests all HTTP API endpoints for payment operations
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@DisplayName("Payment Service Integration Tests")
class PaymentServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private IAccountService accountService;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @MockBean
    private RemoteOrderService remoteOrderService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUpPaymentTests() {
        // Create test user account with payment password
        UserAccount account = accountService.getOrCreateAccount(TEST_USER_ID);
        account.setBalance(TEST_BALANCE);
        account.setPaymentPasswordHash(passwordEncoder.encode(TEST_PAYMENT_PASSWORD));
        userAccountMapper.updateById(account);
    }

    @Nested
    @DisplayName("Execute Payment API Tests")
    class ExecutePaymentTests {

        @Test
        @DisplayName("Should execute balance payment successfully")
        void testExecutePayment_BalanceSuccess() throws Exception {
            // Given
            ExecutePaymentDTO dto = ExecutePaymentDTO.builder()
                .orderId("1001")
                .orderNo("20251114120000001")
                .paymentMethod("balance")
                .amount(new BigDecimal("100.00"))
                .paymentPassword(TEST_PAYMENT_PASSWORD)
                .build();

            // When & Then
            mockMvc.perform(post("/api/payment/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value("1001"))
                .andExpect(jsonPath("$.data.orderNo").value("20251114120000001"))
                .andExpect(jsonPath("$.data.paymentStatus").value("success"))
                .andExpect(jsonPath("$.data.requirePassword").value(false))
                .andExpect(jsonPath("$.data.balance").value(900.00));
        }

        @Test
        @DisplayName("Should require password when not provided")
        void testExecutePayment_RequirePassword() throws Exception {
            // Given
            ExecutePaymentDTO dto = ExecutePaymentDTO.builder()
                .orderId("1002")
                .orderNo("20251114120000002")
                .paymentMethod("balance")
                .amount(new BigDecimal("50.00"))
                .build();

            // When & Then
            mockMvc.perform(post("/api/payment/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paymentStatus").value("require_password"))
                .andExpect(jsonPath("$.data.requirePassword").value(true));
        }

        @Test
        @DisplayName("Should fail payment with incorrect password")
        void testExecutePayment_WrongPassword() throws Exception {
            // Given
            ExecutePaymentDTO dto = ExecutePaymentDTO.builder()
                .orderId("1003")
                .orderNo("20251114120000003")
                .paymentMethod("balance")
                .amount(new BigDecimal("50.00"))
                .paymentPassword("wrongpassword")
                .build();

            // When & Then
            mockMvc.perform(post("/api/payment/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paymentStatus").value("failed"))
                .andExpect(jsonPath("$.data.failureReason").value("支付密码错误"));
        }

        @Test
        @DisplayName("Should fail payment with insufficient balance")
        void testExecutePayment_InsufficientBalance() throws Exception {
            // Given
            ExecutePaymentDTO dto = ExecutePaymentDTO.builder()
                .orderId("1004")
                .orderNo("20251114120000004")
                .paymentMethod("balance")
                .amount(new BigDecimal("5000.00")) // More than available balance
                .paymentPassword(TEST_PAYMENT_PASSWORD)
                .build();

            // When & Then
            mockMvc.perform(post("/api/payment/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paymentStatus").value("failed"))
                .andExpect(jsonPath("$.data.failureReason").value("余额不足"));
        }
    }

    @Nested
    @DisplayName("Verify Password API Tests")
    class VerifyPasswordTests {

        @Test
        @DisplayName("Should verify password and complete payment")
        void testVerifyPassword_Success() throws Exception {
            // Given
            VerifyPasswordDTO dto = VerifyPasswordDTO.builder()
                .orderId("1005")
                .orderNo("20251114120000005")
                .paymentPassword(TEST_PAYMENT_PASSWORD)
                .build();

            // When & Then
            mockMvc.perform(post("/api/payment/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("Should fail verification with wrong password")
        void testVerifyPassword_WrongPassword() throws Exception {
            // Given
            VerifyPasswordDTO dto = VerifyPasswordDTO.builder()
                .orderId("1006")
                .orderNo("20251114120000006")
                .paymentPassword("wrongpassword")
                .build();

            // When & Then
            mockMvc.perform(post("/api/payment/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("Get Payment Methods API Tests")
    class GetPaymentMethodsTests {

        @Test
        @DisplayName("Should get all available payment methods")
        void testGetPaymentMethods_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payment/methods")
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.methods").isArray())
                .andExpect(jsonPath("$.data.methods[0].type").value("balance"))
                .andExpect(jsonPath("$.data.methods[0].name").value("余额支付"))
                .andExpect(jsonPath("$.data.methods[0].enabled").value(true))
                .andExpect(jsonPath("$.data.methods[0].requirePassword").value(true))
                .andExpect(jsonPath("$.data.methods[0].balance").value(TEST_BALANCE));
        }
    }

    @Nested
    @DisplayName("Get Balance API Tests")
    class GetBalanceTests {

        @Test
        @DisplayName("Should get user balance successfully")
        void testGetBalance_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payment/balance")
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.balance").value(TEST_BALANCE))
                .andExpect(jsonPath("$.data.frozenBalance").value(0))
                .andExpect(jsonPath("$.data.availableBalance").value(TEST_BALANCE));
        }
    }
}
