package org.dromara.payment.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.order.api.domain.UpdateOrderStatusRequest;
import org.dromara.payment.domain.dto.ExecutePaymentDTO;
import org.dromara.payment.domain.dto.VerifyPasswordDTO;
import org.dromara.payment.domain.entity.PaymentRecord;
import org.dromara.payment.domain.entity.UserAccount;
import org.dromara.payment.domain.vo.BalanceVO;
import org.dromara.payment.domain.vo.PaymentMethodVO;
import org.dromara.payment.domain.vo.PaymentResultVO;
import org.dromara.payment.mapper.PaymentRecordMapper;
import org.dromara.payment.service.IAccountService;
import org.dromara.payment.service.IPaymentService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务实现
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements IPaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final IAccountService accountService;
    private final RedissonClient redissonClient;

    @DubboReference
    private RemoteOrderService remoteOrderService;

    /**
     * 支付锁key前缀
     */
    private static final String PAYMENT_LOCK_KEY = "payment:lock:";

    /**
     * 支付锁等待时间（秒）
     */
    private static final long LOCK_WAIT_TIME = 3;

    /**
     * 支付锁持有时间（秒）
     */
    private static final long LOCK_LEASE_TIME = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO executePayment(ExecutePaymentDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 使用分布式锁防止重复支付
        String lockKey = PAYMENT_LOCK_KEY + dto.getOrderNo();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!locked) {
                throw new ServiceException("支付处理中，请稍后");
            }

            // 检查支付方式
            if ("balance".equals(dto.getPaymentMethod())) {
                // 余额支付
                return executeBalancePayment(dto, userId);
            } else if ("alipay".equals(dto.getPaymentMethod()) || "wechat".equals(dto.getPaymentMethod())) {
                // 第三方支付（暂不实现）
                throw new ServiceException("暂不支持第三方支付");
            } else {
                throw new ServiceException("不支持的支付方式");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("支付处理异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO verifyPassword(VerifyPasswordDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 验证支付密码
        accountService.verifyPaymentPassword(userId, dto.getPaymentPassword());

        // 执行支付
        ExecutePaymentDTO paymentDTO = ExecutePaymentDTO.builder()
            .orderId(dto.getOrderId())
            .orderNo(dto.getOrderNo())
            .paymentMethod("balance")
            .amount(BigDecimal.ZERO) // 金额从订单获取
            .paymentPassword(dto.getPaymentPassword())
            .build();

        return executePayment(paymentDTO);
    }

    @Override
    public PaymentMethodVO getPaymentMethods() {
        Long userId = StpUtil.getLoginIdAsLong();

        List<PaymentMethodVO.PaymentMethod> methods = new ArrayList<>();

        // 余额支付
        BalanceVO balance = accountService.getBalance(userId);
        methods.add(PaymentMethodVO.PaymentMethod.builder()
            .type("balance")
            .name("余额支付")
            .icon("https://example.com/balance-icon.png")
            .enabled(true)
            .requirePassword(accountService.hasPaymentPassword(userId))
            .balance(balance.getAvailableBalance())
            .build());

        // 支付宝（暂不支持）
        methods.add(PaymentMethodVO.PaymentMethod.builder()
            .type("alipay")
            .name("支付宝")
            .icon("https://example.com/alipay-icon.png")
            .enabled(false)
            .requirePassword(false)
            .build());

        // 微信支付（暂不支持）
        methods.add(PaymentMethodVO.PaymentMethod.builder()
            .type("wechat")
            .name("微信支付")
            .icon("https://example.com/wechat-icon.png")
            .enabled(false)
            .requirePassword(false)
            .build());

        return PaymentMethodVO.builder()
            .methods(methods)
            .build();
    }

    /**
     * 执行余额支付
     */
    private PaymentResultVO executeBalancePayment(ExecutePaymentDTO dto, Long userId) {
        // 检查是否设置支付密码
        if (!accountService.hasPaymentPassword(userId)) {
            return PaymentResultVO.builder()
                .orderId(dto.getOrderId())
                .orderNo(dto.getOrderNo())
                .paymentStatus("require_password")
                .requirePassword(true)
                .failureReason("未设置支付密码")
                .build();
        }

        // 检查是否提供支付密码
        if (dto.getPaymentPassword() == null || dto.getPaymentPassword().isEmpty()) {
            return PaymentResultVO.builder()
                .orderId(dto.getOrderId())
                .orderNo(dto.getOrderNo())
                .paymentStatus("require_password")
                .requirePassword(true)
                .build();
        }

        // 验证支付密码
        try {
            accountService.verifyPaymentPassword(userId, dto.getPaymentPassword());
        } catch (ServiceException e) {
            return PaymentResultVO.builder()
                .orderId(dto.getOrderId())
                .orderNo(dto.getOrderNo())
                .paymentStatus("failed")
                .requirePassword(false)
                .failureReason(e.getMessage())
                .build();
        }

        // 检查余额
        BalanceVO balance = accountService.getBalance(userId);
        if (balance.getAvailableBalance().compareTo(dto.getAmount()) < 0) {
            return PaymentResultVO.builder()
                .orderId(dto.getOrderId())
                .orderNo(dto.getOrderNo())
                .paymentStatus("failed")
                .requirePassword(false)
                .balance(balance.getAvailableBalance())
                .failureReason("余额不足")
                .build();
        }

        // 生成支付流水号
        String paymentNo = generatePaymentNo();

        // 创建支付记录
        PaymentRecord paymentRecord = PaymentRecord.builder()
            .paymentNo(paymentNo)
            .userId(userId)
            .paymentMethod("balance")
            .paymentType("order")
            .referenceId(dto.getOrderId())
            .referenceType("order")
            .amount(dto.getAmount())
            .serviceFee(BigDecimal.ZERO)
            .status("pending")
            .build();
        paymentRecordMapper.insert(paymentRecord);

        try {
            // 扣减余额
            accountService.deductBalance(
                userId,
                dto.getAmount(),
                "订单支付: " + dto.getOrderNo(),
                dto.getOrderId(),
                "order",
                paymentNo
            );

            // 更新支付记录状态
            paymentRecord.setStatus("success");
            paymentRecord.setPaymentTime(LocalDateTime.now());
            paymentRecordMapper.updateById(paymentRecord);

            // 通知订单服务更新状态
            UpdateOrderStatusRequest orderRequest = UpdateOrderStatusRequest.builder()
                .orderId(Long.valueOf(dto.getOrderId()))
                .orderNo(dto.getOrderNo())
                .status("accepted")
                .paymentStatus("success")
                .paymentMethod("balance")
                .paymentTime(LocalDateTime.now())
                .build();
            remoteOrderService.updateOrderStatus(orderRequest);

            // 获取最新余额
            BalanceVO newBalance = accountService.getBalance(userId);

            log.info("支付成功: orderId={}, amount={}, balance={}", dto.getOrderId(), dto.getAmount(), newBalance.getAvailableBalance());

            return PaymentResultVO.builder()
                .orderId(dto.getOrderId())
                .orderNo(dto.getOrderNo())
                .paymentStatus("success")
                .requirePassword(false)
                .balance(newBalance.getAvailableBalance())
                .build();

        } catch (Exception e) {
            // 支付失败，更新支付记录
            paymentRecord.setStatus("failed");
            paymentRecordMapper.updateById(paymentRecord);

            log.error("支付失败: orderId={}, error={}", dto.getOrderId(), e.getMessage(), e);

            throw new ServiceException("支付失败: " + e.getMessage());
        }
    }

    /**
     * 生成支付流水号
     */
    private String generatePaymentNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(9000) + 1000;
        return "PAY" + timestamp + random;
    }
}
