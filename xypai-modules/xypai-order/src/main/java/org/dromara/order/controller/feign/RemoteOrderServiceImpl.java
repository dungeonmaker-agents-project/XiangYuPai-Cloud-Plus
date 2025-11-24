package org.dromara.order.controller.feign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.order.api.domain.OrderCountRequest;
import org.dromara.order.api.domain.UpdateOrderStatusRequest;
import org.dromara.order.service.IOrderService;
import org.springframework.stereotype.Service;

/**
 * 订单远程服务实现（Dubbo RPC Provider）
 *
 * <p>为其他服务提供RPC接口，实现订单状态更新和查询</p>
 *
 * @author XyPai Team
 * @since 2025-11-14
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteOrderServiceImpl implements RemoteOrderService {

    private final IOrderService orderService;

    @Override
    public boolean updateOrderStatus(UpdateOrderStatusRequest request) throws ServiceException {
        log.info("RPC调用 - 更新订单状态: orderId={}, status={}, paymentStatus={}",
            request.getOrderId(), request.getStatus(), request.getPaymentStatus());

        try {
            return orderService.updateOrderStatus(
                request.getOrderId(),
                request.getOrderNo(),
                request.getStatus(),
                request.getPaymentStatus(),
                request.getPaymentMethod()
            );
        } catch (Exception e) {
            log.error("更新订单状态失败: orderId={}", request.getOrderId(), e);
            throw new ServiceException("更新订单状态失败: " + e.getMessage());
        }
    }

    @Override
    public Long getOrderCount(OrderCountRequest request) {
        log.info("RPC调用 - 获取订单数量: userId={}, providerId={}, status={}",
            request.getUserId(), request.getProviderId(), request.getStatus());

        try {
            return orderService.getOrderCount(
                request.getUserId(),
                request.getProviderId(),
                request.getStatus()
            );
        } catch (Exception e) {
            log.error("获取订单数量失败", e);
            return 0L;
        }
    }
}
