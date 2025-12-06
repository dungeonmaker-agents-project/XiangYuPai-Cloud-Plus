package org.dromara.order.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建订单结果（RPC层）
 *
 * <p>返回给BFF层，包含订单基本信息和支付状态</p>
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否成功 */
    private Boolean success;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 订单金额 */
    private BigDecimal amount;

    /** 支付状态: pending=待支付, success=已支付, failed=支付失败 */
    private String paymentStatus;

    /** 错误码（失败时） */
    private String errorCode;

    /** 错误信息（失败时） */
    private String errorMessage;

    /** 创建失败的结果 */
    public static CreateOrderResult fail(String errorCode, String errorMessage) {
        return CreateOrderResult.builder()
            .success(false)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .build();
    }

    /** 创建成功的结果 */
    public static CreateOrderResult success(Long orderId, String orderNo, BigDecimal amount, String paymentStatus) {
        return CreateOrderResult.builder()
            .success(true)
            .orderId(orderId)
            .orderNo(orderNo)
            .amount(amount)
            .paymentStatus(paymentStatus)
            .build();
    }
}
