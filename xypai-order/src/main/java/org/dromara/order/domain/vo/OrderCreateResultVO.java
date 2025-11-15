package org.dromara.order.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单创建结果VO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResultVO implements Serializable {

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
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 是否需要支付
     */
    private Boolean needPayment;

    /**
     * 支付信息
     */
    private PaymentInfo paymentInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 支付金额
         */
        private BigDecimal amount;

        /**
         * 币种
         */
        private String currency;

        /**
         * 用户余额
         */
        private BigDecimal userBalance;

        /**
         * 余额是否充足
         */
        private Boolean sufficientBalance;
    }
}
