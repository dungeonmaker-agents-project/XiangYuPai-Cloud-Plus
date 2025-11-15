package org.dromara.order.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单数量查询请求
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCountRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（下单用户）
     */
    private Long userId;

    /**
     * 服务提供者ID
     */
    private Long providerId;

    /**
     * 服务ID
     */
    private Long serviceId;

    /**
     * 订单状态
     * pending - 待接单
     * accepted - 已接单
     * in_progress - 进行中
     * completed - 已完成
     * cancelled - 已取消
     * refunded - 已退款
     */
    private String status;

    /**
     * 订单类型
     * service - 技能服务订单
     * activity - 活动订单
     */
    private String orderType;
}
