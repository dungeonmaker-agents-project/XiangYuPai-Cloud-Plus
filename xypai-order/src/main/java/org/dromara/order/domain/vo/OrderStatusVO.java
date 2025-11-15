package org.dromara.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单状态VO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusVO implements Serializable {

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
     * 状态标签
     */
    private String statusLabel;

    /**
     * 服务提供者信息
     */
    private ProviderInfo provider;

    /**
     * 服务信息
     */
    private ServiceInfo service;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 接单时间
     */
    private LocalDateTime acceptedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 取消时间
     */
    private LocalDateTime cancelledAt;

    /**
     * 自动取消配置
     */
    private AutoCancelInfo autoCancel;

    /**
     * 可用操作
     */
    private List<ActionInfo> actions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String nickname;
        private String avatar;
        private Boolean isOnline;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoCancelInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Boolean enabled;
        private LocalDateTime cancelAt;
        private Long remainingSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String action;
        private String label;
        private Boolean enabled;
    }
}
