package org.dromara.payment.controller.feign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.payment.api.RemotePaymentService;
import org.dromara.payment.api.domain.*;
import org.dromara.payment.service.IAccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 支付远程服务实现（Dubbo RPC Provider）
 *
 * <p>为其他服务提供RPC接口，实现支付、余额操作</p>
 *
 * @author XyPai Team
 * @since 2025-11-14
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemotePaymentServiceImpl implements RemotePaymentService {

    private final IAccountService accountService;

    @Override
    public PaymentResultDTO createPayment(CreatePaymentRequest request) throws ServiceException {
        log.info("RPC调用 - 创建支付: userId={}, amount={}, paymentType={}",
            request.getUserId(), request.getAmount(), request.getPaymentType());

        try {
            // TODO: 实现创建支付逻辑
            // 这里仅作为示例
            throw new ServiceException("暂未实现");
        } catch (Exception e) {
            log.error("创建支付失败: userId={}", request.getUserId(), e);
            throw new ServiceException("创建支付失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deductBalance(DeductBalanceRequest request) throws ServiceException {
        log.info("RPC调用 - 扣减余额: userId={}, amount={}, reason={}",
            request.getUserId(), request.getAmount(), request.getReason());

        try {
            return accountService.deductBalance(
                request.getUserId(),
                request.getAmount(),
                request.getReason(),
                request.getReferenceId(),
                request.getReferenceType(),
                request.getPaymentNo()
            );
        } catch (Exception e) {
            log.error("扣减余额失败: userId={}", request.getUserId(), e);
            throw new ServiceException("扣减余额失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addBalance(AddBalanceRequest request) throws ServiceException {
        log.info("RPC调用 - 增加余额: userId={}, amount={}, reason={}",
            request.getUserId(), request.getAmount(), request.getReason());

        try {
            return accountService.addBalance(
                request.getUserId(),
                request.getAmount(),
                request.getReason(),
                request.getReferenceId(),
                request.getReferenceType(),
                request.getPaymentNo()
            );
        } catch (Exception e) {
            log.error("增加余额失败: userId={}", request.getUserId(), e);
            throw new ServiceException("增加余额失败: " + e.getMessage());
        }
    }

    @Override
    public boolean refundBalance(String paymentNo, BigDecimal refundAmount, String reason) throws ServiceException {
        log.info("RPC调用 - 退款: paymentNo={}, amount={}, reason={}",
            paymentNo, refundAmount, reason);

        try {
            // TODO: 实现退款逻辑
            // 1. 查询支付记录
            // 2. 验证退款金额
            // 3. 增加余额
            // 4. 更新支付记录状态
            throw new ServiceException("暂未实现");
        } catch (Exception e) {
            log.error("退款失败: paymentNo={}", paymentNo, e);
            throw new ServiceException("退款失败: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        log.info("RPC调用 - 获取余额: userId={}", userId);

        try {
            return accountService.getBalance(userId).getAvailableBalance();
        } catch (Exception e) {
            log.error("获取余额失败: userId={}", userId, e);
            return BigDecimal.ZERO;
        }
    }
}
