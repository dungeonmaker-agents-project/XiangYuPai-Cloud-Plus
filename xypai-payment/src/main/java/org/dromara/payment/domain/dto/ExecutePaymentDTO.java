package org.dromara.payment.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 执行支付DTO
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutePaymentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /**
     * 支付方式: balance-余额支付, alipay-支付宝, wechat-微信支付
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    /**
     * 支付密码（余额支付需要）
     */
    private String paymentPassword;
}
