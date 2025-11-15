package org.dromara.order.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新订单状态请求
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 订单状态
     * pending - 待接单
     * accepted - 已接单
     * in_progress - 进行中
     * completed - 已完成
     * cancelled - 已取消
     * refunded - 已退款
     */
    private String status;

    /**
     * 支付状态
     * pending - 待支付
     * success - 支付成功
     * failed - 支付失败
     */
    private String paymentStatus;

    /**
     * 支付方式
     * balance - 余额支付
     * alipay - 支付宝
     * wechat - 微信支付
     */
    private String paymentMethod;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 备注
     */
    private String remark;
}
