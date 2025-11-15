package org.dromara.trade;

import org.dromara.common.BaseIntegrationTest;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.order.domain.dto.CancelOrderDTO;
import org.dromara.order.domain.dto.CreateOrderDTO;
import org.dromara.order.domain.entity.Order;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderService;
import org.dromara.payment.domain.dto.ExecutePaymentDTO;
import org.dromara.payment.domain.entity.UserAccount;
import org.dromara.payment.domain.vo.BalanceVO;
import org.dromara.payment.domain.vo.PaymentResultVO;
import org.dromara.payment.mapper.UserAccountMapper;
import org.dromara.payment.service.IAccountService;
import org.dromara.payment.service.IPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Trade Module End-to-End Test
 * Tests complete order and payment workflows across both modules
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@DisplayName("Trade Module E2E Tests")
class TradeModuleE2ETest extends BaseIntegrationTest {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUpE2ETests() {
        // Create test user account with payment password and balance
        UserAccount account = accountService.getOrCreateAccount(TEST_USER_ID);
        account.setBalance(TEST_BALANCE);
        account.setPaymentPasswordHash(passwordEncoder.encode(TEST_PAYMENT_PASSWORD));
        account.setPasswordErrorCount(0);
        account.setPasswordLockedUntil(null);
        userAccountMapper.updateById(account);
    }

    @Test
    @DisplayName("Should complete full order flow: preview -> create -> pay -> verify")
    void testCompleteOrderFlow() {
        // Given - User previews order
        var preview = orderService.preview(
            org.dromara.order.domain.dto.OrderPreviewDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(2)
                .build()
        );

        assertThat(preview.getPreview().getTotal()).isEqualByComparingTo(new BigDecimal("105.00"));
        assertThat(preview.getUserBalance()).isEqualByComparingTo(TEST_BALANCE);

        // When - User creates order
        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(2)
            .totalAmount(new BigDecimal("105.00"))
            .build();

        var orderResult = orderService.createOrder(createDto);
        String orderId = orderResult.getOrderId();
        String orderNo = orderResult.getOrderNo();

        assertThat(orderResult.getNeedPayment()).isTrue();
        assertThat(orderResult.getPaymentInfo().getSufficientBalance()).isTrue();

        // And - User pays with balance
        ExecutePaymentDTO payDto = ExecutePaymentDTO.builder()
            .orderId(orderId)
            .orderNo(orderNo)
            .paymentMethod("balance")
            .amount(new BigDecimal("105.00"))
            .paymentPassword(TEST_PAYMENT_PASSWORD)
            .build();

        PaymentResultVO paymentResult = paymentService.executePayment(payDto);

        // Then - Payment succeeds
        assertThat(paymentResult.getPaymentStatus()).isEqualTo("success");
        assertThat(paymentResult.getBalance()).isEqualByComparingTo(new BigDecimal("895.00"));

        // And - Order status is updated
        Order order = orderMapper.selectById(Long.valueOf(orderId));
        assertThat(order.getStatus()).isEqualTo("accepted");
        assertThat(order.getPaymentStatus()).isEqualTo("success");
        assertThat(order.getPaymentMethod()).isEqualTo("balance");

        // And - Balance is deducted
        BalanceVO balance = accountService.getBalance(TEST_USER_ID);
        assertThat(balance.getBalance()).isEqualByComparingTo(new BigDecimal("895.00"));
    }

    @Test
    @DisplayName("Should handle order cancellation with refund")
    void testOrderCancellationWithRefund() {
        // Given - Create and pay for an order
        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        var orderResult = orderService.createOrder(createDto);
        String orderId = orderResult.getOrderId();
        String orderNo = orderResult.getOrderNo();

        // Pay for the order
        ExecutePaymentDTO payDto = ExecutePaymentDTO.builder()
            .orderId(orderId)
            .orderNo(orderNo)
            .paymentMethod("balance")
            .amount(new BigDecimal("52.50"))
            .paymentPassword(TEST_PAYMENT_PASSWORD)
            .build();

        paymentService.executePayment(payDto);

        // Verify balance after payment
        BalanceVO balanceAfterPayment = accountService.getBalance(TEST_USER_ID);
        assertThat(balanceAfterPayment.getBalance()).isEqualByComparingTo(new BigDecimal("947.50"));

        // When - User cancels order
        CancelOrderDTO cancelDto = CancelOrderDTO.builder()
            .orderId(orderId)
            .reason("Changed my mind")
            .build();

        var cancelResult = orderService.cancelOrder(cancelDto);

        // Then - Order is cancelled with refund info
        assertThat(cancelResult.getStatus()).isEqualTo("cancelled");
        assertThat(cancelResult.getRefundAmount()).isEqualByComparingTo(new BigDecimal("52.50"));

        // Note: Actual refund processing would be implemented in the cancelOrder method
        // This test verifies the order status change and refund amount calculation
    }

    @Test
    @DisplayName("Should fail payment when balance is insufficient")
    void testInsufficientBalancePayment() {
        // Given - Create order with amount greater than balance
        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(100)
            .totalAmount(new BigDecimal("5250.00")) // 50 * 100 * 1.05
            .build();

        var orderResult = orderService.createOrder(createDto);

        // When & Then - Payment should fail
        ExecutePaymentDTO payDto = ExecutePaymentDTO.builder()
            .orderId(orderResult.getOrderId())
            .orderNo(orderResult.getOrderNo())
            .paymentMethod("balance")
            .amount(new BigDecimal("5250.00"))
            .paymentPassword(TEST_PAYMENT_PASSWORD)
            .build();

        PaymentResultVO paymentResult = paymentService.executePayment(payDto);

        assertThat(paymentResult.getPaymentStatus()).isEqualTo("failed");
        assertThat(paymentResult.getFailureReason()).isEqualTo("余额不足");
    }

