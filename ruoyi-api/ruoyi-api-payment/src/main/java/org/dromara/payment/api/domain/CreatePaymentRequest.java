package org.dromara.payment.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建支付请求
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 付款用户ID
     */
    private Long userId;

    /**
     * 收款用户ID（可选，平台收款时为空）
     */
    private Long payeeId;

    /**
     * 支付方式
     * balance - 余额支付
     * alipay - 支付宝
     * wechat - 微信支付
     */
    private String paymentMethod;

    /**
     * 支付类型
     * order - 订单支付
     * activity_publish - 活动发布
     * activity_register - 活动报名
     */
    private String paymentType;

    /**
     * 关联ID（订单ID/活动ID）
     */
    private String referenceId;

    /**
     * 关联类型
     */
    private String referenceType;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 服务费
     */
    private BigDecimal serviceFee;

    /**
     * 支付密码（余额支付时需要）
     */
    private String paymentPassword;

    /**
     * 备注
     */
    private String remark;
}
