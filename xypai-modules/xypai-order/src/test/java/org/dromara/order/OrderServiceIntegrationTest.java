package org.dromara.order;

import org.dromara.common.BaseIntegrationTest;
import org.dromara.order.domain.dto.CancelOrderDTO;
import org.dromara.order.domain.dto.CreateOrderDTO;
import org.dromara.order.domain.dto.UpdateOrderPreviewDTO;
import org.dromara.payment.api.RemotePaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Order Service Integration Test
 * Tests all HTTP API endpoints for order management
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@DisplayName("Order Service Integration Tests")
class OrderServiceIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private RemotePaymentService remotePaymentService;

    @Nested
    @DisplayName("Order Preview API Tests")
    class OrderPreviewTests {

        @Test
        @DisplayName("Should preview order successfully with default quantity")
        void testOrderPreview_Success() throws Exception {
            // Given
            when(remotePaymentService.getBalance(anyLong()))
                .thenReturn(TEST_BALANCE);

            // When & Then
            mockMvc.perform(get("/api/order/preview")
                    .param("serviceId", String.valueOf(TEST_SERVICE_ID))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.provider").exists())
                .andExpect(jsonPath("$.data.service").exists())
                .andExpect(jsonPath("$.data.price").exists())
                .andExpect(jsonPath("$.data.preview.quantity").value(1))
                .andExpect(jsonPath("$.data.preview.subtotal").value(50.00))
                .andExpect(jsonPath("$.data.preview.serviceFee").value(2.50))
                .andExpect(jsonPath("$.data.preview.total").value(52.50))
                .andExpect(jsonPath("$.data.userBalance").value(TEST_BALANCE));
        }

        @Test
        @DisplayName("Should preview order successfully with custom quantity")
        void testOrderPreview_WithCustomQuantity() throws Exception {
            // Given
            when(remotePaymentService.getBalance(anyLong()))
                .thenReturn(TEST_BALANCE);

            // When & Then
            mockMvc.perform(get("/api/order/preview")
                    .param("serviceId", String.valueOf(TEST_SERVICE_ID))
                    .param("quantity", "3")
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.preview.quantity").value(3))
                .andExpect(jsonPath("$.data.preview.subtotal").value(150.00))
                .andExpect(jsonPath("$.data.preview.serviceFee").value(7.50))
                .andExpect(jsonPath("$.data.preview.total").value(157.50));
        }

        @Test
        @DisplayName("Should fail preview without authentication")
        void testOrderPreview_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/order/preview")
                    .param("serviceId", String.valueOf(TEST_SERVICE_ID)))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Update Order Preview API Tests")
    class UpdateOrderPreviewTests {

        @Test
        @DisplayName("Should update preview when quantity changes")
        void testUpdatePreview_Success() throws Exception {
            // Given
            UpdateOrderPreviewDTO dto = UpdateOrderPreviewDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(5)
                .build();

            // When & Then
            mockMvc.perform(post("/api/order/preview/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.quantity").value(5))
                .andExpect(jsonPath("$.data.subtotal").value(250.00))
                .andExpect(jsonPath("$.data.serviceFee").value(12.50))
                .andExpect(jsonPath("$.data.total").value(262.50));
        }

        @Test
        @DisplayName("Should fail update with invalid quantity (too low)")
        void testUpdatePreview_InvalidQuantityTooLow() throws Exception {
            // Given
            UpdateOrderPreviewDTO dto = UpdateOrderPreviewDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(0)
                .build();

            // When & Then
            mockMvc.perform(post("/api/order/preview/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should fail update with invalid quantity (too high)")
        void testUpdatePreview_InvalidQuantityTooHigh() throws Exception {
            // Given
            UpdateOrderPreviewDTO dto = UpdateOrderPreviewDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(101)
                .build();

            // When & Then
            mockMvc.perform(post("/api/order/preview/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("Create Order API Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully")
        void testCreateOrder_Success() throws Exception {
            // Given
            when(remotePaymentService.getBalance(anyLong()))
                .thenReturn(TEST_BALANCE);

            CreateOrderDTO dto = CreateOrderDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(2)
                .totalAmount(new BigDecimal("105.00")) // (50 * 2) + (50 * 2 * 0.05)
                .build();

            // When & Then
            mockMvc.perform(post("/api/order/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").exists())
                .andExpect(jsonPath("$.data.orderNo").exists())
                .andExpect(jsonPath("$.data.amount").value(105.00))
                .andExpect(jsonPath("$.data.needPayment").value(true))
                .andExpect(jsonPath("$.data.paymentInfo.amount").value(105.00))
                .andExpect(jsonPath("$.data.paymentInfo.currency").value("coin"))
                .andExpect(jsonPath("$.data.paymentInfo.userBalance").value(TEST_BALANCE))
                .andExpect(jsonPath("$.data.paymentInfo.sufficientBalance").value(true));
        }

        @Test
        @DisplayName("Should fail create order with amount mismatch")
        void testCreateOrder_AmountMismatch() throws Exception {
            // Given
            CreateOrderDTO dto = CreateOrderDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .totalAmount(new BigDecimal("100.00")) // Wrong amount
                .build();

            // When & Then
            mockMvc.perform(post("/api/order/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto))
                    .header("Authorization", testToken))
                .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("Order Detail API Tests")
    class OrderDetailTests {

        @Test
        @DisplayName("Should get order detail successfully")
        void testGetOrderDetail_Success() throws Exception {
            // Given - Create an order first
            when(remotePaymentService.getBalance(anyLong()))
                .thenReturn(TEST_BALANCE);

            CreateOrderDTO createDto = CreateOrderDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .totalAmount(new BigDecimal("52.50"))
                .build();

            String createResponse = mockMvc.perform(post("/api/order/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(createDto))
                    .header("Authorization", testToken))
                .andReturn().getResponse().getContentAsString();

            String orderId = objectMapper.readTree(createResponse)
                .get("data").get("orderId").asText();

            // When & Then - Get order detail
            mockMvc.perform(get("/api/order/detail")
                    .param("orderId", orderId)
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.orderNo").exists())
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.amount").value(52.50))
                .andExpect(jsonPath("$.data.provider").exists())
                .andExpect(jsonPath("$.data.service").exists());
        }
    }

    @Nested
    @DisplayName("Order Status API Tests")
    class OrderStatusTests {

        @Test
        @DisplayName("Should get order status with auto-cancel info")
        void testGetOrderStatus_WithAutoCancelInfo() throws Exception {
            // Given - Create an order first
            when(remotePaymentService.getBalance(anyLong()))
                .thenReturn(TEST_BALANCE);

            CreateOrderDTO createDto = CreateOrderDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .totalAmount(new BigDecimal("52.50"))
                .build();

            String createResponse = mockMvc.perform(post("/api/order/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(createDto))
                    .header("Authorization", testToken))
                .andReturn().getResponse().getContentAsString();

            String orderId = objectMapper.readTree(createResponse)
                .get("data").get("orderId").asText();

            // When & Then - Get order status
            mockMvc.perform(get("/api/order/status")
                    .param("orderId", orderId)
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.statusLabel").value("待接单"))
                .andExpect(jsonPath("$.data.autoCancel.enabled").value(true))
                .andExpect(jsonPath("$.data.autoCancel.remainingSeconds").exists())
                .andExpect(jsonPath("$.data.actions").isArray());
        }
    }

    @Nested
    @DisplayName("Cancel Order API Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel pending order successfully")
        void testCancelOrder_Success() throws Exception {
            // Given - Create an order first
            when(remotePaymentService.getBalance(anyLong()))
                .thenReturn(TEST_BALANCE);

            CreateOrderDTO createDto = CreateOrderDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .totalAmount(new BigDecimal("52.50"))
                .build();

            String createResponse = mockMvc.perform(post("/api/order/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(createDto))
                    .header("Authorization", testToken))
                .andReturn().getResponse().getContentAsString();

            String orderId = objectMapper.readTree(createResponse)
                .get("data").get("orderId").asText();

            // When & Then - Cancel the order
            CancelOrderDTO cancelDto = CancelOrderDTO.builder()
                .orderId(orderId)
                .reason("Changed my mind")
                .build();

            mockMvc.perform(post("/api/order/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(cancelDto))
                    .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.status").value("cancelled"))
                .andExpect(jsonPath("$.data.balance").value(TEST_BALANCE));
        }
    }
}