    @Test
    @DisplayName("Should lock account after multiple wrong password attempts")
    void testPasswordLockoutFlow() {
        // Given - Create an order
        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        var orderResult = orderService.createOrder(createDto);

        // When - Try to pay with wrong password 5 times
        for (int i = 0; i < 5; i++) {
            ExecutePaymentDTO payDto = ExecutePaymentDTO.builder()
                .orderId(orderResult.getOrderId())
                .orderNo(orderResult.getOrderNo())
                .paymentMethod("balance")
                .amount(new BigDecimal("52.50"))
                .paymentPassword("wrongpassword")
                .build();

            paymentService.executePayment(payDto);
        }

        // Then - Account should be locked
        UserAccount account = accountService.getOrCreateAccount(TEST_USER_ID);
        assertThat(account.getPasswordLockedUntil()).isNotNull();
        assertThat(account.getPasswordLockedUntil()).isAfter(LocalDateTime.now());

        // And - Further payment attempts should fail with lockout message
        assertThatThrownBy(() -> accountService.verifyPaymentPassword(TEST_USER_ID, TEST_PAYMENT_PASSWORD))
            .hasMessageContaining("密码已锁定");
    }

    @Test
    @DisplayName("Should prevent concurrent payment for same order")
    void testConcurrentPaymentPrevention() throws Exception {
        // Given - Create an order
        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        var orderResult = orderService.createOrder(createDto);

        ExecutePaymentDTO payDto = ExecutePaymentDTO.builder()
            .orderId(orderResult.getOrderId())
            .orderNo(orderResult.getOrderNo())
            .paymentMethod("balance")
            .amount(new BigDecimal("52.50"))
            .paymentPassword(TEST_PAYMENT_PASSWORD)
            .build();

        // When - Two threads try to pay simultaneously
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Thread thread1 = new Thread(() -> {
            try {
                PaymentResultVO result = paymentService.executePayment(payDto);
                if ("success".equals(result.getPaymentStatus())) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                sleep(50); // Small delay
                PaymentResultVO result = paymentService.executePayment(payDto);
                if ("success".equals(result.getPaymentStatus())) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        thread1.start();
        thread2.start();
        latch.await();

        // Then - Only one payment should succeed
        assertThat(successCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should verify RPC communication between order and payment services")
    void testRPCCommunication() {
        // Given
        var preview = orderService.preview(
            org.dromara.order.domain.dto.OrderPreviewDTO.builder()
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .build()
        );

        // Then - Order service should get balance via RPC from payment service
        assertThat(preview.getUserBalance()).isNotNull();
        assertThat(preview.getUserBalance()).isEqualByComparingTo(TEST_BALANCE);

        // When - Create and pay order
        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        var orderResult = orderService.createOrder(createDto);

        ExecutePaymentDTO payDto = ExecutePaymentDTO.builder()
            .orderId(orderResult.getOrderId())
            .orderNo(orderResult.getOrderNo())
            .paymentMethod("balance")
            .amount(new BigDecimal("52.50"))
            .paymentPassword(TEST_PAYMENT_PASSWORD)
            .build();

        paymentService.executePayment(payDto);

        // Then - Payment service should update order status via RPC
        Order order = orderMapper.selectById(Long.valueOf(orderResult.getOrderId()));
        assertThat(order.getStatus()).isEqualTo("accepted");
        assertThat(order.getPaymentStatus()).isEqualTo("success");
    }

    @Test
    @DisplayName("Should maintain cache consistency across order operations")
    void testCacheConsistency() {
        // Given - Create an order
        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        var orderResult = orderService.createOrder(createDto);
        String orderId = orderResult.getOrderId();

        // When - Get order detail (populates cache)
        var detail1 = orderService.getOrderDetail(orderId);
        String cacheKey = "order:detail:" + orderId;
        assertThat(RedisUtils.getCacheObject(cacheKey)).isNotNull();

        // And - Update order (should invalidate cache)
        orderService.updateOrderStatus(
            Long.valueOf(orderId),
            orderResult.getOrderNo(),
            "accepted",
            "success",
            "balance"
        );

        // Then - Cache should be invalidated
        assertThat(RedisUtils.getCacheObject(cacheKey)).isNull();

        // And - Next query should fetch from database and repopulate cache
        var detail2 = orderService.getOrderDetail(orderId);
        assertThat(detail2.getStatus()).isEqualTo("accepted");
        assertThat(RedisUtils.getCacheObject(cacheKey)).isNotNull();
    }

    @Test
    @DisplayName("Should verify auto-cancel timer workflow")
    void testAutoCancelTimerFlow() {
        // Given - Create an order
        CreateOrderDTO createDto = CreateOrderDTO.builder()
            .serviceId(TEST_SERVICE_ID)
            .quantity(1)
            .totalAmount(new BigDecimal("52.50"))
            .build();

        var orderResult = orderService.createOrder(createDto);
        String orderId = orderResult.getOrderId();

        // When - Get order status
        var status = orderService.getOrderStatus(orderId);

        // Then - Auto-cancel info should be present
        assertThat(status.getAutoCancel()).isNotNull();
        assertThat(status.getAutoCancel().getEnabled()).isTrue();
        assertThat(status.getAutoCancel().getCancelAt()).isNotNull();
        assertThat(status.getAutoCancel().getRemainingSeconds()).isGreaterThan(0);

        // And - Auto-cancel time should be approximately 10 minutes in the future
        Order order = orderMapper.selectById(Long.valueOf(orderId));
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, order.getAutoCancelTime());

        assertThat(duration.toMinutes()).isBetween(9L, 11L);
    }
}
