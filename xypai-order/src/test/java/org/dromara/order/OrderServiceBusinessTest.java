package org.dromara.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.BaseIntegrationTest;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.order.domain.dto.CancelOrderDTO;
import org.dromara.order.domain.dto.CreateOrderDTO;
import org.dromara.order.domain.dto.OrderPreviewDTO;
import org.dromara.order.domain.entity.Order;
import org.dromara.order.domain.vo.OrderPreviewVO;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderService;
import org.dromara.payment.api.RemotePaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Order Service Business Logic Test
 * Tests business rules, calculations, and workflows
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@DisplayName("Order Service Business Logic Tests")
class OrderServiceBusinessTest extends BaseIntegrationTest {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @MockBean
    private RemotePaymentService remotePaymentService;

    @Test
    @DisplayName("Should calculate service fee correctly at 5%")
    void testServiceFeeCalculation() {
        // Given
        when(remotePaymentService.getBalance(anyLong()))
            .thenReturn(TEST_BALANCE);

        OrderPreviewDTO dto = OrderPreviewDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(10)
            .build();

        // When
        OrderPreviewVO result = orderService.preview(dto);

        // Then
        BigDecimal expectedSubtotal = new BigDecimal("500.00"); // 50 * 10
        BigDecimal expectedServiceFee = new BigDecimal("25.00"); // 500 * 0.05
        BigDecimal expectedTotal = new BigDecimal("525.00"); // 500 + 25

        assertThat(result.getPreview().getSubtotal()).isEqualByComparingTo(expectedSubtotal);
        assertThat(result.getPreview().getServiceFee()).isEqualByComparingTo(expectedServiceFee);
        assertThat(result.getPreview().getTotal()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    @DisplayName("Should set auto-cancel time to 10 minutes after order creation")
    void testAutoCancelTimeSetCorrectly() {
        // Given
        when(remotePaymentService.getBalance(anyLong()))
            .thenReturn(TEST_BALANCE);

        CreateOrderDTO dto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        LocalDateTime beforeCreate = LocalDateTime.now();

        // When
        var result = orderService.createOrder(dto);

        // Then
        Order order = orderMapper.selectById(Long.valueOf(result.getOrderId()));
        assertThat(order.getAutoCancelTime()).isNotNull();
        assertThat(order.getAutoCancelTime()).isAfter(beforeCreate.plusMinutes(9));
        assertThat(order.getAutoCancelTime()).isBefore(beforeCreate.plusMinutes(11));
    }

    @Test
    @DisplayName("Should update order status correctly through workflow")
    void testOrderStatusFlow() {
        // Given
        when(remotePaymentService.getBalance(anyLong()))
            .thenReturn(TEST_BALANCE);

        CreateOrderDTO dto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        var createResult = orderService.createOrder(dto);
        Long orderId = Long.valueOf(createResult.getOrderId());
        String orderNo = createResult.getOrderNo();

        // When - Update status through workflow
        orderService.updateOrderStatus(orderId, orderNo, "accepted", "success", "balance");

        // Then
        Order order = orderMapper.selectById(orderId);
        assertThat(order.getStatus()).isEqualTo("accepted");
        assertThat(order.getPaymentStatus()).isEqualTo("success");
        assertThat(order.getPaymentMethod()).isEqualTo("balance");
        assertThat(order.getPaymentTime()).isNotNull();
    }

    @Test
    @DisplayName("Should call RemotePaymentService via RPC for balance check")
    void testRPCCommunicationForBalance() {
        // Given
        when(remotePaymentService.getBalance(anyLong()))
            .thenReturn(TEST_BALANCE);

        OrderPreviewDTO dto = OrderPreviewDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .build();

        // When
        OrderPreviewVO result = orderService.preview(dto);

        // Then
        verify(remotePaymentService, times(1)).getBalance(anyLong());
        assertThat(result.getUserBalance()).isEqualByComparingTo(TEST_BALANCE);
    }

    @Test
    @DisplayName("Should invalidate cache when order is cancelled")
    void testCacheInvalidationOnCancel() {
        // Given
        when(remotePaymentService.getBalance(anyLong()))
            .thenReturn(TEST_BALANCE);

        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        var createResult = orderService.createOrder(dto);
        String orderId = createResult.getOrderId();

        // First, get the order detail to populate cache
        orderService.getOrderDetail(orderId);

        // Verify cache exists
        String cacheKey = "order:detail:" + orderId;
        assertThat(RedisUtils.getCacheObject(cacheKey)).isNotNull();

        // When - Cancel order
        CancelOrderDTO cancelDto = CancelOrderDTO.builder()
            .orderId(orderId)
            .reason("Test cancellation")
            .build();

        orderService.cancelOrder(cancelDto);

        // Then - Cache should be invalidated
        assertThat(RedisUtils.getCacheObject(cacheKey)).isNull();
    }
}
