package org.dromara.payment.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付结果VO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultVO implements Serializable {

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
     * 支付状态: success-支付成功, pending-待支付, require_password-需要密码, failed-支付失败
     */
    private String paymentStatus;

    /**
     * 是否需要密码
     */
    private Boolean requirePassword;

    /**
     * 用户余额（支付后）
     */
    private BigDecimal balance;

    /**
     * 失败原因
     */
    private String failureReason;
}
