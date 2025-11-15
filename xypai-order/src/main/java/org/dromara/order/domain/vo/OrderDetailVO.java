package org.dromara.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情VO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 自动取消时间
     */
    private LocalDateTime autoCancelTime;

    /**
     * 服务提供者信息
     */
    private ProviderInfo provider;

    /**
     * 服务信息
     */
    private ServiceInfo service;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String nickname;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private Integer quantity;
    }
}
