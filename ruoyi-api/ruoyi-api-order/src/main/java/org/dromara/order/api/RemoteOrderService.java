package org.dromara.order.api;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.order.api.domain.CreateOrderRequest;
import org.dromara.order.api.domain.CreateOrderResult;
import org.dromara.order.api.domain.UpdateOrderStatusRequest;
import org.dromara.order.api.domain.OrderCountRequest;

/**
 * 订单远程服务接口
 *
 * <p>用途：其他服务通过Dubbo调用此接口进行订单状态更新和查询</p>
 * <p>实现：xypai-order模块实现此接口</p>
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
public interface RemoteOrderService {

    /**
     * 更新订单状态（支付成功后调用）
     *
     * <p>调用方：PaymentService</p>
     * <p>场景：支付成功后更新订单为已支付状态</p>
     *
     * @param request 订单状态更新请求
     * @return true=更新成功，false=更新失败
     * @throws ServiceException 订单不存在或状态不合法
     */
    boolean updateOrderStatus(UpdateOrderStatusRequest request) throws ServiceException;

    /**
     * 获取用户订单数量（用于统计）
     *
     * <p>调用方：UserService, ContentService</p>
     * <p>场景：用户资料页显示订单统计数据</p>
     *
     * @param request 订单数量查询请求
     * @return 订单数量
     */
    Long getOrderCount(OrderCountRequest request);

    /**
     * 创建订单（含余额支付）
     *
     * <p>调用方：xypai-app-bff (订单确认支付)</p>
     * <p>场景：用户确认订单后创建订单记录，余额支付时同步扣款</p>
     * <p>事务：创建订单 + 扣款在同一事务中</p>
     *
     * @param request 创建订单请求
     * @return 订单创建结果（含支付状态）
     * @throws ServiceException 创建失败/余额不足/服务不存在
     */
    CreateOrderResult createOrder(CreateOrderRequest request) throws ServiceException;
}
