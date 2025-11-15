package org.dromara.payment.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付结果DTO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付ID
     */
    private Long paymentId;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 支付状态
     * pending - 待支付
     * success - 支付成功
     * failed - 支付失败
     * refunded - 已退款
     */
    private String status;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 用户余额（支付后）
     */
    private BigDecimal balance;

    /**
     * 是否需要密码
     */
    private Boolean requirePassword;

    /**
     * 失败原因
     */
    private String failureReason;
}
