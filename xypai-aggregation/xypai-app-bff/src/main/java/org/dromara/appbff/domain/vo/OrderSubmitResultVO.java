package org.dromara.appbff.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单提交结果VO
 *
 * <p>对应前端：提交订单后的响应</p>
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubmitResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否成功 */
    private Boolean success;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付状态: pending=待支付, success=已支付, failed=失败 */
    private String paymentStatus;

    /** 错误码（失败时） */
    private String errorCode;

    /** 错误信息（失败时） */
    private String errorMessage;

    /** 剩余余额（支付成功后） */
    private BigDecimal remainingBalance;

    /** 失败结果 */
    public static OrderSubmitResultVO fail(String errorCode, String errorMessage) {
        return OrderSubmitResultVO.builder()
            .success(false)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .build();
    }

    /** 成功结果 */
    public static OrderSubmitResultVO success(Long orderId, String orderNo, BigDecimal amount, BigDecimal remainingBalance) {
        return OrderSubmitResultVO.builder()
            .success(true)
            .orderId(orderId)
            .orderNo(orderNo)
            .amount(amount)
            .paymentStatus("success")
            .remainingBalance(remainingBalance)
            .build();
    }
}
