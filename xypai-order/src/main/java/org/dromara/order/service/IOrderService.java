package org.dromara.order.service;

import org.dromara.order.domain.dto.*;
import org.dromara.order.domain.vo.*;

/**
 * 订单服务接口
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
public interface IOrderService {

    /**
     * 订单预览
     *
     * @param dto 订单预览请求
     * @return 订单预览信息
     */
    OrderPreviewVO preview(OrderPreviewDTO dto);

    /**
     * 更新订单预览（数量变化）
     *
     * @param dto 更新订单预览请求
     * @return 更新后的预览信息
     */
    OrderPreviewVO.PreviewInfo updatePreview(UpdateOrderPreviewDTO dto);

    /**
     * 创建订单
     *
     * @param dto 创建订单请求
     * @return 订单创建结果
     */
    OrderCreateResultVO createOrder(CreateOrderDTO dto);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(String orderId);

    /**
     * 获取订单状态
     *
     * @param orderId 订单ID
     * @return 订单状态信息
     */
    OrderStatusVO getOrderStatus(String orderId);

    /**
     * 取消订单
     *
     * @param dto 取消订单请求
     * @return 取消结果
     */
    OrderCancelResultVO cancelOrder(CancelOrderDTO dto);

    /**
     * 更新订单状态（RPC调用）
     *
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @param status 订单状态
     * @param paymentStatus 支付状态
     * @param paymentMethod 支付方式
     * @return true=更新成功
     */
    boolean updateOrderStatus(Long orderId, String orderNo, String status, String paymentStatus, String paymentMethod);

    /**
     * 获取订单数量（RPC调用）
     *
     * @param userId 用户ID
     * @param providerId 服务提供者ID
     * @param status 订单状态
     * @return 订单数量
     */
    Long getOrderCount(Long userId, Long providerId, String status);
}
