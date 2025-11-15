package org.dromara.payment.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 扣减余额请求
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductBalanceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 扣减金额
     */
    private BigDecimal amount;

    /**
     * 扣减原因
     */
    private String reason;

    /**
     * 关联ID（订单号/活动ID等）
     */
    private String referenceId;

    /**
     * 关联类型
     */
    private String referenceType;

    /**
     * 支付流水号（可选，用于关联支付记录）
     */
    private String paymentNo;
}
