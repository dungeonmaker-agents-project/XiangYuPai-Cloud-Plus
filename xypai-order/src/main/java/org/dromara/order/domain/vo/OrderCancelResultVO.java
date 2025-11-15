package org.dromara.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单取消结果VO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 用户余额（退款后）
     */
    private BigDecimal balance;
}
