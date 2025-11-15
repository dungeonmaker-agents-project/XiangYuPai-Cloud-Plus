package org.dromara.payment.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 增加余额请求
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddBalanceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 增加金额
     */
    private BigDecimal amount;

    /**
     * 增加原因
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
